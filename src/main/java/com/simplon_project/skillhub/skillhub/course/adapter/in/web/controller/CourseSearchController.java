package com.simplon_project.skillhub.skillhub.course.adapter.in.web.controller;

import com.simplon_project.skillhub.skillhub.course.adapter.in.web.mapper.CourseResponseMapper;
import com.simplon_project.skillhub.skillhub.course.adapter.in.web.request.SearchCoursesByIdsRequest;
import com.simplon_project.skillhub.skillhub.course.adapter.in.web.response.CourseResponse;
import com.simplon_project.skillhub.skillhub.course.application.port.in.SearchCoursesByIdsPort;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.SearchCoursesByIdsCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
public class CourseSearchController {

    private final SearchCoursesByIdsPort searchCoursesByIdsPort;

    @Operation(
            summary = "Search courses by IDs",
            description = "Fetch multiple courses by their IDs in a single request. Returns courses in the order of the database query (by creation date descending)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Courses retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = CourseResponse.class))
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
    public List<CourseResponse> searchCoursesByIds(
            @RequestHeader(name = "X-User-Id", required = false) String externalAuthorId,
            @RequestHeader(name = "X-User-Roles", required = false) String userRoles,
            @RequestBody @Valid @NotNull SearchCoursesByIdsRequest request
    ) {

        SearchCoursesByIdsCommand command = SearchCoursesByIdsCommand.of(request.ids());
        var courses = searchCoursesByIdsPort.searchByIds(command);

        return CourseResponseMapper.mapToCourseResponses(courses);
    }
}

