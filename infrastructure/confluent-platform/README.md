## Call Kafka Connect REST API

### Create Debezium Postgres source connector 
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
                "topic.prefix": "postgres",
                "schema.include.list": "stock",
                "message.key.columns": "stock.price_feed:market,ticker,trade_date;stock.volume_feed:market,ticker,trade_date",
                "topic.delimiter": "_",
                "topic.creation.enable": "true",
                "topic.creation.groups": "postgres",
                "topic.creation.default.replication.factor": "2",
                "topic.creation.default.partitions": "10",
                "topic.creation.postgres.replication.factor": "2",
                "topic.creation.postgres.partitions": "10",
                "topic.creation.postgres.include": "postgres.*",
                "transforms": "insertField,headerFrom,extractNewRecord",
                "transforms.insertField.type": "org.apache.kafka.connect.transforms.InsertField$Value",
                "transforms.insertField.topic.field": "src_topic",
                "transforms.headerFrom.type": "org.apache.kafka.connect.transforms.HeaderFrom$Value",
                "transforms.headerFrom.fields": "src_topic",
                "transforms.headerFrom.headers": "src_topic",
                "transforms.headerFrom.operation": "move",
                "transforms.extractNewRecord.type":"io.debezium.transforms.ExtractNewRecordState",
                "transforms.extractNewRecord.drop.tombstones":"false"
            }
        }'
```


### Delete Debezium Postgres source connector 
```bash
kubectl exec --stdin --tty \
    kafka-connect-1 --namespace=kafka -- \
    curl -X DELETE \
    localhost:8083/connectors/debezium-src-connector
```


### Check connector status 
```bash
kubectl exec --stdin --tty \
    kafka-connect-1 --namespace=kafka -- \
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
    localhost:8083/connectors/debezium-src-connector/restart #pause,resume,stop
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
    --describe --topic postgres_stock_quote
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
    --topic quote --from-beginning

kubectl run --stdin --tty \
    apache-kafka-consumer --image=apache/kafka:3.7.0 \
    --restart=Never --rm --namespace=kafka --command -- \
    /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server broker-10.broker:9092 \
    --topic postgres_stock_price_feed --from-beginning
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

## References
[Kafa Connect REST API References](https://docs.confluent.io/platform/current/connect/references/restapi.html)
[Debezium Postgres source connector: Configuration Reference](https://docs.confluent.io/kafka-connectors/debezium-postgres-source/current/postgres_source_connector_config.html#postgres-source-connector-config)
[Debezium Transformations: New Record State Extraction](https://debezium.io/documentation/reference/stable/transformations/event-flattening.html)
[Kafka UI configuration reference](https://docs.kafka-ui.provectus.io/configuration/misc-configuration-properties)
[Apache Kafka Stream Developer Guide - Stream DSL](https://kafka.apache.org/37/documentation/streams/developer-guide/dsl-api.html#aggregating)
[avro-maven-plugin](https://avro.apache.org/docs/1.10.2/gettingstartedjava.html)
[Schema Registry API Reference](https://docs.confluent.io/legacy/platform/4.1.3/schema-registry/docs/using.html)

[Apache Kafka - Kafka Connect Transformations](https://kafka.apache.org/documentation/#connect_transforms)
[Single Message Transforms (SMT) for Confluent Platform](https://docs.confluent.io/platform/current/connect/transforms/overview.html)
[Debezium - Transformations](https://debezium.io/documentation/reference/stable/transformations/index.html)

