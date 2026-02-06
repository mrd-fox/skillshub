package com.simplon_project.skillhub.skillhub.user.adapter.in.web;

import com.simplon_project.skillhub.skillhub.user.application.port.in.command.CreateUserCommand;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public final class UserHelper {
    public static CreateUserCommand extractCommandFromHeaders(HttpServletRequest request) {
        String externalId = request.getHeader("X-User-Id");
        String firstName = request.getHeader("X-User-FirstName");
        String lastName = request.getHeader("X-User-LastName");
        String email = request.getHeader("X-User-Email");
        String address = request.getHeader("X-User-Address");
        String city = request.getHeader("X-User-City");
        String country = request.getHeader("X-User-Country");
        String phone = request.getHeader("X-User-PhoneNumber");
        String postal = request.getHeader("X-User-PostalCode");
        String rolesHeader = request.getHeader("X-User-Roles");

        Set<String> roles = rolesHeader != null
                ? Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .collect(Collectors.toSet())
                : Set.of();

        return new CreateUserCommand(
                externalId,
                firstName,
                lastName,
                email,
                address,
                city,
                country,
                phone,
                postal,
                roles
        );
    }
}

