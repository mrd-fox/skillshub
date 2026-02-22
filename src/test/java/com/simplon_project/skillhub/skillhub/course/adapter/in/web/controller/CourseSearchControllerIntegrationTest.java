package com.simplon_project.skillhub.skillhub.course.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.CourseEntity;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.repository.JpaCourseRepository;
import com.simplon_project.skillhub.skillhub.course.domain.enums.CourseStatusEnum;
import com.simplon_project.skillhub.skillhub.user.integrationTests.DatabaseTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("CourseSearchController Integration Tests")
class CourseSearchControllerIntegrationTest extends DatabaseTestConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JpaCourseRepository courseJpaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID courseId1;
    private UUID courseId2;
    private UUID softDeletedCourseId;

    @BeforeEach
    void setUp() {
        courseJpaRepository.deleteAll();

        courseId1 = UUID.randomUUID();
        CourseEntity course1 = CourseEntity.builder()
                .courseId(EntityId.of(courseId1))
                .title("Integration Test Course 1")
                .description("First test course")
                .price(100L)
                .status(CourseStatusEnum.PUBLISHED)
                .externalUserId("test-user-1")
                .build();

        courseId2 = UUID.randomUUID();
        CourseEntity course2 = CourseEntity.builder()
                .courseId(EntityId.of(courseId2))
                .title("Integration Test Course 2")
                .description("Second test course")
                .price(200L)
                .status(CourseStatusEnum.DRAFT)
                .externalUserId("test-user-2")
                .build();

        softDeletedCourseId = UUID.randomUUID();
        CourseEntity softDeletedCourse = CourseEntity.builder()
                .courseId(EntityId.of(softDeletedCourseId))
                .title("Soft Deleted Course")
                .description("This course is soft deleted")
                .price(300L)
                .status(CourseStatusEnum.PUBLISHED)
                .externalUserId("test-user-3")
                .deletedAt(Instant.now())
                .build();

        courseJpaRepository.save(course1);
        courseJpaRepository.save(course2);
        courseJpaRepository.save(softDeletedCourse);
    }

    @Nested
    @DisplayName("POST /api/courses/search")
    class SearchCoursesByIds {

        @Test
        @DisplayName("should return 200 and JSON array with 2 courses when both IDs exist")
        void searchCoursesByIds_withValidIds_shouldReturn200AndTwoCourses() throws Exception {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("ids", List.of(courseId1.toString(), courseId2.toString()));

            mockMvc.perform(post("/api/courses/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id").exists())
                    .andExpect(jsonPath("$[0].title").exists())
                    .andExpect(jsonPath("$[0].price").exists())
                    .andExpect(jsonPath("$[1].id").exists())
                    .andExpect(jsonPath("$[1].title").exists())
                    .andExpect(jsonPath("$[1].price").exists());
        }

        @Test
        @DisplayName("should return 400 and problem+json when UUID is invalid")
        void searchCoursesByIds_withInvalidUuid_shouldReturn400AndProblemJson() throws Exception {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("ids", List.of("invalid-uuid", courseId1.toString()));

            mockMvc.perform(post("/api/courses/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType("application/problem+json"))
                    .andExpect(jsonPath("$.title").exists());
        }

        @Test
        @DisplayName("should return 400 and problem+json when ids is null")
        void searchCoursesByIds_withNullIds_shouldReturn400AndProblemJson() throws Exception {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("ids", null);

            mockMvc.perform(post("/api/courses/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType("application/problem+json"))
                    .andExpect(jsonPath("$.title").exists());
        }

        @Test
        @DisplayName("should return 400 and problem+json when ids is empty list")
        void searchCoursesByIds_withEmptyIds_shouldReturn400AndProblemJson() throws Exception {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("ids", List.of());

            mockMvc.perform(post("/api/courses/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType("application/problem+json"))
                    .andExpect(jsonPath("$.title").exists());
        }

        @Test
        @DisplayName("should return empty array when no courses match the IDs")
        void searchCoursesByIds_withNonExistentIds_shouldReturnEmptyArray() throws Exception {
            UUID nonExistentId1 = UUID.randomUUID();
            UUID nonExistentId2 = UUID.randomUUID();

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("ids", List.of(nonExistentId1.toString(), nonExistentId2.toString()));

            mockMvc.perform(post("/api/courses/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("should not return soft-deleted courses")
        void searchCoursesByIds_withSoftDeletedCourseId_shouldNotReturnIt() throws Exception {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("ids", List.of(courseId1.toString(), softDeletedCourseId.toString()));

            String responseJson = mockMvc.perform(post("/api/courses/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(courseId1.toString()))
                    .andExpect(jsonPath("$[0].price").value(100))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertThat(responseJson).doesNotContain(softDeletedCourseId.toString());
            assertThat(responseJson).doesNotContain("Soft Deleted Course");
        }

        @Test
        @DisplayName("should return only existing courses when mix of valid and invalid IDs")
        void searchCoursesByIds_withMixOfValidAndNonExistentIds_shouldReturnOnlyExisting() throws Exception {
            UUID nonExistentId = UUID.randomUUID();

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("ids", List.of(courseId1.toString(), nonExistentId.toString(), courseId2.toString()));

            mockMvc.perform(post("/api/courses/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        @DisplayName("should return courses with correct structure and data")
        void searchCoursesByIds_shouldReturnCoursesWithCorrectData() throws Exception {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("ids", List.of(courseId1.toString()));

            mockMvc.perform(post("/api/courses/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(courseId1.toString()))
                    .andExpect(jsonPath("$[0].title").value("Integration Test Course 1"))
                    .andExpect(jsonPath("$[0].description").value("First test course"))
                    .andExpect(jsonPath("$[0].price").value(100))
                    .andExpect(jsonPath("$[0].status").value("PUBLISHED"))
                    .andExpect(jsonPath("$[0].createdAt").exists())
                    .andExpect(jsonPath("$[0].updatedAt").exists());
        }
    }
}

