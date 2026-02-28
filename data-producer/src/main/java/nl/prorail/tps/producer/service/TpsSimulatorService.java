package nl.prorail.tps.producer.service;

import nl.prorail.tps.producer.data.TpsDataset;
import nl.prorail.tps.producer.model.TreinPositieBericht;
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
public class TpsSimulatorService {

    private static final Logger log = LoggerFactory.getLogger(TpsSimulatorService.class);

    private final KafkaTemplate<String, TreinPositieBericht> kafkaTemplate;
    private final String topic;
    private final List<TreinPositieBericht> dataset = TpsDataset.RECORDS;

    private final AtomicInteger index = new AtomicInteger(0);

    public TpsSimulatorService(KafkaTemplate<String, TreinPositieBericht> kafkaTemplate,
                                @Value("${kafka.topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    /** Initiële snapshot van alle sbNamen direct na opstart (SSS-equivalent) */
    @EventListener(ApplicationReadyEvent.class)
    public void publishInitialSnapshots() {
        log.info("[TPS] Publiceren initiële snapshots naar topic '{}'", topic);
        for (TreinPositieBericht record : dataset) {
            publish(withState(record, "SNAPSHOT"));
            log.info("[TPS] SNAPSHOT → sbNaam='{}'", record.getSbNaam());
        }
    }

    /** Periodieke UPDATE-berichten, cycling door de dataset */
    @Scheduled(fixedDelayString = "${tps.publish.interval-ms:2000}")
    public void publishUpdate() {
        int i = index.getAndUpdate(current -> (current + 1) % dataset.size());
        TreinPositieBericht record = dataset.get(i);
        publish(withState(record, "UPDATE"));
        log.info("[TPS] → topic='{}' key='{}' state=UPDATE treinen={}",
                topic, record.getSbNaam(),
                record.getTreinNummerVensterBezettingList()
                        .stream().map(e -> e.getTreinNummer()).toList());
    }

    private void publish(TreinPositieBericht bericht) {
        kafkaTemplate.send(topic, bericht.getSbNaam(), bericht);
    }

    private TreinPositieBericht withState(TreinPositieBericht source, String state) {
        return new TreinPositieBericht(
                source.getWsViewStatus(),
                source.getSbNaam(),
                state,
                source.getTreinNummerVensterBezettingList(),
                Instant.now().toString()
        );
    }
}
