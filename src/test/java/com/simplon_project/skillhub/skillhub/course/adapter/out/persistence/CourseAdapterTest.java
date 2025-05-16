package com.simplon_project.skillhub.skillhub.course.adapter.out.persistence;


import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.CourseAdapter;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.CourseEntity;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.mapper.CreateCourseEntityMapper;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.repository.CourseRepository;
import com.simplon_project.skillhub.skillhub.course.domain.exception.CourseAlreadyExistsException;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CourseAdapterTest {

    @Mock
    CreateCourseEntityMapper createCourseEntityMapper;

    @Mock
    CourseRepository courseRepository;

    @InjectMocks
    private CourseAdapter courseAdapter;

    public static final String COURSE_TITLE = "Course Title";

    @Test
    void assertCourseNotExists_whenCourseWithSameTitleExists_shouldThrowException() {
        // GIVEN
        var course = Course.builder().title(COURSE_TITLE).build();

        var existingEntity = CourseEntity.builder().title(COURSE_TITLE).build();

        when(courseRepository.findByTitle(COURSE_TITLE.toLowerCase()))
                .thenReturn(Optional.of(existingEntity));

        // WHEN + THEN
        var exception = assertThrows(CourseAlreadyExistsException.class, () -> {
            courseAdapter.assertCourseNotExists(course);
        });

        assertEquals("course-already-exists: Course entity with title Course Title already exists", exception.getMessage());
        verify(courseRepository).findByTitle(COURSE_TITLE.toLowerCase());
    }

    @Test
    void assertCourseNotExists_whenCourseTitleIsUnique_shouldNotThrowException() {
        // GIVEN
        var course = Course.builder().title(COURSE_TITLE).build();

        when(courseRepository.findByTitle(COURSE_TITLE.toLowerCase()))
                .thenReturn(Optional.empty());

        // WHEN + THEN
        assertDoesNotThrow(() -> courseAdapter.assertCourseNotExists(course));
        verify(courseRepository).findByTitle(COURSE_TITLE.toLowerCase());
    }
}
