package com.clinic.infrastructure.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

public interface OutboxJpaRepository extends JpaRepository<OutboxEventEntity, String> {

    List<OutboxEventEntity> findTop50ByPublishedAtIsNullOrderByCreatedAtAsc();

    @Modifying
    @Transactional
    @Query("update OutboxEventEntity e set e.publishedAt = :at where e.id = :id")
    int markPublished(@Param("id") String id, @Param("at") Instant at);
}
