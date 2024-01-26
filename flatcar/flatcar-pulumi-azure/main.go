package main

import (
    "github.com/pulumi/pulumi-azure-native-sdk/compute/v2"
    "github.com/pulumi/pulumi-azure-native-sdk/network/v2"
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
        rgName, err := cfg.Try("resourcegroup")
        if err != nil {
            rgName = "flatcarRg"
        }
        imageUrl := cfg.Require("imageurl")

        // Set parameters for looking up the VM image
        // osImagePublisher := "kinvolk"
        // osImageOffer := "flatcar-container-linux"
        // osImageSku := "stable"
        // osImageVersion := "3602.2.3"

        // Create an SSH key
        sshKey, err := tls.NewPrivateKey(ctx, "ssh-key", &tls.PrivateKeyArgs{
            Algorithm: pulumi.String("RSA"),
            RsaBits:   pulumi.Int(4096),
        })
        if err != nil {
            return err
        }

        // Create a virtual network
        flatcarVnet, err := network.NewVirtualNetwork(ctx, "flatcar-vnet", &network.VirtualNetworkArgs{
            ResourceGroupName: rgName,
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
            ResourceGroupName:  rgName,
            VirtualNetworkName: flatcarVnet.Name,
        })
        if err != nil {
            return err
        }

        // Create a VM image from VHD
        flatcarImg, err := compute.NewImage(ctx, "flatcar-img", &compute.ImageArgs{
            ImageName:         pulumi.String("flatcar-container-linux-stable-3602.2.3"),
            Location:          pulumi.String("westus2"),
            ResourceGroupName: rgName,
            StorageProfile: compute.ImageStorageProfileArgs{
                OsDisk: &compute.ImageOSDiskArgs{
                    BlobUri: imageUrl,
                    OsState: compute.OperatingSystemStateTypesGeneralized,
                    OsType:  compute.OperatingSystemTypesLinux,
                },
                ZoneResilient: pulumi.Bool(true),
            },
        })

        // Create a public IP address for the VM
        flatcarPubIp, err := network.NewPublicIPAddress(ctx, "flatcar-pub-ip", &network.PublicIPAddressArgs{
            ResourceGroupName:        rgName,
            PublicIPAllocationMethod: pulumi.StringPtr("Dynamic"),
        })
        if err != nil {
            return err
        }

        // Create a security group allowing inbound access over ports 80 (for HTTP) and 22 (for SSH)
        flatcarSg, err := network.NewNetworkSecurityGroup(ctx, "flatcar-sg", &network.NetworkSecurityGroupArgs{
            ResourceGroupName: rgName,
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
            ResourceGroupName: rgName,
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
            ResourceGroupName: rgName,
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
                    Id: flatcarImg.ID(),
                },
            },
        })
        if err != nil {
            return err
        }

        // Once the machine is created, fetch its IP address and DNS hostname
        address := flatcarVm.ID().ApplyT(func(_ pulumi.ID) network.LookupPublicIPAddressResultOutput {
            return network.LookupPublicIPAddressOutput(ctx, network.LookupPublicIPAddressOutputArgs{
                ResourceGroupName:   rgName,
                PublicIpAddressName: flatcarPubIp.Name,
            })
        })

        // Export the VM's hostname, public IP address, HTTP URL, and SSH private key
        ctx.Export("ip", address.ApplyT(func(addr network.LookupPublicIPAddressResult) (string, error) {
            return *addr.IpAddress, nil
        }).(pulumi.StringOutput))

        // ctx.Export("hostname", address.ApplyT(func(addr network.LookupPublicIPAddressResult) (string, error) {
        //  return *addr.DnsSettings.Fqdn, nil
        // }).(pulumi.StringOutput))

        ctx.Export("privatekey", sshKey.PrivateKeyOpenssh)

        return nil
    })
}
