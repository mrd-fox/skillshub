package com.simplon_project.skillhub.skillhub.user.application.port.in.command;

import com.simplon_project.skillhub.skillhub.user.domain.model.Id;
import com.simplon_project.skillhub.skillhub.user.domain.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreateUserCommand(
        @NotBlank String externalId,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Email @NotBlank String email,
        String address,
        String city,
        String country,
        String phoneNumber,
        String postalCode
) {
    public User mapToDomain() {
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
                .build();
    }
}