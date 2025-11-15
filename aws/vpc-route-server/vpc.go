package main

import (
	"fmt"
	"log"

	"github.com/pulumi/pulumi-aws/sdk/v7/go/aws"
	"github.com/pulumi/pulumi-aws/sdk/v7/go/aws/ec2"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
)

// Builds base infrastructure when called
func buildInfrastructure(ctx *pulumi.Context, z InfraDetails) (InfraDetails, error) {
	// Look up AZ information for configured region and gather details
	desiredAzState := "available"
	rawAzInfo, err := aws.GetAvailabilityZones(ctx, &aws.GetAvailabilityZonesArgs{
		State: &desiredAzState,
	})
	if err != nil {
		log.Printf("error retrieving AZ details: %s", err.Error())
		return z, err
	}
	z.azNumber = len(rawAzInfo.Names)
	azNames := make([]string, pulumi.Int(z.azNumber))
	for i := range z.azNumber {
		azNames[i] = rawAzInfo.Names[i]
	}

	// Create a new VPC and make the ID accessible outside the function
	rsVpc, err := ec2.NewVpc(ctx, "vpc", &ec2.VpcArgs{
		CidrBlock:          pulumi.String("10.0.0.0/16"),
		EnableDnsHostnames: pulumi.Bool(true),
		EnableDnsSupport:   pulumi.Bool(true),
		Tags: pulumi.StringMap{
			"Name":    pulumi.String("rs-vpc"),
			"Project": pulumi.String("vpc-route-server"),
		},
	})
	if err != nil {
		log.Printf("error creating VPC: %s", err.Error())
		return z, err
	}
	// Update the infrastructure struct with VPC ID information
	z.vpcId = rsVpc.ID()

	// Create an Internet gateway
	inetGw, err := ec2.NewInternetGateway(ctx, "inet-gw", &ec2.InternetGatewayArgs{
		VpcId: rsVpc.ID(),
		Tags: pulumi.StringMap{
			"Name":    pulumi.String("rs-inet-gw"),
			"Project": pulumi.String("vpc-route-server"),
		},
	})
	if err != nil {
		log.Printf("error creating internet gateway: %s", err.Error())
		return z, err
	}

	// Adopt the default route in the VPC
	pubRoute, err := ec2.NewDefaultRouteTable(ctx, "pub-route-tbl", &ec2.DefaultRouteTableArgs{
		DefaultRouteTableId: rsVpc.DefaultRouteTableId,
		Tags: pulumi.StringMap{
			"Name":    pulumi.String("pub-route-tbl"),
			"Project": pulumi.String("vpc-route-server"),
		},
	})
	if err != nil {
		log.Printf("error adopting default route table: %s", err.Error())
		return z, err
	}
	// Set the public route table ID in the infrastructure struct
	z.publicRouteTableId = pubRoute.ID()

	// Associate gateway with default route
	_, err = ec2.NewRoute(ctx, "inet-route", &ec2.RouteArgs{
		RouteTableId:         pubRoute.ID(),
		DestinationCidrBlock: pulumi.String("0.0.0.0/0"),
		GatewayId:            inetGw.ID(),
	})
	if err != nil {
		log.Printf("error associating gateway with route table: %s", err.Error())
		return z, err
	}

	// Create public subnets
	for i := range z.azNumber {
		subnetAddr := i * 32
		subnetCidrBlock := fmt.Sprintf("10.0.%d.0/22", subnetAddr)
		subnet, err := ec2.NewSubnet(ctx, fmt.Sprintf("pub-subnet-%d", i), &ec2.SubnetArgs{
			VpcId:               rsVpc.ID(),
			AvailabilityZone:    pulumi.String(azNames[i]),
			CidrBlock:           pulumi.String(subnetCidrBlock),
			MapPublicIpOnLaunch: pulumi.Bool(true),
			Tags: pulumi.StringMap{
				"Name":    pulumi.String(fmt.Sprintf("pub-subnet-%d", i)),
				"Project": pulumi.String("vpc-route-server"),
			},
		})
		if err != nil {
			log.Printf("error creating public subnet: %s", err.Error())
			return z, err
		}
		// Add ID of subnet to infrastructure struct
		z.publicSubnetIds = append(z.publicSubnetIds, subnet.ID())
	}

	// Create a route table for the NAT instance
	privRoute, err := ec2.NewRouteTable(ctx, "priv-route-tbl", &ec2.RouteTableArgs{
		VpcId: rsVpc.ID(),
		Tags: pulumi.StringMap{
			"Name":    pulumi.String("priv-route-tbl"),
			"Project": pulumi.String("vpc-route-server"),
		},
	})
	if err != nil {
		log.Printf("error creating private route table: %s", err.Error())
		return z, err
	}
	// Set value of private route table in infrastructure struct
	z.privateRouteTableId = privRoute.ID()

	// Create private subnets
	for i := range z.azNumber {
		subnetAddr := (i * 32) + 16
		subnetCidrBlock := fmt.Sprintf("10.0.%d.0/22", subnetAddr)
		subnet, err := ec2.NewSubnet(ctx, fmt.Sprintf("priv-subnet-%d", i), &ec2.SubnetArgs{
			VpcId:               rsVpc.ID(),
			AvailabilityZone:    pulumi.String(azNames[i]),
			CidrBlock:           pulumi.String(subnetCidrBlock),
			MapPublicIpOnLaunch: pulumi.Bool(false),
			Tags: pulumi.StringMap{
				"Name":    pulumi.String(fmt.Sprintf("priv-subnet-%d", i)),
				"Project": pulumi.String("vpc-route-server"),
			},
		})
		if err != nil {
			log.Printf("error creating private subnet: %s", err.Error())
			return z, err
		}
		// Add subnet ID to infrastructure struct
		z.privateSubnetIds = append(z.privateSubnetIds, subnet.ID())
	}

	// Link private subnets to private route table
	for i := range z.azNumber {
		_, err := ec2.NewRouteTableAssociation(ctx, fmt.Sprintf("priv-rta-%d", i), &ec2.RouteTableAssociationArgs{
			SubnetId:     z.privateSubnetIds[i],
			RouteTableId: privRoute.ID(),
		})
		if err != nil {
			log.Printf("error associating private route to route table: %s", err.Error())
			return z, err
		}
	}

	// Uncomment the following lines for additional outputs that may be useful for troubleshooting/diagnostics
	// ctx.Export("vpcId", rsVpc.ID())
	// ctx.Export("pubSubnetIds", z.publicSubnetIds)
	// ctx.Export("privSubnetIds", z.privateSubnetIds)

	// Return to the calling function
	return z, err
}
