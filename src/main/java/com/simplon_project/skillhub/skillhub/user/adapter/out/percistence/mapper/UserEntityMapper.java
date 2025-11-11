package com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.mapper;

import com.simplon_project.skillhub.skillhub.config.helper.DateTimeHelper;
import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.entity.RoleEntity;
import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.entity.UserEntity;
import com.simplon_project.skillhub.skillhub.user.domain.enums.RolesEnum;
import com.simplon_project.skillhub.skillhub.user.domain.model.Id;
import com.simplon_project.skillhub.skillhub.user.domain.model.User;

import java.util.Set;
import java.util.stream.Collectors;

public final class UserEntityMapper {

    private UserEntityMapper() {
    }

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
                .roles(mapRolesToEntities(domain.getRoles()))
                .build();
    }

    public static User mapToDomain(UserEntity entity) {


        return User.builder()
                .id(Id.of(entity.getId().toString()))
                .externalId(entity.getExternalId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .address(entity.getAddress())
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
}