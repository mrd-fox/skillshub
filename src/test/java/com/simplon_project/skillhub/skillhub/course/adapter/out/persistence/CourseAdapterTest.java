package com.simplon_project.skillhub.skillhub.course.adapter.out.persistence;


import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.CourseAdapter;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.CourseEntity;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.mapper.CourseEntityMapper;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.repository.JpaCourseRepository;
import com.simplon_project.skillhub.skillhub.course.domain.exception.CourseAlreadyExistsException;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CourseAdapterTest {

    @Mock
    CourseEntityMapper courseEntityMapper;

    @Mock
    JpaCourseRepository courseJpaRepository;

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
