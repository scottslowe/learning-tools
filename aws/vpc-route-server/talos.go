package main

import (
	"encoding/json"
	"fmt"
	"log"

	"github.com/pulumi/pulumi-aws/sdk/v7/go/aws/ec2"
	"github.com/pulumi/pulumi-aws/sdk/v7/go/aws/elb"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
	"github.com/pulumiverse/pulumi-talos/sdk/go/talos/client"
	"github.com/pulumiverse/pulumi-talos/sdk/go/talos/cluster"
	"github.com/pulumiverse/pulumi-talos/sdk/go/talos/machine"
)

func buildTalosCluster(ctx *pulumi.Context, z InfraDetails, net string) (InfraDetails, error) {
	// Create a security group for the Talos cluster
	// Details: https://www.pulumi.com/registry/packages/aws/api-docs/ec2/securitygroup/
	talosSg, err := ec2.NewSecurityGroup(ctx, "talos-sg", &ec2.SecurityGroupArgs{
		Name:        pulumi.String("talosSg"),
		VpcId:       z.vpcId,
		Description: pulumi.String("Security group for the Talos cluster"),
		Tags: pulumi.StringMap{
			"Name":    pulumi.String("talos-sg"),
			"Project": pulumi.String("vpc-route-server"),
		},
	})
	if err != nil {
		log.Printf("error creating security group: %s", err.Error())
		return z, err
	}

	// Add rules to Talos security group
	// Details: https://www.pulumi.com/registry/packages/aws/api-docs/ec2/securitygrouprule/
	// First, allow all traffic within the security group
	_, err = ec2.NewSecurityGroupRule(ctx, "allow-all-talos-rule", &ec2.SecurityGroupRuleArgs{
		Type:                  pulumi.String("ingress"),
		FromPort:              pulumi.Int(0),
		ToPort:                pulumi.Int(65535),
		Protocol:              pulumi.String("all"),
		SourceSecurityGroupId: talosSg.ID(),
		SecurityGroupId:       talosSg.ID(),
	})
	if err != nil {
		log.Printf("error adding inbound rule to security group: %s", err.Error())
		return z, err
	}

	// Next, allow inbound access to Kubernetes APIs
	_, err = ec2.NewSecurityGroupRule(ctx, "allow-k8s-api", &ec2.SecurityGroupRuleArgs{
		Type:            pulumi.String("ingress"),
		FromPort:        pulumi.Int(6443),
		ToPort:          pulumi.Int(6443),
		Protocol:        pulumi.String("tcp"),
		CidrBlocks:      pulumi.StringArray{pulumi.String("0.0.0.0/0")},
		SecurityGroupId: talosSg.ID(),
	})
	if err != nil {
		log.Printf("error adding inbound rule to security group: %s", err.Error())
		return z, err
	}

	// Allow inbound access to Talos APIs
	_, err = ec2.NewSecurityGroupRule(ctx, "allow-talos-api", &ec2.SecurityGroupRuleArgs{
		Type:            pulumi.String("ingress"),
		FromPort:        pulumi.Int(50000),
		ToPort:          pulumi.Int(50000),
		Protocol:        pulumi.String("tcp"),
		CidrBlocks:      pulumi.StringArray{pulumi.String("0.0.0.0/0")},
		SecurityGroupId: talosSg.ID(),
	})
	if err != nil {
		log.Printf("error adding inbound rule to security group: %s", err.Error())
		return z, err
	}

	// Allow all outbound traffic
	_, err = ec2.NewSecurityGroupRule(ctx, "allow-egress", &ec2.SecurityGroupRuleArgs{
		Type:            pulumi.String("egress"),
		FromPort:        pulumi.Int(0),
		ToPort:          pulumi.Int(65535),
		Protocol:        pulumi.String("all"),
		CidrBlocks:      pulumi.StringArray{pulumi.String("0.0.0.0/0")},
		SecurityGroupId: talosSg.ID(),
	})
	if err != nil {
		log.Printf("error adding outbound rule to security group: %s", err.Error())
		return z, err
	}

	// Create a security group for the load balancer
	talosLbSg, err := ec2.NewSecurityGroup(ctx, "talos-lb-sg", &ec2.SecurityGroupArgs{
		Name:        pulumi.String("talosLbSg"),
		VpcId:       z.vpcId,
		Description: pulumi.String("Security group for the Talos load balancer"),
		Tags: pulumi.StringMap{
			"Name":    pulumi.String("talosLbSg"),
			"Project": pulumi.String("vpc-route-server"),
		},
	})
	if err != nil {
		log.Printf("error creating security group: %s", err.Error())
		return z, err
	}

	// Allow K8s API inbound to load balancer
	_, err = ec2.NewSecurityGroupRule(ctx, "allow-k8s-api-lb", &ec2.SecurityGroupRuleArgs{
		Type:            pulumi.String("ingress"),
		FromPort:        pulumi.Int(6443),
		ToPort:          pulumi.Int(6443),
		Protocol:        pulumi.String("tcp"),
		CidrBlocks:      pulumi.StringArray{pulumi.String("0.0.0.0/0")},
		SecurityGroupId: talosLbSg.ID(),
	})
	if err != nil {
		log.Printf("error adding rule to security group: %s", err.Error())
		return z, err
	}

	// Allow K8s API traffic outbound to nodes
	_, err = ec2.NewSecurityGroupRule(ctx, "allow-egress-lb", &ec2.SecurityGroupRuleArgs{
		Type:            pulumi.String("egress"),
		FromPort:        pulumi.Int(6443),
		ToPort:          pulumi.Int(6443),
		Protocol:        pulumi.String("tcp"),
		CidrBlocks:      pulumi.StringArray{pulumi.String(net)},
		SecurityGroupId: talosLbSg.ID(),
	})
	if err != nil {
		log.Printf("error adding rule to security group: %s", err.Error())
		return z, err
	}

	// Allow traffic from load balancer to Talos cluster
	_, err = ec2.NewSecurityGroupRule(ctx, "allow-talos-lb", &ec2.SecurityGroupRuleArgs{
		Type:                  pulumi.String("ingress"),
		FromPort:              pulumi.Int(0),
		ToPort:                pulumi.Int(65535),
		Protocol:              pulumi.String("all"),
		SourceSecurityGroupId: talosLbSg.ID(),
		SecurityGroupId:       talosSg.ID(),
	})
	if err != nil {
		log.Printf("error adding rule to security group: %s", err.Error())
		return z, err
	}

	// Create a load balancer
	// Details: https://www.pulumi.com/registry/packages/aws/api-docs/elb/loadbalancer/
	talosLb, err := elb.NewLoadBalancer(ctx, "talos-lb", &elb.LoadBalancerArgs{
		Name: pulumi.String("talosLb"),
		Listeners: elb.LoadBalancerListenerArray{
			&elb.LoadBalancerListenerArgs{
				InstancePort:     pulumi.Int(6443),
				InstanceProtocol: pulumi.String("tcp"),
				LbPort:           pulumi.Int(6443),
				LbProtocol:       pulumi.String("tcp"),
			},
		},
		SecurityGroups: pulumi.StringArray{talosLbSg.ID()},
		Subnets:        z.publicSubnetIds,
		Tags: pulumi.StringMap{
			"Name":    pulumi.String("talos-lb"),
			"Project": pulumi.String("vpc-route-server"),
		},
	})
	if err != nil {
		log.Printf("error creating load balancer: %s", err.Error())
		return z, err
	}

	// Get ID for the official Talos Linux AMI
	// Details: https://www.pulumi.com/registry/packages/aws/api-docs/ec2/getami/
	talosAmi, err := ec2.LookupAmi(ctx, &ec2.LookupAmiArgs{
		Owners:     []string{"540036508848"},
		MostRecent: pulumi.BoolRef(true),
		Filters: []ec2.GetAmiFilter{
			{Name: "name", Values: []string{"talos-v1.11*"}},
			{Name: "root-device-type", Values: []string{"ebs"}},
			{Name: "virtualization-type", Values: []string{"hvm"}},
			{Name: "architecture", Values: []string{"x86_64"}},
		},
	})
	if err != nil {
		log.Printf("error looking up Talos Linux AMI: %s", err.Error())
		return z, err
	}

	// Launch EC2 instances for the control plane nodes
	// Details: https://www.pulumi.com/registry/packages/aws/api-docs/ec2/instance/
	cpInstanceIds := make([]pulumi.StringInput, 3)
	cpInstancePrivIps := make([]pulumi.StringInput, 3)
	// cpInstancePubIps := make([]pulumi.StringInput, 3)
	for i := range 3 {
		instance, err := ec2.NewInstance(ctx, fmt.Sprintf("talos-cp-0%d", i), &ec2.InstanceArgs{
			Ami:                      pulumi.String(talosAmi.Id),
			AssociatePublicIpAddress: pulumi.Bool(true),
			IamInstanceProfile:       pulumi.String("control-plane.cloud-provider-aws.sigs.k8s.io"),
			InstanceType:             pulumi.String("m5a.xlarge"),
			SourceDestCheck:          pulumi.BoolPtr(false),
			SubnetId:                 z.publicSubnetIds[i],
			Tags: pulumi.StringMap{
				"Name":    pulumi.Sprintf("talos-cp-0%d", i),
				"Project": pulumi.String("vpc-route-server"),
			},
			VpcSecurityGroupIds: pulumi.StringArray{talosSg.ID()},
		})
		if err != nil {
			log.Printf("error creating instance: %s", err.Error())
			return z, err
		} else {
			cpInstanceIds[i] = instance.ID()
			cpInstancePrivIps[i] = instance.PrivateIp
			z.cpInstanceIps = append(z.cpInstanceIps, instance.PublicIp)
		}
	}

	// Attach control plane instances to load balancer
	// Details: https://www.pulumi.com/registry/packages/aws/api-docs/elb/attachment/
	for i := range 3 {
		_, err := elb.NewAttachment(ctx, fmt.Sprintf("lb-attachment-0%d", i), &elb.AttachmentArgs{
			Elb:      talosLb.ID(),
			Instance: cpInstanceIds[i],
		})
		if err != nil {
			log.Printf("error attaching instance to load balancer: %s", err.Error())
			return z, err
		}
	}

	// Build the Talos cluster configuration
	// First, generate machine secrets
	// Details: https://www.pulumi.com/registry/packages/talos/api-docs/machine/secrets/
	talosSecrets, err := machine.NewSecrets(ctx, "talos-secrets", nil)
	if err != nil {
		log.Printf("error generating machine secrets: %s", err.Error())
		return z, err
	}

	// Get machine configuration for the control plane
	// Details: https://www.pulumi.com/registry/packages/talos/api-docs/machine/getconfiguration/
	talosCpCfg := machine.GetConfigurationOutput(ctx, machine.GetConfigurationOutputArgs{
		ClusterEndpoint: pulumi.Sprintf("https://%v:6443", talosLb.DnsName),
		ClusterName:     pulumi.String("talos-cluster"),
		Docs:            pulumi.BoolPtr(false),
		Examples:        pulumi.BoolPtr(false),
		MachineSecrets: machine.MachineSecretsArgs{
			Certs:      talosSecrets.MachineSecrets.Certs(),
			Cluster:    talosSecrets.MachineSecrets.Cluster(),
			Secrets:    talosSecrets.MachineSecrets.Secrets(),
			Trustdinfo: talosSecrets.MachineSecrets.Trustdinfo(),
		},
		MachineType:  pulumi.String("controlplane"),
		TalosVersion: pulumi.String("v1.11"),
	})

	// Get machine configuration for the worker nodes
	talosWkrCfg := machine.GetConfigurationOutput(ctx, machine.GetConfigurationOutputArgs{
		ClusterEndpoint: pulumi.Sprintf("https://%v:6443", talosLb.DnsName),
		ClusterName:     pulumi.String("talos-cluster"),
		Docs:            pulumi.BoolPtr(false),
		Examples:        pulumi.BoolPtr(false),
		MachineSecrets: machine.MachineSecretsArgs{
			Certs:      talosSecrets.MachineSecrets.Certs(),
			Cluster:    talosSecrets.MachineSecrets.Cluster(),
			Secrets:    talosSecrets.MachineSecrets.Secrets(),
			Trustdinfo: talosSecrets.MachineSecrets.Trustdinfo(),
		},
		MachineType:  pulumi.String("worker"),
		TalosVersion: pulumi.String("v1.11"),
	})

	// Generate a machine patch for the control plane nodes
	tmpJSON0, err := json.Marshal(map[string]any{
		"machine": map[string]any{
			"kubelet": map[string]any{
				"registerWithFQDN": true,
			},
		},
		"cluster": map[string]any{
			"network": map[string]any{
				"cni": map[string]any{
					"name": "none",
				},
			},
		},
	})
	if err != nil {
		log.Printf("error creating control plane patch: %s", err.Error())
		return z, err
	}
	controlPlanePatch := string(tmpJSON0)

	// Generate a machine patch for the worker nodes
	tmpJSON1, err := json.Marshal(map[string]any{
		"machine": map[string]any{
			"kubelet": map[string]any{
				"registerWithFQDN": true,
			},
		},
	})
	if err != nil {
		log.Printf("error creating worker node patch: %s", err.Error())
		return z, err
	}
	workerPatch := string(tmpJSON1)

	// Apply the machine configuration to the control plane nodes
	// Not using a loop here because we need to create a dependency on these resources
	// Details: https://www.pulumi.com/registry/packages/talos/api-docs/machine/configurationapply/
	cpConfigApply00, err := machine.NewConfigurationApply(ctx, "cp-cfg-apply-00", &machine.ConfigurationApplyArgs{
		ClientConfiguration: machine.ClientConfigurationArgs{
			CaCertificate:     talosSecrets.ClientConfiguration.CaCertificate(),
			ClientCertificate: talosSecrets.ClientConfiguration.ClientCertificate(),
			ClientKey:         talosSecrets.ClientConfiguration.ClientKey(),
		},
		ConfigPatches: pulumi.StringArray{
			pulumi.String(controlPlanePatch),
		},
		MachineConfigurationInput: talosCpCfg.MachineConfiguration(),
		Node:                      z.cpInstanceIps[0],
	})
	if err != nil {
		log.Printf("error applying machine configuration: %s", err.Error())
	}

	cpConfigApply01, err := machine.NewConfigurationApply(ctx, "cp-cfg-apply-01", &machine.ConfigurationApplyArgs{
		ClientConfiguration: machine.ClientConfigurationArgs{
			CaCertificate:     talosSecrets.ClientConfiguration.CaCertificate(),
			ClientCertificate: talosSecrets.ClientConfiguration.ClientCertificate(),
			ClientKey:         talosSecrets.ClientConfiguration.ClientKey(),
		},
		ConfigPatches: pulumi.StringArray{
			pulumi.String(controlPlanePatch),
		},
		MachineConfigurationInput: talosCpCfg.MachineConfiguration(),
		Node:                      z.cpInstanceIps[1],
	})
	if err != nil {
		log.Printf("error applying machine configuration: %s", err.Error())
	}

	cpConfigApply02, err := machine.NewConfigurationApply(ctx, "cp-cfg-apply-02", &machine.ConfigurationApplyArgs{
		ClientConfiguration: machine.ClientConfigurationArgs{
			CaCertificate:     talosSecrets.ClientConfiguration.CaCertificate(),
			ClientCertificate: talosSecrets.ClientConfiguration.ClientCertificate(),
			ClientKey:         talosSecrets.ClientConfiguration.ClientKey(),
		},
		ConfigPatches: pulumi.StringArray{
			pulumi.String(controlPlanePatch),
		},
		MachineConfigurationInput: talosCpCfg.MachineConfiguration(),
		Node:                      z.cpInstanceIps[2],
	})
	if err != nil {
		log.Printf("error applying machine configuration: %s", err.Error())
	}

	// Launch EC2 instances for the worker nodes
	// Details: https://www.pulumi.com/registry/packages/aws/api-docs/ec2/instance/
	wkrInstanceIds := make([]pulumi.StringInput, 3)
	wkrInstancePrivIps := make([]pulumi.StringInput, 3)
	// wkrInstancePubIps := make([]pulumi.StringInput, 3)
	for i := range 3 {
		instance, err := ec2.NewInstance(ctx, fmt.Sprintf("talos-wkr-0%d", i), &ec2.InstanceArgs{
			Ami:                      pulumi.String(talosAmi.Id),
			AssociatePublicIpAddress: pulumi.Bool(true),
			IamInstanceProfile:       pulumi.String("nodes.cloud-provider-aws.sigs.k8s.io"),
			InstanceType:             pulumi.String("m5a.xlarge"),
			SourceDestCheck:          pulumi.BoolPtr(false),
			SubnetId:                 z.publicSubnetIds[i],
			Tags: pulumi.StringMap{
				"Name":    pulumi.Sprintf("talos-wkr-0%d", i),
				"Project": pulumi.String("vpc-route-server"),
			},
			VpcSecurityGroupIds: pulumi.StringArray{talosSg.ID()},
		})
		if err != nil {
			log.Printf("error creating instance: %s", err.Error())
			return z, err
		} else {
			wkrInstanceIds[i] = instance.ID()
			wkrInstancePrivIps[i] = instance.PrivateIp
			z.wkrInstanceIps = append(z.wkrInstanceIps, instance.PublicIp)
		}
	}

	// Apply the machine configuration to the worker nodes
	for i := range z.wkrInstanceIps {
		_, err = machine.NewConfigurationApply(ctx, fmt.Sprintf("wkr-cfg-apply-0%d", i), &machine.ConfigurationApplyArgs{
			ClientConfiguration: machine.ClientConfigurationArgs{
				CaCertificate:     talosSecrets.ClientConfiguration.CaCertificate(),
				ClientCertificate: talosSecrets.ClientConfiguration.ClientCertificate(),
				ClientKey:         talosSecrets.ClientConfiguration.ClientKey(),
			},
			ConfigPatches: pulumi.StringArray{
				pulumi.String(workerPatch),
			},
			MachineConfigurationInput: talosWkrCfg.MachineConfiguration(),
			Node:                      z.wkrInstanceIps[i],
		})
		if err != nil {
			log.Printf("error applying machine configuration: %s", err.Error())
			return z, err
		}
	}

	// Bootstrap the first control plane node
	// Details: https://www.pulumi.com/registry/packages/talos/api-docs/machine/bootstrap/
	clusterBootstrap, err := machine.NewBootstrap(ctx, "bootstrap", &machine.BootstrapArgs{
		ClientConfiguration: machine.ClientConfigurationArgs{
			CaCertificate:     talosSecrets.ClientConfiguration.CaCertificate(),
			ClientCertificate: talosSecrets.ClientConfiguration.ClientCertificate(),
			ClientKey:         talosSecrets.ClientConfiguration.ClientKey(),
		},
		Node: z.cpInstanceIps[0],
	}, pulumi.DependsOn([]pulumi.Resource{cpConfigApply00, cpConfigApply01, cpConfigApply02}))
	if err != nil {
		log.Printf("error bootstrapping first node: %s", err.Error())
		return z, err
	}

	// Get client configuration for the Talos cluster
	talosClusterClientCfg := client.GetConfigurationOutput(ctx, client.GetConfigurationOutputArgs{
		ClusterName: pulumi.String("talos-cluster"),
		ClientConfiguration: client.GetConfigurationClientConfigurationArgs{
			CaCertificate:     talosSecrets.ClientConfiguration.CaCertificate(),
			ClientCertificate: talosSecrets.ClientConfiguration.ClientCertificate(),
			ClientKey:         talosSecrets.ClientConfiguration.ClientKey(),
		},
		Nodes: pulumi.StringArray{
			z.cpInstanceIps[0],
			z.cpInstanceIps[1],
			z.cpInstanceIps[2],
		},
		Endpoints: pulumi.StringArray{
			z.cpInstanceIps[0],
			z.cpInstanceIps[1],
			z.cpInstanceIps[2],
		},
	})

	// Get the Kubeconfig for the cluster
	talosKubeCfg, err := cluster.NewKubeconfig(ctx, "talosKubeCfg", &cluster.KubeconfigArgs{
		ClientConfiguration: &cluster.KubeconfigClientConfigurationArgs{
			CaCertificate:     talosSecrets.ClientConfiguration.CaCertificate(),
			ClientCertificate: talosSecrets.ClientConfiguration.ClientCertificate(),
			ClientKey:         talosSecrets.ClientConfiguration.ClientKey(),
		},
		Node:     z.cpInstanceIps[0],
		Endpoint: z.cpInstanceIps[0],
	}, pulumi.DependsOn([]pulumi.Resource{clusterBootstrap}))
	if err != nil {
		log.Printf("error retrieving Kubeconfig: %s", err.Error())
		return z, err
	} else {
		z.kubeconfig = pulumi.StringInput(talosKubeCfg.KubeconfigRaw)
	}

	// Export the Talos client configuration
	ctx.Export("talosctlCfg", talosClusterClientCfg.TalosConfig())

	// Uncomment the following lines for additional outputs that may be useful for troubleshooting/diagnostics
	// ctx.Export("talosSgId", talosSg.ID())
	// ctx.Export("talosLbSgId", talosLbSg.ID())
	// ctx.Export("talosLbDnsName", talosLb.DnsName)
	// ctx.Export("talosLbArn", talosLb.Arn)
	// ctx.Export("talosLbId", talosLb.ID())
	// ctx.Export("cpInstanceIds", pulumi.StringArray(cpInstanceIds))
	// ctx.Export("cpInstancePrivIps", pulumi.StringArray(cpInstancePrivIps))
	// ctx.Export("cpInstancePubIps", pulumi.StringArray(z.cpInstanceIps))
	// ctx.Export("wkrInstanceIds", pulumi.StringArray(wkrInstanceIds))
	// ctx.Export("wkrInstancePrivIps", pulumi.StringArray(wkrInstancePrivIps))
	// ctx.Export("wkrInstancePubIps", pulumi.StringArray(z.wkrInstanceIps))

	// Return to the calling function
	return z, err
}
