package com.simplon_project.skillhub.skillhub.cours.adapter.in.web;

import com.simplon_project.skillhub.skillhub.cours.adapter.in.web.mapper.CreateCourseRequestMapper;
import com.simplon_project.skillhub.skillhub.cours.adapter.in.web.request.CreateCourseRequest;
import com.simplon_project.skillhub.skillhub.cours.adapter.in.web.response.CoursResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cours")
public class CoursController {
    private final CreateCourseRequestMapper createCourseRequestMapper;


    public CoursResponse createCours(@RequestBody @Valid @NotNull CreateCourseRequest request) {
        var course = createCourseRequestMapper.toDomain(request);
//        validateCoursPort.validate(course);
//        return coursReqponseMapper.toDto(createCoursPort.creaCours(course));
        return null;
    }
}
