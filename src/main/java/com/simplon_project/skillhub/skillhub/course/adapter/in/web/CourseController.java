package com.simplon_project.skillhub.skillhub.course.adapter.in.web;

import com.simplon_project.skillhub.skillhub.course.adapter.common.mapper.CycleAvoidingMappingContext;
import com.simplon_project.skillhub.skillhub.course.adapter.in.web.mapper.CreateCourseRequestMapper;
import com.simplon_project.skillhub.skillhub.course.adapter.in.web.mapper.CreateCourseResponseMapper;
import com.simplon_project.skillhub.skillhub.course.adapter.in.web.request.CreateCourseRequest;
import com.simplon_project.skillhub.skillhub.course.adapter.in.web.response.CourseResponse;
import com.simplon_project.skillhub.skillhub.course.application.port.in.CreateCoursePort;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/course")
public class CourseController {
    private final CreateCourseRequestMapper createCourseRequestMapper;
    private final CreateCoursePort createCoursePort;
    private final CreateCourseResponseMapper createCourseResponseMapper;

    @PostMapping
    @Operation(description = "Create a draft of course")
    @ResponseStatus(HttpStatus.CREATED)
    //security?
    public CourseResponse createCourse(@RequestBody @Valid @NotNull CreateCourseRequest request) {
        var course = createCourseRequestMapper.toDomain(request);
        var created = createCoursePort.createCourse(course);

        return createCourseResponseMapper.toDto(created, new CycleAvoidingMappingContext());
    }
}
