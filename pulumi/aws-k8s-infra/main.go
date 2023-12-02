package main

import (
	"fmt"
	"log"
	"net"
	"net/netip"

	"github.com/apparentlymart/go-cidr/cidr"
	"github.com/pulumi/pulumi-aws/sdk/v5/go/aws"
	"github.com/pulumi/pulumi-aws/sdk/v5/go/aws/ec2"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi/config"
)

func main() {
	pulumi.Run(func(ctx *pulumi.Context) error {
		// Get some values from the Pulumi stack configuration
		keyPair := config.Require(ctx, "sshKeyPair")
		bastionAmiType, err := config.Try(ctx, "bastionType")
		if err != nil {
			bastionAmiType = "t3a.small"
		}
		vpcNetworkCidr, err := config.Try(ctx, "networkCidr")
		if err != nil {
			vpcNetworkCidr = "10.0.0.0/16"
		}
		subnetMask, err := config.TryInt(ctx, "subnetMask")
		if err != nil {
			subnetMask = 22
		}
		clusterName, err := config.Try(ctx, "clusterName")
		if err != nil {
			clusterName = "test"
		}
		ownerTagValue, err := config.Try(ctx, "ownerTagValue")
		if err != nil {
			ownerTagValue = "nobody@nowhere.com"
		}
		teamTagValue, err := config.Try(ctx, "teamTagValue")
		if err != nil {
			teamTagValue = "TeamOfOne"
		}

		// Parse the supplied VPC CIDR
		_, network, err := net.ParseCIDR(vpcNetworkCidr)
		if err != nil {
			log.Printf("invalid CIDR expression: %s", err)
			return err
		}
		prefix, err := netip.ParsePrefix(vpcNetworkCidr)
		if err != nil {
			log.Printf("invalid CIDR prefix: %s", err)
			return err
		}
		if prefix.Bits() > subnetMask {
			log.Printf("incorrect subnet mask configuration")
			return err
		}

		// Define some values to be used later
		k8sTag := fmt.Sprintf("kubernetes.io/cluster/%s", clusterName)

		// Look up Availability Zone (AZ) information for configured region
		desiredAzState := "available"
		rawAzInfo, err := aws.GetAvailabilityZones(ctx, &aws.GetAvailabilityZonesArgs{
			State: &desiredAzState,
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
		for idx := 0; idx < numOfAZs; idx++ {
			azNames[idx] = rawAzInfo.Names[idx]
		}

		// Create new VPC
		vpc, err := ec2.NewVpc(ctx, "vpc", &ec2.VpcArgs{
			CidrBlock:          pulumi.String(vpcNetworkCidr),
			EnableDnsSupport:   pulumi.Bool(true),
			EnableDnsHostnames: pulumi.Bool(true),
			Tags: pulumi.StringMap{
				"Name":  pulumi.Sprintf("%s-vpc", clusterName),
				k8sTag:  pulumi.String("shared"),
				"Owner": pulumi.String(ownerTagValue),
				"Team":  pulumi.String(teamTagValue),
			},
		})
		if err != nil {
			log.Printf("error creating VPC: %s", err.Error())
			return err
		}
		ctx.Export("vpcId", vpc.ID())

		// Calculate different between VPC CIDR prefix and requested subnet prefix
		newBits := subnetMask - prefix.Bits()

		// Create public subnets in the VPC
		pubSubnetIds := make([]pulumi.StringInput, numOfAZs)
		for idx := 0; idx < numOfAZs; idx++ {
			subnetCidr, err := cidr.Subnet(network, newBits, idx)
			if err != nil {
				log.Printf("error calculating subnet CIDR: %s", err.Error())
				return err
			}
			subnet, err := ec2.NewSubnet(ctx, fmt.Sprintf("%s-public-%d", clusterName, idx), &ec2.SubnetArgs{
				VpcId:               vpc.ID(),
				AvailabilityZone:    pulumi.String(azNames[idx]),
				CidrBlock:           pulumi.String(subnetCidr.String()),
				MapPublicIpOnLaunch: pulumi.Bool(true),
				Tags: pulumi.StringMap{
					"Name":                   pulumi.Sprintf("%s-public-%d", clusterName, idx),
					k8sTag:                   pulumi.String("shared"),
					"Owner":                  pulumi.String(ownerTagValue),
					"Team":                   pulumi.String(teamTagValue),
					"kubernetes.io/role/elb": pulumi.String("1"),
				},
			})
			if err != nil {
				log.Printf("error creating public subnet: %s", err.Error())
				return err
			}
			pubSubnetIds[idx] = subnet.ID()
		}
		ctx.Export("pubSubnetIds", pulumi.StringArray(pubSubnetIds))

		// Create an Internet gateway for the public subnets
		gw, err := ec2.NewInternetGateway(ctx, "inetgw", &ec2.InternetGatewayArgs{
			VpcId: vpc.ID(),
			Tags: pulumi.StringMap{
				"Name":  pulumi.Sprintf("%s-igw", clusterName),
				k8sTag:  pulumi.String("shared"),
				"Owner": pulumi.String(ownerTagValue),
				"Team":  pulumi.String(teamTagValue),
			},
		})
		if err != nil {
			log.Printf("error creating Internet Gateway: %s", err.Error())
		}
		ctx.Export("gatewayId", gw.ID())

		// Adopt the default route table in the new VPC
		defrt, err := ec2.NewDefaultRouteTable(ctx, "def-rt", &ec2.DefaultRouteTableArgs{
			DefaultRouteTableId: vpc.DefaultRouteTableId,
			Tags: pulumi.StringMap{
				"Name":  pulumi.Sprintf("%s-def-rt", clusterName),
				k8sTag:  pulumi.String("shared"),
				"Owner": pulumi.String(ownerTagValue),
				"Team":  pulumi.String(teamTagValue),
			},
		})
		if err != nil {
			log.Printf("error adopting default route table: %s", err.Error())
		}
		ctx.Export("defaultRoute", defrt.ID())

		// Create a route for Internet access in the default route table
		route, err := ec2.NewRoute(ctx, "inet-route", &ec2.RouteArgs{
			RouteTableId:         defrt.ID(),
			DestinationCidrBlock: pulumi.String("0.0.0.0/0"),
			GatewayId:            gw.ID(),
		})
		if err != nil {
			log.Printf("error creating route: %s", err.Error())
		}
		ctx.Export("inetRoute", route.ID())

		// Create private subnets in the VPC
		privSubnetIds := make([]pulumi.StringInput, numOfAZs)
		for idx := 0; idx < numOfAZs; idx++ {
			subnetCidr, err := cidr.Subnet(network, newBits, idx+numOfAZs)
			if err != nil {
				log.Printf("error calculating subnet CIDR: %s", err.Error())
			}
			subnet, err := ec2.NewSubnet(ctx, fmt.Sprintf("%s-private-%d", clusterName, idx), &ec2.SubnetArgs{
				VpcId:               vpc.ID(),
				AvailabilityZone:    pulumi.String(azNames[idx]),
				CidrBlock:           pulumi.String(subnetCidr.String()),
				MapPublicIpOnLaunch: pulumi.Bool(false),
				Tags: pulumi.StringMap{
					"Name":                            pulumi.Sprintf("%s-private-%d", clusterName, idx),
					k8sTag:                            pulumi.String("shared"),
					"Owner":                           pulumi.String(ownerTagValue),
					"Team":                            pulumi.String(teamTagValue),
					"kubernetes.io/role/internal-elb": pulumi.String("1"),
				},
			})
			if err != nil {
				log.Printf("error creating private subnet: %s", err.Error())
			}
			privSubnetIds[idx] = subnet.ID()
		}
		ctx.Export("privSubnetIds", pulumi.StringArray(privSubnetIds))

		// Create/allocate an Elastic IP address for the NAT gateway for private subnets
		eip, err := ec2.NewEip(ctx, "natgw-eip", &ec2.EipArgs{
			Vpc: pulumi.Bool(true),
			Tags: pulumi.StringMap{
				"Name":  pulumi.Sprintf("%s-natgw-eip", clusterName),
				k8sTag:  pulumi.String("shared"),
				"Owner": pulumi.String(ownerTagValue),
				"Team":  pulumi.String(teamTagValue),
			},
		})
		if err != nil {
			log.Printf("error creating EIP: %s", err.Error())
		}
		ctx.Export("EIP", eip.AllocationId)

		// Create a NAT gateway for the private subnets
		// All private subnets share one NAT Gateway
		natgw, err := ec2.NewNatGateway(ctx, "natgw", &ec2.NatGatewayArgs{
			AllocationId: eip.ID(),
			SubnetId:     pubSubnetIds[0],
			Tags: pulumi.StringMap{
				"Name":  pulumi.Sprintf("%s-natgw", clusterName),
				k8sTag:  pulumi.String("shared"),
				"Owner": pulumi.String(ownerTagValue),
				"Team":  pulumi.String(teamTagValue),
			},
		}, pulumi.DependsOn([]pulumi.Resource{eip}))
		if err != nil {
			log.Printf("error creating NAT gateway: %s", err.Error())
		}
		ctx.Export("natGateway", natgw.ID())

		// Create a new route table for Internet access from private subnets
		privrt, err := ec2.NewRouteTable(ctx, "priv-rt", &ec2.RouteTableArgs{
			VpcId: vpc.ID(),
			Tags: pulumi.StringMap{
				"Name":  pulumi.Sprintf("%s-priv-rt", clusterName),
				k8sTag:  pulumi.String("shared"),
				"Owner": pulumi.String(ownerTagValue),
				"Team":  pulumi.String(teamTagValue),
			},
			Routes: ec2.RouteTableRouteArray{
				&ec2.RouteTableRouteArgs{
					CidrBlock:    pulumi.String("0.0.0.0/0"),
					NatGatewayId: natgw.ID(),
				},
			},
		})
		if err != nil {
			log.Printf("error creating private route table: %s", err.Error())
		}
		ctx.Export("privRouteTableId", privrt.ID())

		// Associate the private subnets with the NAT gateway route table
		for idx := 0; idx < numOfAZs; idx++ {
			_, err := ec2.NewRouteTableAssociation(ctx, fmt.Sprintf("priv-rta-%d", idx), &ec2.RouteTableAssociationArgs{
				SubnetId:     privSubnetIds[idx],
				RouteTableId: privrt.ID(),
			})
			if err != nil {
				log.Printf("error associating private subnet with route table: %s", err.Error())
			}
		}

		// Create a security group for traffic to the SSH bastion host
		bastionSecGrp, err := ec2.NewSecurityGroup(ctx, "bastion-sg", &ec2.SecurityGroupArgs{
			Name:        pulumi.Sprintf("%s-bastion-sg", clusterName),
			VpcId:       vpc.ID(),
			Description: pulumi.String("Allows SSH traffic to bastion hosts"),
			Ingress: ec2.SecurityGroupIngressArray{
				ec2.SecurityGroupIngressArgs{
					Protocol:    pulumi.String("tcp"),
					ToPort:      pulumi.Int(22),
					FromPort:    pulumi.Int(22),
					Description: pulumi.String("Allow inbound SSH (TCP 22) from anywhere"),
					CidrBlocks:  pulumi.StringArray{pulumi.String("0.0.0.0/0")},
				},
				ec2.SecurityGroupIngressArgs{
					Protocol:    pulumi.String("udp"),
					ToPort:      pulumi.Int(51280),
					FromPort:    pulumi.Int(51280),
					Description: pulumi.String("Allow Wireguard VPN (UDP 51280) from anywhere"),
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
				"Name":  pulumi.Sprintf("%s-bastion-sg", clusterName),
				k8sTag:  pulumi.String("shared"),
				"Owner": pulumi.String(ownerTagValue),
				"Team":  pulumi.String(teamTagValue),
			},
		})
		if err != nil {
			log.Printf("error creating security group: %s", err.Error())
		}
		ctx.Export("bastionSecGrpId", bastionSecGrp.ID())

		// Create a security group for Kubernetes nodes in this VPC
		nodeSecGrp, err := ec2.NewSecurityGroup(ctx, "node-sg", &ec2.SecurityGroupArgs{
			Name:        pulumi.Sprintf("%s-node-sg", clusterName),
			VpcId:       vpc.ID(),
			Description: pulumi.String("Allows traffic between and among K8s nodes"),
			Ingress: ec2.SecurityGroupIngressArray{
				ec2.SecurityGroupIngressArgs{
					Protocol:       pulumi.String("tcp"),
					ToPort:         pulumi.Int(22),
					FromPort:       pulumi.Int(22),
					Description:    pulumi.String("Allow inbound SSH (TCP 22) from bastion hosts"),
					SecurityGroups: pulumi.StringArray{bastionSecGrp.ID()},
				},
				ec2.SecurityGroupIngressArgs{
					Protocol:    pulumi.String("-1"),
					ToPort:      pulumi.Int(0),
					FromPort:    pulumi.Int(0),
					Description: pulumi.String("Allow all traffic from this security group"),
					Self:        pulumi.Bool(true),
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
				"Name":  pulumi.Sprintf("%s-node-sg", clusterName),
				k8sTag:  pulumi.String("shared"),
				"Owner": pulumi.String(ownerTagValue),
				"Team":  pulumi.String(teamTagValue),
			},
		})
		if err != nil {
			log.Printf("error creating security group: %s", err.Error())
		}
		ctx.Export("nodeSecGrpId", nodeSecGrp.ID())

		// Create a security group for the AWS cloud provider to manage
		k8sSecGrp, err := ec2.NewSecurityGroup(ctx, "k8s-sg", &ec2.SecurityGroupArgs{
			Name:        pulumi.Sprintf("%s-k8s-sg", clusterName),
			VpcId:       vpc.ID(),
			Description: pulumi.String("Managed by K8s cloud provider"),
			Tags: pulumi.StringMap{
				"Name":  pulumi.Sprintf("%s-k8s-sg", clusterName),
				k8sTag:  pulumi.String("owned"),
				"Owner": pulumi.String(ownerTagValue),
				"Team":  pulumi.String(teamTagValue),
			},
		})
		if err != nil {
			log.Printf("error creating security group: %s", err.Error())
		}
		ctx.Export("k8sSecGrpId", k8sSecGrp.ID())

		// Get AMI ID for bastion host
		mostRecent := true
		instanceAmi, err := ec2.LookupAmi(ctx, &ec2.LookupAmiArgs{
			Owners:     []string{"099720109477"},
			MostRecent: &mostRecent,
			Filters: []ec2.GetAmiFilter{
				{Name: "name", Values: []string{"ubuntu/images/hvm-ssd/ubuntu-focal-20.04-amd64-server*"}},
				{Name: "root-device-type", Values: []string{"ebs"}},
				{Name: "virtualization-type", Values: []string{"hvm"}},
				{Name: "architecture", Values: []string{"x86_64"}},
			},
		})
		if err != nil {
			log.Printf("error looking up AMI: %s", err.Error())
		}

		// Launch an instance to serve as bastion host
		bastion, err := ec2.NewInstance(ctx, "bastion", &ec2.InstanceArgs{
			Ami:                      pulumi.String(instanceAmi.Id),
			InstanceType:             pulumi.String(bastionAmiType),
			AssociatePublicIpAddress: pulumi.Bool(true),
			KeyName:                  pulumi.String(keyPair),
			SubnetId:                 pubSubnetIds[0],
			SourceDestCheck:          pulumi.Bool(false),
			VpcSecurityGroupIds:      pulumi.StringArray{bastionSecGrp.ID()},
			Tags: pulumi.StringMap{
				"Name":  pulumi.Sprintf("bastion-%s", clusterName),
				k8sTag:  pulumi.String("shared"),
				"Owner": pulumi.String(ownerTagValue),
				"Team":  pulumi.String(teamTagValue),
			},
		})
		if err != nil {
			log.Printf("error launching instance: %s", err.Error())
		}
		ctx.Export("bastionInstanceId", bastion.ID())
		ctx.Export("bastionPublicIpAddress", bastion.PublicIp)
		ctx.Export("bastionPrivateIpAddress", bastion.PrivateIp)

		return nil
	})
}
