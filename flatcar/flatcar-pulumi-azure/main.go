package main

import (
	"github.com/pulumi/pulumi-azure-native-sdk/compute/v2"
	"github.com/pulumi/pulumi-azure-native-sdk/network/v2"
	"github.com/pulumi/pulumi-azure-native-sdk/resources/v2"
	"github.com/pulumi/pulumi-azure-native-sdk/storage/v2"
	tls "github.com/pulumi/pulumi-tls/sdk/v4/go/tls"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi/config"
)

func main() {
	pulumi.Run(func(ctx *pulumi.Context) error {

		// Import the program's configuration settings
		cfg := config.New(ctx, "")
		vmSize, err := cfg.Try("vmSize")
		if err != nil {
			vmSize = "Standard_A1_v2"
		}

		// Set parameters for looking up the VM image
		osImagePublisher := "kinvolk"
		osImageOffer := "flatcar-container-linux"
		osImageSku := "stable"
		osImageVersion := "3602.2.3"

		// Create an SSH key
		sshKey, err := tls.NewPrivateKey(ctx, "ssh-key", &tls.PrivateKeyArgs{
			Algorithm: pulumi.String("RSA"),
			RsaBits:   pulumi.Int(4096),
		})
		if err != nil {
			return err
		}

		// Create a resource group
		flatcarRg, err := resources.NewResourceGroup(ctx, "flatcar-rg", nil)
		if err != nil {
			return err
		}

		// Create a virtual network
		flatcarVnet, err := network.NewVirtualNetwork(ctx, "flatcar-vnet", &network.VirtualNetworkArgs{
			ResourceGroupName: flatcarRg.Name,
			AddressSpace: network.AddressSpaceArgs{
				AddressPrefixes: pulumi.ToStringArray([]string{
					"10.0.0.0/16",
				}),
			},
		})
		if err != nil {
			return err
		}

		// Create a subnet within the virtual network
		flatcarSubnet, err := network.NewSubnet(ctx, "flatcar-subnet", &network.SubnetArgs{
			AddressPrefix:      pulumi.String("10.0.1.0/24"),
			ResourceGroupName:  flatcarRg.Name,
			VirtualNetworkName: flatcarVnet.Name,
		})
		if err != nil {
			return err
		}

		// Create a storage account
		flatcarSa, err := storage.NewStorageAccount(ctx, "flatcar-storage-acct", &storage.StorageAccountArgs{
			Kind:              pulumi.String("BlobStorage"),
			Location:          pulumi.String("westus2"),
			ResourceGroupName: flatcarRg.Name,
		})
		if err != nil {
			return err
		}

		// Create a storage container to house the VHD
		flatcarSc, err := storage.NewBlobContainer(ctx, "flatcar-storage-container", &storage.BlobContainerArgs{
			AccountName:       flatcarSa.Name,
			ResourceGroupName: flatcarRg.Name,
		})
		if err != nil {
			return err
		}

		// Here I'll need to add code to upload the VHD to the
		// storage container; this needs to be done before
		// registering the VM image (below)

		// Create a VM image from VHD
		flatcarImg, err := compute.NewImage(ctx, "flatcar-image", &compute.ImageArgs{
			ImageName:         pulumi.String("flatcar-container-linux-stable-3602.2.3"),
			Location:          pulumi.String("westus2"),
			ResourceGroupName: flatcarRg.Name,
			StorageProfile: compute.ImageStorageProfileArgs{
				OsDisk: &compute.ImageOSDiskArgs{
					BlobUri: pulumi.String("value"),
					OsState: compute.OperatingSystemStateTypesGeneralized,
					OsType:  compute.OperatingSystemTypesLinux,
				},
				ZoneResilient: pulumi.Bool(true),
			},
		})

		// Create a public IP address for the VM
		flatcarPubIp, err := network.NewPublicIPAddress(ctx, "flatcar-pub-ip", &network.PublicIPAddressArgs{
			ResourceGroupName:        flatcarRg.Name,
			PublicIPAllocationMethod: pulumi.StringPtr("Dynamic"),
		})
		if err != nil {
			return err
		}

		// Create a security group allowing inbound access over ports 80 (for HTTP) and 22 (for SSH)
		flatcarSg, err := network.NewNetworkSecurityGroup(ctx, "flatcar-sg", &network.NetworkSecurityGroupArgs{
			ResourceGroupName: flatcarRg.Name,
			SecurityRules: network.SecurityRuleTypeArray{
				network.SecurityRuleTypeArgs{
					Name:                     pulumi.StringPtr("flatcar-allow-ssh"),
					Priority:                 pulumi.Int(1000),
					Direction:                pulumi.String("Inbound"),
					Access:                   pulumi.String("Allow"),
					Protocol:                 pulumi.String("Tcp"),
					SourcePortRange:          pulumi.StringPtr("*"),
					SourceAddressPrefix:      pulumi.StringPtr("*"),
					DestinationAddressPrefix: pulumi.StringPtr("*"),
					DestinationPortRanges: pulumi.ToStringArray([]string{
						"22",
					}),
				},
			},
		})
		if err != nil {
			return err
		}

		// Create a network interface with the virtual network, IP address, and security group
		flatcarNetIface, err := network.NewNetworkInterface(ctx, "flatcar-net-iface", &network.NetworkInterfaceArgs{
			ResourceGroupName: flatcarRg.Name,
			NetworkSecurityGroup: &network.NetworkSecurityGroupTypeArgs{
				Id: flatcarSg.ID(),
			},
			IpConfigurations: network.NetworkInterfaceIPConfigurationArray{
				&network.NetworkInterfaceIPConfigurationArgs{
					Name:                      pulumi.String("flatcar-ipconfiguration"),
					PrivateIPAllocationMethod: pulumi.String("Dynamic"),
					Subnet: &network.SubnetTypeArgs{
						Id: flatcarSubnet.ID(),
					},
					PublicIPAddress: &network.PublicIPAddressTypeArgs{
						Id: flatcarPubIp.ID(),
					},
				},
			},
		})
		if err != nil {
			return err
		}

		// Create the virtual machine
		flatcarVm, err := compute.NewVirtualMachine(ctx, "flatcar-vm", &compute.VirtualMachineArgs{
			ResourceGroupName: flatcarRg.Name,
			NetworkProfile: &compute.NetworkProfileArgs{
				NetworkInterfaces: compute.NetworkInterfaceReferenceArray{
					&compute.NetworkInterfaceReferenceArgs{
						Id:      flatcarNetIface.ID(),
						Primary: pulumi.Bool(true),
					},
				},
			},
			HardwareProfile: &compute.HardwareProfileArgs{
				VmSize: pulumi.String(vmSize),
			},
			OsProfile: &compute.OSProfileArgs{
				ComputerName:  pulumi.String("flatcar"),
				AdminUsername: pulumi.String("core"),
				LinuxConfiguration: &compute.LinuxConfigurationArgs{
					DisablePasswordAuthentication: pulumi.Bool(true),
					Ssh: &compute.SshConfigurationArgs{
						PublicKeys: compute.SshPublicKeyTypeArray{
							&compute.SshPublicKeyTypeArgs{
								KeyData: sshKey.PublicKeyOpenssh,
								Path:    pulumi.String("/home/core/.ssh/authorized_keys"),
							},
						},
					},
				},
			},
			StorageProfile: &compute.StorageProfileArgs{
				OsDisk: &compute.OSDiskArgs{
					Name:         pulumi.String("flatcar-osdisk"),
					CreateOption: pulumi.String("FromImage"),
				},
				ImageReference: &compute.ImageReferenceArgs{
					Publisher: pulumi.String(osImagePublisher),
					Offer:     pulumi.String(osImageOffer),
					Sku:       pulumi.String(osImageSku),
					Version:   pulumi.String(osImageVersion),
				},
			},
		})
		if err != nil {
			return err
		}

		// Once the machine is created, fetch its IP address and DNS hostname
		address := flatcarVm.ID().ApplyT(func(_ pulumi.ID) network.LookupPublicIPAddressResultOutput {
			return network.LookupPublicIPAddressOutput(ctx, network.LookupPublicIPAddressOutputArgs{
				ResourceGroupName:   flatcarRg.Name,
				PublicIpAddressName: flatcarPubIp.Name,
			})
		})

		// Export the VM's hostname, public IP address, HTTP URL, and SSH private key
		ctx.Export("ip", address.ApplyT(func(addr network.LookupPublicIPAddressResult) (string, error) {
			return *addr.IpAddress, nil
		}).(pulumi.StringOutput))

		// ctx.Export("hostname", address.ApplyT(func(addr network.LookupPublicIPAddressResult) (string, error) {
		// 	return *addr.DnsSettings.Fqdn, nil
		// }).(pulumi.StringOutput))

		ctx.Export("privatekey", sshKey.PrivateKeyOpenssh)

		return nil
	})
}
