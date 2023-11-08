package main

import (
	"fmt"
	"log"

	"github.com/pulumi/pulumi-aws/sdk/v6/go/aws/ec2"
	awsx "github.com/pulumi/pulumi-awsx/sdk/v2/go/awsx/ec2"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi/config"
)

func main() {
	pulumi.Run(func(ctx *pulumi.Context) error {
		// Set up maps that are used later
		versionMap := map[string]int{"buster": 10, "bullseye": 11, "bookworm": 12}
		typeMap := map[string]string{"amd64": "t3a.small", "arm64": "t4g.small", "x86_64": "t3a.small", "x64": "t3a.small"}

		// Retrieve configuration values
		keyPair := config.Require(ctx, "keypairname")
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
			versionName = "bookworm"
		}
		versionNum, ok := versionMap[versionName]
		if !ok {
			versionNum = 12
		}

		// Create a new VPC, subnets, and associated infrastructure
		debianVpc, err := awsx.NewVpc(ctx, "debian-vpc", &awsx.VpcArgs{
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
		debianSg, err := ec2.NewSecurityGroup(ctx, "debian-sg", &ec2.SecurityGroupArgs{
			VpcId: debianVpc.VpcId,
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

		// Get AMI ID for Debian instance
		mostRecent := true
		amiName := fmt.Sprintf("debian-%d-%s-*", versionNum, instanceCpuArch)
		debianAmi, err := ec2.LookupAmi(ctx, &ec2.LookupAmiArgs{
			Owners:     []string{"136693071363"},
			MostRecent: &mostRecent,
			Filters: []ec2.GetAmiFilter{
				{Name: "name", Values: []string{amiName}},
				{Name: "root-device-type", Values: []string{"ebs"}},
				{Name: "virtualization-type", Values: []string{"hvm"}},
				{Name: "architecture", Values: []string{instanceCpuArch}},
			},
		})
		if err != nil {
			log.Printf("error looking up AMI: %s", err.Error())
		}

		// Launch an instance using Debian AMI
		debianInstance, err := ec2.NewInstance(ctx, "debian-instance", &ec2.InstanceArgs{
			Ami:                      pulumi.String(debianAmi.Id),
			InstanceType:             pulumi.String(instanceType),
			AssociatePublicIpAddress: pulumi.Bool(true),
			KeyName:                  pulumi.String(keyPair),
			SubnetId:                 debianVpc.PublicSubnetIds.Index(pulumi.Int(0)),
			VpcSecurityGroupIds:      pulumi.StringArray{debianSg.ID()},
			Tags: pulumi.StringMap{
				"Name": pulumi.String("debian-instance"),
			},
		})
		if err != nil {
			log.Printf("error launching instance: %s", err.Error())
		}
		ctx.Export("instanceId", debianInstance.ID())
		ctx.Export("instancePublicIpAddress", debianInstance.PublicIp)
		ctx.Export("instancePrivateIpAddress", debianInstance.PrivateIp)

		return nil
	})
}
