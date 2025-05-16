package com.simplon_project.skillhub.skillhub.course.adapter.in.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.simplon_project.skillhub.skillhub.course.domain.enums.CourseStatusEnum;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CourseResponse(
        String id,
        String title,
        String description,
//      List<String> keyWords,
        CourseStatusEnum status,
        List<SectionResponse> sections,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
        //user

) {
}
