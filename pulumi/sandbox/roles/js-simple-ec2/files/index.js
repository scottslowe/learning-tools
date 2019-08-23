"use strict";

const aws = require("@pulumi/aws");

// Specify instance size/type
let size = "t2.micro";    // t2.micro is available in the AWS free tier

// Specify AMI
let ami  = "ami-09b42c38b449cfa59"; // AMI for Ubuntu 16.04 in us-west-2 (Oregon)

// Specify key pair to use
// YOU MUST REPLACE THIS VALUE WITH THE CORRECT NAME FOR YOUR ACCOUNT!
let keypair = "aws_rsa";

// Create a new security group for port 80
let group = new aws.ec2.SecurityGroup("pulumi-secgrp", {
    ingress: [
        { protocol: "tcp", fromPort: 22, toPort: 22, cidrBlocks: ["0.0.0.0/0"] },
        { protocol: "tcp", fromPort: 80, toPort: 80, cidrBlocks: ["0.0.0.0/0"] },
    ],
});

// (optional) create a simple web server using the startup script for the instance
let userData =
`#!/bin/bash
echo "Hello, World!" > index.html
nohup python -m SimpleHTTPServer 80 &`;

let server = new aws.ec2.Instance("pulumi-ubuntu", {
    tags: { "Name": "pulumi-ubuntu" },
    instanceType: size,
    securityGroups: [ group.name ], // reference the group object above
    ami: ami,
    userData: userData,             // start a simple web server
    keyName: keypair
});

exports.publicIp = server.publicIp;
exports.publicHostName = server.publicDns;
