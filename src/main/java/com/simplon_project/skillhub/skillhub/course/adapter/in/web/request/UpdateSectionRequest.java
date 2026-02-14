package com.simplon_project.skillhub.skillhub.course.adapter.in.web.request;

import com.simplon_project.skillhub.skillhub.common.Helper;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.UpdateSectionCommand;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import java.util.List;

/**
 * Web request DTO (adapter.in).
 * <p>
 * PATCH semantics for nested chapters:
 * - chapters == null   => client did NOT send chapters => no changes to chapters for that section.
 * - chapters != null   => patch mode => missing existing chapters are soft-deleted.
 * - chapters = []    => soft-delete ALL existing chapters in this section.
 * <p>
 * This DTO intentionally keeps "chapters" nullable to distinguish "no-op" vs "apply patch".
 */
@Schema(
        name = "UpdateSectionRequest",
        description =
                """
                        Section patch.
                        
                        PATCH SEMANTICS:
                        - id null => CREATE section.
                        - id not null => UPDATE section.
                        
                        Nested chapters PATCH:
                        - chapters omitted (null) => NO chapter changes.
                        - chapters provided (even empty) => APPLY patch:
                          - missing existing chapters are SOFT-DELETED
                          - id null => CREATE chapter
                          - id not null => UPDATE chapter
                          - chapters=[] => SOFT-DELETE ALL chapters in this section
                        """
)
public record UpdateSectionRequest(

        @Schema(
                description = "Section id. Null means create a new section.",
                example = "9b26346a-0c6a-4a94-b7cb-5241dfc8a2be",
                nullable = true
        )
        @Nullable
        String id,

        @Schema(
                description = "Section title. Required when creating (id null). Optional when updating.",
                example = "Section one",
                nullable = true
        )
        @Nullable
        String title,

        @Schema(
                description = "Section position. Optional.",
                example = "1",
                minimum = "1",
                nullable = true
        )
        @Nullable
        @Min(1)
        Integer position,

        @ArraySchema(
                schema = @Schema(implementation = UpdateChapterRequest.class),
                arraySchema = @Schema(
                        description =
                                """
                                        Optional chapters patch.
                                        - Omitted (null) => no chapter changes.
                                        - Provided (even empty) => apply patch (missing existing chapters are soft-deleted).
                                        - Empty list => soft-delete all chapters of this section.
                                        """
                )
        )
        @Nullable
        @Valid
        List<UpdateChapterRequest> chapters
) {
    public UpdateSectionCommand mapToCommand() {
        return UpdateSectionCommand.builder()
                .id(Helper.normalizeOptional(id))
                .title(Helper.normalizeOptional(title))
                .position(position)
                /*
                 * Keep chapters nullable semantics:
                 * - chapters == null => mapToCommands(null) returns null => application interprets as NO-OP for chapters.
                 * - chapters != null => mapToCommands(list) => application interprets as PATCH and can soft-delete missing.
                 */
                .chapters(UpdateChapterRequest.mapToCommands(chapters))
                .build();
    }

    public static List<UpdateSectionCommand> mapToCommands(List<UpdateSectionRequest> requests) {
        if (requests == null) {
            return null;
        }
        return requests.stream()
                .map(UpdateSectionRequest::mapToCommand)
                .toList();
    }
}