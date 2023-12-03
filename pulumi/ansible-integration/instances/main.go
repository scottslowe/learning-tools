package main

import (
	"fmt"
	"log"

	"github.com/pulumi/pulumi-aws/sdk/v6/go/aws/ec2"
	awsx "github.com/pulumi/pulumi-awsx/sdk/v2/go/awsx/ec2"
	"github.com/pulumi/pulumi-tls/sdk/v4/go/tls"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
)

func main() {
	pulumi.Run(func(ctx *pulumi.Context) error {
		// Change these values to change Ubuntu version, CPU architecture,
		// default network CIDR, instance type, or the number of instances
		versionName := "jammy"
		versionNum := "22.04"
		instanceCpuArch := "amd64"
		instanceType := "t3a.small"
		vpcNetworkCidr := "10.0.0.0/16"
		numInstances := 3

		// Create a new VPC, subnets, and associated infrastructure
		ansibleVpc, err := awsx.NewVpc(ctx, "ansible-vpc", &awsx.VpcArgs{
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
		ansibleSg, err := ec2.NewSecurityGroup(ctx, "ansible-sg", &ec2.SecurityGroupArgs{
			VpcId: ansibleVpc.VpcId,
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

		// Get AMI ID for instances
		amiName := fmt.Sprintf("ubuntu/images/hvm-ssd/ubuntu-%s-%s-%s-server*", versionName, versionNum, instanceCpuArch)
		ansibleAmi, err := ec2.LookupAmi(ctx, &ec2.LookupAmiArgs{
			Owners:     []string{"099720109477"},
			MostRecent: pulumi.BoolRef(true),
			Filters: []ec2.GetAmiFilter{
				{Name: "name", Values: []string{amiName}},
				{Name: "root-device-type", Values: []string{"ebs"}},
				{Name: "virtualization-type", Values: []string{"hvm"}},
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
		ansibleKeyPair, err := ec2.NewKeyPair(ctx, "ansible-key-pair", &ec2.KeyPairArgs{
			PublicKey: sshKey.PublicKeyOpenssh,
		})
		if err != nil {
			log.Printf("error creating AWS key pair: %s", err.Error())
		}

		// Launch an instance using Ubuntu AMI
		instanceIpAddresses := make([]pulumi.StringInput, numInstances)
		for i := 0; i < numInstances; i++ {
			instance, err := ec2.NewInstance(ctx, fmt.Sprintf("ansible-instance-%d", i), &ec2.InstanceArgs{
				Ami:                      pulumi.String(ansibleAmi.Id),
				InstanceType:             pulumi.String(instanceType),
				AssociatePublicIpAddress: pulumi.Bool(true),
				KeyName:                  ansibleKeyPair.KeyName,
				SubnetId:                 ansibleVpc.PublicSubnetIds.Index(pulumi.Int(0)),
				VpcSecurityGroupIds:      pulumi.StringArray{ansibleSg.ID()},
				Tags: pulumi.StringMap{
					"Name": pulumi.String("ansible-instance"),
				},
			})
			if err != nil {
				log.Printf("error launching instance: %s", err.Error())
			}
			instanceIpAddresses[i] = instance.PublicIp
		}

		// Make some values available as stack outputs
		ctx.Export("instancePublicIpAddresses", pulumi.StringArray(instanceIpAddresses))
		ctx.Export("privateKey", sshKey.PrivateKeyOpenssh)
		return nil
	})
}
