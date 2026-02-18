package com.simplon_project.skillhub.skillhub.course.adapter.in.web.controller;

import com.simplon_project.skillhub.skillhub.course.application.exception.CourseNotFoundException;
import com.simplon_project.skillhub.skillhub.course.application.port.in.GetStudentCoursePort;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.GetStudentCourseCommand;
import com.simplon_project.skillhub.skillhub.course.domain.enums.CourseStatusEnum;
import com.simplon_project.skillhub.skillhub.course.domain.exception.CourseNotAccessibleException;
import com.simplon_project.skillhub.skillhub.course.domain.exception.MissingUserContextException;
import com.simplon_project.skillhub.skillhub.course.domain.exception.StudentNotEnrolledException;
import com.simplon_project.skillhub.skillhub.course.domain.exception.UnauthorizedCourseAccessException;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentCourseController.class)
@DisplayName("StudentCourseController Integration Tests")
class StudentCourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetStudentCoursePort getStudentCoursePort;

    private static final String BASE_URL = "/api/student/courses";
    private static final String COURSE_ID = "9a5a94e5-04b2-47b8-9ef2-4426d1b640b2";
    private static final String USER_ID = "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d";
    private static final String COURSE_TITLE = "Test Course";

    private Course buildPublishedCourse() {
        return Course.builder()
                .id(Id.of(COURSE_ID))
                .title(COURSE_TITLE)
                .description("Test Description")
                .status(CourseStatusEnum.PUBLISHED)
                .externalUserId("author-123")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Successful Requests")
    class SuccessfulRequests {

        @Test
        @DisplayName("Should return 200 and course details for enrolled student")
        void shouldReturnCourseForEnrolledStudent() throws Exception {
            // GIVEN
            Course course = buildPublishedCourse();
            when(getStudentCoursePort.get(any(GetStudentCourseCommand.class)))
                    .thenReturn(course);

            // WHEN + THEN
            mockMvc.perform(get(BASE_URL + "/" + COURSE_ID)
                            .header("X-User-Id", USER_ID)
                            .header("X-User-Roles", "STUDENT"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json"))
                    .andExpect(jsonPath("$.id").value(COURSE_ID))
                    .andExpect(jsonPath("$.title").value(COURSE_TITLE))
                    .andExpect(jsonPath("$.status").value("PUBLISHED"));

            verify(getStudentCoursePort).get(any(GetStudentCourseCommand.class));
        }

        @Test
        @DisplayName("Should return 200 for tutor acting as student")
        void shouldReturnCourseForTutor() throws Exception {
            // GIVEN
            Course course = buildPublishedCourse();
            when(getStudentCoursePort.get(any(GetStudentCourseCommand.class)))
                    .thenReturn(course);

            // WHEN + THEN
            mockMvc.perform(get(BASE_URL + "/" + COURSE_ID)
                            .header("X-User-Id", USER_ID)
                            .header("X-User-Roles", "TUTOR"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(COURSE_ID));

            verify(getStudentCoursePort).get(any(GetStudentCourseCommand.class));
        }

        @Test
        @DisplayName("Should return 200 for user with STUDENT,TUTOR roles")
        void shouldReturnCourseForStudentAndTutor() throws Exception {
            // GIVEN
            Course course = buildPublishedCourse();
            when(getStudentCoursePort.get(any(GetStudentCourseCommand.class)))
                    .thenReturn(course);

            // WHEN + THEN
            mockMvc.perform(get(BASE_URL + "/" + COURSE_ID)
                            .header("X-User-Id", USER_ID)
                            .header("X-User-Roles", "STUDENT,TUTOR"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(COURSE_ID));

            verify(getStudentCoursePort).get(any(GetStudentCourseCommand.class));
        }
    }

    @Nested
    @DisplayName("Missing Headers - 401 Unauthorized")
    class MissingHeaders {

        @Test
        @DisplayName("Should return 401 when X-User-Id header is missing")
        void shouldReturn401WhenUserIdMissing() throws Exception {
            // WHEN + THEN
            mockMvc.perform(get(BASE_URL + "/" + COURSE_ID)
                            .header("X-User-Roles", "STUDENT"))
                    .andExpect(status().isBadRequest()); // Spring returns 400 for missing required header

            verify(getStudentCoursePort, never()).get(any());
        }

        @Test
        @DisplayName("Should return 401 when X-User-Roles header is missing")
        void shouldReturn401WhenRolesMissing() throws Exception {
            // WHEN + THEN
            mockMvc.perform(get(BASE_URL + "/" + COURSE_ID)
                            .header("X-User-Id", USER_ID))
                    .andExpect(status().isBadRequest()); // Spring returns 400 for missing required header

            verify(getStudentCoursePort, never()).get(any());
        }

        @Test
        @DisplayName("Should return 401 when user context is invalid in command")
        void shouldReturn401WhenContextInvalid() throws Exception {
            // GIVEN
            when(getStudentCoursePort.get(any(GetStudentCourseCommand.class)))
                    .thenThrow(new MissingUserContextException("Invalid user context"));

            // WHEN + THEN
            mockMvc.perform(get(BASE_URL + "/" + COURSE_ID)
                            .header("X-User-Id", "invalid-id")
                            .header("X-User-Roles", "STUDENT"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().contentType("application/problem+json"))
                    .andExpect(jsonPath("$.title").value("Unauthorized"));

            verify(getStudentCoursePort).get(any(GetStudentCourseCommand.class));
        }
    }

    @Nested
    @DisplayName("Authorization - 403 Forbidden")
    class Authorization {

        @Test
        @DisplayName("Should return 403 when user not enrolled")
        void shouldReturn403WhenNotEnrolled() throws Exception {
            // GIVEN
            when(getStudentCoursePort.get(any(GetStudentCourseCommand.class)))
                    .thenThrow(new StudentNotEnrolledException(COURSE_ID));

            // WHEN + THEN
            mockMvc.perform(get(BASE_URL + "/" + COURSE_ID)
                            .header("X-User-Id", USER_ID)
                            .header("X-User-Roles", "STUDENT"))
                    .andExpect(status().isForbidden())
                    .andExpect(content().contentType("application/problem+json"))
                    .andExpect(jsonPath("$.title").value("Forbidden"))
                    .andExpect(jsonPath("$.detail", containsString("enrolled")));

            verify(getStudentCoursePort).get(any(GetStudentCourseCommand.class));
        }

        @Test
        @DisplayName("Should return 403 when user lacks STUDENT/TUTOR role")
        void shouldReturn403WhenWrongRole() throws Exception {
            // GIVEN
            when(getStudentCoursePort.get(any(GetStudentCourseCommand.class)))
                    .thenThrow(new UnauthorizedCourseAccessException("Only STUDENT or TUTOR role allowed"));

            // WHEN + THEN
            mockMvc.perform(get(BASE_URL + "/" + COURSE_ID)
                            .header("X-User-Id", USER_ID)
                            .header("X-User-Roles", "ADMIN"))
                    .andExpect(status().isForbidden())
                    .andExpect(content().contentType("application/problem+json"))
                    .andExpect(jsonPath("$.title").value("Forbidden"));

            verify(getStudentCoursePort).get(any(GetStudentCourseCommand.class));
        }
    }

    @Nested
    @DisplayName("Not Found - 404")
    class NotFound {

        @Test
        @DisplayName("Should return 404 when course not found")
        void shouldReturn404WhenCourseNotFound() throws Exception {
            // GIVEN
            when(getStudentCoursePort.get(any(GetStudentCourseCommand.class)))
                    .thenThrow(new CourseNotFoundException(Id.of(COURSE_ID)));

            // WHEN + THEN
            mockMvc.perform(get(BASE_URL + "/" + COURSE_ID)
                            .header("X-User-Id", USER_ID)
                            .header("X-User-Roles", "STUDENT"))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType("application/problem+json"))
                    .andExpect(jsonPath("$.title").value("Not Found"));

            verify(getStudentCoursePort).get(any(GetStudentCourseCommand.class));
        }

        @Test
        @DisplayName("Should return 404 when course not accessible (not PUBLISHED)")
        void shouldReturn404WhenCourseNotAccessible() throws Exception {
            // GIVEN
            when(getStudentCoursePort.get(any(GetStudentCourseCommand.class)))
                    .thenThrow(new CourseNotAccessibleException(COURSE_ID));

            // WHEN + THEN
            mockMvc.perform(get(BASE_URL + "/" + COURSE_ID)
                            .header("X-User-Id", USER_ID)
                            .header("X-User-Roles", "STUDENT"))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType("application/problem+json"))
                    .andExpect(jsonPath("$.title").value("Not Found"));

            verify(getStudentCoursePort).get(any(GetStudentCourseCommand.class));
        }
    }

    @Nested
    @DisplayName("Invalid Input - 400 Bad Request")
    class InvalidInput {

        @Test
        @DisplayName("Should return 400 when courseId format is invalid")
        void shouldReturn400WhenCourseIdInvalid() throws Exception {
            // WHEN + THEN
            mockMvc.perform(get(BASE_URL + "/invalid-id")
                            .header("X-User-Id", USER_ID)
                            .header("X-User-Roles", "STUDENT"))
                    .andExpect(status().isBadRequest());

            verify(getStudentCoursePort).get(any(GetStudentCourseCommand.class));
        }
    }
}

