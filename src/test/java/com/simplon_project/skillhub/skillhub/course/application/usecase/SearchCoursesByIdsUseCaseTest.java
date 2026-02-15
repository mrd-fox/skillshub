package com.simplon_project.skillhub.skillhub.course.application.usecase;

import com.simplon_project.skillhub.skillhub.course.application.port.in.command.SearchCoursesByIdsCommand;
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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchCoursesByIdsUseCase Unit Tests")
class SearchCoursesByIdsUseCaseTest {

    @Mock
    private LoadCoursesByIdsPort loadCoursesByIdsPort;

    @InjectMocks
    private CourseUseCases useCase;

    @Nested
    @DisplayName("searchByIds() method")
    class SearchByIdsMethod {

        @Test
        @DisplayName("should call loadCoursesByIdsPort with command courseIds and return result")
        void searchByIds_shouldCallPortAndReturnResult() {
            // GIVEN
            String uuid1 = UUID.randomUUID().toString();
            String uuid2 = UUID.randomUUID().toString();
            List<String> rawIds = List.of(uuid1, uuid2);

            SearchCoursesByIdsCommand command = SearchCoursesByIdsCommand.of(rawIds);

            Course course1 = CourseBuilder.aCourse()
                    .withId(uuid1)
                    .withTitle("Course 1")
                    .build();

            Course course2 = CourseBuilder.aCourse()
                    .withId(uuid2)
                    .withTitle("Course 2")
                    .build();

            List<Course> expectedCourses = List.of(course1, course2);

            when(loadCoursesByIdsPort.loadCoursesByIds(any())).thenReturn(expectedCourses);

            // WHEN
            List<Course> result = useCase.searchByIds(command);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            assertThat(result).isEqualTo(expectedCourses);
            assertThat(result).containsExactly(course1, course2);

            ArgumentCaptor<List<Id>> idsCaptor = ArgumentCaptor.forClass(List.class);
            verify(loadCoursesByIdsPort, times(1)).loadCoursesByIds(idsCaptor.capture());

            List<Id> capturedIds = idsCaptor.getValue();
            assertThat(capturedIds).hasSize(2);
            assertThat(capturedIds).isEqualTo(command.courseIds());
            assertThat(capturedIds.get(0).asString()).isEqualTo(uuid1);
            assertThat(capturedIds.get(1).asString()).isEqualTo(uuid2);
        }

        @Test
        @DisplayName("should return empty list when port returns empty list")
        void searchByIds_shouldReturnEmptyListWhenPortReturnsEmpty() {
            // GIVEN
            String uuid = UUID.randomUUID().toString();
            List<String> rawIds = List.of(uuid);

            SearchCoursesByIdsCommand command = SearchCoursesByIdsCommand.of(rawIds);

            when(loadCoursesByIdsPort.loadCoursesByIds(any())).thenReturn(Collections.emptyList());

            // WHEN
            List<Course> result = useCase.searchByIds(command);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();

            verify(loadCoursesByIdsPort, times(1)).loadCoursesByIds(command.courseIds());
        }

        @Test
        @DisplayName("should call port with exact courseIds from command")
        void searchByIds_shouldPassExactCourseIdsFromCommand() {
            // GIVEN
            String uuid1 = UUID.randomUUID().toString();
            String uuid2 = UUID.randomUUID().toString();
            String uuid3 = UUID.randomUUID().toString();
            List<String> rawIds = List.of(uuid1, uuid2, uuid3);

            SearchCoursesByIdsCommand command = SearchCoursesByIdsCommand.of(rawIds);

            Course course = CourseBuilder.aCourse().withId(uuid1).build();
            when(loadCoursesByIdsPort.loadCoursesByIds(any())).thenReturn(List.of(course));

            // WHEN
            useCase.searchByIds(command);

            // THEN
            ArgumentCaptor<List<Id>> idsCaptor = ArgumentCaptor.forClass(List.class);
            verify(loadCoursesByIdsPort, times(1)).loadCoursesByIds(idsCaptor.capture());

            List<Id> capturedIds = idsCaptor.getValue();
            assertThat(capturedIds).hasSize(3);
            assertThat(capturedIds.get(0).asString()).isEqualTo(uuid1);
            assertThat(capturedIds.get(1).asString()).isEqualTo(uuid2);
            assertThat(capturedIds.get(2).asString()).isEqualTo(uuid3);
        }

        @Test
        @DisplayName("should call loadCoursesByIdsPort exactly once")
        void searchByIds_shouldCallPortExactlyOnce() {
            // GIVEN
            String uuid = UUID.randomUUID().toString();
            SearchCoursesByIdsCommand command = SearchCoursesByIdsCommand.of(List.of(uuid));

            Course course = CourseBuilder.aCourse().withId(uuid).build();
            when(loadCoursesByIdsPort.loadCoursesByIds(any())).thenReturn(List.of(course));

            // WHEN
            useCase.searchByIds(command);

            // THEN
            verify(loadCoursesByIdsPort, times(1)).loadCoursesByIds(command.courseIds());
            verifyNoMoreInteractions(loadCoursesByIdsPort);
        }

        @Test
        @DisplayName("should return list in same order as port returns")
        void searchByIds_shouldMaintainOrderFromPort() {
            // GIVEN
            String uuid1 = UUID.randomUUID().toString();
            String uuid2 = UUID.randomUUID().toString();
            String uuid3 = UUID.randomUUID().toString();

            SearchCoursesByIdsCommand command = SearchCoursesByIdsCommand.of(List.of(uuid1, uuid2, uuid3));

            Course course3 = CourseBuilder.aCourse().withId(uuid3).withTitle("Third").build();
            Course course1 = CourseBuilder.aCourse().withId(uuid1).withTitle("First").build();
            Course course2 = CourseBuilder.aCourse().withId(uuid2).withTitle("Second").build();

            List<Course> coursesFromPort = List.of(course3, course1, course2);

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

