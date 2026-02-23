package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.repository;

import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.OutboxEventEntity;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.OutboxStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * JPA repository for outbox events.
 * Provides CRUD operations for transactional outbox pattern.
 */
public interface JpaOutboxEventRepository extends JpaRepository<OutboxEventEntity, EntityId> {

    /**
     * Find outbox events with given status, ordered by creation time (oldest first).
     * Used by dispatcher to poll PENDING events for processing.
     *
     * @param status   outbox status to filter (e.g., PENDING)
     * @param pageable pagination (e.g., PageRequest.of(0, batchSize))
     * @return list of outbox events matching status, ordered by createdAt ASC
     */
    List<OutboxEventEntity> findByStatusOrderByCreatedAtAsc(OutboxStatus status, Pageable pageable);
}
