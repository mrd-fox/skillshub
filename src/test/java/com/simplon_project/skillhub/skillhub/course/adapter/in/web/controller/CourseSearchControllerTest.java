package com.simplon_project.skillhub.skillhub.course.adapter.in.web.controller;

import com.simplon_project.skillhub.skillhub.course.application.port.in.SearchCoursesByIdsPort;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.SearchCoursesByIdsCommand;
import com.simplon_project.skillhub.skillhub.course.domain.enums.CourseStatusEnum;
import com.simplon_project.skillhub.skillhub.course.domain.model.CourseSummary;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourseSearchController.class)
@DisplayName("CourseSearchController - Lightweight Search Tests")
class CourseSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SearchCoursesByIdsPort searchCoursesByIdsPort;

    @Test
    @DisplayName("Should return lightweight course summaries without sections")
    void shouldReturnLightweightCourseSummaries() throws Exception {
        // GIVEN
        var courseId1 = "9a5a94e5-04b2-47b8-9ef2-4426d1b640b2";
        var courseId2 = "8b4c3d2e-1f0a-4b5c-6d7e-8f9a0b1c2d3e";
        var userId = "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d";

        var summary1 = CourseSummary.of(
                Id.of(courseId1),
                "Spring Boot Course",
                "Learn Spring Boot",
                CourseStatusEnum.PUBLISHED,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        var summary2 = CourseSummary.of(
                Id.of(courseId2),
                "Java Course",
                "Learn Java",
                CourseStatusEnum.PUBLISHED,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(searchCoursesByIdsPort.searchByIds(any(SearchCoursesByIdsCommand.class)))
                .thenReturn(List.of(summary1, summary2));

        var requestBody = """
                {
                    "courseIds": ["%s", "%s"]
                }
                """.formatted(courseId1, courseId2);

        // WHEN + THEN
        mockMvc.perform(post("/api/courses/search")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(courseId1))
                .andExpect(jsonPath("$[0].title").value("Spring Boot Course"))
                .andExpect(jsonPath("$[0].description").value("Learn Spring Boot"))
                .andExpect(jsonPath("$[0].status").value("PUBLISHED"))
                .andExpect(jsonPath("$[0].createdAt").exists())
                .andExpect(jsonPath("$[0].updatedAt").exists())
                .andExpect(jsonPath("$[0].sections").doesNotExist())
                .andExpect(jsonPath("$[1].id").value(courseId2))
                .andExpect(jsonPath("$[1].title").value("Java Course"));
    }

    @Test
    @DisplayName("Should return empty list when no enrolled courses")
    void shouldReturnEmptyListWhenNoEnrolledCourses() throws Exception {
        // GIVEN
        var userId = "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d";
        var courseId = "9a5a94e5-04b2-47b8-9ef2-4426d1b640b2";

        when(searchCoursesByIdsPort.searchByIds(any(SearchCoursesByIdsCommand.class)))
                .thenReturn(List.of());

        var requestBody = """
                {
                    "courseIds": ["%s"]
                }
                """.formatted(courseId);

        // WHEN + THEN
        mockMvc.perform(post("/api/courses/search")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}

