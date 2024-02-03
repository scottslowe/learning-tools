package main

import (
	"log"

	"github.com/pulumi/pulumi-aws/sdk/v6/go/aws/ec2"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
)

// Define variables needed outside the buildNat() function
var natPubIpAddr pulumi.StringInput

// Builds base infrastructure when called
func buildNat(ctx *pulumi.Context, key pulumi.StringInput) (err error) {
	// Create a security group for the NAT instance
	natSg, err := ec2.NewSecurityGroup(ctx, "nat-sg", &ec2.SecurityGroupArgs{
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
				Protocol:   pulumi.String("-1"),
				FromPort:   pulumi.Int(0),
				ToPort:     pulumi.Int(0),
				CidrBlocks: pulumi.StringArray{pulumi.String("10.0.0.0/16")},
			},
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

	// Launch a fck-nat instance
	natInstance, err := ec2.NewInstance(ctx, "nat-instance", &ec2.InstanceArgs{
		Ami:                      pulumi.String(natAmi.Id),
		InstanceType:             pulumi.String("t4g.nano"),
		AssociatePublicIpAddress: pulumi.Bool(true),
		KeyName:                  key,
		SourceDestCheck:          pulumi.BoolPtr(false),
		SubnetId:                 publicSubnets[0],
		VpcSecurityGroupIds:      pulumi.StringArray{natSg.ID()},
		Tags: pulumi.StringMap{
			"Name": pulumi.String("nat-instance"),
		},
	})
	if err != nil {
		log.Printf("error launching instance: %s", err.Error())
	}
	// Set value of public variable
	natPubIpAddr = natInstance.PublicIp

	// Create a route in the route table for the private subnets
	_, err = ec2.NewRoute(ctx, "nat-route", &ec2.RouteArgs{
		DestinationCidrBlock: pulumi.String("0.0.0.0/0"),
		NetworkInterfaceId:   natInstance.PrimaryNetworkInterfaceId.ToStringOutput(),
		RouteTableId:         idPrivRoute,
	})

	// Return to the calling function
	return nil
}
