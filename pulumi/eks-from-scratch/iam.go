package main

import (
	"fmt"

	"github.com/pulumi/pulumi-aws/sdk/v6/go/aws/iam"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
)

// Define variables needed outside the createIam() function
var eksClusterRoleArn pulumi.StringInput
var eksNodeRoleArn pulumi.StringInput

func createIam(ctx *pulumi.Context) (err error) {
	// Get policy documents for cluster and node assume role statements
	// First for the cluster
	clusterAssumeRole, err := iam.GetPolicyDocument(ctx, &iam.GetPolicyDocumentArgs{
		Statements: []iam.GetPolicyDocumentStatement{
			{
				Effect: pulumi.StringRef("Allow"),
				Principals: []iam.GetPolicyDocumentStatementPrincipal{
					{
						Type: "Service",
						Identifiers: []string{
							"eks.amazonaws.com",
						},
					},
				},
				Actions: []string{
					"sts:AssumeRole",
				},
			},
		},
	}, nil)
	if err != nil {
		return err
	}
	// Second for the nodes
	nodeAssumeRole, err := iam.GetPolicyDocument(ctx, &iam.GetPolicyDocumentArgs{
		Statements: []iam.GetPolicyDocumentStatement{
			{
				Effect: pulumi.StringRef("Allow"),
				Principals: []iam.GetPolicyDocumentStatementPrincipal{
					{
						Type: "Service",
						Identifiers: []string{
							"ec2.amazonaws.com",
						},
					},
				},
				Actions: []string{
					"sts:AssumeRole",
				},
			},
		},
	}, nil)
	if err != nil {
		return err
	}

	// Define the cluster IAM role
	clusterIamRole, err := iam.NewRole(ctx, "cluster-iam-role", &iam.RoleArgs{
		AssumeRolePolicy: pulumi.String(clusterAssumeRole.Json),
	})
	if err != nil {
		return err
	}

	// Define the node IAM role
	nodeIamRole, err := iam.NewRole(ctx, "node-iam-role", &iam.RoleArgs{
		AssumeRolePolicy: pulumi.String(nodeAssumeRole.Json),
	})
	if err != nil {
		return err
	}

	// Attach the cluster IAM role to necessary policies
	clusterPolicies := []string{
		"arn:aws:iam::aws:policy/AmazonEKSClusterPolicy",
		"arn:aws:iam::aws:policy/AmazonEKSVPCResourceController",
		"arn:aws:iam::aws:policy/AmazonEKSServicePolicy",
	}
	for i, policy := range clusterPolicies {
		_, err := iam.NewRolePolicyAttachment(ctx, fmt.Sprintf("cluster-pa-%d", i), &iam.RolePolicyAttachmentArgs{
			PolicyArn: pulumi.String(policy),
			Role:      clusterIamRole.Name,
		})
		if err != nil {
			return err
		}
	}

	// Attach the node IAM role to necessary policies
	nodePolicies := []string{
		"arn:aws:iam::aws:policy/AmazonEKSWorkerNodePolicy",
		"arn:aws:iam::aws:policy/AmazonEKS_CNI_Policy",
		"arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly",
		"arn:aws:iam::aws:policy/service-role/AmazonEBSCSIDriverPolicy",
	}
	for i, policy := range nodePolicies {
		_, err := iam.NewRolePolicyAttachment(ctx, fmt.Sprintf("node-pa-%d", i), &iam.RolePolicyAttachmentArgs{
			PolicyArn: pulumi.String(policy),
			Role:      nodeIamRole.Name,
		})
		if err != nil {
			return err
		}
	}

	// Make the ARNs of the cluster and node IAM roles visible outside this function
	eksClusterRoleArn = clusterIamRole.Arn
	eksNodeRoleArn = nodeIamRole.Arn

	return nil
}
