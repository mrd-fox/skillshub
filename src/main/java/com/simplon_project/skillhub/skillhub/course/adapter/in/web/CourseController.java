package com.simplon_project.skillhub.skillhub.course.adapter.in.web;

import com.simplon_project.skillhub.skillhub.course.adapter.in.web.mapper.CourseResponseMapper;
import com.simplon_project.skillhub.skillhub.course.adapter.in.web.request.CreateChapterRequest;
import com.simplon_project.skillhub.skillhub.course.adapter.in.web.request.CreateCourseRequest;
import com.simplon_project.skillhub.skillhub.course.adapter.in.web.request.CreateSectionRequest;
import com.simplon_project.skillhub.skillhub.course.adapter.in.web.response.CourseResponse;
import com.simplon_project.skillhub.skillhub.course.application.port.in.CreateChapterPort;
import com.simplon_project.skillhub.skillhub.course.application.port.in.CreateCoursePort;
import com.simplon_project.skillhub.skillhub.course.application.port.in.CreateSectionPort;
import com.simplon_project.skillhub.skillhub.course.application.port.in.GetCoursePort;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.GetCourseCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/course")
public class CourseController {
    private final CreateCoursePort createCoursePort;
    private final CreateChapterPort createChapterPort;
    private final GetCoursePort getCoursePort;
    private final CreateSectionPort createSectionPort;

    @PostMapping
    @Operation(description = "Create a draft of course")
    @ResponseStatus(HttpStatus.CREATED)
    public CourseResponse createCourse(
            @RequestBody @Valid @NotNull CreateCourseRequest request,
            @RequestHeader(name = "X-User-Id", required = false) String externalAuthorId,
            @RequestHeader(name = "X-User-Roles", required = false) String userRoles
    ) {
        log.info("📥 Incoming createCourse request: {}", request);
        // 🧩 Extract headers (for debugging security propagation)
        log.info("👤 authUserId: {}", externalAuthorId != null ? externalAuthorId : "❌ missing");
        log.info("🧩 X-User-Roles: {}", userRoles != null ? userRoles : "❌ missing");
        var command = request.toCourseCommand(externalAuthorId, userRoles);
        var course = createCoursePort.createCourse(command);
        return CourseResponseMapper.mapToCourseResponse(course);
    }

    @PostMapping("courses/{courseId}/sections/{sectionId}/chapters")
    @Operation(description = "Create a draft of chapter")
    @ResponseStatus(HttpStatus.CREATED)
    public CourseResponse createChapter(
            @Parameter(description = "course id", required = true) @PathVariable String courseId,
            @Parameter(description = "section id", required = true) @PathVariable String sectionId,
            @RequestBody @Valid @NotNull CreateChapterRequest request
    ) {
        var command = request.toChapterCommand(courseId, sectionId);
        return CourseResponseMapper.mapToCourseResponse(createChapterPort.createChapter(command));
    }

    @PostMapping("courses/{courseId}/sections")
    @Operation(description = "Create a draft of section")
    @ResponseStatus(HttpStatus.CREATED)
    public CourseResponse createSection(
            @Parameter(description = "course id", required = true) @PathVariable String courseId,
            @RequestBody @Valid @NotNull CreateSectionRequest request
    ) {
        var command = request.toSectionCommand(courseId, null);
        return CourseResponseMapper.mapToCourseResponse(createSectionPort.createSection(command));
    }

    @GetMapping
    public List<CourseResponse> getCourses(
            @RequestHeader(name = "X-User-Id", required = false) String externalUserId,
            @RequestHeader(name = "X-User-Roles", required = false) String userRolesCsv    ){
        var command = GetCourseCommand.of(externalUserId, userRolesCsv);
        var courses = getCoursePort.getCourse(command);
        return CourseResponseMapper.mapToCourseResponses(courses);
    }
}
