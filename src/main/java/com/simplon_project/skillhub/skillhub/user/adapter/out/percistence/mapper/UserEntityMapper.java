package com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.mapper;

import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.entity.UserEntity;
import com.simplon_project.skillhub.skillhub.user.domain.model.Id;
import com.simplon_project.skillhub.skillhub.user.domain.model.User;

public final class UserEntityMapper {
    private UserEntityMapper() {
        // Utility class
    }


    public static UserEntity mapToEntity(User domain) {
        if (domain == null) return null;

        return UserEntity.builder()
                .id(EntityId.of(domain.getId().asUUID()))
                .externalId(domain.getExternalId() != null ? domain.getExternalId().asUUID() : null)
                .firstName(domain.getFirstName())
                .lastName(domain.getLastName())
                .email(domain.getEmail())
                .address(domain.getAddress())
                .postalCode(domain.getPostalCode())
                .city(domain.getCity())
                .country(domain.getCountry())
                .phoneNumber(domain.getPhoneNumber())
                .build();
    }


    public static User mapToDomain(UserEntity entity) {
        if (entity == null) return null;

        return User.builder()
                .id(Id.of(entity.getId().value().toString()))
                .externalId(Id.of(entity.getExternalId().toString()))
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .email(entity.getEmail())
                .address(entity.getAddress())
                .postalCode(entity.getPostalCode())
                .city(entity.getCity())
                .country(entity.getCountry())
                .phoneNumber(entity.getPhoneNumber())
                .build();
    }
}
