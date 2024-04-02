## Call Kafka Connect REST API

### Create the Debezium Postgres source connector 
```bash
kubectl exec --stdin --tty \
    kafka-connect-1 --namespace=kafka -- \
    curl -i -X POST http://localhost:8083/connectors \
    -H "Content-Type: application/json" \
    -d '{
            "name": "debezium-src-connector",
            "config": {
                "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
                "tasks.max": "1",
                "database.hostname": "postgres-db.postgres.svc.cluster.local",
                "database.port": "5432",
                "database.user": "admin",
                "database.password": "password",
                "database.dbname" : "db1",
                "topic.prefix": "postgres_",
                "schema.include.list": "stock"
            }
        }'
```

### Check connector status 
```bash
kubectl exec --stdin --tty \
    kafka-connect-2 --namespace=kafka -- \
    curl -X GET \
    -H "Accept:application/json" \
    localhost:8083/connectors\?expand=status\&expand=info | jq .

kubectl exec --stdin --tty \
    kafka-connect-1 --namespace=kafka -- \
    curl -X GET \
    -H "Accept:application/json" \
    localhost:8083/connectors/debezium-src-connector/status | jq .

kubectl exec --stdin --tty \
    kafka-connect-1 --namespace=kafka -- \
    curl -X GET \
    -H "Accept:application/json" \
    localhost:8083/connectors/debezium-src-connector/config | jq .
```

### Restart Kafka connector
```bash
kubectl exec --stdin --tty \
    kafka-connect-2 --namespace=kafka -- \
    curl -X POST \
    localhost:8083/connectors/debezium-src-connector/restart
```

## Apache Kafka utilities 
```bash
kubectl run --stdin --tty \
    apache-kafka --image=apache/kafka:3.7.0 \
    --restart=Never --rm --namespace=kafka --command -- \
    /bin/sh
```

Create Kafka topic
```bash
kubectl run --stdin --tty \
    apache-kafka --image=apache/kafka:3.7.0 \
    --restart=Never --rm --namespace=kafka --command -- \
    /opt/kafka/bin/kafka-topics.sh --bootstrap-server broker-10.broker:9092 \
    --create --topic testing \
    --partitions 10 --replication-factor 2
```

Describe Kafka topic
```bash
kubectl run --stdin --tty \
    apache-kafka --image=apache/kafka:3.7.0 \
    --restart=Never --rm --namespace=kafka --command -- \
    /opt/kafka/bin/kafka-topics.sh --bootstrap-server broker-10.broker:9092 \
    --describe --topic testing
```

Publish message to topic
```bash
kubectl run --stdin --tty \
    apache-kafka-producer --image=apache/kafka:3.7.0 \
    --restart=Never --rm --namespace=kafka --command -- \
    /opt/kafka/bin/kafka-console-producer.sh --bootstrap-server broker-10.broker:9092 \
    --topic testing
```

Consume message from topic
```bash
kubectl run --stdin --tty \
    apache-kafka-consumer --image=apache/kafka:3.7.0 \
    --restart=Never --rm --namespace=kafka --command -- \
    /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server broker-10.broker:9092 \
    --topic postgres_.stock.quote --from-beginning
```


Run kcat in kubernetes
[kcat GitHub](https://github.com/edenhill/kcat)

```bash
kubectl run --stdin --tty \
    busybox --image=busybox \
    --restart=Never --rm --namespace=kafka -- \
    sh

kubectl run --stdin --tty \
    kcat --image=edenhill/kcat:1.7.1 \
    --restart=Never --rm -- \
    -b kafka-broker-1.kafka.default.svc.cluster.local:29092 -L

kubectl run --stdin --tty \
    kcat --image=edenhill/kcat:1.7.1 \
    --restart=Never --rm --namespace=kafka -- \
    -b broker-10.broker.kafka.svc.cluster.local:9092 -L
```

[Kafka UI configuration reference](https://docs.kafka-ui.provectus.io/configuration/misc-configuration-properties)
