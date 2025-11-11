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

    public static UserEntity toEntity(User domain, Set<RoleEntity> roleEntities) {
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
                .roles(roleEntities)
                .build();
    }

    public static User toDomain(UserEntity entity) {
        Set<RolesEnum> roles = entity.getRoles().stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toSet());

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
                .roles(roles)
                .createdAt(DateTimeHelper.toLocalDateTime(entity.getCreatedAt()))
                .updatedAt(DateTimeHelper.toLocalDateTime(entity.getCreatedAt()))
                .build();
    }
}