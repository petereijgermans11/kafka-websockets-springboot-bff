# kafka-websockets-springboot-bff — TPS EMS flow + Angular frontend

Spring Boot BFF met **TPS EMS → Kafka → STOMP WebSocket** dataflow en een **Angular** frontend (host-app met micro-frontends mf-rits en mf-tps via Native Federation).

## Architectuur

```
TPS EMS Simulator               Kafka                      WebSocket Server
(data-producer)                 (broker)                   (websocket-server)
      │                            │                               │
      │  TreinPositieBericht       │                               │
      │  @Scheduled elke 2s        │   tps-treinpositie topic      │
      │──────────────────────────► │ ─────────────────────────────►│
      │  key = sbNaam              │                               │ SimpMessagingTemplate
      │                            │                               │ .convertAndSend()
      │                            │                        /topic/v1/treinpositie/{sbNaam}
      │                            │                               │
      │                            │                          STOMP clients
      │                            │                          (browser / wscat)
```

### Services

| Service           | Poort    | Beschrijving                                          |
|-------------------|----------|-------------------------------------------------------|
| `kafka`           | 9092/29092 | Kafka broker (KRaft, geen ZooKeeper)                |
| `kafka-init`      | —        | Maakt `tps-treinpositie` topic aan bij opstart        |
| `data-producer`   | —        | TPS simulator, `@Scheduled` + `KafkaTemplate`         |
| `websocket-server`| **8080** | `@KafkaListener` + STOMP WebSocket (`SimpMessagingTemplate`) |
| `kafka-ui`        | **9090** | Kafka UI voor debuggen                                |

## Snel starten

### Backend (Kafka + services)

```bash
cd kafka-websockets-springboot-bff
docker compose up --build
```

> De eerste build duurt langer omdat Maven dependencies gedownload worden (~2-3 min).

Wacht tot je ziet:
```
tps-data-producer    | Started TpsDataProducerApplication
tps-websocket-server | Started TpsWebSocketServerApplication
```

## Testen

### Browser testpagina

Ga naar: [http://localhost:8080/test-client.html](http://localhost:8080/test-client.html)

1. Klik **Verbinden**
2. Klik op een sbNaam-knop (bijv. **ASD**)
3. Je ontvangt direct een `SNAPSHOT` en daarna live `UPDATE`-berichten

### STOMP topics

Het protocol is STOMP over WebSocket (via SockJS):

| Onderdeel | Waarde |
|-----------|--------|
| WebSocket endpoint | `ws://localhost:8080/ws` |
| SockJS endpoint | `http://localhost:8080/ws` |
| Topic updates | `/topic/v1/treinpositie/{sbNaam}` |
| Snapshot (subscribe) | `/app/v1/treinpositie/{sbNaam}` |

## Vergelijking met Node.js versie

| Onderdeel | Node.js (`kafka-websockets`) | Spring Boot (deze repo) |
|-----------|-------------------------------|---------------------------------------------|
| Kafka producer | `kafkajs` KafkaProducer | `KafkaTemplate<String, TreinPositieBericht>` |
| Scheduler | `setInterval` | `@Scheduled` |
| Kafka consumer | `kafkajs` Consumer | `@KafkaListener` |
| WebSocket protocol | Eigen JSON protocol | STOMP over WebSocket (SockJS) |
| Broadcast | `ws.send(json)` | `SimpMessagingTemplate.convertAndSend()` |
| Snapshot-on-subscribe | Custom `TopicRegistry` | `@SubscribeMapping` + `latestState` Map |
| Serialisatie | `JSON.stringify/parse` | Jackson `JsonSerializer/JsonDeserializer` |

## Configuratie (.env)

| Variabele            | Standaard          | Beschrijving                        |
|----------------------|--------------------|-------------------------------------|
| `KAFKA_TOPIC`        | `tps-treinpositie` | Kafka topic naam                    |
| `PUBLISH_INTERVAL_MS`| `2000`             | Interval tussen berichten (ms)      |
| `WS_PORT`            | `8080`             | WebSocket server poort              |

## Lokaal ontwikkelen (zonder Docker)

```bash
# Terminal 1 — alleen Kafka starten
docker compose up kafka kafka-init

# Terminal 2 — data producer
cd data-producer && mvn spring-boot:run

# Terminal 3 — websocket server
cd websocket-server && mvn spring-boot:run
```

## Frontend (Angular)

| App       | Rol                    |
|-----------|------------------------|
| `host-app`| Shell; laadt micro-frontends |
| `mf-rits` | Micro-frontend (RITS)  |
| `mf-tps`  | Micro-frontend (TPS)   |

Ontwikkel lokaal (na `docker compose up` voor de backend):

```bash
# Host + remotes (dev-server met federation)
cd host-app && npm install && npm start
```

Build voor productie:

```bash
cd host-app && npm run build
cd mf-rits  && npm run build
cd mf-tps   && npm run build
```

## Projectstructuur

```
kafka-websockets-springboot-bff/
├── .gitignore
├── .gitattributes
├── docker-compose.yml
├── .env
├── kafka/
│   └── create-topics.sh
├── data-producer/                          ← Spring Boot: TPS EMS simulator
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/nl/prorail/tps/producer/
│       ├── TpsDataProducerApplication.java ← @EnableScheduling
│       ├── model/
│       ├── data/
│       └── service/
├── websocket-server/                       ← Spring Boot: Kafka consumer + STOMP
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/.../websocket/
│   └── src/main/resources/static/
│       └── test-client.html
├── host-app/                               ← Angular shell (Native Federation)
├── mf-rits/                                ← Micro-frontend RITS
├── mf-tps/                                 ← Micro-frontend TPS
├── keycloak/                               ← Auth (optioneel)
└── nginx/                                  ← Reverse proxy (optioneel)
```
