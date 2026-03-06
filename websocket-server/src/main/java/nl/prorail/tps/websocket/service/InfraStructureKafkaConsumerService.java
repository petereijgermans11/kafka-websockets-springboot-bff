package nl.prorail.tps.websocket.service;

import nl.prorail.tps.websocket.model.InfraToestandBericht;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InfraStructureKafkaConsumerService {

    private static final Logger log = LoggerFactory.getLogger(InfraStructureKafkaConsumerService.class);
    private static final String TOPIC_PREFIX = "/topic/v1/infratoestand/";

    private final SimpMessagingTemplate messagingTemplate;

    private final Map<String, InfraToestandBericht> latestState = new ConcurrentHashMap<>();

    public InfraStructureKafkaConsumerService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(
        topics = "${kafka.topic.rits}",
        groupId = "websocket-server-rits-group",
        properties = {
            "spring.json.value.default.type=nl.prorail.tps.websocket.model.InfraToestandBericht"
        }
    )
    public void onMessage(InfraToestandBericht bericht) {
        latestState.put(bericht.getGebiedNaam(), bericht);

        String destination = TOPIC_PREFIX + bericht.getGebiedNaam();
        messagingTemplate.convertAndSend(destination, bericht);

        log.info("[RITS Consumer] Broadcast → destination='{}' state={} elementen={}",
                destination, bericht.getState(),
                bericht.getInfraToestandList()
                        .stream().map(e -> e.getInfraObjectNaam() + "(" + e.getInfraObjectType() + ")").toList());
    }

    public InfraToestandBericht getLatestState(String gebiedNaam) {
        return latestState.get(gebiedNaam);
    }
}

