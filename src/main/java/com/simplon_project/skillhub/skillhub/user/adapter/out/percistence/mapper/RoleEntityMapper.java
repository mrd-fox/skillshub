package com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.mapper;

import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.entity.RoleEntity;
import com.simplon_project.skillhub.skillhub.user.domain.model.Id;
import com.simplon_project.skillhub.skillhub.user.domain.model.Role;

import java.util.Set;
import java.util.stream.Collectors;

public final class RoleEntityMapper {

    private RoleEntityMapper() {
    }

    /**
     * Map RoleEntity to Role domain model
     */
    public static Role mapToDomain(RoleEntity entity) {
        if (entity == null) {
            return null;
        }

        return Role.builder()
                .id(Id.of(entity.getId().toString()))
                .name(entity.getName())
                .build();
    }

    /**
     * Map Set<RoleEntity> to Set<Role>
     */
    public static Set<Role> mapToDomains(Set<RoleEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Set.of();
        }

        return entities.stream()
                .map(RoleEntityMapper::mapToDomain)
                .collect(Collectors.toSet());
    }

    /**
     * Map Role domain model to RoleEntity
     */
    public static RoleEntity mapToEntity(Role domain) {
        if (domain == null) {
            return null;
        }

        return RoleEntity.builder()
                .id(com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.entity.EntityId
                        .fromString(domain.getId().asString()))
                .name(domain.getName())
                .build();
    }

    /**
     * Map Set<Role> to Set<RoleEntity>
     */
    public static Set<RoleEntity> mapToEntities(Set<Role> domains) {
        if (domains == null || domains.isEmpty()) {
            return Set.of();
        }

        return domains.stream()
                .map(RoleEntityMapper::mapToEntity)
                .collect(Collectors.toSet());
    }
}
