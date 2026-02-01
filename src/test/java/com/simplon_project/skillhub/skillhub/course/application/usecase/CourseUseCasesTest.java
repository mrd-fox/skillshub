package com.simplon_project.skillhub.skillhub.course.application.usecase;

import com.simplon_project.skillhub.skillhub.course.adapter.common.exception.CourseNotFoundException;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.repository.JpaCourseRepository;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.*;
import com.simplon_project.skillhub.skillhub.course.application.port.out.course.*;
import com.simplon_project.skillhub.skillhub.course.domain.enums.CourseStatusEnum;
import com.simplon_project.skillhub.skillhub.course.domain.enums.UserRole;
import com.simplon_project.skillhub.skillhub.course.domain.exception.CourseAlreadyExistsException;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import com.simplon_project.skillhub.skillhub.course.domain.model.PublicCourseDetail;
import com.simplon_project.skillhub.skillhub.course.domain.model.PublicCourseSummary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CourseUseCasesTest {

    @Mock
    private JpaCourseRepository courseJpaRepository;

    @Mock
    private SaveCoursePort saveCoursePort;

    @Mock
    private FindCoursePort findCoursePort;

    @Mock
    private LoadPublicCoursesPort loadPublicCoursesPort;

    @Mock
    private LoadPublicCourseDetailPort loadPublicCourseDetailPort;

    @Mock
    private LoadCourseStructurePort loadCourseStructurePort;

    @Mock
    private LoadCourseWithVideoPort loadCourseWithVideoPort;

    @InjectMocks
    private CourseUseCases courseUseCases;

    // ============ Constants ============
    public static final String COURSE_TITLE = "Course Title";
    public static final String COURSE_DESCRIPTION = "Course Description";
    public static final Long COURSE_PRICE = 10L;
    public static final UUID COURSE_ID = UUID.randomUUID();
    public static final String COURSE_ID_STRING = COURSE_ID.toString();
    public static final CourseStatusEnum COURSE_STATUS_DRAFT = CourseStatusEnum.DRAFT;
    public static final CourseStatusEnum COURSE_STATUS_PUBLISHED = CourseStatusEnum.PUBLISHED;
    public static final LocalDateTime CREATED_AT = LocalDateTime.now();
    public static final LocalDateTime UPDATED_AT = LocalDateTime.now();
    public static final String EXTERNAL_AUTHOR_ID = "author-123";
    public static final String OTHER_USER_ID = "other-user-456";
    public static final String RAW_ROLES_TUTOR = "TUTOR";
    public static final String RAW_ROLES_ADMIN = "ADMIN";
    public static final String RAW_ROLES_STUDENT = "STUDENT";

    // ============ Helper methods ============
    private Course buildCourse() {
        return Course.builder()
                .id(Id.of(COURSE_ID_STRING))
                .title(COURSE_TITLE)
                .description(COURSE_DESCRIPTION)
                .price(COURSE_PRICE)
                .status(COURSE_STATUS_DRAFT)
                .externalUserId(EXTERNAL_AUTHOR_ID)
                .createdAt(CREATED_AT)
                .updatedAt(UPDATED_AT)
                .build();
    }

    private Course buildPublishedCourse() {
        return Course.builder()
                .id(Id.of(COURSE_ID_STRING))
                .title(COURSE_TITLE)
                .description(COURSE_DESCRIPTION)
                .price(COURSE_PRICE)
                .status(COURSE_STATUS_PUBLISHED)
                .externalUserId(EXTERNAL_AUTHOR_ID)
                .createdAt(CREATED_AT)
                .updatedAt(UPDATED_AT)
                .build();
    }

    // ========================================================================
    // CREATE COURSE TESTS
    // ========================================================================
    @Nested
    @DisplayName("createCourse")
    class CreateCourse {

        @Test
        @DisplayName("should create course with valid arguments")
        void createCourseDraftWithValidArguments_shouldReturnCreatedCourse() {
            // GIVEN
            var createCourseCommand = CreateCourseCommand.builder()
                    .title(COURSE_TITLE)
                    .description(COURSE_DESCRIPTION)
                    .price(COURSE_PRICE)
                    .externalAuthorId(EXTERNAL_AUTHOR_ID)
                    .rawRoles(RAW_ROLES_TUTOR)
                    .build();

            var expectedCourse = buildCourse();

            doNothing().when(saveCoursePort).assertCourseNotExists(any(Course.class));
            when(saveCoursePort.saveCourse(any(Course.class))).thenReturn(expectedCourse);

            // WHEN
            var createdCourse = courseUseCases.createCourse(createCourseCommand);

            // THEN
            assertNotNull(createdCourse);
            assertEquals(COURSE_TITLE, createdCourse.getTitle());
            assertEquals(COURSE_DESCRIPTION, createdCourse.getDescription());
            assertEquals(COURSE_PRICE, createdCourse.getPrice());
            verify(saveCoursePort).assertCourseNotExists(any(Course.class));
            verify(saveCoursePort).saveCourse(any(Course.class));
        }

        @Test
        @DisplayName("should throw exception when course with same title already exists")
        void createCourseDraftWithDuplicateTitle_shouldThrowException() {
            // GIVEN
            var courseCommand = CreateCourseCommand.builder()
                    .title(COURSE_TITLE)
                    .externalAuthorId(EXTERNAL_AUTHOR_ID)
                    .rawRoles(RAW_ROLES_TUTOR)
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

        @Test
        @DisplayName("should create course with ADMIN role")
        void createCourseAsAdmin_shouldSucceed() {
            // GIVEN
            var createCourseCommand = CreateCourseCommand.builder()
                    .title(COURSE_TITLE)
                    .externalAuthorId(EXTERNAL_AUTHOR_ID)
                    .rawRoles(RAW_ROLES_ADMIN)
                    .build();

            var expectedCourse = buildCourse();

            doNothing().when(saveCoursePort).assertCourseNotExists(any(Course.class));
            when(saveCoursePort.saveCourse(any(Course.class))).thenReturn(expectedCourse);

            // WHEN
            var createdCourse = courseUseCases.createCourse(createCourseCommand);

            // THEN
            assertNotNull(createdCourse);
            verify(saveCoursePort).saveCourse(any(Course.class));
        }
    }

    // ========================================================================
    // UPDATE COURSE TESTS
    // ========================================================================
    @Nested
    @DisplayName("updateCourse")
    class UpdateCourse {

        @Test
        @DisplayName("should update course title successfully")
        void updateCourseTitle_shouldSucceed() {
            // GIVEN
            String newTitle = "Updated Title";
            var existingCourse = buildCourse();

            var updateCommand = UpdateCourseCommand.builder()
                    .courseId(COURSE_ID_STRING)
                    .externalAuthorId(EXTERNAL_AUTHOR_ID)
                    .rawRoles(RAW_ROLES_TUTOR)
                    .title(newTitle)
                    .build();

            when(loadCourseWithVideoPort.loadWithVideo(any(Id.class))).thenReturn(existingCourse);
            doNothing().when(saveCoursePort).assertCourseNotExists(any(Course.class));
            when(saveCoursePort.saveCourse(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // WHEN
            var updatedCourse = courseUseCases.updateCourse(updateCommand);

            // THEN
            assertNotNull(updatedCourse);
            assertEquals(newTitle, updatedCourse.getTitle());
            verify(saveCoursePort).saveCourse(any(Course.class));
        }

        @Test
        @DisplayName("should update course description successfully")
        void updateCourseDescription_shouldSucceed() {
            // GIVEN
            String newDescription = "Updated Description";
            var existingCourse = buildCourse();

            var updateCommand = UpdateCourseCommand.builder()
                    .courseId(COURSE_ID_STRING)
                    .externalAuthorId(EXTERNAL_AUTHOR_ID)
                    .rawRoles(RAW_ROLES_TUTOR)
                    .description(newDescription)
                    .build();

            when(loadCourseWithVideoPort.loadWithVideo(any(Id.class))).thenReturn(existingCourse);
            when(saveCoursePort.saveCourse(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // WHEN
            var updatedCourse = courseUseCases.updateCourse(updateCommand);

            // THEN
            assertNotNull(updatedCourse);
            assertEquals(newDescription, updatedCourse.getDescription());
        }

        @Test
        @DisplayName("should update course price successfully")
        void updateCoursePrice_shouldSucceed() {
            // GIVEN
            Long newPrice = 50L;
            var existingCourse = buildCourse();

            var updateCommand = UpdateCourseCommand.builder()
                    .courseId(COURSE_ID_STRING)
                    .externalAuthorId(EXTERNAL_AUTHOR_ID)
                    .rawRoles(RAW_ROLES_TUTOR)
                    .price(newPrice)
                    .build();

            when(loadCourseWithVideoPort.loadWithVideo(any(Id.class))).thenReturn(existingCourse);
            when(saveCoursePort.saveCourse(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // WHEN
            var updatedCourse = courseUseCases.updateCourse(updateCommand);

            // THEN
            assertNotNull(updatedCourse);
            assertEquals(newPrice, updatedCourse.getPrice());
        }

        @Test
        @DisplayName("should throw exception when course not found")
        void updateCourse_whenCourseNotFound_shouldThrowException() {
            // GIVEN
            var updateCommand = UpdateCourseCommand.builder()
                    .courseId(COURSE_ID_STRING)
                    .externalAuthorId(EXTERNAL_AUTHOR_ID)
                    .rawRoles(RAW_ROLES_TUTOR)
                    .title("New Title")
                    .build();

            when(loadCourseWithVideoPort.loadWithVideo(any(Id.class))).thenReturn(null);

            // WHEN + THEN
            assertThrows(CourseNotFoundException.class,
                    () -> courseUseCases.updateCourse(updateCommand));
        }

        @Test
        @DisplayName("should throw exception when TUTOR tries to update another user's course")
        void updateCourse_whenNotOwner_shouldThrowException() {
            // GIVEN
            var existingCourse = buildCourse();

            var updateCommand = UpdateCourseCommand.builder()
                    .courseId(COURSE_ID_STRING)
                    .externalAuthorId(OTHER_USER_ID)  // Different user
                    .rawRoles(RAW_ROLES_TUTOR)
                    .title("New Title")
                    .build();

            when(loadCourseWithVideoPort.loadWithVideo(any(Id.class))).thenReturn(existingCourse);

            // WHEN + THEN
            assertThrows(CourseNotFoundException.class,
                    () -> courseUseCases.updateCourse(updateCommand));
        }

        @Test
        @DisplayName("should allow ADMIN to update any course")
        void updateCourse_asAdmin_shouldAllowAnyUpdate() {
            // GIVEN
            var existingCourse = buildCourse();
            String newTitle = "Admin Updated Title";

            var updateCommand = UpdateCourseCommand.builder()
                    .courseId(COURSE_ID_STRING)
                    .externalAuthorId(OTHER_USER_ID)  // Different user but ADMIN
                    .rawRoles(RAW_ROLES_ADMIN)
                    .title(newTitle)
                    .build();

            when(loadCourseWithVideoPort.loadWithVideo(any(Id.class))).thenReturn(existingCourse);
            doNothing().when(saveCoursePort).assertCourseNotExists(any(Course.class));
            when(saveCoursePort.saveCourse(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // WHEN
            var updatedCourse = courseUseCases.updateCourse(updateCommand);

            // THEN
            assertNotNull(updatedCourse);
            assertEquals(newTitle, updatedCourse.getTitle());
        }

        @Test
        @DisplayName("should throw exception when updating to duplicate title")
        void updateCourse_withDuplicateTitle_shouldThrowException() {
            // GIVEN
            String duplicateTitle = "Existing Title";
            var existingCourse = buildCourse();

            var updateCommand = UpdateCourseCommand.builder()
                    .courseId(COURSE_ID_STRING)
                    .externalAuthorId(EXTERNAL_AUTHOR_ID)
                    .rawRoles(RAW_ROLES_TUTOR)
                    .title(duplicateTitle)
                    .build();

            when(loadCourseWithVideoPort.loadWithVideo(any(Id.class))).thenReturn(existingCourse);
            doThrow(new CourseAlreadyExistsException(duplicateTitle))
                    .when(saveCoursePort).assertCourseNotExists(any(Course.class));

            // WHEN + THEN
            assertThrows(CourseAlreadyExistsException.class,
                    () -> courseUseCases.updateCourse(updateCommand));
        }
    }

    // ========================================================================
    // GET COURSES TESTS
    // ========================================================================
    @Nested
    @DisplayName("getCourses")
    class GetCourses {

        @Test
        @DisplayName("should return list of courses for author")
        void getCourses_shouldReturnCoursesForAuthor() {
            // GIVEN
            var command = GetCoursesCommand.of(EXTERNAL_AUTHOR_ID, RAW_ROLES_TUTOR);
            var courseList = List.of(buildCourse(), buildPublishedCourse());

            when(findCoursePort.findByExternalUserId(EXTERNAL_AUTHOR_ID)).thenReturn(courseList);

            // WHEN
            var result = courseUseCases.getCourses(command);

            // THEN
            assertNotNull(result);
            assertEquals(2, result.size());
            verify(findCoursePort).findByExternalUserId(EXTERNAL_AUTHOR_ID);
        }

        @Test
        @DisplayName("should return empty list when no courses found")
        void getCourses_whenNoCourses_shouldReturnEmptyList() {
            // GIVEN
            var command = GetCoursesCommand.of(EXTERNAL_AUTHOR_ID, RAW_ROLES_TUTOR);

            when(findCoursePort.findByExternalUserId(EXTERNAL_AUTHOR_ID)).thenReturn(List.of());

            // WHEN
            var result = courseUseCases.getCourses(command);

            // THEN
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    // ========================================================================
    // GET COURSE TESTS
    // ========================================================================
    @Nested
    @DisplayName("getCourse")
    class GetCourse {

        @Test
        @DisplayName("should return full course for ADMIN")
        void getCourse_asAdmin_shouldReturnFullCourse() {
            // GIVEN
            var command = new GetCourseCommand(EXTERNAL_AUTHOR_ID, Set.of(UserRole.ADMIN), COURSE_ID_STRING);
            var expectedCourse = buildCourse();

            when(loadCourseWithVideoPort.loadWithVideo(any(Id.class))).thenReturn(expectedCourse);

            // WHEN
            var result = courseUseCases.getCourse(command);

            // THEN
            assertNotNull(result);
            assertEquals(COURSE_TITLE, result.getTitle());
            verify(loadCourseWithVideoPort).loadWithVideo(any(Id.class));
            verify(loadCourseStructurePort, never()).loadStructure(any(Id.class));
        }

        @Test
        @DisplayName("should return full course for course owner (TUTOR)")
        void getCourse_asOwner_shouldReturnFullCourse() {
            // GIVEN
            var command = new GetCourseCommand(EXTERNAL_AUTHOR_ID, Set.of(UserRole.TUTOR), COURSE_ID_STRING);
            var courseStructure = buildCourse();
            var fullCourse = buildCourse();

            when(loadCourseStructurePort.loadStructure(any(Id.class))).thenReturn(courseStructure);
            when(loadCourseWithVideoPort.loadWithVideo(any(Id.class))).thenReturn(fullCourse);

            // WHEN
            var result = courseUseCases.getCourse(command);

            // THEN
            assertNotNull(result);
            verify(loadCourseStructurePort).loadStructure(any(Id.class));
            verify(loadCourseWithVideoPort).loadWithVideo(any(Id.class));
        }

        @Test
        @DisplayName("should return structure only for PUBLIC access on published course")
        void getCourse_asPublic_onPublishedCourse_shouldReturnStructureOnly() {
            // GIVEN
            var command = new GetCourseCommand(OTHER_USER_ID, Set.of(UserRole.STUDENT), COURSE_ID_STRING);
            var publishedCourse = buildPublishedCourse();

            when(loadCourseStructurePort.loadStructure(any(Id.class))).thenReturn(publishedCourse);

            // WHEN
            var result = courseUseCases.getCourse(command);

            // THEN
            assertNotNull(result);
            verify(loadCourseStructurePort).loadStructure(any(Id.class));
        }

        @Test
        @DisplayName("should throw exception for PUBLIC access on draft course")
        void getCourse_asPublic_onDraftCourse_shouldThrowException() {
            // GIVEN
            var command = new GetCourseCommand(OTHER_USER_ID, Set.of(UserRole.STUDENT), COURSE_ID_STRING);
            var draftCourse = buildCourse(); // DRAFT status

            when(loadCourseStructurePort.loadStructure(any(Id.class))).thenReturn(draftCourse);

            // WHEN + THEN
            assertThrows(CourseNotFoundException.class,
                    () -> courseUseCases.getCourse(command));
        }
    }

    // ========================================================================
    // LIST PUBLIC COURSES TESTS
    // ========================================================================
    @Nested
    @DisplayName("listPublicCourses")
    class ListPublicCourses {

        @Test
        @DisplayName("should return list of public course summaries")
        void listPublicCourses_shouldReturnSummaries() {
            // GIVEN
            var summaries = List.of(
                    PublicCourseSummary.of(Id.of(COURSE_ID_STRING), COURSE_TITLE, COURSE_DESCRIPTION, COURSE_PRICE),
                    PublicCourseSummary.of(Id.of(UUID.randomUUID().toString()), "Course 2", "Description 2", 20L)
            );

            when(loadPublicCoursesPort.loadPublicCourses()).thenReturn(summaries);

            // WHEN
            var result = courseUseCases.listPublicCourses();

            // THEN
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(COURSE_TITLE, result.get(0).getTitle());
            verify(loadPublicCoursesPort).loadPublicCourses();
        }

        @Test
        @DisplayName("should return empty list when no public courses")
        void listPublicCourses_whenNoCourses_shouldReturnEmptyList() {
            // GIVEN
            when(loadPublicCoursesPort.loadPublicCourses()).thenReturn(List.of());

            // WHEN
            var result = courseUseCases.listPublicCourses();

            // THEN
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    // ========================================================================
    // GET PUBLIC COURSE DETAIL TESTS
    // ========================================================================
    @Nested
    @DisplayName("getPublicCourseDetail")
    class GetPublicCourseDetail {

        @Test
        @DisplayName("should return public course detail when found")
        void getPublicCourseDetail_whenFound_shouldReturnDetail() {
            // GIVEN
            var command = GetPublicCourseDetailCommand.of(COURSE_ID_STRING);
            var detail = PublicCourseDetail.of(
                    Id.of(COURSE_ID_STRING),
                    COURSE_TITLE,
                    COURSE_DESCRIPTION,
                    COURSE_PRICE,
                    List.of()
            );

            when(loadPublicCourseDetailPort.loadPublicCourseDetail(any(Id.class)))
                    .thenReturn(Optional.of(detail));

            // WHEN
            var result = courseUseCases.getPublicCourseDetail(command);

            // THEN
            assertNotNull(result);
            assertEquals(COURSE_TITLE, result.getTitle());
            assertEquals(COURSE_DESCRIPTION, result.getDescription());
            assertEquals(COURSE_PRICE, result.getPrice());
            verify(loadPublicCourseDetailPort).loadPublicCourseDetail(any(Id.class));
        }

        @Test
        @DisplayName("should throw exception when public course not found")
        void getPublicCourseDetail_whenNotFound_shouldThrowException() {
            // GIVEN
            var command = GetPublicCourseDetailCommand.of(COURSE_ID_STRING);

            when(loadPublicCourseDetailPort.loadPublicCourseDetail(any(Id.class)))
                    .thenReturn(Optional.empty());

            // WHEN + THEN
            assertThrows(CourseNotFoundException.class,
                    () -> courseUseCases.getPublicCourseDetail(command));
        }
    }
}
