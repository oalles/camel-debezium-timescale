package es.omarall.camel.debezium.timescale;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SampleEntityId implements Serializable {
    private String id;
    private Instant ts;
}
