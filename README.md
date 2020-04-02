### Start Kafka

```
  cd scripts
  docker-compose up -d
```

### Watch the Kafka topic

download kafka tar and run:

```
   bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic transaction.errors --from-beginning
```

### Run the stream example

```
   sbt pr
```
