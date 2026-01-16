package com.simplon_project.skillhub.skillhub.course.adapter.in.web.request;

import com.simplon_project.skillhub.skillhub.course.application.port.in.command.ConfirmVideoUploadCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "ConfirmVideoInChapterRequest", description = "Confirms that the video upload is completed on the provider side.")
public record ConfirmVideoInChapterRequest(

        @Schema(
                description = "Canonical source URI of the uploaded video (provider-agnostic). Example: vimeo://123456789",
                example = "vimeo://123456789",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "sourceUri must not be blank")
        String sourceUri
) {

    public ConfirmVideoInChapterRequest {
        if (sourceUri == null || sourceUri.isBlank()) {
            throw new IllegalArgumentException("sourceUri is required");
        }
    }

    public ConfirmVideoUploadCommand toCommand(String courseId, String sectionId, String chapterId) {
        return new ConfirmVideoUploadCommand(courseId, sectionId, chapterId, sourceUri);
    }
}
