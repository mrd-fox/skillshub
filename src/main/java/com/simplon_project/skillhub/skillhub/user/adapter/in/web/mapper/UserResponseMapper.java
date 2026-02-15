package com.simplon_project.skillhub.skillhub.user.adapter.in.web.mapper;

import com.simplon_project.skillhub.skillhub.user.adapter.in.web.response.RoleResponse;
import com.simplon_project.skillhub.skillhub.user.adapter.in.web.response.UserResponse;
import com.simplon_project.skillhub.skillhub.user.application.port.in.dto.GetUserByExternalIdResult;
import com.simplon_project.skillhub.skillhub.user.domain.model.User;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserResponseMapper {
    public static UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId().asString())
                .externalId(user.getExternalId().toString())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .address(user.getAddress())
                .city(user.getCity())
                .country(user.getCountry())
                .phoneNumber(user.getPhoneNumber())
                .postalCode(user.getPostalCode())
                .active(user.isActive())
                .roles(mapRoles(user))
                .build();
    }

    public static UserResponse mapToResponse(GetUserByExternalIdResult result) {

        User user = result.user();

        return UserResponse.builder()
                .id(user.getId().asString())
                .externalId(user.getExternalId().toString())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .address(user.getAddress())
                .city(user.getCity())
                .country(user.getCountry())
                .phoneNumber(user.getPhoneNumber())
                .postalCode(user.getPostalCode())
                .active(user.isActive())
                .roles(mapRoles(user))
                .enrolledCourseIds(mapEnrolledCourseIds(result.enrolledCourseIds()))
                .build();
    }

    private static List<String> mapEnrolledCourseIds(List<UUID> enrolledCourseIds) {
        if (enrolledCourseIds == null || enrolledCourseIds.isEmpty()) {
            return List.of();
        } else {
            return enrolledCourseIds.stream()
                    .map(UUID::toString)
                    .toList();
        }
    }

    private static Set<RoleResponse> mapRoles(User user) {
        if (user.getRoles() == null) return Set.of();
        return user.getRoles().stream()
                .map(RoleResponse::new)
                .collect(Collectors.toSet());
    }
}
