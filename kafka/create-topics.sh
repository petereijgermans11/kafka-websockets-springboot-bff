#!/bin/bash
set -e

BOOTSTRAP=kafka:29092
TPS_TOPIC=${KAFKA_TOPIC:-tps-treinpositie}
RITS_TOPIC=${KAFKA_TOPIC_RITS:-rits-infratoestand}

echo "Creating Kafka topic: $TPS_TOPIC"

kafka-topics \
  --bootstrap-server "$BOOTSTRAP" \
  --create \
  --if-not-exists \
  --topic "$TPS_TOPIC" \
  --partitions 3 \
  --replication-factor 1

echo "Creating Kafka topic: $RITS_TOPIC"

kafka-topics \
  --bootstrap-server "$BOOTSTRAP" \
  --create \
  --if-not-exists \
  --topic "$RITS_TOPIC" \
  --partitions 3 \
  --replication-factor 1

echo "Topics aangemaakt:"
kafka-topics --bootstrap-server "$BOOTSTRAP" --list
