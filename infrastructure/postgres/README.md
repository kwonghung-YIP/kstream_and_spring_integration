### Attach and run the postgres CLI within postgres pod 
```bash
kubectl get pods -lapp=postgres-db -n=postgres -o jsonpath='{.items[0].metadata.name}'

kubectl exec --stdin --tty \
    $(kubectl get pods -lapp=postgres-db -n=postgres -o jsonpath='{.items[0].metadata.name}') \
    --namespace=postgres -- \
    psql --host=localhost --username=admin --dbname=db1
```

```sql
update stock.price_feed
set price = random_normal(price,price*0.05),
    ver = ver + 1,
    lastupd = current_timestamp
where ticker = 'AAPL';

update stock.volume_feed
set volume = volume * (1 + random()/10000),
    ver = ver + 1,
    lastupd = current_timestamp
where ticker = 'AAPL';

call genRandomQuote('AAPL',10);
```