package main

import (
	"encoding/json"

	"github.com/pulumi/pulumi-aws/sdk/v6/go/aws/iam"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
)

func main() {
	pulumi.Run(func(ctx *pulumi.Context) error {
		// Create a policy for the Kubernetes control plane nodes
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
		controlPlanePolicy, err := iam.NewPolicy(ctx, "control-plane-policy", &iam.PolicyArgs{
			Name:        pulumi.String("control-plane-policy"),
			Path:        pulumi.String("/"),
			Description: pulumi.String("Policy for Kubernetes control plane nodes to interact with AWS API"),
			Policy:      pulumi.String(json0),
		})
		if err != nil {
			return err
		}
		ctx.Export("controlPlanePolicyArn", controlPlanePolicy.Arn)

		// Create a policy for the Kubernetes worker nodes
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
		workerNodePolicy, err := iam.NewPolicy(ctx, "worker-node-policy", &iam.PolicyArgs{
			Name:        pulumi.String("worker-node-policy"),
			Path:        pulumi.String("/"),
			Description: pulumi.String("Policy for Kubernetes worker nodes to interact with AWS API"),
			Policy:      pulumi.String(json1),
		})
		if err != nil {
			return err
		}
		ctx.Export("workerNodePolicyArn", workerNodePolicy.Arn)

		// Create formatted JSON for a role assumption policy
		tmpJSON2, err := json.Marshal(map[string]interface{}{
			"Version": "2012-10-17",
			"Statement": []map[string]interface{}{
				map[string]interface{}{
					"Action": "sts:AssumeRole",
					"Effect": "Allow",
					"Principal": map[string]interface{}{
						"Service": "ec2.amazonaws.com",
					},
				},
			},
		})
		if err != nil {
			return err
		}
		json2 := string(tmpJSON2)

		// Define a role for the Kubernetes control plane nodes
		controlPlaneRole, err := iam.NewRole(ctx, "control-plane-role", &iam.RoleArgs{
			AssumeRolePolicy: pulumi.String(json2),
			Name:             pulumi.String("control-plane-role"),
		})
		if err != nil {
			return err
		}

		// Attach the control plane policy to the control plane role
		_, err = iam.NewRolePolicyAttachment(ctx, "cp-policy-attach", &iam.RolePolicyAttachmentArgs{
			PolicyArn: controlPlanePolicy.Arn,
			Role:      controlPlaneRole.Name,
		})
		if err != nil {
			return err
		}

		// Create an instance profile using the control plane role
		cpInstanceProfile, err := iam.NewInstanceProfile(ctx, "cp-instance-profile", &iam.InstanceProfileArgs{
			Name: pulumi.String("control-plane-instance-profile"),
			Role: controlPlaneRole.Name,
		})
		if err != nil {
			return err
		}
		ctx.Export("cpInstanceProfileName", cpInstanceProfile.Name)

		// Define a role for the Kubernetes worker nodes
		workerNodeRole, err := iam.NewRole(ctx, "worker-node-role", &iam.RoleArgs{
			AssumeRolePolicy: pulumi.String(json2),
			Name:             pulumi.String("worker-node-role"),
		})
		if err != nil {
			return err
		}

		// Attach the worker node policy to the worker node role
		_, err = iam.NewRolePolicyAttachment(ctx, "worker-policy-attach", &iam.RolePolicyAttachmentArgs{
			PolicyArn: workerNodePolicy.Arn,
			Role:      workerNodeRole.Name,
		})
		if err != nil {
			return err
		}

		// Create an instance profile using the control plane role
		wkrInstanceProfile, err := iam.NewInstanceProfile(ctx, "wkr-instance-profile", &iam.InstanceProfileArgs{
			Name: pulumi.String("worker-node-instance-profile"),
			Role: workerNodeRole.Name,
		})
		if err != nil {
			return err
		}
		ctx.Export("wkrInstanceProfileName", wkrInstanceProfile.Name)

		return nil
	})
}
