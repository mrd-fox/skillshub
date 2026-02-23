package com.simplon_project.skillhub.skillhub.user.domain.model;

import com.simplon_project.skillhub.skillhub.user.domain.enums.RolesEnum;
import com.simplon_project.skillhub.skillhub.user.domain.exception.InvalidUserStateException;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@SuperBuilder(toBuilder = true)
@Setter
public class User extends Base {

    // --- Identification ---
    private final UUID externalId;     // Keycloak ID
    private final String email;

    // --- Personal information ---
    private String firstName;
    private String lastName;
    private String address;
    private String postalCode;
    private String city;
    private String country;
    private String phoneNumber;

    // --- Account state ---
    private boolean active;

    // --- Roles ---
    private Set<RolesEnum> roles;

    private User(Id id,
                 UUID externalId,
                 String email,
                 String firstName,
                 String lastName,
                 String address,
                 String postalCode,
                 String city,
                 String country,
                 String phoneNumber,
                 boolean active,
                 Set<RolesEnum> roles,
                 LocalDateTime createdAt,
                 LocalDateTime updatedAt) {
        super(id, createdAt, updatedAt);

        if (externalId == null)
            throw new InvalidUserStateException("External ID (Keycloak ID) cannot be null");

        if (email == null || email.isBlank())
            throw new InvalidUserStateException("Email cannot be null or blank");

        if (roles == null || roles.isEmpty())
            throw new InvalidUserStateException("User must have at least one role");

        this.externalId = externalId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.postalCode = postalCode;
        this.city = city;
        this.country = country;
        this.phoneNumber = phoneNumber;
        this.active = active;
        this.roles = new HashSet<>(roles);
    }

    public static User of(
            Id id,
            UUID externalId,
            String email,
            String firstName,
            String lastName,
            String address,
            String postalCode,
            String city,
            String country,
            String phoneNumber,
            Set<RolesEnum> roles
    ) {
        Set<RolesEnum> assignedRoles = (roles == null || roles.isEmpty())
                ? Set.of(RolesEnum.STUDENT)
                : new HashSet<>(roles);

        return User.builder()
                .id(id)
                .externalId(externalId)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .address(address)
                .postalCode(postalCode)
                .city(city)
                .country(country)
                .phoneNumber(phoneNumber)
                .active(false)
                .roles(assignedRoles)
                .build();
    }

    public void activate() {
        if (this.active)
            throw new InvalidUserStateException("User is already active");
        this.active = true;
    }

    public void deactivate() {
        if (!this.active)
            throw new InvalidUserStateException("User is already inactive");
        this.active = false;
    }

    public void updateName(String firstName, String lastName) {
        if (firstName == null || lastName == null)
            throw new InvalidUserStateException("Names cannot be null");
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public void addRole(RolesEnum role) {
        if (role == null)
            throw new InvalidUserStateException("Role cannot be null");
        this.roles.add(role);
    }

    public void removeRole(RolesEnum role) {
        if (this.roles.size() == 1 && this.roles.contains(role))
            throw new InvalidUserStateException("User must have at least one role");
        this.roles.remove(role);
    }

    public boolean hasRole(RolesEnum role) {
        return this.roles.contains(role);
    }


}