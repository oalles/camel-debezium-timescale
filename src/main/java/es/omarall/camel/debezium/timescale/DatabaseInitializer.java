package es.omarall.camel.debezium.timescale;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PSQLException;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLWarning;
import java.sql.Statement;

@Component
@Slf4j
@RequiredArgsConstructor
public class DatabaseInitializer {

    private final DataSource dataSource;

    private final String CREATE_REPLICATION_SLOT =
            "SELECT * FROM pg_create_logical_replication_slot('debezium','pgoutput');";

    @PostConstruct
    public void initialize() throws Exception {
        createReplicationSlotIfNeeded();
    }

    private void createReplicationSlotIfNeeded() throws Exception {
        try {
            executeQuery(dataSource.getConnection(), CREATE_REPLICATION_SLOT);
            log.debug("Replication slot created successfully");
        } catch (PSQLException e) {
            if (e.getSQLState().equals("42710")) {
                log.debug("Replication slot already exists");
            } else {
                throw e;
            }
        }
    }

    private void executeQuery(Connection conn, String query) throws Exception {
        Statement statement = conn.createStatement();
        statement.execute(query);
        SQLWarning warnings = statement.getWarnings();
        if (warnings != null) {
            log.debug("{}", warnings.getMessage());
            SQLWarning nextWarning = warnings.getNextWarning();
            while (nextWarning != null) {
                log.debug("{}", nextWarning.getMessage());
                nextWarning = nextWarning.getNextWarning();
            }
        }
    }
}
