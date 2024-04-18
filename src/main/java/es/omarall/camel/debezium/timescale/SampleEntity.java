package es.omarall.camel.debezium.timescale;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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

    @Id
    private String id;

    @Column(nullable = false)
    private Instant ts;

    private String details;

    public SampleEntity(String details) {
        this.id = TYPE_PREFIX + UUID.randomUUID();
        this.ts = Instant.now();
        this.details = details;
    }
}

