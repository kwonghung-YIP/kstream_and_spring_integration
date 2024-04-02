### Attach and run the postgres CLI within postgres pod 
```bash
kubectl get pods -lapp=postgres-db -n=postgres -o jsonpath='{.items[0].metadata.name}'

kubectl exec --stdin --tty \
    $(kubectl get pods -lapp=postgres-db -n=postgres -o jsonpath='{.items[0].metadata.name}') \
    --namespace=postgres -- \
    psql --host=localhost --username=admin --dbname=db1
```
