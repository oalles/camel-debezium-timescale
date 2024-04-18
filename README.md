# POC: Using Debezium PostgreSQL Connector with Apache Camel

This project is a proof of concept demonstrating the usage of Apache Camel and Debezium to capture changes in a database
and process them.

## Project Description

The project is divided into four parts, each represented by a branch in the repository:

### 1. Camel Debezium Postgresql Connector with MemoryOffsetBackingStore

This part demonstrates how to configure a Debezium connector in Apache Camel using MemoryOffsetBackingStore to store the
offsets.

### 2. Configuring a JdbcOffsetBackingStore (Failure)

Here, an attempt is made to configure the connector to use JdbcOffsetBackingStore, but an issue arises causing the
configuration to fail.

### 3. Fixed JdbcOffsetBackingStore Configuration

The issue from Part 2 is resolved, and it showcases how to properly configure JdbcOffsetBackingStore to store offsets in
a JDBC database.

### 4. Connector Configuration to Monitor Changes in a Timescaledb Hypertable

This part demonstrates how to configure the connector to monitor changes in a Timescaledb hypertable, providing a
practical example of usage in a real-world scenario.

## Repository Usage

Each part of the project is represented by a branch in this repository. You can navigate between branches to view the
code and specific configuration of each part.

To clone and run the project locally, follow these steps:

1. Clone the repository:

   ```bash
   git clone https://github.com/oalles/camel-debezium-timescale
   ```
