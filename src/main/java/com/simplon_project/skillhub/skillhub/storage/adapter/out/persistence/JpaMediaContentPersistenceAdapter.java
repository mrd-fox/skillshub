package com.simplon_project.skillhub.skillhub.storage.adapter.out.persistence;

import com.simplon_project.skillhub.skillhub.storage.adapter.out.persistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.storage.adapter.out.persistence.mappers.MediaContentMapper;
import com.simplon_project.skillhub.skillhub.storage.application.port.out.SaveMediaContentPort;
import com.simplon_project.skillhub.skillhub.storage.application.port.out.UpdateMediaPort;
import com.simplon_project.skillhub.skillhub.storage.domaine.model.MediaContent;
import com.simplon_project.skillhub.skillhub.storage.domaine.model.MediaId;
import com.simplon_project.skillhub.skillhub.storage.domaine.model.VideoStatusEnum;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component

public class JpaMediaContentPersistenceAdapter implements SaveMediaContentPort, UpdateMediaPort {


    private final EntityManager entityManager;
    private final MediaFileJpaRepository jpa;     // Spring Data interne

    public JpaMediaContentPersistenceAdapter(
            @Qualifier("storageEntityManager") EntityManager entityManager,
            MediaFileJpaRepository jpa) {
        this.entityManager = entityManager;
        this.jpa = jpa;
    }

    @Override
    @Transactional("storageTxManager")
    public MediaContent save(MediaContent media) {
        try {
            var entity = MediaContentMapper.mapToEntity(media);
            var saved = jpa.saveAndFlush(entity);
            return MediaContentMapper.mapToDomain(saved);
        } catch (Exception e) {
            throw new PersistenceException("Failed to persist media metadata.", e);
        }
    }

    @Override
    @Transactional("storageTxManager")
    public void updateMetadata(MediaId id, int width, int height, long duration, VideoStatusEnum status) {
        try {
            entityManager.createQuery(
                            """
                                    
                                        UPDATE MediaFileEntity m
                                    SET m.width = :width,
                                        m.height = :height,
                                        m.duration = :duration,
                                        m.status = :status
                                    WHERE m.id = :id
                                    """)
                            .
                    setParameter("width", width)
                    .setParameter("height", height)
                    .setParameter("duration", duration)
                    .setParameter("status",
                            status)
                    .setParameter("id", EntityId.of(id.asUUID()))
                    .executeUpdate();
        } catch (Exception e) {
            throw new PersistenceException("Failed to update media metadata.", e);
        }
    }
}
