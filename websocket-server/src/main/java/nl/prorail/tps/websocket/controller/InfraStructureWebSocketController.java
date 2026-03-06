package nl.prorail.tps.websocket.controller;

import nl.prorail.tps.websocket.model.InfraToestandBericht;
import nl.prorail.tps.websocket.service.InfraStructureKafkaConsumerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

@Controller
public class InfraStructureWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(InfraStructureWebSocketController.class);

    private final InfraStructureKafkaConsumerService consumerService;

    public InfraStructureWebSocketController(InfraStructureKafkaConsumerService consumerService) {
        this.consumerService = consumerService;
    }

    @SubscribeMapping("/v1/infratoestand/{gebiedNaam}")
    public InfraToestandBericht onSubscribe(@DestinationVariable String gebiedNaam) {
        log.info("[RITS Controller] Client geabonneerd op gebiedNaam='{}'", gebiedNaam);

        InfraToestandBericht latest = consumerService.getLatestState(gebiedNaam);
        if (latest != null) {
            return new InfraToestandBericht(
                    latest.getWsViewStatus(),
                    latest.getGebiedNaam(),
                    "SNAPSHOT",
                    latest.getInfraToestandList(),
                    latest.getTimestamp()
            );
        }
        return new InfraToestandBericht("NOT_FOUND", gebiedNaam, "SNAPSHOT", java.util.List.of(), null);
    }

    @MessageMapping("/v1/infratoestand/{gebiedNaam}")
    public void onMessage(@DestinationVariable String gebiedNaam) {
        log.debug("[RITS Controller] Snapshot-verzoek voor gebiedNaam='{}'", gebiedNaam);
    }
}

