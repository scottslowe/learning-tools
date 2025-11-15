package main

import (
	"log"

	"github.com/pulumi/pulumi-aws/sdk/v7/go/aws/ec2"
	"github.com/pulumi/pulumi-tls/sdk/v4/go/tls"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
)

type InfraDetails struct {
	vpcId               pulumi.StringInput
	publicSubnetIds     pulumi.StringArray
	privateSubnetIds    pulumi.StringArray
	publicRouteTableId  pulumi.StringInput
	privateRouteTableId pulumi.StringInput
	azNumber            int
	keyName             pulumi.StringInput
	natPublicIp         pulumi.StringInput
	natSecGroupId       pulumi.StringInput
	kubeconfig          pulumi.StringInput
	cpInstanceIps       pulumi.StringArray
	wkrInstanceIps      pulumi.StringArray
}

func main() {
	pulumi.Run(func(ctx *pulumi.Context) error {
		// Define a new variable to hold infrastructure details
		var details InfraDetails

		// Create an SSH key for the NAT instance
		// Docs:
		natSshKey, err := tls.NewPrivateKey(ctx, "nat-ssh-key", &tls.PrivateKeyArgs{
			Algorithm: pulumi.String("ED25519"),
		})
		if err != nil {
			log.Printf("error creating SSH key: %s", err.Error())
		}

		// Create an AWS key pair and store the name of the key pair
		// Docs:
		natKeyPair, err := ec2.NewKeyPair(ctx, "nat-key-pair", &ec2.KeyPairArgs{
			PublicKey: natSshKey.PublicKeyOpenssh,
		})
		if err != nil {
			log.Printf("error creating AWS key pair: %s", err.Error())
		} else {
			details.keyName = natKeyPair.KeyName
		}

		// Create a new VPC, subnets, route tables, and public route
		// Private route will be created later with the NAT instance
		details, err = buildInfrastructure(ctx, details)
		if err != nil {
			log.Printf("error building infrastructure: %s", err.Error())
		}

		// Create the NAT instance, security group, and route for private subnets
		details, err = buildNat(ctx, details)
		if err != nil {
			log.Printf("error building NAT instance: %s", err.Error())
		}

		// Build a Kubernetes cluster
		details, err = buildTalosCluster(ctx, details, "10.0.0.0/16")
		if err != nil {
			log.Printf("error creating Kubernetes cluster: %s", err.Error())
		}

		// Create the VPC Route Server and associated components
		details, err = buildRouting(ctx, details)
		if err != nil {
			log.Printf("error creating routing infrastructure: %s", err.Error())
		}

		// Export values from the stack
		ctx.Export("kubeconfig", details.kubeconfig)

		return nil
	})
}
