package main

import (
	"encoding/json"
	"github.com/pulumi/pulumi-aws/sdk/v6/go/aws/iam"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
)

func main() {
	pulumi.Run(func(ctx *pulumi.Context) error {
		// Create a policy for Kubernetes control plane nodes
		tmpJSON0, err := json.Marshal(map[string]interface{}{
			"Version": "2012-10-17",
			"Statement": []map[string]interface{}{
				map[string]interface{}{
					"Action": []string{
						"autoscaling:DescribeAutoScalingGroups",
						"autoscaling:DescribeLaunchConfigurations",
						"autoscaling:DescribeTags",
						"ec2:DescribeInstances",
						"ec2:DescribeRegions",
						"ec2:DescribeRouteTables",
						"ec2:DescribeSecurityGroups",
						"ec2:DescribeSubnets",
						"ec2:DescribeVolumes",
						"ec2:DescribeAvailabilityZones",
						"ec2:CreateSecurityGroup",
						"ec2:CreateTags",
						"ec2:CreateVolume",
						"ec2:ModifyInstanceAttribute",
						"ec2:ModifyVolume",
						"ec2:AttachVolume",
						"ec2:AuthorizeSecurityGroupIngress",
						"ec2:CreateRoute",
						"ec2:DeleteRoute",
						"ec2:DeleteSecurityGroup",
						"ec2:DeleteVolume",
						"ec2:DetachVolume",
						"ec2:RevokeSecurityGroupIngress",
						"ec2:DescribeVpcs",
						"elasticloadbalancing:AddTags",
						"elasticloadbalancing:AttachLoadBalancerToSubnets",
						"elasticloadbalancing:ApplySecurityGroupsToLoadBalancer",
						"elasticloadbalancing:CreateLoadBalancer",
						"elasticloadbalancing:CreateLoadBalancerPolicy",
						"elasticloadbalancing:CreateLoadBalancerListeners",
						"elasticloadbalancing:ConfigureHealthCheck",
						"elasticloadbalancing:DeleteLoadBalancer",
						"elasticloadbalancing:DeleteLoadBalancerListeners",
						"elasticloadbalancing:DescribeLoadBalancers",
						"elasticloadbalancing:DescribeLoadBalancerAttributes",
						"elasticloadbalancing:DetachLoadBalancerFromSubnets",
						"elasticloadbalancing:DeregisterInstancesFromLoadBalancer",
						"elasticloadbalancing:ModifyLoadBalancerAttributes",
						"elasticloadbalancing:RegisterInstancesWithLoadBalancer",
						"elasticloadbalancing:SetLoadBalancerPoliciesForBackendServer",
						"elasticloadbalancing:AddTags",
						"elasticloadbalancing:CreateListener",
						"elasticloadbalancing:CreateTargetGroup",
						"elasticloadbalancing:DeleteListener",
						"elasticloadbalancing:DeleteTargetGroup",
						"elasticloadbalancing:DescribeListeners",
						"elasticloadbalancing:DescribeLoadBalancerPolicies",
						"elasticloadbalancing:DescribeTargetGroups",
						"elasticloadbalancing:DescribeTargetHealth",
						"elasticloadbalancing:ModifyListener",
						"elasticloadbalancing:ModifyTargetGroup",
						"elasticloadbalancing:RegisterTargets",
						"elasticloadbalancing:DeregisterTargets",
						"elasticloadbalancing:SetLoadBalancerPoliciesOfListener",
						"iam:CreateServiceLinkedRole",
						"kms:DescribeKey",
					},
					"Effect":   "Allow",
					"Resource": "*",
				},
			},
		})
		if err != nil {
			return err
		}
		json0 := string(tmpJSON0)

		// Define IAM policy using JSON object previously defined
		cpInstancePolicy, err := iam.NewPolicy(ctx, "cp-instance-policy", &iam.PolicyArgs{
			Name:        pulumi.String("control-plane-instance-policy"),
			Path:        pulumi.String("/"),
			Description: pulumi.String("Policy for Kubernetes control plane nodes to interact with AWS API"),
			Policy:      pulumi.String(json0),
		})
		if err != nil {
			return err
		}
		ctx.Export("cpInstancePolicyArn", cpInstancePolicy.Arn)

		// Create a policy for Kubernetes control plane nodes
		tmpJSON1, err := json.Marshal(map[string]interface{}{
			"Version": "2012-10-17",
			"Statement": []map[string]interface{}{
				map[string]interface{}{
					"Action": []string{
						"ec2:DescribeInstances",
						"ec2:DescribeRegions",
						"ecr:GetAuthorizationToken",
						"ecr:BatchCheckLayerAvailability",
						"ecr:GetDownloadUrlForLayer",
						"ecr:GetRepositoryPolicy",
						"ecr:DescribeRepositories",
						"ecr:ListImages",
						"ecr:BatchGetImages",
					},
					"Effect":   "Allow",
					"Resource": "*",
				},
			},
		})
		if err != nil {
			return err
		}
		json1 := string(tmpJSON1)

		// Define IAM policy using JSON object previously defined
		nodeInstancePolicy, err := iam.NewPolicy(ctx, "node-instance-policy", &iam.PolicyArgs{
			Name:        pulumi.String("node-instance-policy"),
			Path:        pulumi.String("/"),
			Description: pulumi.String("Policy for Kubernetes worker nodes to interact with AWS API"),
			Policy:      pulumi.String(json1),
		})
		if err != nil {
			return err
		}
		ctx.Export("nodeInstancePolicyArn", nodeInstancePolicy.Arn)

		return nil
	})
}
