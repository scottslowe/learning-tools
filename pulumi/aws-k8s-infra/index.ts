// Sample TypeScript code to instantiate AWS infrastructure
// for use with Kubernetes. Intended to be used with Pulumi.

// Import necessary modules
import * as aws from "@pulumi/aws";
import * as pulumi from "@pulumi/pulumi";

// Set some default values for later
let bastionType = aws.ec2.InstanceTypes.T2_Small;
let nodeType = aws.ec2.InstanceTypes.T2_Large;
let keypair: string = "my_keypair_name"; // CHANGE THIS
let numCpNodes: number = 3;
let numWrkNodes: number = 3;
let k8sTagName: string = "kubernetes.io/cluster/blogtest";

// Get information on AZs
const rawAzInfo = aws.getAvailabilityZones({
    state: "available",
});
let azNames: Array<string> = rawAzInfo.names;
let numberOfAZs: number = azNames.length;

// Get AMI ID for Ubuntu
const amiId = pulumi.output(aws.getAmi({
    owners: [ "123456789012" ], // CHANGE THIS
    mostRecent: true,
    filters: [
        { name: "name", values: [ "ami-ubuntu-18.04-1.15.2*" ], }, // CHANGE THIS
        { name: "root-device-type", values: [ "ebs" ], },
        { name: "virtualization-type", values: [ "hvm" ], },
        { name: "architecture", values: [ "x86_64" ] },
    ],
}));

// Create new VPC
const vpc = new aws.ec2.Vpc("ubuntu-vpc", {
    cidrBlock: "10.1.0.0/16",
    enableDnsHostnames: true,
    enableDnsSupport: true,
    tags: {
        Name: "ubuntu-vpc",
        [k8sTagName]: "shared",
    },
});

// Create subnets in the new VPC
let subnets = [];
for (let i = 0; i < numberOfAZs; i++) {
    let subnetAddr: number = i*16;
    let netAddr: string = "10.1.";
    let cidrSubnet: string = netAddr.concat(String(subnetAddr), ".0/20");
    subnets.push(new aws.ec2.Subnet(`subnet-${i+1}`, {
        availabilityZone: azNames[i],
        cidrBlock: cidrSubnet,
        mapPublicIpOnLaunch: true,
        vpcId: vpc.id,
        tags: {
            Name: `subnet-${i+1}`,
            [k8sTagName]: "shared",
        },
    }));
};

// Capture a list of subnet IDs
let subnetIds = subnets.map(s => s.id);

// Create an Internet gateway
const gw = new aws.ec2.InternetGateway("gw", {
    vpcId: vpc.id,
    tags: {
        Name: "gw",
        [k8sTagName]: "shared",
    },
});

// Create a route table for Internet access
const rt = new aws.ec2.RouteTable("rt-inet", {
    vpcId: vpc.id,
    routes: [
        { cidrBlock: "0.0.0.0/0", gatewayId: gw.id },
    ],
    tags: {
        Name: "rt",
        [k8sTagName]: "shared",
    },
});

// Associate the subnets with the route table
let rtAssociations = [];
for (let i = 0; i < numberOfAZs; i++) {
    rtAssociations.push(new aws.ec2.RouteTableAssociation(`rta-${i+1}`, {
        routeTableId: rt.id,
        subnetId: subnets[i].id,
    }));
};

// Create a security group for traffic to bastion host
const bastionSecGrp = new aws.ec2.SecurityGroup("bastion-sg", {
    name: "bastion-sg",
    vpcId: vpc.id,
    description: "Security group for SSH bastion hosts",
    ingress: [
        { protocol: "tcp", fromPort: 22, toPort: 22, cidrBlocks: ["0.0.0.0/0"] },
    ],
    egress: [
        { protocol: "-1", fromPort: 0, toPort: 0, cidrBlocks: ["0.0.0.0/0"] },
    ],
    tags: {
        Name: "bastion-sg",
        [k8sTagName]: "shared",
    },
});

// Create a security group for control plane ELB
const elbSecGrp = new aws.ec2.SecurityGroup("elb-sg", {
    name: "elb-sg",
    vpcId: vpc.id,
    description: "Allow traffic to/from control plane ELB",
    ingress: [
        { protocol: "tcp", fromPort: 6443, toPort: 6443, cidrBlocks: ["0.0.0.0/0"] },
    ],
    egress: [
        { protocol: "tcp", fromPort: 6443, toPort: 6443, cidrBlocks: ["0.0.0.0/0"] },
    ],
    tags: {
        Name: "elb-sg",
        [k8sTagName]: "shared",
    },
});

// Create a security group to be managed by the K8s cloud provider
const k8sSecGrp = new aws.ec2.SecurityGroup("k8s-sg", {
    name: "k8s-sg",
    vpcId: vpc.id,
    description: "K8s-managed security group",
    tags: {
        Name: "k8s-sg",
        [k8sTagName]: "owned",
    },
});

// Create a security group for non-bastion hosts
const nodeSecGrp = new aws.ec2.SecurityGroup("nodes-sg", {
    name: "nodes-sg",
    vpcId: vpc.id,
    description: "Allow traffic to non-bastion hosts",
    ingress: [
        { protocol: "-1", fromPort: 0, toPort: 0, self: true },
        { protocol: "-1", fromPort: 0, toPort: 0, securityGroups: [ k8sSecGrp.id ] },
        { protocol: "tcp", fromPort: 22, toPort: 22, securityGroups: [ bastionSecGrp.id ] },
        { protocol: "tcp", fromPort: 6443, toPort: 6443, securityGroups: [ bastionSecGrp.id ] },
        { protocol: "tcp", fromPort: 6443, toPort: 6443, securityGroups: [ elbSecGrp.id ] },
    ],
    egress: [
        { protocol: "-1", fromPort: 0, toPort: 0, cidrBlocks: ["0.0.0.0/0"] },
    ],
    tags: {
        Name: "nodes-sg",
        [k8sTagName]: "shared",
    },
});

// Create the bastion host
let bastionInstance = new aws.ec2.Instance("bastion", {
    instanceType: bastionType,
    securityGroups: [ bastionSecGrp.id ],
    ami: amiId.apply(amiId => amiId.imageId),
    keyName: keypair,
    subnetId: subnets[0].id,
    tags: {
        Name: "bastion",
        [k8sTagName]: "shared",
    },
});

// Create the control plane nodes
let cpNodes = [];
for (let i = 0; i < numCpNodes; i++) {
    cpNodes.push(new aws.ec2.Instance(`cpnode-${i+1}`, {
        instanceType: nodeType,
        ami: amiId.apply(amiId => amiId.imageId),
        securityGroups: [ nodeSecGrp.id ],
        keyName: keypair,
        subnetId: subnets[i].id,
        iamInstanceProfile: "k8s-control-plane-role", // CHANGE THIS
        tags: {
            Name: `cpnode-${i+1}`,
            [k8sTagName]: "owned",
        },
    }));
};

// Capture list of IDs for the control plane instances
let cpInstanceIds = cpNodes.map(n => n.id);

// Create the worker nodes
let wrkNodes = [];
for (let i = 0; i < numWrkNodes; i++) {
    wrkNodes.push(new aws.ec2.Instance(`wrknode-${i+1}`, {
        instanceType: nodeType,
        ami: amiId.apply(amiId => amiId.imageId),
        securityGroups: [ nodeSecGrp.id ],
        keyName: keypair,
        subnetId: subnets[i].id,
        iamInstanceProfile: "k8s-worker-role", // CHANGE THIS
        tags: {
            Name: `wrknode-${i+1}`,
            [k8sTagName]: "owned",
        },
    }));
};

// Create load balancer for the control plane
const elb = new aws.elb.LoadBalancer("cpelb", {
    crossZoneLoadBalancing: true,
    instances: cpInstanceIds,
    securityGroups: [ elbSecGrp.id ],
    subnets: subnetIds,
    listeners: [{
        lbPort: 6443,
        lbProtocol: "tcp",
        instancePort: 6443,
        instanceProtocol: "tcp",
    }],
    healthCheck: {
        healthyThreshold: 3,
        interval: 30,
        target: "SSL:6443",
        timeout: 5,
        unhealthyThreshold: 3,
    },
    tags: {
        Name: "cpelb",
        [k8sTagName]: "shared",
    },
});

// Display useful information
export let bastionPubIpAddress = bastionInstance.publicIp;
export let cpNodeIpAddresses = cpNodes.map(c => c.privateIp);
export let wrkNodeIpAddresses = wrkNodes.map(w => w.privateIp);
