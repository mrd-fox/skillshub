package com.simplon_project.skillhub.skillhub.user.application.port.in.command;

import com.simplon_project.skillhub.skillhub.user.domain.model.Id;
import com.simplon_project.skillhub.skillhub.user.domain.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserCommand(
        @NotBlank String externalId,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Email @NotBlank String email,
        String address,
        String city,
        String country
) {
    public User mapToDomain() {
        return User.builder()
                .id(Id.random())
                .externalId(Id.of(externalId))
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .address(address)
                .city(city)
                .country(country)
                .build();
    }
}