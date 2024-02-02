package main

import (
	"fmt"
	"log"

	"github.com/pulumi/pulumi-aws/sdk/v6/go/aws/ec2"
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
		// vpcNetworkCidr, err := config.Try(ctx, "networkcidr")
		// if err != nil {
		// 	vpcNetworkCidr = "10.0.0.0/16"
		// }
		versionName, err := config.Try(ctx, "version")
		if err != nil {
			versionName = "jammy"
		}
		versionNum, ok := versionMap[versionName]
		if !ok {
			versionName = "jammy"
			versionNum = "22.04"
		}

		// Create an SSH key
		natSshKey, err := tls.NewPrivateKey(ctx, "nat-ssh-key", &tls.PrivateKeyArgs{
			Algorithm: pulumi.String("ED25519"),
		})
		if err != nil {
			log.Printf("error creating SSH key: %s", err.Error())
		}

		// Create an AWS key pair
		natKeyPair, err := ec2.NewKeyPair(ctx, "nat-key-pair", &ec2.KeyPairArgs{
			PublicKey: natSshKey.PublicKeyOpenssh,
		})
		if err != nil {
			log.Printf("error creating AWS key pair: %s", err.Error())
		}

		// Create a new VPC, subnets, route tables, and public route
		// Private routes will be created later
		buildInfrastructure(ctx)

		// Create the NAT infrastructure
		buildNat(ctx, natKeyPair.KeyName)

		// Create a security group that we can use to connect to our instance
		privateSg, err := ec2.NewSecurityGroup(ctx, "private-sg", &ec2.SecurityGroupArgs{
			VpcId: idOfVpc,
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
					CidrBlocks: pulumi.StringArray{pulumi.String("10.0.0.0/16")},
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

		// Launch an instance using Ubuntu AMI
		ubuntuInstance, err := ec2.NewInstance(ctx, "ubuntu-instance", &ec2.InstanceArgs{
			Ami:                      pulumi.String(ubuntuAmi.Id),
			InstanceType:             pulumi.String(instanceType),
			AssociatePublicIpAddress: pulumi.Bool(false),
			KeyName:                  natKeyPair.KeyName,
			SubnetId:                 privateSubnets[0],
			VpcSecurityGroupIds:      pulumi.StringArray{privateSg.ID()},
			Tags: pulumi.StringMap{
				"Name": pulumi.String("ubuntu-instance"),
			},
		})
		if err != nil {
			log.Printf("error launching instance: %s", err.Error())
		}

		// Export some values as stack outputs
		ctx.Export("instanceId", ubuntuInstance.ID())
		ctx.Export("natPublicIpAddress", natPubIpAddr)
		ctx.Export("instancePrivateIpAddress", ubuntuInstance.PrivateIp)
		ctx.Export("privateKey", natSshKey.PrivateKeyOpenssh)

		return nil
	})
}
