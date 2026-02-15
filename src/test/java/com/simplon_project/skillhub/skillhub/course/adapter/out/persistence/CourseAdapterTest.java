package com.simplon_project.skillhub.skillhub.course.adapter.out.persistence;


import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.CourseAdapter;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.CourseEntity;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.repository.JpaCourseRepository;
import com.simplon_project.skillhub.skillhub.course.domain.enums.CourseStatusEnum;
import com.simplon_project.skillhub.skillhub.course.domain.exception.CourseAlreadyExistsException;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import jakarta.persistence.EntityManager;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CourseAdapterTest {

    @Mock
    private JpaCourseRepository courseJpaRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private CourseAdapter courseAdapter;

    public static final String COURSE_TITLE = "Course Title";

    @Test
    void assertCourseNotExists_whenCourseWithSameTitleExists_shouldThrowException() {
        // GIVEN
        var course = Course.builder().title(COURSE_TITLE).build();


        var existingEntity = CourseEntity.builder()
                .courseId(EntityId.of(UUID.randomUUID()))
                .title(COURSE_TITLE)
                .build();


        when(courseJpaRepository.findAll()).thenReturn(List.of(existingEntity));

        // WHEN + THEN
        var ex = assertThrows(CourseAlreadyExistsException.class,
                () -> courseAdapter.assertCourseNotExists(course));

        assertEquals(
                "course-already-exists: Course entity with title Course Title already exists",
                ex.getMessage()
        );


        verify(courseJpaRepository).findAll();
        verifyNoMoreInteractions(courseJpaRepository);
    }

    @Nested
    @DisplayName("loadCoursesByIds() method")
    class LoadCoursesByIds {

        @Test
        @DisplayName("should call repository with UUIDs and return mapped domain courses")
        void loadCoursesByIds_shouldCallRepositoryAndReturnMappedCourses() {
            // GIVEN
            UUID uuid1 = UUID.randomUUID();
            UUID uuid2 = UUID.randomUUID();

            Id id1 = Id.of(uuid1.toString());
            Id id2 = Id.of(uuid2.toString());
            List<Id> courseIds = List.of(id1, id2);

            CourseEntity entity1 = CourseEntity.builder()
                    .courseId(EntityId.of(uuid1))
                    .title("Course 1")
                    .description("Description 1")
                    .price(100L)
                    .status(CourseStatusEnum.PUBLISHED)
                    .externalUserId("user-1")
                    .build();

            CourseEntity entity2 = CourseEntity.builder()
                    .courseId(EntityId.of(uuid2))
                    .title("Course 2")
                    .description("Description 2")
                    .price(200L)
                    .status(CourseStatusEnum.DRAFT)
                    .externalUserId("user-2")
                    .build();

            List<CourseEntity> entities = List.of(entity1, entity2);
            List<UUID> expectedUuids = List.of(uuid1, uuid2);

            when(courseJpaRepository.findAllByIdIn(expectedUuids)).thenReturn(entities);

            // WHEN
            List<Course> result = courseAdapter.loadCoursesByIds(courseIds);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId().asString()).isEqualTo(uuid1.toString());
            assertThat(result.get(0).getTitle()).isEqualTo("Course 1");
            assertThat(result.get(1).getId().asString()).isEqualTo(uuid2.toString());
            assertThat(result.get(1).getTitle()).isEqualTo("Course 2");

            ArgumentCaptor<List<UUID>> uuidsCaptor = ArgumentCaptor.forClass(List.class);
            verify(courseJpaRepository, times(1)).findAllByIdIn(uuidsCaptor.capture());

            List<UUID> capturedUuids = uuidsCaptor.getValue();
            assertThat(capturedUuids).hasSize(2);
            assertThat(capturedUuids).containsExactly(uuid1, uuid2);

            verifyNoMoreInteractions(courseJpaRepository);
        }

        @Test
        @DisplayName("should return empty list when repository returns empty list")
        void loadCoursesByIds_shouldReturnEmptyListWhenRepositoryReturnsEmpty() {
            // GIVEN
            UUID uuid = UUID.randomUUID();
            Id id = Id.of(uuid.toString());
            List<Id> courseIds = List.of(id);

            when(courseJpaRepository.findAllByIdIn(anyList())).thenReturn(Collections.emptyList());

            // WHEN
            List<Course> result = courseAdapter.loadCoursesByIds(courseIds);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();

            verify(courseJpaRepository, times(1)).findAllByIdIn(List.of(uuid));
            verifyNoMoreInteractions(courseJpaRepository);
        }

        @Test
        @DisplayName("should return empty list when input is empty")
        void loadCoursesByIds_shouldReturnEmptyListWhenInputIsEmpty() {
            // GIVEN
            List<Id> courseIds = Collections.emptyList();

            when(courseJpaRepository.findAllByIdIn(anyList())).thenReturn(Collections.emptyList());

            // WHEN
            List<Course> result = courseAdapter.loadCoursesByIds(courseIds);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();

            verify(courseJpaRepository, times(1)).findAllByIdIn(Collections.emptyList());
        }

        @Test
        @DisplayName("should handle multiple courses with different statuses")
        void loadCoursesByIds_shouldHandleMultipleCoursesWithDifferentStatuses() {
            // GIVEN
            UUID uuid1 = UUID.randomUUID();
            UUID uuid2 = UUID.randomUUID();
            UUID uuid3 = UUID.randomUUID();

            List<Id> courseIds = List.of(
                    Id.of(uuid1.toString()),
                    Id.of(uuid2.toString()),
                    Id.of(uuid3.toString())
            );

            CourseEntity draftCourse = CourseEntity.builder()
                    .courseId(EntityId.of(uuid1))
                    .title("Draft Course")
                    .status(CourseStatusEnum.DRAFT)
                    .externalUserId("user-1")
                    .build();

            CourseEntity publishedCourse = CourseEntity.builder()
                    .courseId(EntityId.of(uuid2))
                    .title("Published Course")
                    .status(CourseStatusEnum.PUBLISHED)
                    .externalUserId("user-2")
                    .build();

            CourseEntity waitingCourse = CourseEntity.builder()
                    .courseId(EntityId.of(uuid3))
                    .title("Waiting Course")
                    .status(CourseStatusEnum.WAITING_VALIDATION)
                    .externalUserId("user-3")
                    .build();

            List<CourseEntity> entities = List.of(draftCourse, publishedCourse, waitingCourse);

            when(courseJpaRepository.findAllByIdIn(anyList())).thenReturn(entities);

            // WHEN
            List<Course> result = courseAdapter.loadCoursesByIds(courseIds);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getStatus()).isEqualTo(CourseStatusEnum.DRAFT);
            assertThat(result.get(1).getStatus()).isEqualTo(CourseStatusEnum.PUBLISHED);
            assertThat(result.get(2).getStatus()).isEqualTo(CourseStatusEnum.WAITING_VALIDATION);

            verify(courseJpaRepository, times(1)).findAllByIdIn(List.of(uuid1, uuid2, uuid3));
        }
    }

//    @Test
//    void assertCourseNotExists_whenCourseTitleIsUnique_shouldNotThrowException() {
//        // GIVEN
//        var course = Course.builder().title(COURSE_TITLE).build();
//
//        when(courseJpaRepository.findAll()).thenReturn(List.of());
//
//        assertDoesNotThrow(() -> courseAdapter.assertCourseNotExists(course));
//
//        verify(courseJpaRepository).findAll();
//        verifyNoMoreInteractions(courseJpaRepository);
//    }
}
