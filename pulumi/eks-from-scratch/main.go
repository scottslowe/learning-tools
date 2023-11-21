package main

import (
	"github.com/pulumi/pulumi-aws/sdk/v6/go/aws/ec2"
	"github.com/pulumi/pulumi-aws/sdk/v6/go/aws/eks"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
)

func main() {
	pulumi.Run(func(ctx *pulumi.Context) error {
		// Build out the base infrastructure (see vpc.go)
		buildInfrastructure(ctx)

		// Create IAM role (see iam.go)
		createIam(ctx)

		// Create a Security Group that we can use to actually connect to our cluster
		clusterSg, err := ec2.NewSecurityGroup(ctx, "cluster-sg", &ec2.SecurityGroupArgs{
			VpcId: vpcId,
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
					Protocol:   pulumi.String("tcp"),
					FromPort:   pulumi.Int(80),
					ToPort:     pulumi.Int(80),
					CidrBlocks: pulumi.StringArray{pulumi.String("0.0.0.0/0")},
				},
				ec2.SecurityGroupIngressArgs{
					Protocol:   pulumi.String("tcp"),
					FromPort:   pulumi.Int(443),
					ToPort:     pulumi.Int(443),
					CidrBlocks: pulumi.StringArray{pulumi.String("0.0.0.0/0")},
				},
			},
		})
		if err != nil {
			return err
		}

		// Create an EKS cluster
		testCluster, err := eks.NewCluster(ctx, "test-cluster", &eks.ClusterArgs{
			Name:    pulumi.String("testcluster"),
			RoleArn: eksClusterRoleArn,
			VpcConfig: &eks.ClusterVpcConfigArgs{
				EndpointPrivateAccess: pulumi.Bool(false),
				EndpointPublicAccess:  pulumi.Bool(true),
				SecurityGroupIds:      pulumi.StringArray{clusterSg.ID()},
				SubnetIds:             privateSubnets,
			},
		})
		if err != nil {
			return err
		}

		// Create a node group for the EKS cluster
		_, err = eks.NewNodeGroup(ctx, "node-group", &eks.NodeGroupArgs{
			ClusterName: testCluster.Name,
			NodeRoleArn: eksNodeRoleArn,
			SubnetIds:   privateSubnets,
			ScalingConfig: &eks.NodeGroupScalingConfigArgs{
				DesiredSize: pulumi.Int(3),
				MaxSize:     pulumi.Int(6),
				MinSize:     pulumi.Int(3),
			},
			UpdateConfig: &eks.NodeGroupUpdateConfigArgs{
				MaxUnavailable: pulumi.Int(1),
			},
		})
		if err != nil {
			return err
		}

		// Install the VPC CNI addon
		_, err = eks.NewAddon(ctx, "aws-vpc-cni", &eks.AddonArgs{
			ClusterName:              testCluster.Name,
			AddonName:                pulumi.String("vpc-cni"),            // Need to verify
			AddonVersion:             pulumi.String("v1.24.0-eksbuild.1"), // Need to verify
			ResolveConflictsOnUpdate: pulumi.String("PRESERVE"),
		})
		if err != nil {
			return err
		}

		// Install the CoreDNS addon
		_, err = eks.NewAddon(ctx, "coredns", &eks.AddonArgs{
			ClusterName:              testCluster.Name,
			AddonName:                pulumi.String("coredns"),            // Need to verify
			AddonVersion:             pulumi.String("v1.24.0-eksbuild.1"), // Need to verify
			ResolveConflictsOnUpdate: pulumi.String("PRESERVE"),
		})
		if err != nil {
			return err
		}

		// Install the AWS EBS CSI addon
		_, err = eks.NewAddon(ctx, "aws-ebs-csi", &eks.AddonArgs{
			ClusterName:              testCluster.Name,
			AddonName:                pulumi.String("aws-ebs-csi-driver"),
			AddonVersion:             pulumi.String("v1.24.0-eksbuild.1"),
			ResolveConflictsOnUpdate: pulumi.String("PRESERVE"),
		})
		if err != nil {
			return err
		}

		// Generate a Kubeconfig to access the cluster and make it accessible
		clusterKubeconfig := generateKubeconfig(testCluster.Endpoint, testCluster.CertificateAuthority.Data().Elem(), testCluster.Name)
		ctx.Export("kubeconfig", clusterKubeconfig)
		return nil
	})
}

// Create the KubeConfig structure as per https://docs.aws.amazon.com/eks/latest/userguide/create-kubeconfig.html
func generateKubeconfig(clusterEndpoint pulumi.StringOutput, certData pulumi.StringOutput, clusterName pulumi.StringOutput) pulumi.StringOutput {
	return pulumi.Sprintf(`{
        "apiVersion": "v1",
        "clusters": [{
            "cluster": {
                "server": "%s",
                "certificate-authority-data": "%s"
            },
            "name": "kubernetes",
        }],
        "contexts": [{
            "context": {
                "cluster": "kubernetes",
                "user": "aws",
            },
            "name": "aws",
        }],
        "current-context": "aws",
        "kind": "Config",
        "users": [{
            "name": "aws",
            "user": {
                "exec": {
                    "apiVersion": "client.authentication.k8s.io/v1beta1",
                    "command": "aws-iam-authenticator",
                    "args": [
                        "token",
                        "-i",
                        "%s",
                    ],
                },
            },
        }],
    }`, clusterEndpoint, certData, clusterName)
}
