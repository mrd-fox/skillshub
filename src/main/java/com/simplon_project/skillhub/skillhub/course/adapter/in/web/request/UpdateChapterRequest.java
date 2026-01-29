package com.simplon_project.skillhub.skillhub.course.adapter.in.web.request;

import com.simplon_project.skillhub.skillhub.common.Helper;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.UpdateChapterCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;

import java.util.List;

@Schema(name = "UpdateChapterRequest", description = "Chapter patch. If id is null => create.")
public record UpdateChapterRequest(

        @Schema(description = "Chapter id. Null means create a new chapter.",
                example = "4b5d356d-ff48-42d6-a5ed-ece98763396c", nullable = true)
        @Nullable
        String id,

        @Schema(description = "Chapter title. Required when creating (id null). Optional when updating.",
                example = "Chapter 1")
        @Nullable
        String title,

        @Schema(description = "Chapter position. Optional.",
                example = "1", minimum = "1")
        @Nullable
        @Min(1)
        Integer position
) {
    public UpdateChapterCommand mapToCommand() {
        return UpdateChapterCommand.builder()
                .id(Helper.normalizeOptional(id))
                .title(Helper.normalizeOptional(title))
                .position(position)
                .build();
    }

    public static List<UpdateChapterCommand> mapToCommands(List<UpdateChapterRequest> requests) {
        if (requests == null) {
            return null;
        }
        return requests.stream()
                .map(UpdateChapterRequest::mapToCommand)
                .toList();
    }
}

