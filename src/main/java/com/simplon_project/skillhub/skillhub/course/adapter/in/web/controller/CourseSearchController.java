package com.simplon_project.skillhub.skillhub.course.adapter.in.web.controller;

import com.simplon_project.skillhub.skillhub.course.adapter.in.web.mapper.CourseResponseMapper;
import com.simplon_project.skillhub.skillhub.course.adapter.in.web.request.SearchCoursesByIdsRequest;
import com.simplon_project.skillhub.skillhub.course.adapter.in.web.response.CourseSummaryResponse;
import com.simplon_project.skillhub.skillhub.course.application.port.in.SearchCoursesByIdsPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping("/api/courses")
@Tag(name = "Course Search", description = "Batch course search for student dashboard")
public class CourseSearchController {

    private final SearchCoursesByIdsPort searchCoursesByIdsPort;

    @Operation(
            summary = "Search courses by IDs (student dashboard)",
            description = "Fetch multiple enrolled courses by their IDs. Returns lightweight summaries without sections/chapters/videos for grid display."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Courses retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = CourseSummaryResponse.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request - null/empty IDs list or invalid UUID format",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = Problem.class)
                    )
            )
    })
    @PostMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public List<CourseSummaryResponse> searchCoursesByIds(
            @Parameter(description = "External user ID (Keycloak UUID)", required = true, example = "9a5a94e5-04b2-47b8-9ef2-4426d1b640b2")
            @RequestHeader("X-User-Id") String externalUserIdRaw,
            @RequestBody @Valid @NotNull SearchCoursesByIdsRequest request
    ) {
        var command = request.mapToCommand(externalUserIdRaw);
        var summaries = searchCoursesByIdsPort.searchByIds(command);

        return CourseResponseMapper.mapToCourseSummaryResponses(summaries);
    }
}



