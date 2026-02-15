package com.simplon_project.skillhub.skillhub.course.application.usecase;

import com.simplon_project.skillhub.skillhub.course.application.port.in.command.SearchCoursesByIdsCommand;
import com.simplon_project.skillhub.skillhub.course.application.port.out.LoadEnrolledCourseIdsPort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.course.LoadCoursesByIdsPort;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import com.simplon_project.skillhub.skillhub.helpers.builders.CourseBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchCoursesByIdsUseCase Unit Tests")
class SearchCoursesByIdsUseCaseTest {

    @Mock
    private LoadCoursesByIdsPort loadCoursesByIdsPort;

    @Mock
    private LoadEnrolledCourseIdsPort loadEnrolledCourseIdsPort;

    @InjectMocks
    private CourseUseCases useCase;

    private static final UUID EXTERNAL_USER_ID = UUID.randomUUID();
    private static final String EXTERNAL_USER_ID_STRING = EXTERNAL_USER_ID.toString();

    @Nested
    @DisplayName("searchByIds() method")
    class SearchByIdsMethod {

        @Test
        @DisplayName("should call loadCoursesByIdsPort with enrolled course IDs only and return result")
        void searchByIds_shouldCallPortWithEnrolledCoursesAndReturnResult() {
            // GIVEN
            String uuid1 = UUID.randomUUID().toString();
            String uuid2 = UUID.randomUUID().toString();
            List<String> rawIds = List.of(uuid1, uuid2);

            SearchCoursesByIdsCommand command = SearchCoursesByIdsCommand.of(rawIds, EXTERNAL_USER_ID_STRING);

            // User is enrolled in both courses
            Set<Id> enrolledIds = Set.of(Id.of(uuid1), Id.of(uuid2));

            Course course1 = CourseBuilder.aCourse()
                    .withId(uuid1)
                    .withTitle("Course 1")
                    .build();

            Course course2 = CourseBuilder.aCourse()
                    .withId(uuid2)
                    .withTitle("Course 2")
                    .build();

            List<Course> expectedCourses = List.of(course1, course2);

            when(loadEnrolledCourseIdsPort.loadEnrolledCourseIds(EXTERNAL_USER_ID)).thenReturn(enrolledIds);
            when(loadCoursesByIdsPort.loadCoursesByIds(any())).thenReturn(expectedCourses);

            // WHEN
            List<Course> result = useCase.searchByIds(command);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            assertThat(result).isEqualTo(expectedCourses);
            assertThat(result).containsExactly(course1, course2);

            verify(loadEnrolledCourseIdsPort, times(1)).loadEnrolledCourseIds(EXTERNAL_USER_ID);

            ArgumentCaptor<List<Id>> idsCaptor = ArgumentCaptor.forClass(List.class);
            verify(loadCoursesByIdsPort, times(1)).loadCoursesByIds(idsCaptor.capture());

            List<Id> capturedIds = idsCaptor.getValue();
            assertThat(capturedIds).hasSize(2);
        }

        @Test
        @DisplayName("should return empty list when user is not enrolled in any requested courses")
        void searchByIds_shouldReturnEmptyListWhenNotEnrolled() {
            // GIVEN
            String uuid = UUID.randomUUID().toString();
            List<String> rawIds = List.of(uuid);

            SearchCoursesByIdsCommand command = SearchCoursesByIdsCommand.of(rawIds, EXTERNAL_USER_ID_STRING);

            // User has no enrollments
            Set<Id> enrolledIds = Set.of();

            when(loadEnrolledCourseIdsPort.loadEnrolledCourseIds(EXTERNAL_USER_ID)).thenReturn(enrolledIds);

            // WHEN
            List<Course> result = useCase.searchByIds(command);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();

            verify(loadEnrolledCourseIdsPort, times(1)).loadEnrolledCourseIds(EXTERNAL_USER_ID);
            verifyNoInteractions(loadCoursesByIdsPort);
        }

        @Test
        @DisplayName("should filter out non-enrolled courses from requested list")
        void searchByIds_shouldFilterNonEnrolledCourses() {
            // GIVEN
            String uuid1 = UUID.randomUUID().toString();
            String uuid2 = UUID.randomUUID().toString();
            String uuid3 = UUID.randomUUID().toString();
            List<String> rawIds = List.of(uuid1, uuid2, uuid3);

            SearchCoursesByIdsCommand command = SearchCoursesByIdsCommand.of(rawIds, EXTERNAL_USER_ID_STRING);

            // User is only enrolled in uuid1
            Set<Id> enrolledIds = Set.of(Id.of(uuid1));

            Course course = CourseBuilder.aCourse().withId(uuid1).build();
            when(loadEnrolledCourseIdsPort.loadEnrolledCourseIds(EXTERNAL_USER_ID)).thenReturn(enrolledIds);
            when(loadCoursesByIdsPort.loadCoursesByIds(any())).thenReturn(List.of(course));

            // WHEN
            useCase.searchByIds(command);

            // THEN
            verify(loadEnrolledCourseIdsPort, times(1)).loadEnrolledCourseIds(EXTERNAL_USER_ID);

            ArgumentCaptor<List<Id>> idsCaptor = ArgumentCaptor.forClass(List.class);
            verify(loadCoursesByIdsPort, times(1)).loadCoursesByIds(idsCaptor.capture());

            List<Id> capturedIds = idsCaptor.getValue();
            assertThat(capturedIds).hasSize(1);
            assertThat(capturedIds.get(0).asString()).isEqualTo(uuid1);
        }

        @Test
        @DisplayName("should call enrollment port with correct external user ID")
        void searchByIds_shouldCallEnrollmentPortWithCorrectUserId() {
            // GIVEN
            String uuid = UUID.randomUUID().toString();
            SearchCoursesByIdsCommand command = SearchCoursesByIdsCommand.of(List.of(uuid), EXTERNAL_USER_ID_STRING);

            Set<Id> enrolledIds = Set.of(Id.of(uuid));
            Course course = CourseBuilder.aCourse().withId(uuid).build();

            when(loadEnrolledCourseIdsPort.loadEnrolledCourseIds(EXTERNAL_USER_ID)).thenReturn(enrolledIds);
            when(loadCoursesByIdsPort.loadCoursesByIds(any())).thenReturn(List.of(course));

            // WHEN
            useCase.searchByIds(command);

            // THEN
            ArgumentCaptor<UUID> userIdCaptor = ArgumentCaptor.forClass(UUID.class);
            verify(loadEnrolledCourseIdsPort, times(1)).loadEnrolledCourseIds(userIdCaptor.capture());

            UUID capturedUserId = userIdCaptor.getValue();
            assertThat(capturedUserId).isEqualTo(EXTERNAL_USER_ID);
            verifyNoMoreInteractions(loadEnrolledCourseIdsPort);
        }

        @Test
        @DisplayName("should return list in same order as port returns")
        void searchByIds_shouldMaintainOrderFromPort() {
            // GIVEN
            String uuid1 = UUID.randomUUID().toString();
            String uuid2 = UUID.randomUUID().toString();
            String uuid3 = UUID.randomUUID().toString();

            SearchCoursesByIdsCommand command = SearchCoursesByIdsCommand.of(
                    List.of(uuid1, uuid2, uuid3),
                    EXTERNAL_USER_ID_STRING
            );

            // User is enrolled in all three
            Set<Id> enrolledIds = Set.of(Id.of(uuid1), Id.of(uuid2), Id.of(uuid3));

            Course course3 = CourseBuilder.aCourse().withId(uuid3).withTitle("Third").build();
            Course course1 = CourseBuilder.aCourse().withId(uuid1).withTitle("First").build();
            Course course2 = CourseBuilder.aCourse().withId(uuid2).withTitle("Second").build();

            List<Course> coursesFromPort = List.of(course3, course1, course2);

            when(loadEnrolledCourseIdsPort.loadEnrolledCourseIds(EXTERNAL_USER_ID)).thenReturn(enrolledIds);
            when(loadCoursesByIdsPort.loadCoursesByIds(any())).thenReturn(coursesFromPort);

            // WHEN
            List<Course> result = useCase.searchByIds(command);

            // THEN
            assertThat(result).hasSize(3);
            assertThat(result).containsExactly(course3, course1, course2);
            assertThat(result.get(0).getTitle()).isEqualTo("Third");
            assertThat(result.get(1).getTitle()).isEqualTo("First");
            assertThat(result.get(2).getTitle()).isEqualTo("Second");
        }
    }
}

