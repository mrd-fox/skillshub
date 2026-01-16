package com.simplon_project.skillhub.skillhub.course.adapter.in.web.request;

import com.simplon_project.skillhub.skillhub.course.application.port.in.command.InitVideoCommand;
import jakarta.validation.constraints.Positive;

public record InitVideoInChapterRequest(

        @Positive
        long sizeBytes
) {
    public InitVideoCommand mapToCommand(
            String courseId,
            String sectionId,
            String chapterId
    ) {
        return new InitVideoCommand(
                courseId,
                sectionId,
                chapterId,
                sizeBytes
        );
    }
}