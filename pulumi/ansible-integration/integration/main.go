package main

import (
	"context"
	"fmt"
	"io"
	"os"
	"path/filepath"

	"github.com/pulumi/pulumi/sdk/v3/go/auto"
	"github.com/pulumi/pulumi/sdk/v3/go/auto/optdestroy"
	"github.com/pulumi/pulumi/sdk/v3/go/auto/optup"
)

func main() {
	// Set some values to be used later
	org := "scottslowe"
	awsRegion := "us-west-2"
	stack := awsRegion
	programLocation := filepath.Join("..", "instances")

	// Determine mode of operation; default is to refresh/update
	destroy := false
	argsWithoutProg := os.Args[1:]
	if len(argsWithoutProg) > 0 {
		if argsWithoutProg[0] == "destroy" {
			destroy = true
		}
	}

	// Set up context
	ctx := context.Background()

	// Set up base stack
	autoStackFQSN := auto.FullyQualifiedStackName(org, "instances", stack)
	autoStack, err := auto.UpsertStackLocalSource(ctx, autoStackFQSN, programLocation)
	if err != nil {
		fmt.Printf("error creating/selecting stack: %v\n", err)
		os.Exit(1)
	}

	// Set some necessary configuration
	autoStack.SetConfig(ctx, "aws:region", auto.ConfigValue{Value: awsRegion})

	// Refresh the stack
	_, err = autoStack.Refresh(ctx)
	if err != nil {
		fmt.Printf("error refreshing the stack: %v\n", err)
		os.Exit(1)
	}

	// Destroy the stack if destroy == true (destroy specified as argument)
	if destroy {
		tmp, err := os.CreateTemp(os.TempDir(), "")
		if err != nil {
			fmt.Printf("error creating temporary file: %v\n", err)
			os.Exit(1)
		}
		progressStreams := []io.Writer{os.Stdout, tmp}
		_, err = autoStack.Destroy(ctx, optdestroy.ProgressStreams(progressStreams...))
		if err != nil {
			fmt.Printf("error destroying stack: %v\n", err)
			os.Exit(1)
		}
		os.Exit(0)
	}

	// Run an update against the stack
	tmp, err := os.CreateTemp(os.TempDir(), "")
	if err != nil {
		fmt.Printf("error creating temporary file: %v\n", err)
		os.Exit(1)
	}
	progressStreams := []io.Writer{os.Stdout, tmp}
	res, err := autoStack.Up(ctx, optup.ProgressStreams(progressStreams...))
	if err != nil {
		fmt.Printf("error updating stack: %v\n", err)
		os.Exit(1)
	}

	// Get outputs from the stack
	// pKey, ok := res.Outputs["privateKey"].Value.(string)
	ipAddrOutputs, ok := res.Outputs["instancePublicIpAddresses"].Value.([]interface{})
	if !ok {
		fmt.Printf("error getting infrastructure information: %v\n", err)
		os.Exit(1)
	}
	var ipAddrs []string
	if ipAddrOutputs != nil {
		for _, v := range ipAddrOutputs {
			ipAddrs = append(ipAddrs, v.(string))
		}
	}

	// Create the Ansible inventory file
	fName := filepath.Join("..", "ansible", "hosts")
	f, err := os.Create(fName)
	if err != nil {
		fmt.Printf("error creating file: %v\n", err)
		os.Exit(1)
	}

	// Write the IP addresses to the file
	for _, addr := range ipAddrs {
		_, err := f.WriteString(addr + "\n")
		if err != nil {
			fmt.Printf("error writing to file: %v\n", err)
			os.Exit(1)
		}
	}

	// Close the file
	err = f.Close()
	if err != nil {
		fmt.Printf("error closing file: %v\n", err)
	}
}
