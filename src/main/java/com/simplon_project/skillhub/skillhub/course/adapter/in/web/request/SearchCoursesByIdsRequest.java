package com.simplon_project.skillhub.skillhub.course.adapter.in.web.request;

import com.simplon_project.skillhub.skillhub.course.application.port.in.command.SearchCoursesByIdsCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "Request payload to search multiple courses by their IDs")
public record SearchCoursesByIdsRequest(

        @NotNull(message = "ids cannot be null")
        @Schema(
                description = "List of course UUIDs",
                example = "[\"18ed3119-b31b-435a-a456-81e9aa6ee396\", \"0d19e3f6-5913-4018-a83b-80bdc9c2d161\"]",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        List<String> ids
) {

    public SearchCoursesByIdsCommand mapToCommand(String externalUserIdRaw) {
        return SearchCoursesByIdsCommand.of(this.ids, externalUserIdRaw);
    }
}

