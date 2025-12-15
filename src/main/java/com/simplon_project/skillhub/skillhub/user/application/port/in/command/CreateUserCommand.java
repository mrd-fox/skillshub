package com.simplon_project.skillhub.skillhub.user.application.port.in.command;

import com.simplon_project.skillhub.skillhub.user.domain.enums.RolesEnum;
import com.simplon_project.skillhub.skillhub.user.domain.model.Id;
import com.simplon_project.skillhub.skillhub.user.domain.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.lang.NonNull;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record CreateUserCommand(
        @NotBlank String externalId,
         String firstName,
         String lastName,
        @Email @NotBlank String email,
        String address,
        String city,
        String country,
        String phoneNumber,
        String postalCode,
        @NonNull Set<String> roles

) {
    public User mapToDomain() {
        validate();

        return User.builder()
                .id(Id.random())
                .externalId(UUID.fromString(externalId))
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .address(address)
                .city(city)
                .country(country)
                .phoneNumber(phoneNumber)
                .postalCode(postalCode)
                .roles(mapRoles()) // Set<RolesEnum>
                .build();
    }

    private void validate() {
        if (externalId == null || externalId.isBlank())
            throw new IllegalArgumentException("Missing externalId header");
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("Missing email header");
//        if (firstName == null || lastName == null)
//            throw new IllegalArgumentException("Missing name headers");
    }

    private Set<RolesEnum> mapRoles() {
        return roles.stream()
                .map(role -> {
                    try {
                        return RolesEnum.valueOf(role.trim().toUpperCase());
                    } catch (IllegalArgumentException ex) {
                        throw new IllegalArgumentException("Invalid role: " + role);
                    }
                })
                .collect(Collectors.toSet());
    }

}