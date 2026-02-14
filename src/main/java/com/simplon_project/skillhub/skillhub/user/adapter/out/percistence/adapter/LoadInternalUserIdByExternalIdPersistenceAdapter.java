package com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.adapter;

import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.entity.UserEntity;
import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.repository.JpaUserRepository;
import com.simplon_project.skillhub.skillhub.user.application.port.out.LoadInternalUserIdByExternalIdPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Persistence adapter for loading internal user ID from external user ID.
 * Maps between JPA entities and application layer primitives.
 */
@Component
@RequiredArgsConstructor
public class LoadInternalUserIdByExternalIdPersistenceAdapter implements LoadInternalUserIdByExternalIdPort {

    private final JpaUserRepository userRepository;

    /**
     * Load the internal user ID by external user ID.
     * Maps UserEntity.id (EntityId) to UUID.
     *
     * @param externalUserId the external user UUID (Keycloak ID)
     * @return Optional containing the internal user ID if found, empty otherwise
     */
    @Override
    public Optional<UUID> loadInternalUserId(UUID externalUserId) {
        Optional<UserEntity> userEntity = userRepository.findByExternalId(externalUserId);

        return userRepository.findByExternalId(externalUserId)
                .map(user -> user.getId().value());
    }
}

