package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.adapter;

import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.VideoEntity;
import com.simplon_project.skillhub.skillhub.course.application.dto.VideoDeletionTarget;
import com.simplon_project.skillhub.skillhub.course.application.port.out.video.LoadVideoEntityByIdPort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.video.SaveVideoEntityPort;
import com.simplon_project.skillhub.skillhub.course.domain.enums.ExternalDeletionStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Persistence adapter for deletion worker.
 * <p>
 * Key point:
 * VideoEntity is annotated with @SQLRestriction("deleted_at is null").
 * Therefore, repository findById() will NOT see soft-deleted rows.
 * <p>
 * This adapter uses a native query to load rows INCLUDING soft-deleted ones.
 */
@Slf4j
@Component
public class VideoEntityDeletionAdapter implements LoadVideoEntityByIdPort, SaveVideoEntityPort {

    /**
     * IMPORTANT:
     * Adjust column name if your embedded id column is not "id".
     * In your project you stated EntityId column is "id".
     */
    private static final String SQL_LOAD_VIDEO_INCLUDING_SOFT_DELETED =
            "SELECT * FROM \"videos\" WHERE id = ?";

    private final EntityManager entityManager;

    public VideoEntityDeletionAdapter(@Qualifier("courseEntityManager") EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(readOnly = true, transactionManager = "courseTxManager")
    public Optional<VideoDeletionTarget> loadIncludingSoftDeleted(String videoId) {

        Optional<VideoEntity> maybeEntity = loadEntityIncludingSoftDeleted(videoId);

        if (maybeEntity.isEmpty()) {
            return Optional.empty();
        }

        VideoEntity entity = maybeEntity.get();

        // Use the embedded UUID value (avoid EntityId.toString() formatting surprises)
        String idAsString = entity.getVideoId() != null && entity.getVideoId().value() != null
                ? entity.getVideoId().value().toString()
                : null;

        return Optional.of(new VideoDeletionTarget(
                idAsString,
                entity.getSourceUri(),
                entity.getExternalDeletionStatus(),
                entity.getDeleteAttemptCount()
        ));
    }

    @Override
    @Transactional(transactionManager = "courseTxManager")
    public void markDeleted(String videoId) {

        VideoEntity entity = loadEntityIncludingSoftDeletedOrThrow(videoId);

        entity.setExternalDeletionStatus(ExternalDeletionStatus.DELETED);
        entity.setDeleteLastError(null);
        // Keep deleteAttemptCount as-is (audit trail)

        entityManager.merge(entity);

        log.debug(
                "Video marked DELETED: videoId={} attempts={}",
                entity.getVideoId() != null ? entity.getVideoId().value() : null,
                entity.getDeleteAttemptCount()
        );
    }

    @Override
    @Transactional(transactionManager = "courseTxManager")
    public void markFailed(String videoId, int attempt, String errorMessage) {

        VideoEntity entity = loadEntityIncludingSoftDeletedOrThrow(videoId);

        entity.setExternalDeletionStatus(ExternalDeletionStatus.FAILED);
        entity.setDeleteAttemptCount(attempt);
        entity.setDeleteLastError(errorMessage);

        entityManager.merge(entity);

        log.debug(
                "Video marked FAILED: videoId={} attempts={} error={}",
                entity.getVideoId() != null ? entity.getVideoId().value() : null,
                attempt,
                errorMessage
        );
    }

    @Override
    @Transactional(transactionManager = "courseTxManager")
    public void markRetryScheduled(String videoId, int attempt, String errorMessage) {

        VideoEntity entity = loadEntityIncludingSoftDeletedOrThrow(videoId);

        // stays REQUESTED (still pending external deletion)
        entity.setExternalDeletionStatus(ExternalDeletionStatus.REQUESTED);

        // IMPORTANT:
        // attempt passed here MUST be the NEXT attempt (listener fix already does that)
        entity.setDeleteAttemptCount(attempt);

        entity.setDeleteLastError(errorMessage);

        entityManager.merge(entity);

        log.debug(
                "Video retry scheduled: videoId={} attempts={} error={}",
                entity.getVideoId() != null ? entity.getVideoId().value() : null,
                attempt,
                errorMessage
        );
    }

    /**
     * Native load bypassing @SQLRestriction("deleted_at is null").
     * Returns a managed entity within the current persistence context.
     */
    private Optional<VideoEntity> loadEntityIncludingSoftDeleted(String videoId) {

        if (videoId == null || videoId.isBlank()) {
            throw new IllegalArgumentException("videoId must not be blank");
        }

        EntityId entityId = EntityId.fromString(videoId);

        try {
            VideoEntity entity = (VideoEntity) entityManager
                    .createNativeQuery(SQL_LOAD_VIDEO_INCLUDING_SOFT_DELETED, VideoEntity.class)
                    .setParameter(1, entityId.value())
                    .getSingleResult();

            return Optional.of(entity);

        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    private VideoEntity loadEntityIncludingSoftDeletedOrThrow(String videoId) {
        return loadEntityIncludingSoftDeleted(videoId)
                .orElseThrow(() -> new IllegalStateException(
                        "Video not found (including soft-deleted): " + videoId
                ));
    }
}