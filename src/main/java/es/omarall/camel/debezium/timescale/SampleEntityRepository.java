package es.omarall.camel.debezium.timescale;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SampleEntityRepository extends JpaRepository<SampleEntity, SampleEntityId> {
}

