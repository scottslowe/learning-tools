package main

import (
	"log"

	"github.com/pulumi/pulumi-aws/sdk/v5/go/aws"
	"github.com/pulumi/pulumi-aws/sdk/v5/go/aws/ec2"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
)

func main() {
	pulumi.Run(func(ctx *pulumi.Context) error {
		// Look up the default VPC
		isDefault := true
		desiredState := "available"
		vpc, err := ec2.LookupVpc(ctx, &ec2.LookupVpcArgs{
			Default: &isDefault,
			State:   &desiredState,
		},
		)
		if err != nil {
			log.Printf("error looking up VPC: %s", err.Error())
			return err
		}
		ctx.Export("defaultVpcId", pulumi.String(vpc.Id))

		// Look up availability zones in the desired region
		rawAzInfo, err := aws.GetAvailabilityZones(ctx, &aws.GetAvailabilityZonesArgs{
			State: &desiredState,
		})
		if err != nil {
			log.Printf("error getting AZs: %s", err.Error())
			return err
		}

		// Determine how many AZs are present
		numOfAZs := len(rawAzInfo.Names)
		ctx.Export("numOfAZs", pulumi.Int(numOfAZs))

		// Build a list of AZ names
		azNames := make([]string, numOfAZs)
		for i := 0; i < numOfAZs; i++ {
			azNames[i] = rawAzInfo.Names[i]
		}
		// ctx.Export("listOfAzNames", pulumi.StringArray(azNames[]))

		// Iterate through the AZs to discover subnets
		pubSubnetIds := make([]pulumi.StringInput, numOfAZs)
		privSubnetIds := make([]pulumi.StringInput, numOfAZs)
		for i := 0; i < numOfAZs; i++ {
			selectedAz := azNames[i]
			azDefault := true
			subnet, err := ec2.LookupSubnet(ctx, &ec2.LookupSubnetArgs{
				AvailabilityZone: &selectedAz,
				DefaultForAz:     &azDefault,
				VpcId:            &vpc.Id,
			})
			if err != nil {
				log.Printf("error looking up subnet in AZ: %s", err.Error())
			}
			if subnet.MapPublicIpOnLaunch {
				pubSubnetIds[i] = pulumi.String(subnet.Id)
			} else {
				privSubnetIds[i] = pulumi.String(subnet.Id)
			}
		}
		ctx.Export("pubSubnetIds", pulumi.StringArray(pubSubnetIds))
		ctx.Export("privSubnetIds", pulumi.StringArray(privSubnetIds))

		// Identify default SG
		defaultSgName := "default"
		sg, err := ec2.LookupSecurityGroup(ctx, &ec2.LookupSecurityGroupArgs{
			Name: &defaultSgName,
		})
		if err != nil {
			log.Printf("error looking up default security group: %s", err.Error())
		}
		ctx.Export("defaultSgId", pulumi.String(sg.Id))

		// Launch an instance
		instance, err := ec2.NewInstance(ctx, "instance", &ec2.InstanceArgs{
			Ami:                      pulumi.String("ami-0aab355e1bfa1e72e_VERIFY_ME"),
			InstanceType:             pulumi.String("t3a.small_VERIFY_ME"),
			AssociatePublicIpAddress: pulumi.Bool(true),
			KeyName:                  pulumi.String("CHANGE_ME"),
			SubnetId:                 pubSubnetIds[0],
			SourceDestCheck:          pulumi.Bool(false),
			VpcSecurityGroupIds:      pulumi.StringArray{pulumi.String(sg.Id)},
			Tags: pulumi.StringMap{
				"Name": pulumi.String("instance"),
			},
		})
		if err != nil {
			log.Printf("error launching instance: %s", err.Error())
		}
		ctx.Export("instanceId", instance.ID())
		ctx.Export("instancePublicIpAddress", instance.PublicIp)
		ctx.Export("instancePrivateIpAddress", instance.PrivateIp)

		// End
		return nil
	})
}
