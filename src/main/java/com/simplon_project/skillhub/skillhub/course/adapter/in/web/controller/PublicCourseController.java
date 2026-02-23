package com.simplon_project.skillhub.skillhub.course.adapter.in.web.controller;

import com.simplon_project.skillhub.skillhub.course.adapter.in.web.mapper.PublicCourseResponseMapper;
import com.simplon_project.skillhub.skillhub.course.adapter.in.web.response.PublicCourseDetailResponse;
import com.simplon_project.skillhub.skillhub.course.adapter.in.web.response.PublicCourseListItemResponse;
import com.simplon_project.skillhub.skillhub.course.application.port.in.GetPublicCourseDetailPort;
import com.simplon_project.skillhub.skillhub.course.application.port.in.ListPublicCoursesPort;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.GetPublicCourseDetailCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/courses")
public class PublicCourseController {

    private final ListPublicCoursesPort listPublicCoursesPort;
    private final GetPublicCourseDetailPort getPublicCourseDetailPort;

    @Operation(
            summary = "Public catalog: list courses",
            description = "Metadata only. No authentication required."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK (may be an empty list)",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PublicCourseListItemResponse.class))
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
    @GetMapping
    public List<PublicCourseListItemResponse> listPublicCourses() {

        var courses = listPublicCoursesPort.listPublicCourses();
        return PublicCourseResponseMapper.mapToResponses(courses);
    }

    @Operation(
            summary = "Public catalog: get course detail",
            description = "Public outline only (sections and chapters). No authentication required."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PublicCourseDetailResponse.class)
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
    public PublicCourseDetailResponse getPublicCourseDetail(
            @Parameter(description = "course id", required = true)
            @PathVariable String courseId
    ) {
        var command = GetPublicCourseDetailCommand.of(courseId);
        var course = getPublicCourseDetailPort.getPublicCourseDetail(command);
        return PublicCourseResponseMapper.mapToResponse(course);
    }
}