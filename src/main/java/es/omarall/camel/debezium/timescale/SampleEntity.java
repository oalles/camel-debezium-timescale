package es.omarall.camel.debezium.timescale;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SampleEntity {
    public static final String TYPE_PREFIX = "se-";


    @EmbeddedId
    private SampleEntityId id;
    private String details;

    public SampleEntity(String details) {
        this.id = new SampleEntityId(TYPE_PREFIX + UUID.randomUUID(), Instant.now());
        this.details = details;
    }
}

