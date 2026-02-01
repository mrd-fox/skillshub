package com.simplon_project.skillhub.skillhub.course.application.usecase;

import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.repository.JpaCourseRepository;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.CreateCourseCommand;
import com.simplon_project.skillhub.skillhub.course.application.port.out.course.FindCoursePort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.course.SaveCoursePort;
import com.simplon_project.skillhub.skillhub.course.domain.enums.CourseStatusEnum;
import com.simplon_project.skillhub.skillhub.course.domain.exception.CourseAlreadyExistsException;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CourseUseCasesTest {

    @Mock
    private JpaCourseRepository courseJpaRepository;

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

//        @Test
//        void createCourseDraftWithValidArguments_shouldReturnCreatedCourse() {
//            //GIVEN
//            var createCourseCommande = CreateCourseCommand.builder()
//                    .title(COURSE_TITLE)
//                    .description(COURSE_DESCRIPTION)
//                    .price(COURSE_PRICE)
//                    .build();
//
//            var courseToSave = Course.builder()
//                    .title(COURSE_TITLE)
//                    .description(COURSE_DESCRIPTION)
//                    .price(COURSE_PRICE)
//                    .status(COURSE_STATUS_DRAFT)
//                    .build();
//            var courseSaved = Course.builder()
//                    .id(Id.of(COURSE_ID.toString()))
//                    .title(COURSE_TITLE)
//                    .description(COURSE_DESCRIPTION)
//                    .price(COURSE_PRICE)
//                    .status(COURSE_STATUS_DRAFT)
//                    .createdAt(CREATED_AT)
//                    .updatedAt(UPDATED_AT)
//                    .build();
//
//            when(saveCoursePort.saveCourse(courseToSave)).thenReturn(courseSaved);
//
//            //WHEN
//            var createdCourse = courseUseCases.createCourse(createCourseCommande);
//
//            //THAN
//            assertEquals(courseSaved, createdCourse);
//            verify(saveCoursePort).saveCourse(courseToSave);
//
//        }

        @Test
        void createCourseDraftWithDuplicateTitle_shouldThrowException() {
            var courseCommand = CreateCourseCommand.builder()
                    .title(COURSE_TITLE)
                    .externalAuthorId("1234")
                    .rawRoles("TUTOR, STUDENT")
                    .build();


            ArgumentCaptor<Course> captor = ArgumentCaptor.forClass(Course.class);

            doThrow(new CourseAlreadyExistsException(COURSE_TITLE))
                    .when(saveCoursePort)
                    .assertCourseNotExists(argThat(c -> COURSE_TITLE.equals(c.getTitle())));
            // WHEN + THEN
            var exception = assertThrows(CourseAlreadyExistsException.class,
                    () -> courseUseCases.createCourse(courseCommand));

            assertEquals("course-already-exists: Course entity with title Course Title already exists", exception.getMessage());
            verify(saveCoursePort).assertCourseNotExists(captor.capture());
            assertThat(captor.getValue().getTitle()).isEqualTo(COURSE_TITLE);
        }

    }


}
