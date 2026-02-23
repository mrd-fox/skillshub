package com.simplon_project.skillhub.skillhub.user.adapter.in.web.response;


import com.simplon_project.skillhub.skillhub.user.domain.enums.RolesEnum;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User role representation")
public record RoleResponse(
        @Schema(example = "STUDENT")
        RolesEnum name
) {
}
