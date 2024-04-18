# POC: Using Debezium PostgreSQL Connector with Apache Camel

This project is a proof of concept demonstrating the usage of Apache Camel and Debezium to capture changes in a PostgreSQL / TimescaleDb database.

## Objective
The original idea of this project is to showcase the integration of Debezium with a TimescaleDB database using Apache Camel, to capture changes in a hypertable.

After noticing that in all tutorials exploring the integration of Camel with Debezium, in relation to `OffsetBackingStore` configuration, 
either `FileOffsetBackingStore` or `MemoryOffsetBackingStore` is always chosen, we also aim to be able to configure a `JdbcOffsetBackingStore`.

## Project Description
This project focuses on the periodic persistence of a test entity called `SampleEntity` using a corresponding repository. 
The main goal is to configure the following Apache Camel route, to capture insertion events in its corresponding table to display them:

```java
from("debezium-postgres:localPG")
    .routeId(this.getClass().getName() + ".cdc")
    //  .log(LoggingLevel.DEBUG, "Incoming message with headers ${headers}")
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
```

## Stages
The project is divided into four stages, each represented by a branch in the repository.

### 1. Camel Debezium Postgresql Connector with MemoryOffsetBackingStore
This part demonstrates how to configure a Debezium connector in Apache Camel using MemoryOffsetBackingStore to store the offsets.

```yaml
camel:
  component:
    debezium-postgres:
      enabled: true
      plugin-name: pgoutput
      database-dbname: ${application.db.database}
      database-hostname: ${application.db.hostname}
      database-port: ${application.db.port}
      database-user: ${application.db.user}
      database-password: ${application.db.password}
      offset-storage: org.apache.kafka.connect.storage.MemoryOffsetBackingStore
      schema-include-list: public
      table-include-list: public.sample_entity
      include-unknown-datatypes: true
      bridge-error-handler: true
      topic-prefix: cdc
```

### 2. Configuring a JdbcOffsetBackingStore (Failure)
Here, an attempt is made to configure the connector to use `JdbcOffsetBackingStore`.

We add the following dependency to the project in order to use the `JdbcOffsetBackingStore`:
```xml
<dependency>
    <groupId>io.debezium</groupId>
    <artifactId>debezium-storage-jdbc</artifactId>
    <version>${debezium-storage-jdbc.version}</version>
</dependency>
```

And provide the expected configuration as additional properties:
```yaml
camel:
   component:
      debezium-postgres:
         enabled: true
         plugin-name: pgoutput
         database-dbname: ${application.db.database}
         database-hostname: ${application.db.hostname}
         database-port: ${application.db.port}
         database-user: ${application.db.user}
         database-password: ${application.db.password}
         offset-storage: io.debezium.storage.jdbc.offset.JdbcOffsetBackingStore
         schema-include-list: public
         table-include-list: public.sample_entity
         include-unknown-datatypes: true
         bridge-error-handler: true
         topic-prefix: cdc
         additional-properties:
            "[offset.storage.jdbc.url]": ${application.db.url}
            "[offset.storage.jdbc.user]": ${application.db.user}
            "[offset.storage.jdbc.password]": ${application.db.password}
            "[offset.storage.jdbc.offset.table-name]": debezium_offsets
```

When the application attempts to create a replication slot using the SQL command:
```sql
CREATE_REPLICATION_SLOT "debezium"  LOGICAL pgoutput
```
An issue arises during execution, although it may seem like the command is executed successfully(. We can verify its execution by querying the pg_replication_slots table:
```sql
SELECT * FROM pg_replication_slots WHERE slot_name = 'debezium' AND plugin = 'pgoutput';
```

This query is intended to check if the [replication slot](https://www.postgresql.org/docs/current/logicaldecoding-explanation.html#LOGICALDECODING-EXPLANATION) with the plugin "pgoutput" was created. 
However, despite the appearance of success, the application encounters a problem during execution.

The error occurs within the Java class `PostgresReplicationConnection`, specifically at line 530, where the replication slot is requested to be created
```java
String createCommand = String.format(
        "CREATE_REPLICATION_SLOT \"%s\" %s LOGICAL %s",
        slotName,
        tempPart,
        plugin.getPostgresPluginName());
            
stmt.execute(createCommand);
```
The execution will get blocked in `QueryExecutorImpl.java` at line 2155, where the code `c = pgStream.receiveChar();` blocks.


### 3. Fixed JdbcOffsetBackingStore Configuration
The previous issue is resolved, and it showcases how to properly configure `JdbcOffsetBackingStore` to store offsets in a JDBC database.

Two actions required:
1. Explicitly require the creation of the logical replication slot in the PostgreSQL database.
```sql
SELECT * FROM pg_create_logical_replication_slot('debezium','pgoutput')
```
2. Add a primary key to the offset storage table.
```sql
ALTER TABLE <off_st_table> ADD PRIMARY KEY (id);
```
which can be included providing the following property:
```yaml
additional-properties:
  "[offset.storage.jdbc.offset.table.ddl]": "CREATE TABLE %1$s (id VARCHAR(36) NOT NULL,offset_key VARCHAR(1255), offset_val VARCHAR(1255),record_insert_ts TIMESTAMP NOT NULL,record_insert_seq INTEGER NOT NULL);ALTER TABLE %1$s ADD PRIMARY KEY (id);"
```

### 4. Connector Configuration to Monitor Changes in a Timescaledb Hypertable
This part demonstrates how to configure the connector to monitor changes in a Timescaledb hypertable, providing a practical example of usage in a real-world scenario.

The Debezium PostgreSQL connector can capture data changes from TimescaleDB. 

We just convert SampleEntity to an humongous table in TimescaleDB, and configure the connector to monitor changes in the table.

We use the `io.debezium.connector.postgresql.transforms.timescaledb.TimescaleDb` [transformation](https://debezium.io/documentation/reference/stable/transformations/timescaledb.html)

```yaml
camel:
  component:
    debezium-postgres:
 
      schema-include-list: _timescaledb_internal
 
      additional-properties:
        
        "[transforms]": timescaledb
        "[transforms.timescaledb.type]": io.debezium.connector.postgresql.transforms.timescaledb.TimescaleDb
        "[transforms.timescaledb.database.dbname]": ${application.db.database}
        "[transforms.timescaledb.database.hostname]": ${application.db.hostname}
        "[transforms.timescaledb.database.port]": ${application.db.port}
        "[transforms.timescaledb.database.user]": ${application.db.user}
        "[transforms.timescaledb.database.password]": ${application.db.password}
```

## Useful Links
* https://debezium.io/blog/2020/02/19/debezium-camel-integration/
* https://camel.apache.org/blog/2023/05/camel-debezium-quarkus/
* https://debezium.io/documentation/reference/stable/development/engine.html
* https://debezium.io/documentation/reference/stable/transformations/timescaledb.html
* https://debezium.io/blog/2024/01/11/Debezium-and-TimescaleDB/
* https://access.redhat.com/documentation/en-us/red_hat_build_of_debezium/2.5.4/html-single/debezium_user_guide/index#debezium-connector-for-postgresql
* https://cwiki.apache.org/confluence/display/KAFKA/KIP-66%3A+Single+Message+Transforms+for+Kafka+Connect