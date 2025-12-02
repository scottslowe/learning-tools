package main

import (
	"log"

	"github.com/pulumi/pulumi-aws/sdk/v7/go/aws/vpc"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
)

func main() {
	pulumi.Run(func(ctx *pulumi.Context) error {
		// Declare some variables used later
		userSuppliedVpcId := "NEEDS-VALUE"
		userSuppliedPrivateSubnetId := "NEEDS-VALUE"
		userSuppliedPrivateRouteTableId := "NEEDS-VALUE"
		userSuppliedPeerIpAddress := "NEEDS-VALUE"

		// Create a VPC Route Server
		// Docs: https://www.pulumi.com/registry/packages/aws/api-docs/vpc/routeserver/
		rs, err := vpc.NewRouteServer(ctx, "rs", &vpc.RouteServerArgs{
			AmazonSideAsn: pulumi.Int(65534),
			Tags: pulumi.StringMap{
				"Name":    pulumi.String("rs"),
				"Project": pulumi.String("vpc-route-server"),
			},
		})
		if err != nil {
			log.Printf("error creating Route Server: %s", err.Error())
			return err
		}

		// Associate the Route Server with our VPC
		// Docs: https://www.pulumi.com/registry/packages/aws/api-docs/vpc/routeservervpcassociation/
		rsa, err := vpc.NewRouteServerVpcAssociation(ctx, "rsa", &vpc.RouteServerVpcAssociationArgs{
			RouteServerId: rs.ID(),
			VpcId:         pulumi.String(userSuppliedVpcId),
		})
		if err != nil {
			log.Printf("error associating Route Server with VPC: %s", err.Error())
			return err
		}

		// Create a Route Server Endpoint
		// Docs: https://www.pulumi.com/registry/packages/aws/api-docs/vpc/routeserverendpoint/
		rse, err := vpc.NewRouteServerEndpoint(ctx, "rse", &vpc.RouteServerEndpointArgs{
			RouteServerId: rs.RouteServerId,
			SubnetId:      pulumi.String(userSuppliedPrivateSubnetId),
			Tags: pulumi.StringMap{
				"Name":    pulumi.String("rse"),
				"Project": pulumi.String("vpc-route-server"),
			},
		}, pulumi.DependsOn([]pulumi.Resource{rsa}))
		if err != nil {
			log.Printf("error creating Route Server Endpoint: %s", err.Error())
			return err
		}

		// Create a Route Server Propagation
		// Docs: https://www.pulumi.com/registry/packages/aws/api-docs/vpc/routeserverpropagation/
		_, err = vpc.NewRouteServerPropagation(ctx, "rs-prop", &vpc.RouteServerPropagationArgs{
			RouteServerId: rs.RouteServerId,
			RouteTableId:  pulumi.String(userSuppliedPrivateRouteTableId),
		}, pulumi.DependsOn([]pulumi.Resource{rsa}))
		if err != nil {
			log.Printf("error creating Route Server Propagation: %s", err.Error())
			return err
		}

		_, err = vpc.NewRouteServerPeer(ctx, "rs-peer-01", &vpc.RouteServerPeerArgs{
			BgpOptions: &vpc.RouteServerPeerBgpOptionsArgs{
				PeerAsn:               pulumi.Int(65001),
				PeerLivenessDetection: pulumi.String("bgp-keepalive"),
			},
			PeerAddress:           pulumi.String(userSuppliedPeerIpAddress),
			RouteServerEndpointId: rse.RouteServerEndpointId,
			Tags: pulumi.StringMap{
				"Name":    pulumi.String("rs-peer-01"),
				"Project": pulumi.String("vpc-route-server"),
			},
		}, pulumi.DependsOn([]pulumi.Resource{rs, rse}))
		if err != nil {
			log.Printf("error creating Route Server peer: %s", err.Error())
			return err
		}

		// Export the name of the bucket
		ctx.Export("routeServerId", rs.RouteServerId)
		ctx.Export("routeServerEndpointId", rse.RouteServerEndpointId)
		ctx.Export("routeServerEndpointAddress", rse.EniAddress)

		return nil
	})
}
