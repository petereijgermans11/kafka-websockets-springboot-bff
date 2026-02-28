package nl.prorail.tps.producer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TpsDataProducerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TpsDataProducerApplication.class, args);
    }
}
