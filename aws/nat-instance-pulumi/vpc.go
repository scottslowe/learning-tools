package main

import (
	"fmt"

	"github.com/pulumi/pulumi-aws/sdk/v6/go/aws"
	"github.com/pulumi/pulumi-aws/sdk/v6/go/aws/ec2"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
)

// Define variables needed outside the buildInfrastructure() function
var idOfVpc pulumi.StringInput
var publicSubnets pulumi.StringArray
var privateSubnets pulumi.StringArray
var idPubRoute pulumi.StringInput  // ID of default route table
var idPrivRoute pulumi.StringInput // ID of NAT route table
var azNumber int                   // Number of AZs

// Builds base infrastructure when called
func buildInfrastructure(ctx *pulumi.Context) (err error) {
	// Look up AZ information for configured region and gather details
	desiredAzState := "available"
	rawAzInfo, err := aws.GetAvailabilityZones(ctx, &aws.GetAvailabilityZonesArgs{
		State: &desiredAzState,
	})
	if err != nil {
		return err
	}
	numOfAzs := len(rawAzInfo.Names)
	azNames := make([]string, pulumi.Int(numOfAzs))
	for i := 0; i < numOfAzs; i++ {
		azNames[i] = rawAzInfo.Names[i]
	}
	// Set value of public variable
	azNumber = numOfAzs

	// Create a new VPC and make the ID accessible outside the function
	vpc, err := ec2.NewVpc(ctx, "vpc", &ec2.VpcArgs{
		CidrBlock:          pulumi.String("10.0.0.0/16"),
		EnableDnsHostnames: pulumi.Bool(true),
		EnableDnsSupport:   pulumi.Bool(true),
		Tags: pulumi.StringMap{
			"project": pulumi.String("nat-instance-pulumi"),
		},
	})
	if err != nil {
		return err
	}
	// Set value of public variable
	idOfVpc = vpc.ID()

	// Create an Internet gateway
	inetGw, err := ec2.NewInternetGateway(ctx, "inet-gw", &ec2.InternetGatewayArgs{
		VpcId: vpc.ID(),
		Tags: pulumi.StringMap{
			"project": pulumi.String("nat-instance-pulumi"),
		},
	})
	if err != nil {
		return err
	}

	// Adopt the default route in the VPC
	defRoute, err := ec2.NewDefaultRouteTable(ctx, "def-route-tbl", &ec2.DefaultRouteTableArgs{
		DefaultRouteTableId: vpc.DefaultRouteTableId,
		Tags: pulumi.StringMap{
			"project": pulumi.String("nat-instance-pulumi"),
		},
	})
	if err != nil {
		return err
	}
	// Set value of public variable
	idPubRoute = defRoute.ID()

	// Associate gateway with default route
	_, err = ec2.NewRoute(ctx, "inet-route", &ec2.RouteArgs{
		RouteTableId:         defRoute.ID(),
		DestinationCidrBlock: pulumi.String("0.0.0.0/0"),
		GatewayId:            inetGw.ID(),
	})
	if err != nil {
		return err
	}

	// Create public subnets
	for i := 0; i < numOfAzs; i++ {
		subnetAddr := i * 32
		subnetCidrBlock := fmt.Sprintf("10.0.%d.0/22", subnetAddr)
		subnet, err := ec2.NewSubnet(ctx, fmt.Sprintf("pub-subnet-%d", i), &ec2.SubnetArgs{
			VpcId:               vpc.ID(),
			AvailabilityZone:    pulumi.String(azNames[i]),
			CidrBlock:           pulumi.String(subnetCidrBlock),
			MapPublicIpOnLaunch: pulumi.Bool(true),
			Tags: pulumi.StringMap{
				"project": pulumi.String("nat-instance-pulumi"),
			},
		})
		if err != nil {
			return err
		}
		// Add value to array
		publicSubnets = append(publicSubnets, subnet.ID())
	}

	// Create a route for the NAT Gateway
	natRoute, err := ec2.NewRouteTable(ctx, "nat-route-tbl", &ec2.RouteTableArgs{
		VpcId: vpc.ID(),
		Tags: pulumi.StringMap{
			"project": pulumi.String("nat-instance-pulumi"),
		},
	})
	if err != nil {
		return err
	}
	// Set value of public variable
	idPrivRoute = natRoute.ID()

	// Create private subnets
	for i := 0; i < numOfAzs; i++ {
		subnetAddr := (i * 32) + 16
		subnetCidrBlock := fmt.Sprintf("10.0.%d.0/22", subnetAddr)
		subnet, err := ec2.NewSubnet(ctx, fmt.Sprintf("priv-subnet-%d", i), &ec2.SubnetArgs{
			VpcId:               vpc.ID(),
			AvailabilityZone:    pulumi.String(azNames[i]),
			CidrBlock:           pulumi.String(subnetCidrBlock),
			MapPublicIpOnLaunch: pulumi.Bool(false),
			Tags: pulumi.StringMap{
				"project": pulumi.String("nat-instance-pulumi"),
			},
		})
		if err != nil {
			return err
		}
		// Add value to array
		privateSubnets = append(privateSubnets, subnet.ID())
	}

	// Link private subnets to private route table
	for i := 0; i < numOfAzs; i++ {
		_, err := ec2.NewRouteTableAssociation(ctx, fmt.Sprintf("priv-rta-%d", i), &ec2.RouteTableAssociationArgs{
			SubnetId:     privateSubnets[i],
			RouteTableId: natRoute.ID(),
		})
		if err != nil {
			return err
		}
	}

	// Return to the calling function
	return nil
}
