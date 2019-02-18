## Common Tasks

### Update the VM to a new version of Mesos

Re-build the VM. It will pull the latest package down from the repo.

```bash
( vagrant provision )
```

### Connect to the VM directly with SSH
```bash
ssh -i ~/.vagrant.d/insecure_private_key -p 2222 vagrant@10.141.141.10
```

This will result in an SSH session similar to:
```bash
( vagrant ssh )
```

[15]: config.md "Configuration"
