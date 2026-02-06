package com.simplon_project.skillhub.skillhub.course.adapter.in.web.controller;

import com.simplon_project.skillhub.skillhub.course.adapter.in.web.mapper.CourseResponseMapper;
import com.simplon_project.skillhub.skillhub.course.adapter.in.web.request.CreateChapterRequest;
import com.simplon_project.skillhub.skillhub.course.adapter.in.web.request.CreateCourseRequest;
import com.simplon_project.skillhub.skillhub.course.adapter.in.web.request.CreateSectionRequest;
import com.simplon_project.skillhub.skillhub.course.adapter.in.web.request.UpdateCourseRequest;
import com.simplon_project.skillhub.skillhub.course.adapter.in.web.response.CourseResponse;
import com.simplon_project.skillhub.skillhub.course.application.port.in.*;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.GetCourseCommand;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.GetCoursesCommand;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.PublishCourseCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.zalando.problem.Problem;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/course")
public class CourseController {
    private final CreateCoursePort createCoursePort;
    private final UpdateCoursePort updateCoursePort;
    private final CreateChapterPort createChapterPort;
    private final GetCoursePort getCoursePort;
    private final CreateSectionPort createSectionPort;
    private final PublishCoursePort publishCoursePort;

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

    @PutMapping("/{courseId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Update a course (PATCH-like payload)",
            description = "Updates course fields and optionally applies sections/chapters patch. "
                    + "If section/chapter id is null => create. If id is present => update."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Course updated",
                    content = @Content(schema = @Schema(implementation = CourseResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Missing user context"),
            @ApiResponse(responseCode = "403", description = "Not allowed"),
            @ApiResponse(responseCode = "404", description = "Course not found")
    })
    public CourseResponse updateCourse(
            @Parameter(description = "Course id", required = true)
            @PathVariable @NotBlank String courseId,

            @Parameter(description = "Authenticated user external id (injected by Gateway)", required = true)
            @RequestHeader("X-User-Id") @NotBlank String externalAuthorId,

            @Parameter(description = "Authenticated user roles CSV (injected by Gateway)", required = true)
            @RequestHeader("X-User-Roles") @NotBlank String rawRoles,

            @RequestBody @Valid @NotNull UpdateCourseRequest request
    ) {

        var command = request.mapToCommand(courseId, externalAuthorId, rawRoles);
        var updatedCourse = updateCoursePort.updateCourse(command);

        return CourseResponseMapper.mapToCourseResponse(updatedCourse);
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
            @RequestHeader(name = "X-User-Roles", required = false) String userRolesCsv) {
        var command = GetCoursesCommand.of(externalUserId, userRolesCsv);
        var courses = getCoursePort.getCourses(command);
        return CourseResponseMapper.mapToCourseResponses(courses);
    }

    @Operation(
            summary = "Get course detail",
            description = "Returns full course outline (sections and chapters). \" +\n" +
                    "                \"If authenticated, includes Vimeo video links. Otherwise, returns public view only."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CourseResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Course not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = Problem.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = Problem.class)
                    )
            )
    })
    @GetMapping("/{courseId}")
    public CourseResponse getPublicCourseDetail(
            @RequestHeader(name = "X-User-Id", required = false) String externalUserId,
            @RequestHeader(name = "X-User-Roles", required = false) String userRolesCsv,
            @Parameter(description = "course id", required = true) @PathVariable String courseId
    ) {
        var command = GetCourseCommand.of(externalUserId, userRolesCsv, courseId);
        var course = getCoursePort.getCourse(command);
        return CourseResponseMapper.mapToCourseResponse(course);
    }

    @Operation(
            summary = "Publish a course (submit for validation)",
            description = "Submits a course for validation. The course status will be set to WAITING_VALIDATION. " +
                    "Only ADMIN or course owner (TUTOR) can publish. " +
                    "All chapters must have videos in READY status."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Course successfully submitted for validation",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CourseResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing user context (X-User-Id or X-User-Roles headers)",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = Problem.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Unauthorized - insufficient role or not course owner",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = Problem.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Course not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = Problem.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Course already submitted (WAITING_VALIDATION or PUBLISHED)",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = Problem.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Course not publishable - missing videos or videos not ready",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = Problem.class)
                    )
            )
    })
    @PostMapping("/{courseId}/publish")
    @ResponseStatus(HttpStatus.OK)
    public CourseResponse publishCourse(
            @Parameter(description = "Course id", required = true)
            @PathVariable @NotBlank String courseId,

            @Parameter(description = "Authenticated user external id (injected by Gateway)", required = true)
            @RequestHeader("X-User-Id") @NotBlank String externalUserId,

            @Parameter(description = "Authenticated user roles CSV (injected by Gateway)", required = true)
            @RequestHeader("X-User-Roles") @NotBlank String rawRoles
    ) {
        log.info("📤 Incoming publishCourse request for courseId: {}", courseId);
        log.info("👤 X-User-Id: {}", externalUserId);
        log.info("🔐 X-User-Roles: {}", rawRoles);

        var command = PublishCourseCommand.of(courseId, externalUserId, rawRoles);
        var publishedCourse = publishCoursePort.publishCourse(command);

        log.info("✅ Course {} published successfully with status: {}", courseId, publishedCourse.getStatus());
        return CourseResponseMapper.mapToCourseResponse(publishedCourse);
    }

}
