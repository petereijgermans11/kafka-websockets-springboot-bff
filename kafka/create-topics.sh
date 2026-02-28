#!/bin/bash
set -e

BOOTSTRAP=kafka:29092
TOPIC=${KAFKA_TOPIC:-tps-treinpositie}

echo "Creating Kafka topic: $TOPIC"

kafka-topics \
  --bootstrap-server "$BOOTSTRAP" \
  --create \
  --if-not-exists \
  --topic "$TOPIC" \
  --partitions 3 \
  --replication-factor 1

echo "Topics aangemaakt:"
kafka-topics --bootstrap-server "$BOOTSTRAP" --list
