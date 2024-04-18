package es.omarall.camel.debezium.timescale;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@EnableJpaRepositories
@SpringBootApplication
@Slf4j
@EntityScan("es.omarall")
public class Application {

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    ApplicationRunner runner(SampleEntityRepository repository) {
        return args -> {
            // Insert a new entity every second
            executor.scheduleAtFixedRate(() -> {
                SampleEntity sampleEntity = new SampleEntity("Some details");
                repository.save(sampleEntity);
                log.debug("\n\n[Entity Inserted] Id {}", sampleEntity.getId());
            }, 5, 1, TimeUnit.SECONDS);
        };

    }

}
