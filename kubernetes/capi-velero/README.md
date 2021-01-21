# Infrastructure for CAPI-Velero Testing

This repository contains the files necessary to create an environment for testing the use of [Velero](https://velero.io) with [Cluster API](https://cluster-api.sigs.k8s.io). This includes a [Terraform](https://terraform.io) configuration for creating the required AWS infrastructure, and `kubeadm` configuration files for bootstrapping the resulting instances into a [Kubernetes](https://kubernetes.io) cluster.

## Prerequisites/Assumptions

* This repository and these instructions assume the presence of IAM roles and policies that enable the AWS cloud provider. Refer to [this blog post](https://blog.scottlowe.org/2019/08/14/setting-up-aws-integrated-kubernetes-115-cluster-kubeadm/) for more information.

## Instructions

1. Create a `terraform.tfvars` file using the included `terraform.tfvars.example` file as an example.
2. Review the default values in `variables.tf` and override them, as needed, with additional values in the `terraform.tfvars` file created in step 1.
3. Review the IAM instance profile specified in `instances.tf` and make sure that the names there match the names of IAM instance profiles in your AWS account that enable/support the AWS cloud provider.
4. Run `terraform plan` and review the output.
5. If the output of step 4 is acceptable, run `terraform apply` to create the infrastructure.
6. Using SSH, log into each of the instances created in step 4 and ensure that the local hostname matches the EC2 Private DNS entry. Running `sudo hostnamectl set-hostname $(curl -s http://169.254.169.254/latest/meta-data/local-hostname)` will make sure the hostname is set correctly. (Note that SSH access is via a bastion host, so some local SSH configuration may be necessary.)
7. After step 5 and step 6 are completes, use `terraform output` to customize the `kubeadm-cp-mgmt-a.yaml` file. Specifically, change the value of the "controlPlaneEndpoint" line to reflect the correct DNS name of the load balancer created for the "mgmt-a" cluster. Also change the value of the "name" field under "nodeRegistration" from `HOSTNAME` to the full hostname set in step 6.
8. Repeat step 7, but for the `kubeadm-cp-mgmt-b.yaml` file and using the correct DNS name of the load balancer created for the "mgmt-b" cluster.
9. Use `rsync` or `scp` to copy the modified `kubeadm-cp-mgmt-a.yaml` and `kubeadm-cp-mgmt-b.yaml` to the control plane instances for the "mgmt-a" and "mgmt-b" clusters, respectively. Name the files `kubeadm.yaml` on the destination systems.
10. On the control plane instances for the "mgmt-a" and "mgmt-b" clusters (you can use `terraform output` to get the information on these instances), run `kubeadm init --config kubeadm.yaml`.
11. Using the information displayed by the `kubeadm init` command on each of the control plane nodes, customize the `kubeadm-wkr-mgmt-a.yaml` and `kubeadm-wkr-mgmt-b.yaml` files. Specifically, you will need to supply the value of the bootstrap token, the SHA256 hash of the control plane certificate, the DNS name of the control plane endpoint, and the hostname of the system (which, in step 6, you set to same value as the EC2 Private DNS entry).
12. Copy the files modified in step 11 to the worker instances for the "mgmt-a" and "mgmt-b" clusters, respectively. Name the files `kubeadm.yaml` on the destination systems.
13. On each of the worker instances, run `kubeadm join --config kubeadm.yaml`.
14. When step 13 completes, install a CNI plugin. Refer to the CNI plugin's documentation for specifics.

Upon the completion of the above steps, you will have a functional Kubernetes cluster that is ready to be made into a Cluster API management cluster using `clusterctl init`. These management clusters can then be used for testing the use of Velero for backing up and restoring Cluster API objects.
