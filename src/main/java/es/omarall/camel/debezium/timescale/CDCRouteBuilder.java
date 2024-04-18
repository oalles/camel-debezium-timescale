package es.omarall.camel.debezium.timescale;


import io.debezium.data.Envelope;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Predicate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.debezium.DebeziumConstants;
import org.apache.kafka.connect.data.Struct;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CDCRouteBuilder extends RouteBuilder {

    private final Predicate isCreate =
            header(DebeziumConstants.HEADER_OPERATION)
                    .isEqualTo(constant(Envelope.Operation.CREATE.code()));

    @Override
    public void configure() throws Exception {

        from("debezium-postgres:localPG")
                .routeId(this.getClass().getName() + ".cdc")
//              .log(LoggingLevel.DEBUG, "Incoming message with headers ${headers}")
                .choice()
                .when(isCreate)
                .process(exchange -> {
                    // Use a converter here and convertBodyTo()
                    Struct struct = exchange.getIn().getBody(Struct.class);
                    String id = struct.getString("id");
                    String ts = struct.getString("ts");
                    String details = struct.getString("details");
                    log.info("\n[Data insertion captured] Id: {} - Details: {} - Instant: {}, \n\n", id, details, ts);
                })
                .endChoice()
//                .otherwise()
//              .log(LoggingLevel.DEBUG, "Operation: ${headers[" + DebeziumConstants.HEADER_OPERATION + "]}")
                .endParent();
    }
}
