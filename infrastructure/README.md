## microk8s helm3 commands

### test, install, list, and uninstall postgres helm chart 

```bash
microk8s helm3 install postgres ./postgres \
    --dry-run --debug --namespace postgres

microk8s helm3 install postgres ./postgres \
    --namespace postgres --create-namespace

microk8s helm3 list -n postgres

microk8s helm3 uninstall postgres -n postgres 
```

### test, install, list, and uninstall confluent platform helm chart 

```bash
microk8s helm3 install confluent ./confluent-platform \
    --namespace kafka --create-namespace

microk8s helm3 install confluent ./confluent-platform \
    --dry-run --debug --namespace kafka

microk8s helm3 list -n kafka

microk8s helm3 uninstall confluent -n kafka 
```

