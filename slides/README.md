# Slides — One Socket, Many Frontends

Slidev-presentatie over de real-time microfrontend-architectuur (shared WebSocket, Native Federation) in dit project.

## Starten

```bash
cd slides
npx slidev slides.md --open
```

## Build (static)

```bash
npx slidev export slides.md
```

Output: `slides.pdf` (of formaat via `--format`).

## Inhoud

- **Stakes** — ProRail/NS, Spoorviewer, waarom real-time
- **Architectuur** — TPS EMS → Kafka → STOMP → Angular MFEs
- **Probleem** — N MFEs × 1 socket = socket storm
- **Oplossing** — `shared-websocket` + Native Federation singleton
- **Code** — `SharedStompConnectionService`, TpsDataService, federation.config.js
- **Docker** — volume-mount voor `shared-websocket` in containers
- **Takeaways** — één socket per user, onafhankelijke deploys
