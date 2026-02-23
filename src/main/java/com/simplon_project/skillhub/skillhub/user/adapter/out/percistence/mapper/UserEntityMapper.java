package com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.mapper;

import com.simplon_project.skillhub.skillhub.config.helper.DateTimeHelper;
import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.entity.RoleEntity;
import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.entity.UserEntity;
import com.simplon_project.skillhub.skillhub.user.domain.enums.RolesEnum;
import com.simplon_project.skillhub.skillhub.user.domain.model.Id;
import com.simplon_project.skillhub.skillhub.user.domain.model.User;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class UserEntityMapper {

    private UserEntityMapper() {
    }

    private static final Map<RolesEnum, EntityId> ROLE_ID_MAP = Map.of(
            RolesEnum.STUDENT, EntityId.fromString("13fe3542-858f-4e20-8f52-1e6c35e78831"),
            RolesEnum.TUTOR, EntityId.fromString("008b0f58-7d3d-4c3c-ac51-cf90a28010a7"),
            RolesEnum.ADMIN, EntityId.fromString("5464d17e-15e0-4598-ae3e-0a2573e6bd76")
    );

    public static UserEntity mapToEntity(User domain) {
        return UserEntity.builder()
                .id(EntityId.fromString(domain.getId().asString()))
                .externalId(domain.getExternalId())
                .email(domain.getEmail())
                .firstName(domain.getFirstName())
                .lastName(domain.getLastName())
                .address(domain.getAddress())
                .postalCode(domain.getPostalCode())
                .city(domain.getCity())
                .country(domain.getCountry())
                .phoneNumber(domain.getPhoneNumber())
                .active(domain.isActive())
                .build();
    }

    public static User mapToDomain(UserEntity entity) {


        return User.builder()
                .id(Id.of(entity.getId().toString()))
                .externalId(entity.getExternalId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .address(entity.getAddress())
                .email(entity.getEmail())
                .postalCode(entity.getPostalCode())
                .city(entity.getCity())
                .country(entity.getCountry())
                .phoneNumber(entity.getPhoneNumber())
                .active(entity.isActive())
                .roles(mapRolesToDomain(entity.getRoles()))
                .createdAt(DateTimeHelper.toLocalDateTime(entity.getCreatedAt()))
                .updatedAt(DateTimeHelper.toLocalDateTime(entity.getCreatedAt()))
                .build();
    }

    private static Set<RoleEntity> mapRolesToEntities(Set<RolesEnum> roles) {
        if (roles == null || roles.isEmpty()) return Set.of();

        return roles.stream()
                .map(roleEnum -> RoleEntity.builder()
                        .id(resolveRoleEntityId(roleEnum))
                        .name(roleEnum)
                        .build())
                .collect(Collectors.toSet());
    }

    private static Set<RolesEnum> mapRolesToDomain(Set<RoleEntity> entities) {
        if (entities == null || entities.isEmpty()) return Set.of();

        return entities.stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Resolve the fixed ID associated with a given role enum.
     * Falls back to a random ID if the role is unknown (should never happen in production).
     */
    private static EntityId resolveRoleEntityId(RolesEnum role) {
        return ROLE_ID_MAP.getOrDefault(role, EntityId.random());
    }
}