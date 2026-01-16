package com.simplon_project.skillhub.skillhub.course.adapter.in.web;

import com.simplon_project.skillhub.skillhub.course.adapter.in.web.mapper.InitVideoResponseMapper;
import com.simplon_project.skillhub.skillhub.course.adapter.in.web.request.InitVideoInChapterRequest;
import com.simplon_project.skillhub.skillhub.course.adapter.in.web.response.InitVideoResponse;
import com.simplon_project.skillhub.skillhub.course.application.port.in.InitVideoInChapterPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/course/{courseId}/sections/{sectionId}/chapters/{chapterId}/video")
public class CourseSectionChapterController {

    private final InitVideoInChapterPort initVideoInChapterPort;
//    private final ConfirmVideoUploadPort confirmVideoUploadPort;

    @PostMapping("/init")
    @Operation(description = "Init a video upload for a chapter (Vimeo). Creates Video in PENDING and returns upload info.")
    @ResponseStatus(HttpStatus.CREATED)
    public InitVideoResponse initVideo(
            @Parameter(description = "course id", required = true) @PathVariable String courseId,
            @Parameter(description = "section id", required = true) @PathVariable String sectionId,
            @Parameter(description = "chapter id", required = true) @PathVariable String chapterId,
            @RequestBody(required = false) @Valid InitVideoInChapterRequest request
    ) {
        // request may be null if you choose to init with no payload
        var command = request.mapToCommand(courseId, sectionId, chapterId);
        var init = initVideoInChapterPort.init(command);
        return InitVideoResponseMapper.mapToResponse(init);
    }

//    @PostMapping("/confirm")
//    @Operation(description = "Confirm that upload is finished. Sets status to PROCESSING and starts server-side polling.")
//    @ResponseStatus(HttpStatus.OK)
//    public InitVideoResponse confirmVideo(
//            @Parameter(description = "course id", required = true) @PathVariable String courseId,
//            @Parameter(description = "section id", required = true) @PathVariable String sectionId,
//            @Parameter(description = "chapter id", required = true) @PathVariable String chapterId,
//            @RequestBody @Valid @NotNull ConfirmVideoInChapterRequest request
//    ) {
//        var command = request.toCommand(courseId, sectionId, chapterId);
//        var updatedVideo = confirmVideoUploadPort.confirm(command);
//        return VideoResponseMapper.mapToVideoResponse(updatedVideo);
//    }
}