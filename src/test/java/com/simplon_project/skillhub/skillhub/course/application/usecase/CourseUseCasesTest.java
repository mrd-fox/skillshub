package com.simplon_project.skillhub.skillhub.course.application.usecase;

import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.repository.CourseRepository;
import com.simplon_project.skillhub.skillhub.course.application.port.out.FindCoursePort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.SaveCoursePort;
import com.simplon_project.skillhub.skillhub.course.domain.enums.CourseStatusEnum;
import com.simplon_project.skillhub.skillhub.course.domain.exception.CourseAlreadyExistsException;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CourseUseCasesTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private SaveCoursePort saveCoursePort;

    @Mock
    private FindCoursePort findCoursePort;

    @InjectMocks
    private CourseUseCases courseUseCases;

    public static final String COURSE_TITLE = "Course Title";
    public static final String COURSE_DESCRIPTION = "Course Description";
    public static final Long COURSE_PRICE = 10L;
    public static final UUID COURSE_ID = UUID.randomUUID();
    public static final CourseStatusEnum COURSE_STATUS_DRAFT = CourseStatusEnum.DRAFT;
    public static final LocalDateTime CREATED_AT = LocalDateTime.now();
    public static final LocalDateTime UPDATED_AT = LocalDateTime.now();

    @Nested
    class CreateCourse {

        @Test
        void createCourseDraftWithValidArguments_shouldReturnCreatedCourse() {
            //GIVEN
            var courseToSave = Course.builder()
                    .title(COURSE_TITLE)
                    .description(COURSE_DESCRIPTION)
                    .price(COURSE_PRICE)
                    .status(COURSE_STATUS_DRAFT)
                    .build();
            var courseSaved = Course.builder()
                    .id(String.valueOf(COURSE_ID))
                    .title(COURSE_TITLE)
                    .description(COURSE_DESCRIPTION)
                    .price(COURSE_PRICE)
                    .status(COURSE_STATUS_DRAFT)
                    .createdAt(CREATED_AT)
                    .updatedAt(UPDATED_AT)
                    .build();

            when(saveCoursePort.saveCourse(courseToSave)).thenReturn(courseSaved);

            //WHEN
            var createdCourse = courseUseCases.createCourse(courseToSave);

            //THAN
            assertEquals(courseSaved, createdCourse);
            verify(saveCoursePort).saveCourse(courseToSave);

        }

        @Test
        void createCourseDraftWithDuplicateTitle_shouldThrowException() {
            var courseToSave = Course.builder()
                    .title(COURSE_TITLE)
                    .build();

            var courseExisting = Course.builder()
                    .id(String.valueOf(COURSE_ID))
                    .title(COURSE_TITLE)
                    .build();


            doThrow(new CourseAlreadyExistsException(courseExisting.getTitle()))
                    .when(saveCoursePort).assertCourseNotExists(courseToSave);

            // WHEN + THEN
            var exception = assertThrows(CourseAlreadyExistsException.class,
                    () -> courseUseCases.createCourse(courseToSave));

            assertEquals("course-already-exists: Course entity with title Course Title already exists", exception.getMessage());
            verify(saveCoursePort).assertCourseNotExists(courseToSave);
            verify(saveCoursePort, never()).saveCourse(any());
        }

    }


}
