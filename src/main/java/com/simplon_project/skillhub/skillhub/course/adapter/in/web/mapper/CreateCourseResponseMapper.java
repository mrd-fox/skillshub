package com.simplon_project.skillhub.skillhub.course.adapter.in.web.mapper;

import com.simplon_project.skillhub.skillhub.course.adapter.common.mapper.GenericDtoMapper;
import com.simplon_project.skillhub.skillhub.course.adapter.in.web.response.CourseResponse;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import org.mapstruct.Builder;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface CreateCourseResponseMapper extends GenericDtoMapper<Course, CourseResponse> {
}


