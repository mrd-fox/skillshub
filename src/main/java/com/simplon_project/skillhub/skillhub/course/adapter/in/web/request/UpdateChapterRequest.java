package com.simplon_project.skillhub.skillhub.course.adapter.in.web.request;

import com.simplon_project.skillhub.skillhub.common.Helper;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.UpdateChapterCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;

import java.util.List;

/**
 * Web request DTO (adapter.in).
 * <p>
 * PATCH semantics:
 * - id null => CREATE chapter
 * - id not null => UPDATE chapter
 * <p>
 * Note:
 * The "chapter deletion" behavior is not expressed here directly.
 * Deletion is inferred at the parent level (UpdateSectionRequest.chapters):
 * - chapters == null => no-op
 * - chapters != null => missing existing chapters are soft-deleted
 */
@Schema(
        name = "UpdateChapterRequest",
        description =
                """
                        Chapter patch.
                        
                        PATCH SEMANTICS:
                        - id null => CREATE chapter.
                        - id not null => UPDATE chapter.
                        """
)
public record UpdateChapterRequest(

        @Schema(
                description = "Chapter id. Null means create a new chapter.",
                example = "4b5d356d-ff48-42d6-a5ed-ece98763396c",
                nullable = true
        )
        @Nullable
        String id,

        @Schema(
                description = "Chapter title. Required when creating (id null). Optional when updating.",
                example = "Chapter 1",
                nullable = true
        )
        @Nullable
        String title,

        @Schema(
                description = "Chapter position. Optional.",
                example = "1",
                minimum = "1",
                nullable = true
        )
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