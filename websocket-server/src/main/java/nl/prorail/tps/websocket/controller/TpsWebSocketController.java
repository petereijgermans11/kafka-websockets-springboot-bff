package nl.prorail.tps.websocket.controller;

import nl.prorail.tps.websocket.model.TreinPositieBericht;
import nl.prorail.tps.websocket.service.TpsKafkaConsumerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

/**
 * Behandelt STOMP-abonnementen van clients.
 *
 * Client stuurt: SUBSCRIBE /topic/v1/treinpositie/ASD
 * → Spring roept @SubscribeMapping aan via /app/v1/treinpositie/ASD
 * → stuurt onmiddellijk de laatste bekende staat terug als snapshot
 */
@Controller
public class TpsWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(TpsWebSocketController.class);

    private final TpsKafkaConsumerService consumerService;

    public TpsWebSocketController(TpsKafkaConsumerService consumerService) {
        this.consumerService = consumerService;
    }

    /**
     * Stuurt een snapshot terug zodra een client zich abonneert op een sbNaam.
     * Het antwoord gaat rechtstreeks naar de subscriber (niet broadcast).
     * Destination: /app/v1/treinpositie/{sbNaam}
     */
    @SubscribeMapping("/v1/treinpositie/{sbNaam}")
    public TreinPositieBericht onSubscribe(@DestinationVariable String sbNaam) {
        log.info("[Controller] Client geabonneerd op sbNaam='{}'", sbNaam);

        TreinPositieBericht latest = consumerService.getLatestState(sbNaam);
        if (latest != null) {
            return new TreinPositieBericht(
                    latest.getWsViewStatus(),
                    latest.getSbNaam(),
                    "SNAPSHOT",
                    latest.getTreinNummerVensterBezettingList(),
                    latest.getTimestamp()
            );
        }
        return new TreinPositieBericht("NOT_FOUND", sbNaam, "SNAPSHOT", java.util.List.of(), null);
    }

    /**
     * Optioneel: client kan expliciet een snapshot opvragen via /app/v1/treinpositie/{sbNaam}
     */
    @MessageMapping("/v1/treinpositie/{sbNaam}")
    public void onMessage(@DestinationVariable String sbNaam) {
        log.debug("[Controller] Snapshot-verzoek voor sbNaam='{}'", sbNaam);
    }
}
