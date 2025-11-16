package com.simplon_project.skillhub.skillhub.user.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.Set;

@Builder
@Schema(description = "User response DTO")
public record UserResponse(
        @Schema(example = "1f26c07d-9a1a-4b9a-8a5e-3e05e09197c9")
        String id,
        @Schema(example = "9a5a94e5-04b2-47b8-9ef2-4426d1b640b2")
        String externalId,
        @Schema(example = "Marina")
        String firstName,
        @Schema(example = "Darde")
        String lastName,
        @Schema(example = "marina.darde@3wa.io")
        String email,
        String address,
        String city,
        String country,
        String phoneNumber,
        String postalCode,
        boolean active,
        Set<RoleResponse> roles
) {

}
