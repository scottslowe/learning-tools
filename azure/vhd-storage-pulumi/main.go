package main

import (
	"github.com/pulumi/pulumi-azure-native-sdk/resources/v2"
	"github.com/pulumi/pulumi-azure-native-sdk/storage/v2"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
)

func main() {
	pulumi.Run(func(ctx *pulumi.Context) error {
		// Create an Azure Resource Group
		vhdRg, err := resources.NewResourceGroup(ctx, "vhd-rg", nil)
		if err != nil {
			return err
		}

		// Create a storage account
		vhdSa, err := storage.NewStorageAccount(ctx, "vhdsa", &storage.StorageAccountArgs{
			AccessTier:        storage.AccessTierHot,
			Kind:              pulumi.String("BlobStorage"),
			Location:          pulumi.String("westus2"),
			ResourceGroupName: vhdRg.Name,
			Sku: &storage.SkuArgs{
				Name: pulumi.String("Standard_LRS"),
			},
		})
		if err != nil {
			return err
		}

		// Create a storage container to house the VHD
		vhdSc, err := storage.NewBlobContainer(ctx, "vhd-sc", &storage.BlobContainerArgs{
			AccountName:       vhdSa.Name,
			ResourceGroupName: vhdRg.Name,
		})
		if err != nil {
			return err
		}

		// Define a file asset for the downloaded VHD
		// vhdFile := pulumi.NewFileAsset("./flatcar_production_azure_image.vhd.bz2")

		// Create a storage blob from the VHD
		// flatcarSb, err := storage.NewBlob(ctx, "flatcar-sb", &storage.BlobArgs{
		//     AccountName:        vhdSa.Name,
		//     BlobName:           pulumi.String("flatcar-vhd"),
		//     ContainerName:      vhdSc.Name,
		//     ResourceGroupName:  flatcarRg.Name,
		//     Source:             vhdFile,
		//     Type:               storage.BlobTypeBlock,
		// })

		// Export some values as stack outputs
		ctx.Export("storageAcctId", vhdSa.ID())
		ctx.Export("storageAcctName", vhdSa.Name)
		ctx.Export("storageContainerId", vhdSc.ID())
		ctx.Export("storageContainerId", vhdSc.Name)
		ctx.Export("resourceGroupName", vhdRg.Name)

		return nil
	})
}
