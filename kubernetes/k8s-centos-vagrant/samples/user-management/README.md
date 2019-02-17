## k8s user mgmt

1. Create serviceaccount for user `kubectl apply -f jenkins-sa.yml`
2. Create user role `kubectl apply -f jenkins-role.yml`
3. Create role binding `kubectl apply -f jenkins-role-binding.yml`

** Note: ** Everything should be created in one namespace

4. Get token name `kubectl describe sa -n jenkins-namespace jenkins-user`
5. Get token `kubectl describe secret -n jenkins-namespace <secret_name>`
6. Generate kubeconfig:
```
apiVersion: v1
clusters:
- cluster:
    insecure-skip-tls-verify: true # TODO: fix tls verify
    server: <cluster_url> 
  name: kubernetes
contexts:
- context:
    cluster: kubernetes
    namespace: jenkins-namespace
    user: jenkins-user
  name: jenkins-context
current-context: jenkins-context
kind: Config
preferences: {}
users:
- name: jenkins-user
  user:
    token: < from step 5 >
```
7. Enjoy new user :) 

### Notes:
* https://kubernetes.io/docs/admin/authorization/rbac/ -- details about kubernetes roles

