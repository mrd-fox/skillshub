package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.repository;

import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA repository for outbox events.
 * Provides CRUD operations for transactional outbox pattern.
 */
public interface JpaOutboxEventRepository extends JpaRepository<OutboxEventEntity, EntityId> {
}
