package com.simplon_project.skillhub.skillhub.course.adapter.in.web.request;

import com.simplon_project.skillhub.skillhub.common.Helper;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.UpdateSectionCommand;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import java.util.List;

@Schema(name = "UpdateSectionRequest", description = "Section patch. If id is null => create.")
public record UpdateSectionRequest(

        @Schema(description = "Section id. Null means create a new section.",
                example = "9b26346a-0c6a-4a94-b7cb-5241dfc8a2be", nullable = true)
        @Nullable
        String id,

        @Schema(description = "Section title. Required when creating (id null). Optional when updating.",
                example = "Section one")
        @Nullable
        String title,

        @Schema(description = "Section position. Optional.",
                example = "1", minimum = "1")
        @Nullable
        @Min(1)
        Integer position,

        @ArraySchema(schema = @Schema(implementation = UpdateChapterRequest.class),
                arraySchema = @Schema(description = "Optional chapters patch. If omitted => no chapter changes."))
        @Nullable
        @Valid
        List<UpdateChapterRequest> chapters
) {
    public UpdateSectionCommand mapToCommand() {
        return UpdateSectionCommand.builder()
                .id(Helper.normalizeOptional(id))
                .title(Helper.normalizeOptional(title))
                .position(position)
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