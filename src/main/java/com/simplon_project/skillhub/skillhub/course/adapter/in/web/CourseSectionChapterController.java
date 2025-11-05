package com.simplon_project.skillhub.skillhub.course.adapter.in.web;

import com.simplon_project.skillhub.skillhub.course.adapter.in.web.request.AddVideoInChapterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/course/{courseId}/sections/{sectionId}")
public class CourseSectionChapterController {
    @PostMapping("/chapters/{chapterId}/video")
    @Operation(description = "Add a video to chapter")
    @ResponseStatus(HttpStatus.CREATED)
    public void createChapter(
            @Parameter(description = "course id", required = true) @PathVariable String courseId,
            @Parameter(description = "section id", required = true) @PathVariable String sectionId,
            @Parameter(description = "chapter id", required = true) @PathVariable String chapterId,
            @RequestParam MultipartFile video,
            @RequestBody @Valid @NotNull AddVideoInChapterRequest request
    ) {
        var chapterCommand = request.toVideoCommand(courseId, sectionId, chapterId, video);
        //todo
        //chapterCommand.mapToResponse;
//        createChapterPort.createChapter(chapterCommand);
    }
}
