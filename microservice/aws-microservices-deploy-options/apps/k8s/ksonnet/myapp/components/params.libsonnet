{
    global: {
        // User-defined global parameters; accessible to all component and environments, Ex:
        // replicas: 4,
    },
    components: {
        // Component-level parameters, defined initially from 'ks prototype use ...'
        // Each object below should correspond to a component in the components/ directory
        webapp: {
            containerPort: 8080,
            image: "huangzhaorongit/webapp",
            name: "webapp",
            replicas: 1,
            servicePort: 80,
            type: "LoadBalancer",
        },
        name: {
            containerPort: 8082,
            image: "huangzhaorongit/name",
            name: "name",
            replicas: 1,
            servicePort: 8082,
            type: "ClusterIP",
        },
        greeting: {
            containerPort: 8081,
            image: "huangzhaorongit/greeting",
            name: "greeting",
            replicas: 1,
            servicePort: 8081,
            type: "ClusterIP",
        },
    },
}
