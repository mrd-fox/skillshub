package com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.repository;

import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaUserRepository extends JpaRepository<UserEntity, EntityId> {

    Optional<UserEntity> findByExternalId(UUID externalId);

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}