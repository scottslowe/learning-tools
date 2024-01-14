package main

import (
	"fmt"
	"log"
	"slices"

	"github.com/pulumi/pulumi-aws/sdk/v6/go/aws/ec2"
	awsx "github.com/pulumi/pulumi-awsx/sdk/v2/go/awsx/ec2"
	"github.com/pulumi/pulumi-tls/sdk/v4/go/tls"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi/config"
)

func main() {
	pulumi.Run(func(ctx *pulumi.Context) error {
		// Set up maps that are used later
		streams := []string{"stable", "testing", "next"}
		typeMap := map[string]string{"amd64": "t3a.small", "arm64": "t4g.small", "x86_64": "t3a.small", "x64": "t3a.small"}

		// Retrieve configuration values
		instanceCpuArch, err := config.Try(ctx, "architecture")
		if err != nil {
			instanceCpuArch = "arm64"
		}
		instanceType, ok := typeMap[instanceCpuArch]
		if !ok {
			instanceCpuArch = "arm64"
			instanceType = "t4g.small"
		}
		if instanceCpuArch == "amd64" || instanceCpuArch == "x64" {
			instanceCpuArch = "x86_64"
		}
		vpcNetworkCidr, err := config.Try(ctx, "networkcidr")
		if err != nil {
			vpcNetworkCidr = "10.0.0.0/16"
		}
		channel, err := config.Try(ctx, "channel")
		if err != nil {
			channel = "stable"
		}
		if !slices.Contains(streams, channel) {
			channel = "stable"
		}

		// Create a new VPC, subnets, and associated infrastructure
		fcosVpc, err := awsx.NewVpc(ctx, "fcos-vpc", &awsx.VpcArgs{
			CidrBlock:          &vpcNetworkCidr,
			EnableDnsHostnames: pulumi.Bool(true),
			EnableDnsSupport:   pulumi.Bool(true),
			NatGateways: &awsx.NatGatewayConfigurationArgs{
				Strategy: awsx.NatGatewayStrategyNone,
			},
			SubnetSpecs: []awsx.SubnetSpecArgs{
				{
					Type: awsx.SubnetTypePublic,
				},
			},
		})
		if err != nil {
			log.Printf("error creating VPC: %s", err.Error())
		}

		// Create a Security Group that we can use to connect to our instance
		fcosSg, err := ec2.NewSecurityGroup(ctx, "fcos-sg", &ec2.SecurityGroupArgs{
			VpcId: fcosVpc.VpcId,
			Egress: ec2.SecurityGroupEgressArray{
				ec2.SecurityGroupEgressArgs{
					Protocol:   pulumi.String("-1"),
					FromPort:   pulumi.Int(0),
					ToPort:     pulumi.Int(0),
					CidrBlocks: pulumi.StringArray{pulumi.String("0.0.0.0/0")},
				},
			},
			Ingress: ec2.SecurityGroupIngressArray{
				ec2.SecurityGroupIngressArgs{
					Protocol:   pulumi.String("tcp"),
					FromPort:   pulumi.Int(22),
					ToPort:     pulumi.Int(22),
					CidrBlocks: pulumi.StringArray{pulumi.String("0.0.0.0/0")},
				},
			},
		})
		if err != nil {
			log.Printf("error creating security group: %s", err.Error())
		}

		// Get AMI ID for Fedora CoreOS instance
		descValue := fmt.Sprintf("*%s*", channel)
		fcosAmi, err := ec2.LookupAmi(ctx, &ec2.LookupAmiArgs{
			Owners:     []string{"125523088429"},
			MostRecent: pulumi.BoolRef(true),
			Filters: []ec2.GetAmiFilter{
				{Name: "name", Values: []string{"fedora-coreos-*"}},
				{Name: "root-device-type", Values: []string{"ebs"}},
				{Name: "virtualization-type", Values: []string{"hvm"}},
				{Name: "architecture", Values: []string{instanceCpuArch}},
				{Name: "description", Values: []string{descValue}},
			},
		})
		if err != nil {
			log.Printf("error looking up AMI: %s", err.Error())
		}

		// Create an SSH key
		sshKey, err := tls.NewPrivateKey(ctx, "ssh-key", &tls.PrivateKeyArgs{
			Algorithm: pulumi.String("ED25519"),
		})
		if err != nil {
			log.Printf("error creating SSH key: %s", err.Error())
		}

		// Create an AWS key pair
		fcosKeyPair, err := ec2.NewKeyPair(ctx, "fcos-key-pair", &ec2.KeyPairArgs{
			PublicKey: sshKey.PublicKeyOpenssh,
		})
		if err != nil {
			log.Printf("error creating AWS key pair: %s", err.Error())
		}

		// Launch an instance using Fedora CoreOS AMI
		fcosInstance, err := ec2.NewInstance(ctx, "fcos-instance", &ec2.InstanceArgs{
			Ami:                      pulumi.String(fcosAmi.Id),
			InstanceType:             pulumi.String(instanceType),
			AssociatePublicIpAddress: pulumi.Bool(true),
			KeyName:                  fcosKeyPair.KeyName,
			SubnetId:                 fcosVpc.PublicSubnetIds.Index(pulumi.Int(0)),
			VpcSecurityGroupIds:      pulumi.StringArray{fcosSg.ID()},
			Tags: pulumi.StringMap{
				"Name": pulumi.String("fcos-instance"),
			},
		})
		if err != nil {
			log.Printf("error launching instance: %s", err.Error())
		}
		ctx.Export("instanceId", fcosInstance.ID())
		ctx.Export("instancePublicIpAddress", fcosInstance.PublicIp)
		ctx.Export("instancePrivateIpAddress", fcosInstance.PrivateIp)
		ctx.Export("privateKey", sshKey.PrivateKeyOpenssh)

		return nil
	})
}
