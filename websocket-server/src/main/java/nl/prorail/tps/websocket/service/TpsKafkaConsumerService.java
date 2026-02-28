package nl.prorail.tps.websocket.service;

import nl.prorail.tps.websocket.model.TreinPositieBericht;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TpsKafkaConsumerService {

    private static final Logger log = LoggerFactory.getLogger(TpsKafkaConsumerService.class);
    private static final String TOPIC_PREFIX = "/topic/v1/treinpositie/";

    private final SimpMessagingTemplate messagingTemplate;

    /** Laatste bekende state per sbNaam — voor snapshot-on-subscribe */
    private final Map<String, TreinPositieBericht> latestState = new ConcurrentHashMap<>();

    public TpsKafkaConsumerService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(topics = "${kafka.topic}", groupId = "websocket-server-group")
    public void onMessage(TreinPositieBericht bericht) {
        latestState.put(bericht.getSbNaam(), bericht);

        String destination = TOPIC_PREFIX + bericht.getSbNaam();
        messagingTemplate.convertAndSend(destination, bericht);

        log.info("[Consumer] Broadcast → destination='{}' state={} treinen={}",
                destination, bericht.getState(),
                bericht.getTreinNummerVensterBezettingList()
                        .stream().map(e -> e.getTreinNummer()).toList());
    }

    /** Geeft de laatste bekende staat terug voor snapshot-on-subscribe */
    public TreinPositieBericht getLatestState(String sbNaam) {
        return latestState.get(sbNaam);
    }
}
