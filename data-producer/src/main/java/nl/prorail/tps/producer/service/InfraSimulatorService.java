package nl.prorail.tps.producer.service;

import nl.prorail.tps.producer.data.InfraDataset;
import nl.prorail.tps.producer.model.InfraToestandBericht;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class InfraSimulatorService {

    private static final Logger log = LoggerFactory.getLogger(InfraSimulatorService.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;
    private final List<InfraToestandBericht> dataset = InfraDataset.RECORDS;

    private final AtomicInteger index = new AtomicInteger(0);

    public InfraSimulatorService(KafkaTemplate<String, Object> kafkaTemplate,
                                 @Value("${kafka.topic.rits}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void publishInitialSnapshots() {
        log.info("[RITS] Publiceren initiële snapshots naar topic '{}'", topic);
        for (InfraToestandBericht record : dataset) {
            publish(withState(record, "SNAPSHOT"));
            log.info("[RITS] SNAPSHOT → gebiedNaam='{}'", record.getGebiedNaam());
        }
    }

    @Scheduled(fixedDelayString = "${rits.publish.interval-ms:3000}")
    public void publishUpdate() {
        int i = index.getAndUpdate(current -> (current + 1) % dataset.size());
        InfraToestandBericht record = dataset.get(i);
        publish(withState(record, "UPDATE"));
        log.info("[RITS] → topic='{}' key='{}' state=UPDATE elementen={}",
                topic, record.getGebiedNaam(),
                record.getInfraToestandList()
                        .stream().map(e -> e.getInfraObjectNaam() + "(" + e.getInfraObjectType() + ")").toList());
    }

    private void publish(InfraToestandBericht bericht) {
        kafkaTemplate.send(topic, bericht.getGebiedNaam(), bericht);
    }

    private InfraToestandBericht withState(InfraToestandBericht source, String state) {
        return new InfraToestandBericht(
                source.getWsViewStatus(),
                source.getGebiedNaam(),
                state,
                source.getInfraToestandList(),
                Instant.now().toString()
        );
    }
}

