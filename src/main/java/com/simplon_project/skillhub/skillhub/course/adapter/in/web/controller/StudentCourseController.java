package com.simplon_project.skillhub.skillhub.course.adapter.in.web.controller;

import com.simplon_project.skillhub.skillhub.course.adapter.in.web.mapper.CourseResponseMapper;
import com.simplon_project.skillhub.skillhub.course.adapter.in.web.response.CourseResponse;
import com.simplon_project.skillhub.skillhub.course.application.port.in.GetStudentCoursePort;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.GetStudentCourseCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.zalando.problem.Problem;

/**
 * REST controller for student-specific course access.
 * Enforces enrollment-based access control.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/student/courses")
@Tag(name = "Student Courses", description = "Endpoints for student course access")
public class StudentCourseController {

    private final GetStudentCoursePort getStudentCoursePort;

    @Operation(
            summary = "Get course detail for student",
            description = "Retrieve full course tree including sections, chapters, and video playback details. " +
                    "Requires enrollment in the course. Only PUBLISHED courses are accessible."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Course retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CourseResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - missing or invalid user context",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = Problem.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - user not enrolled or lacks STUDENT role",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = Problem.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Course not found, soft-deleted, or not accessible",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = Problem.class)
                    )
            )
    })
    @GetMapping("/{courseId}")
    @ResponseStatus(HttpStatus.OK)
    public CourseResponse getCourse(
            @Parameter(description = "Course ID", required = true, example = "9a5a94e5-04b2-47b8-9ef2-4426d1b640b2")
            @PathVariable String courseId,

            @Parameter(description = "External user ID (Keycloak UUID)", required = true, example = "9a5a94e5-04b2-47b8-9ef2-4426d1b640b2")
            @RequestHeader("X-User-Id") String externalUserId,

            @Parameter(description = "Comma-separated user roles", required = true, example = "STUDENT")
            @RequestHeader("X-User-Roles") String userRolesCsv
    ) {
        log.info("Student course request: courseId={}, userId={}", courseId, externalUserId);

        var command = GetStudentCourseCommand.of(courseId, externalUserId, userRolesCsv);
        var course = getStudentCoursePort.get(command);

        return CourseResponseMapper.mapToCourseResponse(course);
    }
}

