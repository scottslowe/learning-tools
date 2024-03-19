package main

import (
	"fmt"
	"log"
	"net"
	"net/netip"

	"github.com/apparentlymart/go-cidr/cidr"
	"github.com/pulumi/pulumi-aws/sdk/v6/go/aws"
	"github.com/pulumi/pulumi-aws/sdk/v6/go/aws/ec2"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi/config"
)

func main() {
	pulumi.Run(func(ctx *pulumi.Context) error {
		// Get some values from the Pulumi stack configuration
		keyPair := config.Require(ctx, "sshkeypair")
		vpcNetworkCidr, err := config.Try(ctx, "networkcidr")
		if err != nil {
			vpcNetworkCidr = "10.0.0.0/16"
		}
		subnetMask, err := config.TryInt(ctx, "subnetmask")
		if err != nil {
			subnetMask = 22
		}
		clusterName, err := config.Try(ctx, "clustername")
		if err != nil {
			clusterName = "test"
		}
		ownerTagValue, err := config.Try(ctx, "ownertagvalue")
		if err != nil {
			ownerTagValue = "nobody@nowhere.com"
		}
		teamTagValue, err := config.Try(ctx, "teamtagvalue")
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
			subnet, err := ec2.NewSubnet(ctx, fmt.Sprintf("public-%d", idx), &ec2.SubnetArgs{
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
		defRt, err := ec2.NewDefaultRouteTable(ctx, "def-rt", &ec2.DefaultRouteTableArgs{
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
		ctx.Export("defaultRoute", defRt.ID())

		// Create a route for Internet access in the default route table
		route, err := ec2.NewRoute(ctx, "inet-route", &ec2.RouteArgs{
			RouteTableId:         defRt.ID(),
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
			subnet, err := ec2.NewSubnet(ctx, fmt.Sprintf("private-%d", idx), &ec2.SubnetArgs{
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

		// Create a security group for the NAT instance/bastion host
		edgeSecGrp, err := ec2.NewSecurityGroup(ctx, "edge-sg", &ec2.SecurityGroupArgs{
			Name:        pulumi.Sprintf("%s-edge-sg", clusterName),
			VpcId:       vpc.ID(),
			Description: pulumi.String("Allows inbound SSH and WG traffic"),
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
				ec2.SecurityGroupIngressArgs{
					Protocol:    pulumi.String("-1"),
					ToPort:      pulumi.Int(0),
					FromPort:    pulumi.Int(0),
					Description: pulumi.String("Allow all traffic from VPC network CIDR"),
					CidrBlocks:  pulumi.StringArray{pulumi.String(vpcNetworkCidr)},
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
				"Name":  pulumi.Sprintf("%s-edge-sg", clusterName),
				k8sTag:  pulumi.String("shared"),
				"Owner": pulumi.String(ownerTagValue),
				"Team":  pulumi.String(teamTagValue),
			},
		})
		if err != nil {
			log.Printf("error creating security group: %s", err.Error())
		}
		ctx.Export("edgeSecGrpId", edgeSecGrp.ID())

		// Get AMI ID for the NAT instance
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
			KeyName:                  pulumi.String(keyPair),
			SourceDestCheck:          pulumi.BoolPtr(false),
			SubnetId:                 pubSubnetIds[0],
			VpcSecurityGroupIds:      pulumi.StringArray{edgeSecGrp.ID()},
			Tags: pulumi.StringMap{
				"Name":  pulumi.String("nat-instance"),
				k8sTag:  pulumi.String("shared"),
				"Owner": pulumi.String(ownerTagValue),
				"Team":  pulumi.String(teamTagValue),
			},
		})
		if err != nil {
			log.Printf("error launching instance: %s", err.Error())
		}
		ctx.Export("natInstanceId", natInstance.ID())
		ctx.Export("natPublicIpAddress", natInstance.PublicIp)
		ctx.Export("natPrivateIpAddress", natInstance.PrivateIp)

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
					CidrBlock:          pulumi.String("0.0.0.0/0"),
					NetworkInterfaceId: natInstance.PrimaryNetworkInterfaceId.ToStringOutput(),
				},
			},
		})
		if err != nil {
			log.Printf("error creating private route table: %s", err.Error())
		}
		ctx.Export("privRouteTableId", privrt.ID())

		// Associate the private subnets with the NAT instance route table
		for idx := 0; idx < numOfAZs; idx++ {
			_, err := ec2.NewRouteTableAssociation(ctx, fmt.Sprintf("priv-rta-%d", idx), &ec2.RouteTableAssociationArgs{
				SubnetId:     privSubnetIds[idx],
				RouteTableId: privrt.ID(),
			})
			if err != nil {
				log.Printf("error associating private subnet with route table: %s", err.Error())
			}
		}

		// Create a security group for Kubernetes nodes in this VPC
		nodeSecGrp, err := ec2.NewSecurityGroup(ctx, "node-sg", &ec2.SecurityGroupArgs{
			Name:        pulumi.Sprintf("%s-node-sg", clusterName),
			VpcId:       vpc.ID(),
			Description: pulumi.String("Allows traffic between and among K8s nodes"),
			Ingress: ec2.SecurityGroupIngressArray{
				ec2.SecurityGroupIngressArgs{
					Protocol:       pulumi.String("-1"),
					ToPort:         pulumi.Int(0),
					FromPort:       pulumi.Int(0),
					Description:    pulumi.String("Allow all traffic from edge security group"),
					SecurityGroups: pulumi.StringArray{edgeSecGrp.ID()},
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

		return nil
	})
}
