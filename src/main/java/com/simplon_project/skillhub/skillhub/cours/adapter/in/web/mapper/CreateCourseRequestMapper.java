package com.simplon_project.skillhub.skillhub.cours.adapter.in.web.mapper;

import com.simplon_project.skillhub.skillhub.cours.adapter.in.web.request.CreateCourseRequest;
import com.simplon_project.skillhub.skillhub.cours.adapter.in.web.request.CreateSectionRequest;
import com.simplon_project.skillhub.skillhub.cours.domain.enums.CoursStatusEnum;
import com.simplon_project.skillhub.skillhub.cours.domain.model.Course;
import com.simplon_project.skillhub.skillhub.cours.domain.model.Section;
import org.mapstruct.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface CreateCourseRequestMapper {
    @Mapping(target = "sections", qualifiedByName = "mapToDomainSections")
    Course toDomain(CreateCourseRequest createCourseRequest);

    @Named("mapToDomainSections")
    default List<Section> mapToDomainSections(List<CreateSectionRequest> reqSections) {
        if (reqSections != null && !reqSections.isEmpty()) {
            return reqSections.stream().map(section -> Section.builder().build()).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @AfterMapping
    default void fixStatus(@MappingTarget Course course) {
        course.setStatusCours(CoursStatusEnum.CREATED);
    }

}
