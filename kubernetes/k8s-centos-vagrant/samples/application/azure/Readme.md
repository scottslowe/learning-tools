# HOWTO

## Caveats

Make sure `cifs-utils` package installed on All Nodes
```
$ yum install cifs-utils
```

## Encrypt Credential:

```
# echo -n "${AZURE_STORAGE_ACCOUNT_NAME}" | base64
# echo -n "${AZURE_STORAGE_ACCOUNT_KEY}" | base64
```

## Update azure-file-demo.yaml

```
  azurestorageaccountname: << PUT HERE >>
  azurestorageaccountkey: << PUT HERE >>
```

## Create Share in Storage Account

[example](azure-file-demo.yaml#L21)

```
19:  azureFile: 
20:    secretName: azure-secret 
21:    shareName: example      # Share Name
22:    readOnly: false 

```

## Deploy Configuration

```
# kubectl apply -f azure-file-demo.yaml
secret "azure-secret" created
persistentvolume "azure-file-0001" created
persistentvolumeclaim "azure-file-demo-pvc" created
deployment "azure-file-demo" created
service "azure-file-demo-svc" created
ingress "azure-file-demo-ingress" created
```

## Troubleshoot

```
# curl b3.tools-cluster.platform.mnscorp.net
# kubectl describe pod $(kubectl get pods | grep azure-file-demo | awk '{print $1}')
# kubectl logs $(kubectl get pods | grep azure-file-demo | awk '{print $1}') azure-file-demo-container-init
```