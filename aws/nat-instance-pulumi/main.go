package main

import (
	"fmt"
	"log"

	"github.com/pulumi/pulumi-aws/sdk/v6/go/aws/ec2"
	awsx "github.com/pulumi/pulumi-awsx/sdk/v2/go/awsx/ec2"
	"github.com/pulumi/pulumi-tls/sdk/v4/go/tls"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi/config"
)

func main() {
	pulumi.Run(func(ctx *pulumi.Context) error {
		// Set up maps that are used later
		versionMap := map[string]string{"jammy": "22.04", "focal": "20.04", "bionic": "18.04"}
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
		if instanceCpuArch == "x86_64" || instanceCpuArch == "x64" {
			instanceCpuArch = "amd64"
		}
		vpcNetworkCidr, err := config.Try(ctx, "networkcidr")
		if err != nil {
			vpcNetworkCidr = "10.0.0.0/16"
		}
		versionName, err := config.Try(ctx, "version")
		if err != nil {
			versionName = "jammy"
		}
		versionNum, ok := versionMap[versionName]
		if !ok {
			versionName = "jammy"
			versionNum = "22.04"
		}

		// Create a new VPC, subnets, and associated infrastructure
		testVpc, err := awsx.NewVpc(ctx, "test-vpc", &awsx.VpcArgs{
			CidrBlock:          &vpcNetworkCidr,
			EnableDnsHostnames: pulumi.Bool(true),
			EnableDnsSupport:   pulumi.Bool(true),
			NatGateways: &awsx.NatGatewayConfigurationArgs{
				Strategy: awsx.NatGatewayStrategyNone,
			},
		})
		if err != nil {
			log.Printf("error creating VPC: %s", err.Error())
		}

		// Create a Security Group that we can use to connect to our instance
		testSg, err := ec2.NewSecurityGroup(ctx, "test-sg", &ec2.SecurityGroupArgs{
			VpcId: testVpc.VpcId,
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

		// Get AMI ID for Ubuntu instance
		amiName := fmt.Sprintf("ubuntu/images/hvm-ssd/ubuntu-%s-%s-%s-server*", versionName, versionNum, instanceCpuArch)
		ubuntuAmi, err := ec2.LookupAmi(ctx, &ec2.LookupAmiArgs{
			Owners:     []string{"099720109477"},
			MostRecent: pulumi.BoolRef(true),
			Filters: []ec2.GetAmiFilter{
				{Name: "name", Values: []string{amiName}},
				{Name: "root-device-type", Values: []string{"ebs"}},
				{Name: "virtualization-type", Values: []string{"hvm"}},
				{Name: "architecture", Values: []string{instanceCpuArch}},
			},
		})
		if err != nil {
			log.Printf("error looking up Ubuntu AMI: %s", err.Error())
		}

		// Get AMI ID for the fck-nat instance
		natAmi, err := ec2.LookupAmi(ctx, &ec2.LookupAmiArgs{
			Owners:     []string{"568608671756"},
			MostRecent: pulumi.BoolRef(true),
			Filters: []ec2.GetAmiFilter{
				{Name: "name", Values: []string{"fck-nat-amzn2-*"}},
				{Name: "root-device-type", Values: []string{"ebs"}},
				{Name: "virtualization-type", Values: []string{"hvm"}},
				{Name: "architecture", Values: []string{"arm64"}},
			},
		})
		if err != nil {
			log.Printf("error looking up NAT AMI: %s", err.Error())
		}

		// Create an SSH key
		sshKey, err := tls.NewPrivateKey(ctx, "ssh-key", &tls.PrivateKeyArgs{
			Algorithm: pulumi.String("ED25519"),
		})
		if err != nil {
			log.Printf("error creating SSH key: %s", err.Error())
		}

		// Create an AWS key pair
		testKeyPair, err := ec2.NewKeyPair(ctx, "test-key-pair", &ec2.KeyPairArgs{
			PublicKey: sshKey.PublicKeyOpenssh,
		})
		if err != nil {
			log.Printf("error creating AWS key pair: %s", err.Error())
		}

		// Launch an instance using Ubuntu AMI
		ubuntuInstance, err := ec2.NewInstance(ctx, "ubuntu-instance", &ec2.InstanceArgs{
			Ami:                      pulumi.String(ubuntuAmi.Id),
			InstanceType:             pulumi.String(instanceType),
			AssociatePublicIpAddress: pulumi.Bool(false),
			KeyName:                  testKeyPair.KeyName,
			SubnetId:                 testVpc.PrivateSubnetIds.Index(pulumi.Int(0)),
			VpcSecurityGroupIds:      pulumi.StringArray{testSg.ID()},
			Tags: pulumi.StringMap{
				"Name": pulumi.String("ubuntu-instance"),
			},
		})
		if err != nil {
			log.Printf("error launching instance: %s", err.Error())
		}

		// Launch a fck-nat instance
		natInstance, err := ec2.NewInstance(ctx, "nat-instance", &ec2.InstanceArgs{
			Ami:                      pulumi.String(natAmi.Id),
			InstanceType:             pulumi.String("t4g.nano"),
			AssociatePublicIpAddress: pulumi.Bool(true),
			KeyName:                  testKeyPair.KeyName,
			SourceDestCheck:          pulumi.BoolPtr(false),
			SubnetId:                 testVpc.PublicSubnetIds.Index(pulumi.Int(0)),
			VpcSecurityGroupIds:      pulumi.StringArray{testSg.ID()},
			Tags: pulumi.StringMap{
				"Name": pulumi.String("nat-instance"),
			},
		})
		if err != nil {
			log.Printf("error launching instance: %s", err.Error())
		}

		// Export some values as stack outputs
		ctx.Export("instanceId", ubuntuInstance.ID())
		ctx.Export("natPublicIpAddress", natInstance.PublicIp)
		ctx.Export("instancePrivateIpAddress", ubuntuInstance.PrivateIp)
		ctx.Export("privateKey", sshKey.PrivateKeyOpenssh)

		return nil
	})
}
