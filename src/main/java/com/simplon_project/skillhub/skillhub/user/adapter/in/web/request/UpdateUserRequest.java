package com.simplon_project.skillhub.skillhub.user.adapter.in.web.request;

import com.simplon_project.skillhub.skillhub.user.application.port.in.command.UpdateUserCommand;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(
        name = "UpdateUserRequest",
        description = "PATCH-like user update payload. Only provided fields are applied. rolesToAdd is additive and restricted to TUTOR."
)
public record UpdateUserRequest(

        @Schema(description = "First name. If omitted or blank, it is not updated.", example = "Marina", nullable = true)
        String firstName,

        @Schema(description = "Last name. If omitted or blank, it is not updated.", example = "Darde", nullable = true)
        String lastName,

        @Schema(description = "Address. If omitted or blank, it is not updated.", example = "12 rue de la Paix", nullable = true)
        String address,

        @Schema(description = "Postal code. If omitted or blank, it is not updated.", example = "06000", nullable = true)
        String postalCode,

        @Schema(description = "City. If omitted or blank, it is not updated.", example = "Nice", nullable = true)
        String city,

        @Schema(description = "Country. If omitted or blank, it is not updated.", example = "France", nullable = true)
        String country,

        @Schema(description = "Phone number. If omitted or blank, it is not updated.", example = "+33612345678", nullable = true)
        String phoneNumber,

        @Schema(
                description = "Roles to add (additive only). Allowed values: [\"TUTOR\"]. Existing roles are preserved.",
                example = "[\"TUTOR\"]",
                nullable = true
        )
        Set<String> rolesToAdd
) {

    public void validate() {
        boolean hasProfileField =
                (firstName != null && !firstName.isBlank())
                        || (lastName != null && !lastName.isBlank())
                        || (address != null && !address.isBlank())
                        || (postalCode != null && !postalCode.isBlank())
                        || (city != null && !city.isBlank())
                        || (country != null && !country.isBlank())
                        || (phoneNumber != null && !phoneNumber.isBlank());

        boolean hasRolesToAdd = (rolesToAdd != null && !rolesToAdd.isEmpty());

        if (!hasProfileField && !hasRolesToAdd) {
            throw new IllegalArgumentException("UpdateUserRequest is empty: provide at least one field to update.");
        }

        if (hasRolesToAdd) {
            for (String role : rolesToAdd) {
                if (role == null || role.isBlank()) {
                    throw new IllegalArgumentException("rolesToAdd contains a blank role.");
                }
                if (!"TUTOR".equalsIgnoreCase(role.trim())) {
                    throw new IllegalArgumentException("rolesToAdd supports only TUTOR for now.");
                }
            }
        }
    }

    public UpdateUserCommand toUpdateUserCommand(String externalId) {
        this.validate();

        return new UpdateUserCommand(
                externalId,
                firstName,
                lastName,
                address,
                postalCode,
                city,
                country,
                phoneNumber,
                rolesToAdd
        );
    }
}