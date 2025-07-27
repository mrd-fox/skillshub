package com.simplon_project.skillhub.skillhub.course.adapter.in.web;

import com.simplon_project.skillhub.skillhub.course.adapter.in.web.mapper.CourseResponseMapper;
import com.simplon_project.skillhub.skillhub.course.adapter.in.web.request.CreateChapterRequest;
import com.simplon_project.skillhub.skillhub.course.adapter.in.web.request.CreateCourseRequest;
import com.simplon_project.skillhub.skillhub.course.adapter.in.web.response.CourseResponse;
import com.simplon_project.skillhub.skillhub.course.application.port.in.CreateCoursePort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/course")
public class CourseController {
    //    private final CreateCourseRequestMapper createCourseRequestMapper;
    private final CreateCoursePort createCoursePort;
//    private final CreateCourseResponseMapper createCourseResponseMapper;
//    private final CreateChapterPort createChapterPort;

    @PostMapping
    @Operation(description = "Create a draft of course")
    @ResponseStatus(HttpStatus.CREATED)
    //security?
    public CourseResponse createCourse(@RequestBody @Valid @NotNull CreateCourseRequest request) {
        var command = request.toCourseCommand();
        return CourseResponseMapper.mapToCourseResponse(createCoursePort.createCourse(command));
    }

    @PostMapping("courses/{courseId}/sections/{sectionId}/chapters")
    @Operation(description = "Create a draft of chapter")
    @ResponseStatus(HttpStatus.CREATED)
    public void createChapter(
            @Parameter(description = "course id", required = true) @PathVariable String courseId,
            @Parameter(description = "section id", required = true) @PathVariable String sectionId,
            @RequestBody @Valid @NotNull CreateChapterRequest request
    ) {
        var chapterCommand = request.toChapterCommand(courseId, sectionId);
        //todo
        //chapterCommand.mapToResponse;
        createChapterPort.createChapter(chapterCommand);
    }
}
