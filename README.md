# kafka-websockets-springboot-bff — TPS EMS flow + Angular frontend

Spring Boot BFF met **TPS EMS → Kafka → STOMP WebSocket** dataflow en een **Angular** frontend (host-app met micro-frontends mf-rits en mf-tps via Native Federation).

## Architectuur

### Overzicht (TPS-treinpositie)

```
TPS EMS Simulator               Kafka                      WebSocket Server
(data-producer)                 (broker)                   (websocket-server)
      │                            │                               │
      │  TreinPositieBericht       │                               │
      │  @Scheduled (default 2s)   │   tps-treinpositie topic      │
      │──────────────────────────► │ ─────────────────────────────►│
      │  key = sbNaam              │                               │ SimpMessagingTemplate
      │                            │                               │ .convertAndSend()
      │                            │                        /topic/v1/treinpositie/{sbNaam}
      │                            │                               │
      │                            │                          STOMP clients
      │                            │                          (browser / wscat)
```

**Parallel:** dezelfde producer stuurt ook **infratoestand** naar het topic `rits-infratoestand` (default elke 3s); de WebSocket-server consumeert dat topic en broadcast naar `/topic/v1/infratoestand/{gebiedNaam}`. Zie `InfraSimulatorService` / `InfraStructureKafkaConsumerService`.

### Onderdelen en rol in de flow

**Data Producer (TPS EMS simulator)** — map / Docker-service `data-producer`

- **Wat:** simuleert **treinposities** (`TpsSimulatorService`, `@Scheduled`, interval uit `tps.publish.interval-ms`, default **2000 ms**) en **infratoestand** (`InfraSimulatorService`, default **3000 ms**) en publiceert beide via **`KafkaTemplate`** naar Kafka.
- **Rol:** *producer* → genereert events en schrijft ze naar de juiste topics (partition key o.a. `sbNaam` voor TPS).

**Kafka (broker)** — map `kafka/` (`create-topics.sh`) + Docker-service `kafka`

- **Wat:** ontvangt en bewaart berichten in topics totdat consumers ze lezen. Topics worden bij opstart door **`kafka-init`** aangemaakt (`auto.create.topics.enable` staat uit). Standaard: `tps-treinpositie` en `rits-infratoestand` (ieder 3 partitions).
- **Rol:** *message broker* → ontkoppelt schrijvers en lezers; buffer en log voor events.

**WebSocket Server** — map / Docker-service `websocket-server`

- **Wat:** luistert met **`@KafkaListener`** naar de TPS- en infra-topics en stuurt payloads door naar STOMP-clients met **`SimpMessagingTemplate`**. Bij subscribe levert **`@SubscribeMapping`** een **snapshot** (laatste staat); daarna volgen live updates.
- **Rol:** *consumer + real-time forwarder* → leest Kafka en pusht hetzelfde domeinmodel naar browsers (of tools zoals `wscat`).

### End-to-end flow (TPS)

```
TPS EMS Simulator (data-producer)
        │  genereert treinposities → KafkaTemplate → topic tps-treinpositie
        ▼
Kafka (broker)
        │  bewaart events per partition (key o.a. sbNaam)
        ▼
WebSocket Server (websocket-server)
        │  @KafkaListener leest topic
        │  SimpMessagingTemplate → /topic/v1/treinpositie/{sbNaam}
        ▼
STOMP WebSocket clients (browser, test-client.html, Angular)
```

### Kortom

| Stap | Component | Taak |
|------|-----------|------|
| 1 | `data-producer` | Produceert events (TPS + optioneel infra-simulator). |
| 2 | Kafka | Buffert events in topics; levering aan consumers. |
| 3 | `websocket-server` | Consumeert events en zet ze realtime op STOMP-destinations voor clients. |

### Services (Docker Compose)

| Service           | Poort    | Beschrijving                                          |
|-------------------|----------|-------------------------------------------------------|
| `kafka`           | 9092/29092 | Kafka broker (KRaft, geen ZooKeeper)                |
| `kafka-init`      | —        | Maakt o.a. `tps-treinpositie` en `rits-infratoestand` aan |
| `data-producer`   | —        | TPS + infra-simulator: `@Scheduled` + `KafkaTemplate` |
| `websocket-server`| **8080** | `@KafkaListener` + STOMP (`SimpMessagingTemplate`), JWT (Keycloak JWKS) |
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

### Inloggen (BFF + Keycloak)

Open de app via: [http://localhost:4300](http://localhost:4300)

- Niet ingelogd? Dan word je automatisch doorgestuurd naar `/login`.
- Je kunt ook direct naar: [http://localhost:4300/login](http://localhost:4300/login)

Relevante auth URL's:

| Onderdeel | URL |
|-----------|-----|
| Host app (frontend) | `http://localhost:4300` |
| BFF gateway (nginx) | `http://localhost:8008` |
| Keycloak admin | `http://localhost:8000` |

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
| TPS topic updates | `/topic/v1/treinpositie/{sbNaam}` |
| TPS snapshot (subscribe) | `/app/v1/treinpositie/{sbNaam}` |
| Infra topic updates | `/topic/v1/infratoestand/{gebiedNaam}` |
| Infra snapshot (subscribe) | `/app/v1/infratoestand/{gebiedNaam}` |

## Configuratie (.env)

| Variabele                 | Standaard             | Beschrijving                           |
|---------------------------|-----------------------|----------------------------------------|
| `KAFKA_TOPIC`             | `tps-treinpositie`    | TPS Kafka-topic                        |
| `PUBLISH_INTERVAL_MS`     | `2000`                | TPS publish-interval (ms)              |
| `KAFKA_TOPIC_RITS`        | `rits-infratoestand`  | Infra/RITS Kafka-topic                 |
| `RITS_PUBLISH_INTERVAL_MS`| `3000`                | Infra publish-interval (ms)           |
| `WS_PORT`                 | `8080`                | WebSocket server poort                 |

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
