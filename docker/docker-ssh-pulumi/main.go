package main

import (
	"fmt"
	"log"
	"slices"

	"github.com/pulumi/pulumi-aws/sdk/v6/go/aws/ec2"
	awsx "github.com/pulumi/pulumi-awsx/sdk/v2/go/awsx/ec2"
	"github.com/pulumi/pulumi-docker/sdk/v4/go/docker"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi/config"
	"github.com/pulumiverse/pulumi-time/sdk/go/time"
)

func main() {
	pulumi.Run(func(ctx *pulumi.Context) error {
		// Set up maps that are used later
		channelNames := []string{"stable", "beta", "alpha", "lts"}
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
		channel, err := config.Try(ctx, "channel")
		if err != nil {
			channel = "stable"
		}
		if !slices.Contains(channelNames, channel) {
			channel = "stable"
		}
		userSuppliedKeyPair := config.Require(ctx, "keypair")
		userSuppliedPrivateKeyFile := config.Require(ctx, "privatekeyfile")

		// Create a new VPC, subnets, and associated infrastructure
		dockerVpc, err := awsx.NewVpc(ctx, "docker-vpc", &awsx.VpcArgs{
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
		dockerSg, err := ec2.NewSecurityGroup(ctx, "docker-sg", &ec2.SecurityGroupArgs{
			VpcId: dockerVpc.VpcId,
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
				ec2.SecurityGroupIngressArgs{
					Protocol:    pulumi.String("tcp"),
					FromPort:    pulumi.Int(2379),
					ToPort:      pulumi.Int(2380),
					Description: pulumi.String("Allow etcd traffic from this security group"),
					Self:        pulumi.Bool(true),
				},
				ec2.SecurityGroupIngressArgs{
					Protocol:    pulumi.String("tcp"),
					FromPort:    pulumi.Int(4001),
					ToPort:      pulumi.Int(4001),
					Description: pulumi.String("Allow etcd traffic from this security group"),
					Self:        pulumi.Bool(true),
				},
				ec2.SecurityGroupIngressArgs{
					Protocol:    pulumi.String("tcp"),
					FromPort:    pulumi.Int(7001),
					ToPort:      pulumi.Int(7001),
					Description: pulumi.String("Allow etcd traffic from this security group"),
					Self:        pulumi.Bool(true),
				},
			},
		})
		if err != nil {
			log.Printf("error creating security group: %s", err.Error())
		}

		// Get AMI ID for Flatcar Linux instance
		amiName := fmt.Sprintf("Flatcar-%s-*", channel)
		flatcarAmi, err := ec2.LookupAmi(ctx, &ec2.LookupAmiArgs{
			Owners:     []string{"075585003325"},
			MostRecent: pulumi.BoolRef(true),
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

		// Launch an instance using Flatcar Linux AMI
		flatcarInstance, err := ec2.NewInstance(ctx, "flatcar-instance", &ec2.InstanceArgs{
			Ami:                      pulumi.String(flatcarAmi.Id),
			InstanceType:             pulumi.String(instanceType),
			AssociatePublicIpAddress: pulumi.Bool(true),
			KeyName:                  pulumi.StringPtr(userSuppliedKeyPair),
			SubnetId:                 dockerVpc.PublicSubnetIds.Index(pulumi.Int(0)),
			VpcSecurityGroupIds:      pulumi.StringArray{dockerSg.ID()},
			Tags: pulumi.StringMap{
				"Name": pulumi.String("flatcar-instance"),
			},
		})
		if err != nil {
			log.Printf("error launching instance: %s", err.Error())
		}

		// Sleep for 15 seconds to allow instance to boot
		instanceBootDelay, err := time.NewSleep(ctx, "instance-boot-delay", &time.SleepArgs{
			CreateDuration: pulumi.String("20s"),
		}, pulumi.DependsOn([]pulumi.Resource{flatcarInstance}))
		if err != nil {
			log.Printf("error generating delay: %s", err.Error())
		}

		// Create a new Docker provider
		remoteDocker, err := docker.NewProvider(ctx, "remote-docker", &docker.ProviderArgs{
			Host: pulumi.Sprintf("ssh://core@%s", flatcarInstance.PublicIp),
			SshOpts: pulumi.StringArray{
				pulumi.String("-i"), pulumi.String(userSuppliedPrivateKeyFile),
				pulumi.String("-o"), pulumi.String("StrictHostKeyChecking=no"),
				pulumi.String("-o"), pulumi.String("UserKnownHostsFile=/dev/null"),
			},
		}, pulumi.DependsOn([]pulumi.Resource{instanceBootDelay}))
		if err != nil {
			log.Printf("error creating provider: %s", err.Error())
		}

		// Pull down a container image on the remote host
		nginxImage, err := docker.NewRemoteImage(ctx, "nginx-image", &docker.RemoteImageArgs{
			Name: pulumi.String("nginx:1.17.4-alpine"),
		}, pulumi.Provider(remoteDocker))
		if err != nil {
			log.Printf("error pulling remote image: %s", err.Error())
		}

		// Launch a container on the remote host
		_, err = docker.NewContainer(ctx, "nginx-container", &docker.ContainerArgs{
			Image: nginxImage.ImageId,
		}, pulumi.Provider(remoteDocker))
		if err != nil {
			log.Printf("error creating remote container: %s", err.Error())
		}

		// Export some values as stack outputs
		ctx.Export("instanceId", flatcarInstance.ID())
		ctx.Export("instancePublicIpAddress", flatcarInstance.PublicIp)
		ctx.Export("instancePrivateIpAddress", flatcarInstance.PrivateIp)

		return nil
	})
}
