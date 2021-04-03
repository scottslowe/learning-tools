package main

import (
	"fmt"
	"log"
	"os"

	"github.com/pulumi/pulumi-aws/sdk/v3/go/aws"
	"github.com/pulumi/pulumi-aws/sdk/v3/go/aws/ec2"
	"github.com/pulumi/pulumi/sdk/v2/go/pulumi"
)

func main() {
	pulumi.Run(func(ctx *pulumi.Context) error {
		// Look up the default VPC
		t := true
		vpc, err := ec2.LookupVpc(ctx, &ec2.LookupVpcArgs{Default: &t})
		if err != nil {
			log.Printf("error getting VPC: %s", err.Error())
		}

		// Get subnet IDs from the default VPC
		subnets, err := ec2.GetSubnetIds(ctx, &ec2.GetSubnetIdsArgs{VpcId: vpc.Id})
		if err != nil {
			log.Printf("error getting subnet IDs: %s", err.Error())
		}

		// Get a count of the subnet IDs
		numSubnets := len(subnets.Ids)

		// Create a security group for traffic to the SSH bastion host
		bastionSecGrp, err := ec2.NewSecurityGroup(ctx, "bastion-sg", &ec2.SecurityGroupArgs{
			Name:        pulumi.String("bastion-sg"),
			VpcId:       pulumi.String(vpc.Id),
			Description: pulumi.String("Allows SSH traffic to bastion hosts"),
			Ingress: ec2.SecurityGroupIngressArray{
				ec2.SecurityGroupIngressArgs{
					Protocol:    pulumi.String("tcp"),
					ToPort:      pulumi.Int(22),
					FromPort:    pulumi.Int(22),
					Description: pulumi.String("Allow inbound SSH (TCP 22) from anywhere"),
					CidrBlocks:  pulumi.StringArray{pulumi.String("0.0.0.0/0")},
				},
			},
			Egress: ec2.SecurityGroupEgressArray{
				ec2.SecurityGroupEgressArgs{
					Protocol:    pulumi.String("-1"),
					ToPort:      pulumi.Int(0),
					FromPort:    pulumi.Int(0),
					Description: pulumi.String("Allow all outbound traffic"),
					CidrBlocks:  pulumi.StringArray{pulumi.String("0.0.0.0/0")},
				},
			},
			Tags: pulumi.StringMap{
				"Name": pulumi.String("ans-int-bastion-sg"),
			},
		})
		if err != nil {
			log.Printf("error creating bastion security group: %s", err.Error())
		}
		ctx.Export("bastionSecGrpId", bastionSecGrp.ID())

		// Create a security group for private nodes
		nodeSecGrp, err := ec2.NewSecurityGroup(ctx, "node-sg", &ec2.SecurityGroupArgs{
			Name:        pulumi.String("node-sg"),
			VpcId:       pulumi.String(vpc.Id),
			Description: pulumi.String("Allows traffic between and among private nodes"),
			Ingress: &ec2.SecurityGroupIngressArray{
				&ec2.SecurityGroupIngressArgs{
					Protocol:       pulumi.String("tcp"),
					ToPort:         pulumi.Int(22),
					FromPort:       pulumi.Int(22),
					Description:    pulumi.String("Allow inbound SSH (TCP 22) from bastion hosts"),
					SecurityGroups: pulumi.StringArray{bastionSecGrp.ID()},
				},
				&ec2.SecurityGroupIngressArgs{
					Protocol:    pulumi.String("-1"),
					ToPort:      pulumi.Int(0),
					FromPort:    pulumi.Int(0),
					Description: pulumi.String("Allow all traffic from this security group"),
					Self:        pulumi.Bool(true),
				},
			},
			Egress: &ec2.SecurityGroupEgressArray{
				&ec2.SecurityGroupEgressArgs{
					Protocol:    pulumi.String("-1"),
					ToPort:      pulumi.Int(0),
					FromPort:    pulumi.Int(0),
					Description: pulumi.String("Allow all outbound traffic"),
					CidrBlocks:  pulumi.StringArray{pulumi.String("0.0.0.0/0")},
				},
			},
			Tags: pulumi.StringMap{
				"Name": pulumi.String("private-node-sg"),
			},
		})
		if err != nil {
			log.Printf("error creating node security group: %s", err.Error())
		}
		ctx.Export("nodeSecGrpId", nodeSecGrp.ID())

		// Get AMI ID for a CAPA base image
		mostRecent := true
		amiID, err := aws.GetAmi(ctx, &aws.GetAmiArgs{
			Owners:     []string{"258751437250"},
			MostRecent: &mostRecent,
			Filters: []aws.GetAmiFilter{
				{Name: "name", Values: []string{"capa-ami-ubuntu-18.04-1.18.2*"}},
				{Name: "root-device-type", Values: []string{"ebs"}},
				{Name: "virtualization-type", Values: []string{"hvm"}},
				{Name: "architecture", Values: []string{"x86_64"}},
			},
		})
		if err != nil {
			log.Printf("error looking up AMI: %s", err.Error())
		}
		ctx.Export("amiId", pulumi.String(amiID.Id))

		// Create a file handle for the inventory file
		f, err := os.Create("hosts")
		if err != nil {
			log.Printf("error creating file: %s", err.Error())
		}

		// Launch an EC2 instance to serve as bastion host
		bastion, err := ec2.NewInstance(ctx, "bastion", &ec2.InstanceArgs{
			Ami:                      pulumi.String(amiID.Id),
			InstanceType:             pulumi.String("t2.small"),
			AssociatePublicIpAddress: pulumi.Bool(true),
			KeyName:                  pulumi.String("aws_vmw_rsa"),
			SubnetId:                 pulumi.String(subnets.Ids[0]),
			VpcSecurityGroupIds:      pulumi.StringArray{bastionSecGrp.ID()},
			Tags: pulumi.StringMap{
				"Name": pulumi.String("ans-int-bastion"),
			},
		})
		if err != nil {
			log.Printf("error launching bastion instance: %s", err.Error())
		}
		ctx.Export("bastionInstanceId", bastion.ID())
		ctx.Export("bastionPublicIpAddress", bastion.PublicIp)
		ctx.Export("bastionPrivateIpAddress", bastion.PrivateIp)
		// Write IP address to inventory file
		tmp := bastion.PrivateIp.ApplyT(func(v string) string {
			res := v
			fmt.Fprintln(f, v)
			return res
		})
		// fmt.Fprintln(f, bastion.PrivateIp)
		if err != nil {
			log.Printf("error writing to file: %s", err.Error())
		}
		ctx.Export("tmp", tmp)

		// Launch a private EC2 instance in each subnet/AZ
		nodeIds := make([]pulumi.StringInput, numSubnets)
		for idx := 0; idx < numSubnets; idx++ {
			instance, err := ec2.NewInstance(ctx, fmt.Sprintf("node-%d", idx), &ec2.InstanceArgs{
				Ami:                      pulumi.String(amiID.Id),
				InstanceType:             pulumi.String("t2.large"),
				AssociatePublicIpAddress: pulumi.Bool(false),
				KeyName:                  pulumi.String("aws_vmw_rsa"),
				SubnetId:                 pulumi.String(subnets.Ids[idx]),
				VpcSecurityGroupIds:      pulumi.StringArray{nodeSecGrp.ID()},
				Tags: pulumi.StringMap{
					"Name": pulumi.String(fmt.Sprintf("ans-int-%d", idx)),
				},
			})
			if err != nil {
				log.Printf("error launching instance: %s", err.Error())
			}
			// Export some information
			nodeIds[idx] = instance.ID()
			ctx.Export(fmt.Sprintf("node%dPublicIpAddress", idx), instance.PublicIp)
			ctx.Export(fmt.Sprintf("node%dPrivateIpAddress", idx), instance.PrivateIp)
			// Write IP address to inventory file
			tmp = instance.PrivateIp.ApplyT(func(v string) string {
				res := v
				fmt.Fprintln(f, v)
				return res
			})
			if err != nil {
				log.Printf("error writing to file: %s", err.Error())
			}
		}
		ctx.Export("nodeIdArray", pulumi.StringArray(nodeIds))

		// Close the file handle on the inventory file
		err = f.Close()
		if err != nil {
			log.Printf("error closing file: %s", err.Error())
		}

		// Return a nil value
		return nil
	})
}
