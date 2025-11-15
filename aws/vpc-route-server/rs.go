package main

import (
	"fmt"
	"log"

	"github.com/pulumi/pulumi-aws/sdk/v7/go/aws/vpc"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
)

func buildRouting(ctx *pulumi.Context, z InfraDetails) (InfraDetails, error) {
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
		return z, err
	}

	// Associate the Route Server with our VPC
	// Docs: https://www.pulumi.com/registry/packages/aws/api-docs/vpc/routeservervpcassociation/
	rsa, err := vpc.NewRouteServerVpcAssociation(ctx, "rsa", &vpc.RouteServerVpcAssociationArgs{
		RouteServerId: rs.ID(),
		VpcId:         z.vpcId,
	})
	if err != nil {
		log.Printf("error associating Route Server with VPC: %s", err.Error())
		return z, err
	}

	// Create a Route Server Endpoint
	// Docs: https://www.pulumi.com/registry/packages/aws/api-docs/vpc/routeserverendpoint/
	rse, err := vpc.NewRouteServerEndpoint(ctx, "rse", &vpc.RouteServerEndpointArgs{
		RouteServerId: rs.RouteServerId,
		SubnetId:      z.privateSubnetIds[0],
		Tags: pulumi.StringMap{
			"Name":    pulumi.String("rse"),
			"Project": pulumi.String("vpc-route-server"),
		},
	}, pulumi.DependsOn([]pulumi.Resource{rsa}))
	if err != nil {
		log.Printf("error creating Route Server Endpoint: %s", err.Error())
		return z, err
	}

	// Create a Route Server Propagation
	// Docs: https://www.pulumi.com/registry/packages/aws/api-docs/vpc/routeserverpropagation/
	_, err = vpc.NewRouteServerPropagation(ctx, "rs-prop", &vpc.RouteServerPropagationArgs{
		RouteServerId: rs.RouteServerId,
		RouteTableId:  z.privateRouteTableId,
	}, pulumi.DependsOn([]pulumi.Resource{rsa}))
	if err != nil {
		log.Printf("error creating Route Server Propagation: %s", err.Error())
		return z, err
	}

	// Create peers for the Kubernetes control plane nodes
	for i := range z.cpInstanceIps {
		_, err := vpc.NewRouteServerPeer(ctx, fmt.Sprintf("rs-peer-cp-0%d", i), &vpc.RouteServerPeerArgs{
			BgpOptions: &vpc.RouteServerPeerBgpOptionsArgs{
				PeerAsn:               pulumi.Int(65001),
				PeerLivenessDetection: pulumi.String("bgp-keepalive"),
			},
			PeerAddress:           z.cpInstanceIps[i],
			RouteServerEndpointId: rse.RouteServerEndpointId,
			Tags: pulumi.StringMap{
				"Name":    pulumi.Sprintf("rs-peer-cp-0%d", i),
				"Project": pulumi.String("vpc-route-server"),
			},
		}, pulumi.DependsOn([]pulumi.Resource{rs, rse}))
		if err != nil {
			log.Printf("error creating Route Server peer: %s", err.Error())
			return z, err
		}
	}

	// Create peers for the Kubernetes worker nodes
	for i := range z.cpInstanceIps {
		_, err := vpc.NewRouteServerPeer(ctx, fmt.Sprintf("rs-peer-wkr-0%d", i), &vpc.RouteServerPeerArgs{
			BgpOptions: &vpc.RouteServerPeerBgpOptionsArgs{
				PeerAsn:               pulumi.Int(65001),
				PeerLivenessDetection: pulumi.String("bgp-keepalive"),
			},
			PeerAddress:           z.wkrInstanceIps[i],
			RouteServerEndpointId: rse.RouteServerEndpointId,
			Tags: pulumi.StringMap{
				"Name":    pulumi.Sprintf("rs-peer-wkr-0%d", i),
				"Project": pulumi.String("vpc-route-server"),
			},
		}, pulumi.DependsOn([]pulumi.Resource{rs, rse}))
		if err != nil {
			log.Printf("error creating Route Server peer: %s", err.Error())
			return z, err
		}
	}

	// Uncomment the following lines for additional outputs that may be useful for troubleshooting/diagnostics
	// ctx.Export("routeServerId", rs.RouteServerId)
	// ctx.Export("routeServerEndpointId", rse.RouteServerEndpointId)

	// Return to the calling function
	return z, err
}
