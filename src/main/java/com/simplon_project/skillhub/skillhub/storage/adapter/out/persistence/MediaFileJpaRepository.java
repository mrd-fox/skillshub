package com.simplon_project.skillhub.skillhub.storage.adapter.out.persistence;


import com.simplon_project.skillhub.skillhub.storage.adapter.out.persistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.storage.adapter.out.persistence.entity.MediaFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MediaFileJpaRepository extends JpaRepository<MediaFileEntity, EntityId> {
}
