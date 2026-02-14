package com.simplon_project.skillhub.skillhub.course.application.usecase;

import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.repository.JpaCourseRepository;
import com.simplon_project.skillhub.skillhub.course.application.exception.CourseNotFoundException;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.*;
import com.simplon_project.skillhub.skillhub.course.application.port.out.course.*;
import com.simplon_project.skillhub.skillhub.course.application.port.out.outbox.EnqueueOutboxEventPort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.video.ExistsInFlightVideoForCoursePort;
import com.simplon_project.skillhub.skillhub.course.domain.enums.CourseStatusEnum;
import com.simplon_project.skillhub.skillhub.course.domain.enums.ExternalDeletionStatus;
import com.simplon_project.skillhub.skillhub.course.domain.enums.UserRole;
import com.simplon_project.skillhub.skillhub.course.domain.enums.VideoStatusEnum;
import com.simplon_project.skillhub.skillhub.course.domain.exception.*;
import com.simplon_project.skillhub.skillhub.course.domain.model.*;
import com.simplon_project.skillhub.skillhub.helpers.builders.ChapterBuilder;
import com.simplon_project.skillhub.skillhub.helpers.builders.SectionBuilder;
import com.simplon_project.skillhub.skillhub.helpers.builders.VideoBuilder;
import com.simplon_project.skillhub.skillhub.helpers.mothers.CourseMother;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CourseUseCasesTest {

    @Mock
    private JpaCourseRepository courseJpaRepository;

    @Mock
    private CreateNewCoursePort createNewCoursePort;

    @Mock
    private UpdateCourseStructurePort updateCourseStructurePort;

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

    @Mock
    private EnqueueOutboxEventPort enqueueOutboxEventPort;

    @Mock
    private ExistsInFlightVideoForCoursePort existsInFlightVideoForCoursePort;

    @Mock
    SoftDeleteCoursePort softDeleteCoursePort;

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

            doNothing().when(createNewCoursePort).assertCourseNotExists(any(Course.class));
            when(createNewCoursePort.createNewCourse(any(Course.class))).thenReturn(expectedCourse);

            // WHEN
            var createdCourse = courseUseCases.createCourse(createCourseCommand);

            // THEN
            assertNotNull(createdCourse);
            assertEquals(COURSE_TITLE, createdCourse.getTitle());
            assertEquals(COURSE_DESCRIPTION, createdCourse.getDescription());
            assertEquals(COURSE_PRICE, createdCourse.getPrice());
            verify(createNewCoursePort).assertCourseNotExists(any(Course.class));
            verify(createNewCoursePort).createNewCourse(any(Course.class));
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
                    .when(createNewCoursePort)
                    .assertCourseNotExists(argThat(c -> COURSE_TITLE.equals(c.getTitle())));

            // WHEN + THEN
            var exception = assertThrows(CourseAlreadyExistsException.class,
                    () -> courseUseCases.createCourse(courseCommand));

            assertEquals("course-already-exists: Course entity with title Course Title already exists", exception.getMessage());
            verify(createNewCoursePort).assertCourseNotExists(captor.capture());
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

            doNothing().when(createNewCoursePort).assertCourseNotExists(any(Course.class));
            when(createNewCoursePort.createNewCourse(any(Course.class))).thenReturn(expectedCourse);

            // WHEN
            var createdCourse = courseUseCases.createCourse(createCourseCommand);

            // THEN
            assertNotNull(createdCourse);
            verify(createNewCoursePort).createNewCourse(any(Course.class));
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
            doNothing().when(createNewCoursePort).assertCourseNotExists(any(Course.class));
            when(updateCourseStructurePort.updateCourseStructure(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // WHEN
            var updatedCourse = courseUseCases.updateCourse(updateCommand);

            // THEN
            assertNotNull(updatedCourse);
            assertEquals(newTitle, updatedCourse.getTitle());
            verify(updateCourseStructurePort).updateCourseStructure(any(Course.class));
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
            when(updateCourseStructurePort.updateCourseStructure(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

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
            when(updateCourseStructurePort.updateCourseStructure(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

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

            Id courseId = Id.of(COURSE_ID_STRING);

            when(loadCourseWithVideoPort.loadWithVideo(any(Id.class)))
                    .thenThrow(new CourseNotFoundException(courseId));

            // WHEN + THEN
            assertThrows(CourseNotFoundException.class,
                    () -> courseUseCases.updateCourse(updateCommand));

            verifyNoInteractions(updateCourseStructurePort);
        }

        @Test
        @DisplayName("should throw exception when TUTOR tries to update another user's course")
        void updateCourse_whenNotOwner_shouldThrowException() {
            // GIVEN
            var existingCourse = buildCourse();

            var updateCommand = UpdateCourseCommand.builder()
                    .courseId(COURSE_ID_STRING)
                    .externalAuthorId(OTHER_USER_ID)
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
                    .externalAuthorId(OTHER_USER_ID)
                    .rawRoles(RAW_ROLES_ADMIN)
                    .title(newTitle)
                    .build();

            when(loadCourseWithVideoPort.loadWithVideo(any(Id.class))).thenReturn(existingCourse);
            doNothing().when(createNewCoursePort).assertCourseNotExists(any(Course.class));
            when(updateCourseStructurePort.updateCourseStructure(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

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
                    .when(createNewCoursePort).assertCourseNotExists(any(Course.class));

            // WHEN + THEN
            assertThrows(CourseAlreadyExistsException.class,
                    () -> courseUseCases.updateCourse(updateCommand));
        }

        // ========================================================================
        // STRUCTURE LOCK ENFORCEMENT TESTS
        // ========================================================================

        @Test
        @DisplayName("updateCourse_metaOnly_shouldSucceed_whenVideoPending")
        void updateCourse_metaOnly_shouldSucceed_whenVideoPending() {
            // GIVEN - Meta-only update (sections = null)
            String newTitle = "Updated Title";
            var existingCourse = buildCourse();

            var updateCommand = UpdateCourseCommand.builder()
                    .courseId(COURSE_ID_STRING)
                    .externalAuthorId(EXTERNAL_AUTHOR_ID)
                    .rawRoles(RAW_ROLES_TUTOR)
                    .title(newTitle)
                    .sections(null)  // null = no structure change
                    .build();

            when(loadCourseWithVideoPort.loadWithVideo(any(Id.class))).thenReturn(existingCourse);
            doNothing().when(createNewCoursePort).assertCourseNotExists(any(Course.class));
            when(updateCourseStructurePort.updateCourseStructure(any(Course.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // WHEN
            var updatedCourse = courseUseCases.updateCourse(updateCommand);

            // THEN
            assertNotNull(updatedCourse);
            assertEquals(newTitle, updatedCourse.getTitle());
            verify(updateCourseStructurePort).updateCourseStructure(any(Course.class));
            // existsInFlightVideoForCoursePort should NOT be called when sections = null
            verify(existsInFlightVideoForCoursePort, never()).existsInFlightVideoForCourse(any(Id.class), anySet());
        }

        @Test
        @DisplayName("updateCourse_structurePatch_shouldFail_whenVideoPending")
        void updateCourse_structurePatch_shouldFail_whenVideoPending() {
            // GIVEN - Structure update (sections provided)
            var existingCourse = buildCourse();

            var updateCommand = UpdateCourseCommand.builder()
                    .courseId(COURSE_ID_STRING)
                    .externalAuthorId(EXTERNAL_AUTHOR_ID)
                    .rawRoles(RAW_ROLES_TUTOR)
                    .sections(List.of())  // empty list = structural change attempt
                    .build();

            when(loadCourseWithVideoPort.loadWithVideo(any(Id.class))).thenReturn(existingCourse);

            // Simulate in-flight video with PENDING status
            when(existsInFlightVideoForCoursePort.existsInFlightVideoForCourse(any(Id.class), anySet()))
                    .thenReturn(true);

            // WHEN + THEN
            var exception = assertThrows(CourseStructureLockedException.class,
                    () -> courseUseCases.updateCourse(updateCommand));

            assertNotNull(exception);
            assertTrue(exception.getMessage().contains("in-flight status detected"));
            verify(existsInFlightVideoForCoursePort).existsInFlightVideoForCourse(any(Id.class), anySet());
            verify(updateCourseStructurePort, never()).updateCourseStructure(any(Course.class));
        }

        @Test
        @DisplayName("updateCourse_structurePatch_shouldFail_whenVideoProcessing")
        void updateCourse_structurePatch_shouldFail_whenVideoProcessing() {
            // GIVEN - Structure update with PROCESSING video
            var existingCourse = buildCourse();

            var sectionCmd = UpdateSectionCommand.builder()
                    .id(UUID.randomUUID().toString())
                    .title("Section 1")
                    .position(1)
                    .chapters(List.of())
                    .build();

            var updateCommand = UpdateCourseCommand.builder()
                    .courseId(COURSE_ID_STRING)
                    .externalAuthorId(EXTERNAL_AUTHOR_ID)
                    .rawRoles(RAW_ROLES_TUTOR)
                    .sections(List.of(sectionCmd))
                    .build();

            when(loadCourseWithVideoPort.loadWithVideo(any(Id.class))).thenReturn(existingCourse);

            // Simulate in-flight video with PROCESSING status
            when(existsInFlightVideoForCoursePort.existsInFlightVideoForCourse(any(Id.class), anySet()))
                    .thenReturn(true);

            // WHEN + THEN
            var exception = assertThrows(CourseStructureLockedException.class,
                    () -> courseUseCases.updateCourse(updateCommand));

            assertNotNull(exception);
            verify(existsInFlightVideoForCoursePort).existsInFlightVideoForCourse(any(Id.class), anySet());
            verify(updateCourseStructurePort, never()).updateCourseStructure(any(Course.class));
        }

        @Test
        @DisplayName("updateCourse_structurePatch_shouldSucceed_whenNoInFlightVideos")
        void updateCourse_structurePatch_shouldSucceed_whenNoInFlightVideos() {
            // GIVEN - Structure update with no in-flight videos
            var existingCourse = buildCourse();

            var sectionCmd = UpdateSectionCommand.builder()
                    .id(UUID.randomUUID().toString())
                    .title("Section 1")
                    .position(1)
                    .chapters(List.of())
                    .build();

            var updateCommand = UpdateCourseCommand.builder()
                    .courseId(COURSE_ID_STRING)
                    .externalAuthorId(EXTERNAL_AUTHOR_ID)
                    .rawRoles(RAW_ROLES_TUTOR)
                    .sections(List.of(sectionCmd))
                    .build();

            when(loadCourseWithVideoPort.loadWithVideo(any(Id.class))).thenReturn(existingCourse);

            // No in-flight videos exist
            when(existsInFlightVideoForCoursePort.existsInFlightVideoForCourse(any(Id.class), anySet()))
                    .thenReturn(false);

            when(updateCourseStructurePort.updateCourseStructure(any(Course.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // WHEN
            var updatedCourse = courseUseCases.updateCourse(updateCommand);

            // THEN
            assertNotNull(updatedCourse);
            verify(existsInFlightVideoForCoursePort).existsInFlightVideoForCourse(any(Id.class), anySet());
            verify(updateCourseStructurePort).updateCourseStructure(any(Course.class));
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
            var draftCourse = buildCourse();

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

    @Nested
    @DisplayName("PublishCourse Tests")
    class PublishCourse {

        @Test
        @DisplayName("ADMIN should successfully publish a publishable course")
        void publishCourse_asAdmin_shouldSucceed() {
            // GIVEN
            String courseId = COURSE_ID_STRING;
            String adminUserId = "admin-123";
            Set<UserRole> adminRoles = Set.of(UserRole.ADMIN);

            var command = PublishCourseCommand.builder()
                    .courseId(courseId)
                    .externalUserId(adminUserId)
                    .userRoles(adminRoles)
                    .build();

            Course courseWithVideo = buildPublishableCourse();
            Course savedCourse = buildPublishableCourse();
            savedCourse.setStatus(CourseStatusEnum.WAITING_VALIDATION);

            when(loadCourseWithVideoPort.loadWithVideo(any(Id.class))).thenReturn(courseWithVideo);
            when(updateCourseStructurePort.updateCourseStructure(any(Course.class))).thenReturn(savedCourse);

            // WHEN
            Course result = courseUseCases.publishCourse(command);

            // THEN
            assertNotNull(result);
            assertEquals(CourseStatusEnum.WAITING_VALIDATION, result.getStatus());
            verify(loadCourseWithVideoPort).loadWithVideo(any(Id.class));
            verify(updateCourseStructurePort).updateCourseStructure(any(Course.class));
        }

        @Test
        @DisplayName("TUTOR owner should successfully publish a publishable course")
        void publishCourse_asTutorOwner_shouldSucceed() {
            // GIVEN
            String courseId = COURSE_ID_STRING;
            String tutorUserId = EXTERNAL_AUTHOR_ID;
            Set<UserRole> tutorRoles = Set.of(UserRole.TUTOR);

            var command = PublishCourseCommand.builder()
                    .courseId(courseId)
                    .externalUserId(tutorUserId)
                    .userRoles(tutorRoles)
                    .build();

            Course courseWithVideo = buildPublishableCourse();
            courseWithVideo.setExternalUserId(tutorUserId);

            Course savedCourse = buildPublishableCourse();
            savedCourse.setStatus(CourseStatusEnum.WAITING_VALIDATION);

            when(loadCourseWithVideoPort.loadWithVideo(any(Id.class))).thenReturn(courseWithVideo);
            when(updateCourseStructurePort.updateCourseStructure(any(Course.class))).thenReturn(savedCourse);

            // WHEN
            Course result = courseUseCases.publishCourse(command);

            // THEN
            assertNotNull(result);
            assertEquals(CourseStatusEnum.WAITING_VALIDATION, result.getStatus());
            verify(loadCourseWithVideoPort).loadWithVideo(any(Id.class));
            verify(updateCourseStructurePort).updateCourseStructure(any(Course.class));
        }

        @Test
        @DisplayName("TUTOR non-owner should fail with 403")
        void publishCourse_asTutorNonOwner_shouldFail() {
            // GIVEN
            String courseId = COURSE_ID_STRING;
            String tutorUserId = OTHER_USER_ID;
            Set<UserRole> tutorRoles = Set.of(UserRole.TUTOR);

            var command = PublishCourseCommand.builder()
                    .courseId(courseId)
                    .externalUserId(tutorUserId)
                    .userRoles(tutorRoles)
                    .build();

            Course courseWithVideo = buildPublishableCourse();
            courseWithVideo.setExternalUserId(EXTERNAL_AUTHOR_ID);

            when(loadCourseWithVideoPort.loadWithVideo(any(Id.class))).thenReturn(courseWithVideo);

            // WHEN + THEN
            assertThrows(UnauthorizedCourseAccessException.class,
                    () -> courseUseCases.publishCourse(command));

            verify(loadCourseWithVideoPort).loadWithVideo(any(Id.class));
            verify(updateCourseStructurePort, never()).updateCourseStructure(any(Course.class));
        }

        @Test
        @DisplayName("STUDENT should fail with 403")
        void publishCourse_asStudent_shouldFail() {
            // GIVEN
            String courseId = COURSE_ID_STRING;
            String studentUserId = "student-123";
            Set<UserRole> studentRoles = Set.of(UserRole.STUDENT);

            var command = PublishCourseCommand.builder()
                    .courseId(courseId)
                    .externalUserId(studentUserId)
                    .userRoles(studentRoles)
                    .build();

            Course courseWithVideo = buildPublishableCourse();

            when(loadCourseWithVideoPort.loadWithVideo(any(Id.class))).thenReturn(courseWithVideo);

            // WHEN + THEN
            assertThrows(UnauthorizedCourseAccessException.class,
                    () -> courseUseCases.publishCourse(command));

            verify(loadCourseWithVideoPort).loadWithVideo(any(Id.class));
            verify(updateCourseStructurePort, never()).updateCourseStructure(any(Course.class));
        }

        @Test
        @DisplayName("Should fail with 409 if course already WAITING_VALIDATION")
        void publishCourse_alreadyWaitingValidation_shouldFail() {
            // GIVEN
            String courseId = COURSE_ID_STRING;
            Set<UserRole> adminRoles = Set.of(UserRole.ADMIN);

            var command = PublishCourseCommand.builder()
                    .courseId(courseId)
                    .externalUserId("admin-123")
                    .userRoles(adminRoles)
                    .build();

            Course courseWithVideo = buildPublishableCourse();
            courseWithVideo.setStatus(CourseStatusEnum.WAITING_VALIDATION);

            when(loadCourseWithVideoPort.loadWithVideo(any(Id.class))).thenReturn(courseWithVideo);

            // WHEN + THEN
            assertThrows(CourseAlreadySubmittedException.class,
                    () -> courseUseCases.publishCourse(command));

            verify(loadCourseWithVideoPort).loadWithVideo(any(Id.class));
            verify(updateCourseStructurePort, never()).updateCourseStructure(any(Course.class));
        }

        @Test
        @DisplayName("Should fail with 409 if course already PUBLISHED")
        void publishCourse_alreadyPublished_shouldFail() {
            // GIVEN
            String courseId = COURSE_ID_STRING;
            Set<UserRole> adminRoles = Set.of(UserRole.ADMIN);

            var command = PublishCourseCommand.builder()
                    .courseId(courseId)
                    .externalUserId("admin-123")
                    .userRoles(adminRoles)
                    .build();

            Course courseWithVideo = buildPublishableCourse();
            courseWithVideo.setStatus(CourseStatusEnum.PUBLISHED);

            when(loadCourseWithVideoPort.loadWithVideo(any(Id.class))).thenReturn(courseWithVideo);

            // WHEN + THEN
            assertThrows(CourseAlreadySubmittedException.class,
                    () -> courseUseCases.publishCourse(command));

            verify(loadCourseWithVideoPort).loadWithVideo(any(Id.class));
            verify(updateCourseStructurePort, never()).updateCourseStructure(any(Course.class));
        }

        @Test
        @DisplayName("Should fail with 422 if chapter has no video")
        void publishCourse_chapterWithoutVideo_shouldFail() {
            // GIVEN
            String courseId = COURSE_ID_STRING;
            Set<UserRole> adminRoles = Set.of(UserRole.ADMIN);

            var command = PublishCourseCommand.builder()
                    .courseId(courseId)
                    .externalUserId("admin-123")
                    .userRoles(adminRoles)
                    .build();

            Course courseWithoutVideo = buildCourseWithoutVideo();

            when(loadCourseWithVideoPort.loadWithVideo(any(Id.class))).thenReturn(courseWithoutVideo);

            // WHEN + THEN
            assertThrows(CourseNotPublishableException.class,
                    () -> courseUseCases.publishCourse(command));

            verify(loadCourseWithVideoPort).loadWithVideo(any(Id.class));
            verify(updateCourseStructurePort, never()).updateCourseStructure(any(Course.class));
        }

        @Test
        @DisplayName("Should fail with 422 if video status is PROCESSING")
        void publishCourse_videoNotReady_shouldFail() {
            // GIVEN
            String courseId = COURSE_ID_STRING;
            Set<UserRole> adminRoles = Set.of(UserRole.ADMIN);

            var command = PublishCourseCommand.builder()
                    .courseId(courseId)
                    .externalUserId("admin-123")
                    .userRoles(adminRoles)
                    .build();

            Course courseWithProcessingVideo = buildCourseWithProcessingVideo();

            when(loadCourseWithVideoPort.loadWithVideo(any(Id.class))).thenReturn(courseWithProcessingVideo);

            // WHEN + THEN
            assertThrows(CourseNotPublishableException.class,
                    () -> courseUseCases.publishCourse(command));

            verify(loadCourseWithVideoPort).loadWithVideo(any(Id.class));
            verify(updateCourseStructurePort, never()).updateCourseStructure(any(Course.class));
        }

        @Test
        @DisplayName("Should fail with 422 if video status is FAILED")
        void publishCourse_videoFailed_shouldFail() {
            // GIVEN
            String courseId = COURSE_ID_STRING;
            Set<UserRole> adminRoles = Set.of(UserRole.ADMIN);

            var command = PublishCourseCommand.builder()
                    .courseId(courseId)
                    .externalUserId("admin-123")
                    .userRoles(adminRoles)
                    .build();

            Course courseWithFailedVideo = buildCourseWithFailedVideo();

            when(loadCourseWithVideoPort.loadWithVideo(any(Id.class))).thenReturn(courseWithFailedVideo);

            // WHEN + THEN
            assertThrows(CourseNotPublishableException.class,
                    () -> courseUseCases.publishCourse(command));

            verify(loadCourseWithVideoPort).loadWithVideo(any(Id.class));
            verify(updateCourseStructurePort, never()).updateCourseStructure(any(Course.class));
        }

        // Helper methods for building test courses
        private Course buildPublishableCourse() {
            VideoInfo readyVideo = VideoInfo.builder()
                    .id(Id.of(UUID.randomUUID().toString()))
                    .sourceUri("vimeo://123456")
                    .status(VideoStatusEnum.READY)
                    .duration(3600L)
                    .externalDeletionStatus(ExternalDeletionStatus.NONE)
                    .build();

            Chapter chapter = Chapter.builder()
                    .id(Id.of(UUID.randomUUID().toString()))
                    .title("Chapter 1")
                    .position(1)
                    .video(readyVideo)
                    .build();

            Section section = Section.builder()
                    .id(Id.of(UUID.randomUUID().toString()))
                    .title("Section 1")
                    .position(1)
                    .chapters(Set.of(chapter))
                    .build();

            chapter.setSection(section);

            Course course = Course.builder()
                    .id(Id.of(COURSE_ID_STRING))
                    .title(COURSE_TITLE)
                    .description(COURSE_DESCRIPTION)
                    .price(COURSE_PRICE)
                    .status(CourseStatusEnum.DRAFT)
                    .externalUserId(EXTERNAL_AUTHOR_ID)
                    .sections(Set.of(section))
                    .build();

            section.setCourse(course);
            return course;
        }

        private Course buildCourseWithoutVideo() {
            Chapter chapter = Chapter.builder()
                    .id(Id.of(UUID.randomUUID().toString()))
                    .title("Chapter 1")
                    .position(1)
                    .video(null)
                    .build();

            Section section = Section.builder()
                    .id(Id.of(UUID.randomUUID().toString()))
                    .title("Section 1")
                    .position(1)
                    .chapters(Set.of(chapter))
                    .build();

            chapter.setSection(section);

            Course course = Course.builder()
                    .id(Id.of(COURSE_ID_STRING))
                    .title(COURSE_TITLE)
                    .description(COURSE_DESCRIPTION)
                    .price(COURSE_PRICE)
                    .status(CourseStatusEnum.DRAFT)
                    .externalUserId(EXTERNAL_AUTHOR_ID)
                    .sections(Set.of(section))
                    .build();

            section.setCourse(course);
            return course;
        }

        private Course buildCourseWithProcessingVideo() {
            VideoInfo processingVideo = VideoInfo.builder()
                    .id(Id.of(UUID.randomUUID().toString()))
                    .sourceUri("vimeo://123456")
                    .status(VideoStatusEnum.PROCESSING)
                    .externalDeletionStatus(ExternalDeletionStatus.NONE)
                    .build();

            Chapter chapter = Chapter.builder()
                    .id(Id.of(UUID.randomUUID().toString()))
                    .title("Chapter 1")
                    .position(1)
                    .video(processingVideo)
                    .build();

            Section section = Section.builder()
                    .id(Id.of(UUID.randomUUID().toString()))
                    .title("Section 1")
                    .position(1)
                    .chapters(Set.of(chapter))
                    .build();

            chapter.setSection(section);

            Course course = Course.builder()
                    .id(Id.of(COURSE_ID_STRING))
                    .title(COURSE_TITLE)
                    .description(COURSE_DESCRIPTION)
                    .price(COURSE_PRICE)
                    .status(CourseStatusEnum.DRAFT)
                    .externalUserId(EXTERNAL_AUTHOR_ID)
                    .sections(Set.of(section))
                    .build();

            section.setCourse(course);
            return course;
        }

        private Course buildCourseWithFailedVideo() {
            VideoInfo failedVideo = VideoInfo.builder()
                    .id(Id.of(UUID.randomUUID().toString()))
                    .sourceUri("vimeo://123456")
                    .status(VideoStatusEnum.FAILED)
                    .errorMessage("Upload failed")
                    .externalDeletionStatus(ExternalDeletionStatus.NONE)
                    .build();

            Chapter chapter = Chapter.builder()
                    .id(Id.of(UUID.randomUUID().toString()))
                    .title("Chapter 1")
                    .position(1)
                    .video(failedVideo)
                    .build();

            Section section = Section.builder()
                    .id(Id.of(UUID.randomUUID().toString()))
                    .title("Section 1")
                    .position(1)
                    .chapters(Set.of(chapter))
                    .build();

            chapter.setSection(section);

            Course course = Course.builder()
                    .id(Id.of(COURSE_ID_STRING))
                    .title(COURSE_TITLE)
                    .description(COURSE_DESCRIPTION)
                    .price(COURSE_PRICE)
                    .status(CourseStatusEnum.DRAFT)
                    .externalUserId(EXTERNAL_AUTHOR_ID)
                    .sections(Set.of(section))
                    .build();

            section.setCourse(course);
            return course;
        }
    }

    // ========================================================================
    // SOFT DELETE TESTS (SH-166 STEP 2)
    // ========================================================================
    @Nested
    @DisplayName("updateCourse - Soft Delete")
    class UpdateCourseSoftDelete {

        @Test
        @DisplayName("should soft delete video and enqueue outbox event when chapter is removed")
        void updateCourse_whenChapterRemoved_shouldSoftDeleteVideoAndEnqueueOutboxEvent() {
            // GIVEN
            Course existingCourse = CourseMother.withVideoNone(EXTERNAL_AUTHOR_ID);
            Section existingSection = existingCourse.getSectionsSorted().get(0);
            Chapter removedChapter = existingSection.getChaptersSorted().get(0);
            VideoInfo originalVideo = removedChapter.getVideo();

            String videoId = originalVideo.id().asString();
            String sourceUri = originalVideo.sourceUri();

            // Patch: same section id, but chapters list EMPTY => remove chapter cleanly
            UpdateSectionCommand updatedSection = UpdateSectionCommand.builder()
                    .id(existingSection.getId().asString())
                    .title(existingSection.getTitle())
                    .position(existingSection.getPosition())
                    .chapters(List.of())
                    .build();

            var updateCommand = UpdateCourseCommand.builder()
                    .courseId(COURSE_ID_STRING)
                    .externalAuthorId(EXTERNAL_AUTHOR_ID)
                    .rawRoles(RAW_ROLES_TUTOR)
                    .sections(List.of(updatedSection))
                    .build();

            when(loadCourseWithVideoPort.loadWithVideo(any(Id.class))).thenReturn(existingCourse);
            when(updateCourseStructurePort.updateCourseStructure(any(Course.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // WHEN
            courseUseCases.updateCourse(updateCommand);

            // THEN
            ArgumentCaptor<Id> idCaptor = ArgumentCaptor.forClass(Id.class);
            ArgumentCaptor<String> uriCaptor = ArgumentCaptor.forClass(String.class);

            verify(enqueueOutboxEventPort).enqueueVideoDeletionRequested(idCaptor.capture(), uriCaptor.capture());
            assertEquals(videoId, idCaptor.getValue().asString());
            assertEquals(sourceUri, uriCaptor.getValue());

            assertNotNull(removedChapter.getDeletedAt());

            VideoInfo updatedVideo = removedChapter.getVideo();
            assertNotNull(updatedVideo);
            assertEquals(ExternalDeletionStatus.REQUESTED, updatedVideo.externalDeletionStatus());
            assertNotNull(updatedVideo.deletedAt());
        }

        @Test
        @DisplayName("should soft delete section with all chapters and videos when section is removed")
        void updateCourse_whenSectionRemoved_shouldSoftDeleteSectionChaptersAndVideos() {
            // GIVEN - Course with 2 videos in 2 chapters
            VideoInfo video1 = VideoBuilder.aVideo()
                    .withExternalDeletionStatus(ExternalDeletionStatus.NONE)
                    .build();

            VideoInfo video2 = VideoBuilder.aVideo()
                    .withExternalDeletionStatus(ExternalDeletionStatus.NONE)
                    .build();

            Chapter chapter1 = ChapterBuilder.aChapter()
                    .withVideo(video1)
                    .build();

            Chapter chapter2 = ChapterBuilder.aChapter()
                    .withVideo(video2)
                    .build();

            Section section = SectionBuilder.aSection()
                    .withChapter(chapter1)
                    .withChapter(chapter2)
                    .build();

            Course existingCourse = Course.builder()
                    .id(Id.of(COURSE_ID_STRING))
                    .title(COURSE_TITLE)
                    .externalUserId(EXTERNAL_AUTHOR_ID)
                    .sections(new HashSet<>(Set.of(section)))
                    .build();

            section.setCourse(existingCourse);

            var updateCommand = UpdateCourseCommand.builder()
                    .courseId(COURSE_ID_STRING)
                    .externalAuthorId(EXTERNAL_AUTHOR_ID)
                    .rawRoles(RAW_ROLES_TUTOR)
                    .sections(List.of())
                    .build();

            when(loadCourseWithVideoPort.loadWithVideo(any(Id.class))).thenReturn(existingCourse);
            when(updateCourseStructurePort.updateCourseStructure(any(Course.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // WHEN
            courseUseCases.updateCourse(updateCommand);

            // THEN
            assertNotNull(section.getDeletedAt());
            assertNotNull(chapter1.getDeletedAt());
            assertNotNull(chapter2.getDeletedAt());

            VideoInfo updatedVideo1 = chapter1.getVideo();
            VideoInfo updatedVideo2 = chapter2.getVideo();

            assertNotNull(updatedVideo1);
            assertNotNull(updatedVideo2);

            assertEquals(ExternalDeletionStatus.REQUESTED, updatedVideo1.externalDeletionStatus());
            assertEquals(ExternalDeletionStatus.REQUESTED, updatedVideo2.externalDeletionStatus());

            assertNotNull(updatedVideo1.deletedAt());
            assertNotNull(updatedVideo2.deletedAt());

            verify(enqueueOutboxEventPort, times(2)).enqueueVideoDeletionRequested(any(Id.class), anyString());
        }

        @Test
        @DisplayName("should be idempotent when video already REQUESTED - no enqueue")
        void updateCourse_whenVideoAlreadyRequested_shouldNotEnqueueAgain() {
            // GIVEN
            Course existingCourse = CourseMother.withVideoAlreadyRequested(EXTERNAL_AUTHOR_ID);
            Section existingSection = existingCourse.getSectionsSorted().get(0);

            // Patch: remove section (which has video already REQUESTED)
            var updateCommand = UpdateCourseCommand.builder()
                    .courseId(COURSE_ID_STRING)
                    .externalAuthorId(EXTERNAL_AUTHOR_ID)
                    .rawRoles(RAW_ROLES_TUTOR)
                    .sections(List.of())
                    .build();

            when(loadCourseWithVideoPort.loadWithVideo(any(Id.class))).thenReturn(existingCourse);
            when(updateCourseStructurePort.updateCourseStructure(any(Course.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // WHEN
            courseUseCases.updateCourse(updateCommand);

            // THEN
            assertNotNull(existingSection.getDeletedAt());

            Chapter chapter = existingSection.getChaptersSorted().get(0);
            assertNotNull(chapter.getDeletedAt());

            VideoInfo video = chapter.getVideo();
            assertNotNull(video);
            assertEquals(ExternalDeletionStatus.REQUESTED, video.externalDeletionStatus());
            assertNotNull(video.deletedAt());

            // NO new outbox event should be enqueued (idempotent)
            verify(enqueueOutboxEventPort, never()).enqueueVideoDeletionRequested(any(Id.class), anyString());
        }
    }

    // ========================================================================
    // DELETE COURSE TESTS
    // ========================================================================
    @Nested
    @DisplayName("delete")
    class DeleteCourse {

        @Test
        @DisplayName("ADMIN should successfully delete course and enqueue videos with status NONE only")
        void delete_asAdmin_shouldSucceedAndEnqueueVideosWithNoneStatus() {
            // GIVEN
            Course existingCourse = CourseMother.withVideoNone(EXTERNAL_AUTHOR_ID);
            Section section = existingCourse.getSectionsSorted().get(0);
            Chapter chapter = section.getChaptersSorted().get(0);
            VideoInfo video = chapter.getVideo();

            String videoId = video.id().asString();
            String sourceUri = video.sourceUri();

            var command = DeleteCourseCommand.builder()
                    .courseId(COURSE_ID_STRING)
                    .externalUserId("admin-user-123")
                    .rawRoles(RAW_ROLES_ADMIN)
                    .build();

            when(loadCourseWithVideoPort.loadWithVideo(any(Id.class))).thenReturn(existingCourse);

            // WHEN
            courseUseCases.delete(command);

            // THEN
            ArgumentCaptor<Course> courseCaptor = ArgumentCaptor.forClass(Course.class);
            ArgumentCaptor<Instant> instantCaptor = ArgumentCaptor.forClass(Instant.class);

            verify(softDeleteCoursePort).softDelete(courseCaptor.capture(), instantCaptor.capture());

            Course capturedCourse = courseCaptor.getValue();
            assertNotNull(capturedCourse.getDeletedAt());
            assertNotNull(section.getDeletedAt());
            assertNotNull(chapter.getDeletedAt());

            VideoInfo updatedVideo = chapter.getVideo();
            assertNotNull(updatedVideo);
            assertEquals(ExternalDeletionStatus.REQUESTED, updatedVideo.externalDeletionStatus());
            assertNotNull(updatedVideo.deletedAt());

            ArgumentCaptor<Id> idCaptor = ArgumentCaptor.forClass(Id.class);
            ArgumentCaptor<String> uriCaptor = ArgumentCaptor.forClass(String.class);

            verify(enqueueOutboxEventPort).enqueueVideoDeletionRequested(idCaptor.capture(), uriCaptor.capture());
            assertEquals(videoId, idCaptor.getValue().asString());
            assertEquals(sourceUri, uriCaptor.getValue());
        }

        @Test
        @DisplayName("delete should only enqueue videos with NONE status, not REQUESTED or DELETED")
        void delete_shouldOnlyEnqueueVideosWithNoneStatus() {
            // GIVEN - Course with 3 videos: NONE, REQUESTED, DELETED
            VideoInfo videoNone = VideoBuilder.aVideo()
                    .withExternalDeletionStatus(ExternalDeletionStatus.NONE)
                    .build();

            VideoInfo videoRequested = VideoBuilder.aVideo()
                    .withExternalDeletionStatus(ExternalDeletionStatus.REQUESTED)
                    .build();

            VideoInfo videoDeleted = VideoBuilder.aVideo()
                    .withExternalDeletionStatus(ExternalDeletionStatus.DELETED)
                    .build();

            Chapter chapter1 = ChapterBuilder.aChapter().withVideo(videoNone).build();
            Chapter chapter2 = ChapterBuilder.aChapter().withVideo(videoRequested).build();
            Chapter chapter3 = ChapterBuilder.aChapter().withVideo(videoDeleted).build();

            Section section = SectionBuilder.aSection()
                    .withChapter(chapter1)
                    .withChapter(chapter2)
                    .withChapter(chapter3)
                    .build();

            Course existingCourse = Course.builder()
                    .id(Id.of(COURSE_ID_STRING))
                    .title(COURSE_TITLE)
                    .externalUserId(EXTERNAL_AUTHOR_ID)
                    .sections(new HashSet<>(Set.of(section)))
                    .build();

            section.setCourse(existingCourse);

            var command = DeleteCourseCommand.builder()
                    .courseId(COURSE_ID_STRING)
                    .externalUserId("admin-user-123")
                    .rawRoles(RAW_ROLES_ADMIN)
                    .build();

            when(loadCourseWithVideoPort.loadWithVideo(any(Id.class))).thenReturn(existingCourse);

            // WHEN
            courseUseCases.delete(command);

            // THEN
            // Only 1 enqueue for videoNone
            verify(enqueueOutboxEventPort, times(1)).enqueueVideoDeletionRequested(any(Id.class), anyString());

            ArgumentCaptor<Id> idCaptor = ArgumentCaptor.forClass(Id.class);
            verify(enqueueOutboxEventPort).enqueueVideoDeletionRequested(idCaptor.capture(), anyString());
            assertEquals(videoNone.id().asString(), idCaptor.getValue().asString());

            // Verify all videos have deletedAt
            assertNotNull(chapter1.getVideo().deletedAt());
            assertNotNull(chapter2.getVideo().deletedAt());
            assertNotNull(chapter3.getVideo().deletedAt());

            // Verify externalDeletionStatus transitions
            assertEquals(ExternalDeletionStatus.REQUESTED, chapter1.getVideo().externalDeletionStatus());
            assertEquals(ExternalDeletionStatus.REQUESTED, chapter2.getVideo().externalDeletionStatus());
            assertEquals(ExternalDeletionStatus.DELETED, chapter3.getVideo().externalDeletionStatus());

            verify(softDeleteCoursePort).softDelete(any(Course.class), any(Instant.class));
        }

        @Test
        @DisplayName("TUTOR non-owner should fail with UnauthorizedCourseAccessException")
        void delete_asTutorNonOwner_shouldThrowUnauthorizedCourseAccessException() {
            // GIVEN
            Course existingCourse = CourseMother.withVideoNone(EXTERNAL_AUTHOR_ID);

            var command = DeleteCourseCommand.builder()
                    .courseId(COURSE_ID_STRING)
                    .externalUserId(OTHER_USER_ID)
                    .rawRoles(RAW_ROLES_TUTOR)
                    .build();

            when(loadCourseWithVideoPort.loadWithVideo(any(Id.class))).thenReturn(existingCourse);

            // WHEN + THEN
            assertThrows(UnauthorizedCourseAccessException.class,
                    () -> courseUseCases.delete(command));

            verify(loadCourseWithVideoPort).loadWithVideo(any(Id.class));
            verifyNoInteractions(softDeleteCoursePort);
            verifyNoInteractions(enqueueOutboxEventPort);
        }

        @Test
        @DisplayName("TUTOR owner should successfully delete own course")
        void delete_asTutorOwner_shouldSucceed() {
            // GIVEN
            Course existingCourse = CourseMother.withVideoNone(EXTERNAL_AUTHOR_ID);

            var command = DeleteCourseCommand.builder()
                    .courseId(COURSE_ID_STRING)
                    .externalUserId(EXTERNAL_AUTHOR_ID)
                    .rawRoles(RAW_ROLES_TUTOR)
                    .build();

            when(loadCourseWithVideoPort.loadWithVideo(any(Id.class))).thenReturn(existingCourse);

            // WHEN
            courseUseCases.delete(command);

            // THEN
            verify(softDeleteCoursePort).softDelete(any(Course.class), any(Instant.class));
            verify(enqueueOutboxEventPort).enqueueVideoDeletionRequested(any(Id.class), anyString());
        }

        @Test
        @DisplayName("delete should throw CourseNotFoundException when course not found")
        void delete_whenCourseNotFound_shouldThrowCourseNotFoundException() {
            // GIVEN
            var command = DeleteCourseCommand.builder()
                    .courseId(COURSE_ID_STRING)
                    .externalUserId(EXTERNAL_AUTHOR_ID)
                    .rawRoles(RAW_ROLES_ADMIN)
                    .build();

            Id courseId = Id.of(COURSE_ID_STRING);
            when(loadCourseWithVideoPort.loadWithVideo(any(Id.class)))
                    .thenThrow(new CourseNotFoundException(courseId));

            // WHEN + THEN
            assertThrows(CourseNotFoundException.class,
                    () -> courseUseCases.delete(command));

            verify(loadCourseWithVideoPort).loadWithVideo(any(Id.class));
            verifyNoInteractions(softDeleteCoursePort);
            verifyNoInteractions(enqueueOutboxEventPort);
        }

        @Test
        @DisplayName("delete should throw IllegalStateException when video has null sourceUri")
        void delete_whenVideoHasNullSourceUri_shouldThrowIllegalStateException() {
            // GIVEN
            VideoInfo videoWithoutSourceUri = VideoBuilder.aVideo()
                    .withoutSourceUri()
                    .withExternalDeletionStatus(ExternalDeletionStatus.NONE)
                    .build();

            Chapter chapter = ChapterBuilder.aChapter()
                    .withVideo(videoWithoutSourceUri)
                    .build();

            Section section = SectionBuilder.aSection()
                    .withChapter(chapter)
                    .build();

            Course existingCourse = Course.builder()
                    .id(Id.of(COURSE_ID_STRING))
                    .title(COURSE_TITLE)
                    .externalUserId(EXTERNAL_AUTHOR_ID)
                    .sections(new HashSet<>(Set.of(section)))
                    .build();

            section.setCourse(existingCourse);

            var command = DeleteCourseCommand.builder()
                    .courseId(COURSE_ID_STRING)
                    .externalUserId(EXTERNAL_AUTHOR_ID)
                    .rawRoles(RAW_ROLES_ADMIN)
                    .build();

            when(loadCourseWithVideoPort.loadWithVideo(any(Id.class))).thenReturn(existingCourse);

            // WHEN + THEN
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> courseUseCases.delete(command));

            assertThat(exception.getMessage()).contains("sourceUri");

            verify(loadCourseWithVideoPort).loadWithVideo(any(Id.class));
            verifyNoInteractions(softDeleteCoursePort);
            verifyNoInteractions(enqueueOutboxEventPort);
        }
    }
}
