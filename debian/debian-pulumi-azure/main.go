package main

import (
    "fmt"
    "strconv"

    "github.com/pulumi/pulumi-azure-native-sdk/compute/v2"
    "github.com/pulumi/pulumi-azure-native-sdk/network/v2"
    "github.com/pulumi/pulumi-azure-native-sdk/resources/v2"
    tls "github.com/pulumi/pulumi-tls/sdk/v4/go/tls"
    "github.com/pulumi/pulumi/sdk/v3/go/pulumi"
    "github.com/pulumi/pulumi/sdk/v3/go/pulumi/config"
)

func main() {
    pulumi.Run(func(ctx *pulumi.Context) error {
        // Set up maps that are used later
        versionMap := map[string]int{"buster": 10, "bullseye": 11, "bookworm": 12}

        // Import the program's configuration settings
        cfg := config.New(ctx, "")
        vmSize, err := cfg.Try("vmSize")
        if err != nil {
            vmSize = "Standard_A1_v2"
        }
        versionName, err := config.Try(ctx, "version")
        if err != nil {
            versionName = "bookworm"
        }
        versionNum, ok := versionMap[versionName]
        if !ok {
            versionNum = 12
        }

        // Set parameters for looking up the VM image
        osImagePublisher := "Debian"
        osImageOffer := fmt.Sprintf("debian-%d", versionNum)
        osImageSku := strconv.Itoa(versionNum)
        osImageVersion := "latest"

        // Create an SSH key
        sshKey, err := tls.NewPrivateKey(ctx, "ssh-key", &tls.PrivateKeyArgs{
            Algorithm: pulumi.String("RSA"),
            RsaBits:   pulumi.Int(4096),
        })
        if err != nil {
            return err
        }

        // Create a resource group
        debianRg, err := resources.NewResourceGroup(ctx, "debian-rg", nil)
        if err != nil {
            return err
        }

        // Create a virtual network
        debianVnet, err := network.NewVirtualNetwork(ctx, "debian-vnet", &network.VirtualNetworkArgs{
            ResourceGroupName: debianRg.Name,
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
        debianSubnet, err := network.NewSubnet(ctx, "debian-subnet", &network.SubnetArgs{
            AddressPrefix:      pulumi.String("10.0.1.0/24"),
            ResourceGroupName:  debianRg.Name,
            VirtualNetworkName: debianVnet.Name,
        })
        if err != nil {
            return err
        }

        // Create a public IP address for the VM
        debianPubIp, err := network.NewPublicIPAddress(ctx, "debian-pub-ip", &network.PublicIPAddressArgs{
            ResourceGroupName:        debianRg.Name,
            PublicIPAllocationMethod: pulumi.StringPtr("Dynamic"),
        })
        if err != nil {
            return err
        }

        // Create a security group allowing inbound access over ports 80 (for HTTP) and 22 (for SSH)
        debianSg, err := network.NewNetworkSecurityGroup(ctx, "debian-sg", &network.NetworkSecurityGroupArgs{
            ResourceGroupName: debianRg.Name,
            SecurityRules: network.SecurityRuleTypeArray{
                network.SecurityRuleTypeArgs{
                    Name:                     pulumi.StringPtr("debian-allow-ssh"),
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
        debianNetIface, err := network.NewNetworkInterface(ctx, "debian-net-iface", &network.NetworkInterfaceArgs{
            ResourceGroupName: debianRg.Name,
            NetworkSecurityGroup: &network.NetworkSecurityGroupTypeArgs{
                Id: debianSg.ID(),
            },
            IpConfigurations: network.NetworkInterfaceIPConfigurationArray{
                &network.NetworkInterfaceIPConfigurationArgs{
                    Name:                      pulumi.String("debian-ipconfiguration"),
                    PrivateIPAllocationMethod: pulumi.String("Dynamic"),
                    Subnet: &network.SubnetTypeArgs{
                        Id: debianSubnet.ID(),
                    },
                    PublicIPAddress: &network.PublicIPAddressTypeArgs{
                        Id: debianPubIp.ID(),
                    },
                },
            },
        })
        if err != nil {
            return err
        }

        // Create the virtual machine
        debianVm, err := compute.NewVirtualMachine(ctx, "debian-vm", &compute.VirtualMachineArgs{
            ResourceGroupName: debianRg.Name,
            NetworkProfile: &compute.NetworkProfileArgs{
                NetworkInterfaces: compute.NetworkInterfaceReferenceArray{
                    &compute.NetworkInterfaceReferenceArgs{
                        Id:      debianNetIface.ID(),
                        Primary: pulumi.Bool(true),
                    },
                },
            },
            HardwareProfile: &compute.HardwareProfileArgs{
                VmSize: pulumi.String(vmSize),
            },
            OsProfile: &compute.OSProfileArgs{
                ComputerName:  pulumi.String("debian"),
                AdminUsername: pulumi.String("azureuser"),
                LinuxConfiguration: &compute.LinuxConfigurationArgs{
                    DisablePasswordAuthentication: pulumi.Bool(true),
                    Ssh: &compute.SshConfigurationArgs{
                        PublicKeys: compute.SshPublicKeyTypeArray{
                            &compute.SshPublicKeyTypeArgs{
                                KeyData: sshKey.PublicKeyOpenssh,
                                Path:    pulumi.String("/home/azureuser/.ssh/authorized_keys"),
                            },
                        },
                    },
                },
            },
            StorageProfile: &compute.StorageProfileArgs{
                OsDisk: &compute.OSDiskArgs{
                    Name:         pulumi.String("debian-osdisk"),
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
        address := debianVm.ID().ApplyT(func(_ pulumi.ID) network.LookupPublicIPAddressResultOutput {
            return network.LookupPublicIPAddressOutput(ctx, network.LookupPublicIPAddressOutputArgs{
                ResourceGroupName:   debianRg.Name,
                PublicIpAddressName: debianPubIp.Name,
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
