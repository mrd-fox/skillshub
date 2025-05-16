package com.simplon_project.skillhub.skillhub.course.adapter.in.web.mapper;

import com.simplon_project.skillhub.skillhub.course.adapter.in.web.request.CreateChapterRequest;
import com.simplon_project.skillhub.skillhub.course.adapter.in.web.request.CreateCourseRequest;
import com.simplon_project.skillhub.skillhub.course.adapter.in.web.request.CreateSectionRequest;
import com.simplon_project.skillhub.skillhub.course.domain.enums.CourseStatusEnum;
import com.simplon_project.skillhub.skillhub.course.domain.model.Chapter;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import com.simplon_project.skillhub.skillhub.course.domain.model.Section;
import org.mapstruct.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface CreateCourseRequestMapper {
    @Mapping(target = "sections", source = "sections", qualifiedByName = "mapToDomainSections")
    Course toDomain(CreateCourseRequest createCourseRequest);

    @Named("mapToDomainSections")
    default List<Section> mapToDomainSections(List<CreateSectionRequest> reqSections) {

        if (reqSections == null || reqSections.isEmpty()) {
            return new ArrayList<>();
        }

        return reqSections.stream()
                .map(req -> Section.builder()
                        .title(req.title())
                        .chapters(mapToDomainChapters(req.chapters()))
                        .build())
                .collect(Collectors.toList());
    }

    default List<Chapter> mapToDomainChapters(List<CreateChapterRequest> reqChapters) {
        if (reqChapters == null || reqChapters.isEmpty()) {
            return new ArrayList<>();
        }

        return reqChapters.stream()
                .map(req -> Chapter.builder()
                        .title(req.title())
                        .videoUrl(req.videoUrl())
                        .build())
                .collect(Collectors.toList());
    }

    @AfterMapping
    default void fixStatus(@MappingTarget Course course) {
        course.setStatus(CourseStatusEnum.DRAFT);
    }

}
