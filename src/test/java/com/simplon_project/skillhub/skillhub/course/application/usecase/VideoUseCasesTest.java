package com.simplon_project.skillhub.skillhub.course.application.usecase;

import com.simplon_project.skillhub.skillhub.course.application.dto.VideoUploadInit;
import com.simplon_project.skillhub.skillhub.course.application.dto.VideoUploadInitResult;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.ConfirmVideoUploadCommand;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.InitProviderUploadCommand;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.InitVideoCommand;
import com.simplon_project.skillhub.skillhub.course.application.port.out.chapter.LoadChapterForVideoOpsPort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.video.CreatePendingVideoForChapterPort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.video.EnqueueVideoPollingRequestPort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.video.SaveVideoInfoPort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.video.VideoProviderInitPort;
import com.simplon_project.skillhub.skillhub.course.domain.enums.ExternalDeletionStatus;
import com.simplon_project.skillhub.skillhub.course.domain.enums.VideoStatusEnum;
import com.simplon_project.skillhub.skillhub.course.domain.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VideoUseCases Unit Tests")
class VideoUseCasesTest {

    @Mock
    private LoadChapterForVideoOpsPort loadChapterForVideoOpsPort;

    @Mock
    private CreatePendingVideoForChapterPort createPendingVideoPort;

    @Mock
    private VideoProviderInitPort videoProviderInitPort;

    @Mock
    private SaveVideoInfoPort saveVideoInfoPort;

    @Mock
    private EnqueueVideoPollingRequestPort enqueueVideoPollingRequestPort;

    @InjectMocks
    private VideoUseCases videoUseCases;

    // ============ Constants ============
    private static final String COURSE_ID = UUID.randomUUID().toString();
    private static final String SECTION_ID = UUID.randomUUID().toString();
    private static final String CHAPTER_ID = UUID.randomUUID().toString();
    private static final String VIDEO_ID = UUID.randomUUID().toString();
    private static final String CHAPTER_TITLE = "Introduction to Java";
    private static final long SIZE_BYTES = 1024 * 1024 * 100L; // 100 MB
    private static final String SOURCE_URI = "vimeo://123456789";
    private static final String UPLOAD_URL = "https://vimeo.com/upload/tus/123456789";
    private static final String PROVIDER_VIDEO_ID = "123456789";

    // ============ Helper methods ============
    private Chapter createChapterWithCourseRelation() {
        var course = Course.builder()
                .id(Id.of(COURSE_ID))
                .title("Java Course")
                .build();

        var section = Section.builder()
                .id(Id.of(SECTION_ID))
                .title("Getting Started")
                .course(course)
                .build();

        return Chapter.builder()
                .id(Id.of(CHAPTER_ID))
                .title(CHAPTER_TITLE)
                .section(section)
                .video(null)
                .build();
    }

    private Chapter createChapterWithExistingVideo() {
        var chapter = createChapterWithCourseRelation();
        chapter.setVideo(createPendingVideo());
        return chapter;
    }

    private Chapter createChapterWithNullTitle() {
        var chapter = createChapterWithCourseRelation();
        chapter.setTitle(null);
        return chapter;
    }

    private Chapter createChapterWithBlankTitle() {
        var chapter = createChapterWithCourseRelation();
        chapter.setTitle("   ");
        return chapter;
    }

    private Chapter createChapterWithoutCourseRelation() {
        var section = Section.builder()
                .id(Id.of(SECTION_ID))
                .title("Getting Started")
                .course(null)  // Missing course
                .build();

        return Chapter.builder()
                .id(Id.of(CHAPTER_ID))
                .title(CHAPTER_TITLE)
                .section(section)
                .video(null)
                .build();
    }

    private Chapter createChapterFromDifferentCourse() {
        var differentCourseId = UUID.randomUUID().toString();
        var course = Course.builder()
                .id(Id.of(differentCourseId))
                .title("Different Course")
                .build();

        var section = Section.builder()
                .id(Id.of(SECTION_ID))
                .title("Getting Started")
                .course(course)
                .build();

        return Chapter.builder()
                .id(Id.of(CHAPTER_ID))
                .title(CHAPTER_TITLE)
                .section(section)
                .video(null)
                .build();
    }

    private VideoInfo createPendingVideo() {
        return VideoInfo.builder()
                .id(Id.of(VIDEO_ID))
                .sourceUri(SOURCE_URI)
                .status(VideoStatusEnum.PENDING)
                .externalDeletionStatus(ExternalDeletionStatus.NONE)
                .build();
    }

    private VideoInfo createProcessingVideo() {
        return VideoInfo.builder()
                .id(Id.of(VIDEO_ID))
                .sourceUri(SOURCE_URI)
                .status(VideoStatusEnum.PROCESSING)
                .externalDeletionStatus(ExternalDeletionStatus.NONE)
                .build();
    }

    private VideoUploadInitResult createProviderInitResult(Instant expiresAt) {
        return new VideoUploadInitResult(
                PROVIDER_VIDEO_ID,
                UPLOAD_URL,
                expiresAt,
                SOURCE_URI
        );
    }

    private InitVideoCommand createValidInitCommand() {
        return new InitVideoCommand(COURSE_ID, SECTION_ID, CHAPTER_ID, SIZE_BYTES);
    }

    private ConfirmVideoUploadCommand createValidConfirmCommand() {
        return new ConfirmVideoUploadCommand(COURSE_ID, SECTION_ID, CHAPTER_ID);
    }

    // ========================================================================
    // INIT VIDEO TESTS
    // ========================================================================
    @Nested
    @DisplayName("init() method")
    class InitVideo {

        @Test
        @DisplayName("should initialize video upload successfully")
        void init_withValidCommand_shouldReturnVideoUploadInit() {
            // GIVEN
            var command = createValidInitCommand();
            var chapter = createChapterWithCourseRelation();
            var expiresAt = Instant.now().plusSeconds(3600);
            var providerResult = createProviderInitResult(expiresAt);
            var savedVideo = createPendingVideo();

            when(loadChapterForVideoOpsPort.loadChapterForVideoOps(any(Id.class)))
                    .thenReturn(Optional.of(chapter));
            when(videoProviderInitPort.initTusUpload(any(InitProviderUploadCommand.class)))
                    .thenReturn(providerResult);
            when(createPendingVideoPort.createPendingVideo(any(Id.class), eq(SOURCE_URI)))
                    .thenReturn(savedVideo);

            // WHEN
            VideoUploadInit result = videoUseCases.init(command);

            // THEN
            assertNotNull(result);
            assertNotNull(result.video());
            assertNotNull(result.upload());

            assertEquals(VIDEO_ID, result.video().id().asString());
            assertEquals(SOURCE_URI, result.video().sourceUri());
            assertEquals(VideoStatusEnum.PENDING, result.video().status());

            assertEquals(VideoUseCases.PROVIDER_NAME, result.upload().provider());
            assertEquals(UPLOAD_URL, result.upload().uploadUrl());
            assertEquals(expiresAt, result.upload().expiresAt());

            verify(loadChapterForVideoOpsPort).loadChapterForVideoOps(Id.of(CHAPTER_ID));
            verify(videoProviderInitPort).initTusUpload(any(InitProviderUploadCommand.class));
            verify(createPendingVideoPort).createPendingVideo(Id.of(CHAPTER_ID), SOURCE_URI);
        }

        @Test
        @DisplayName("should use chapter title in provider command")
        void init_shouldUseChapterTitleInProviderCommand() {
            // GIVEN
            var command = createValidInitCommand();
            var chapter = createChapterWithCourseRelation();
            var providerResult = createProviderInitResult(Instant.now().plusSeconds(3600));
            var savedVideo = createPendingVideo();

            when(loadChapterForVideoOpsPort.loadChapterForVideoOps(any(Id.class)))
                    .thenReturn(Optional.of(chapter));
            when(videoProviderInitPort.initTusUpload(any(InitProviderUploadCommand.class)))
                    .thenReturn(providerResult);
            when(createPendingVideoPort.createPendingVideo(any(Id.class), anyString()))
                    .thenReturn(savedVideo);

            // WHEN
            videoUseCases.init(command);

            // THEN
            ArgumentCaptor<InitProviderUploadCommand> captor = ArgumentCaptor.forClass(InitProviderUploadCommand.class);
            verify(videoProviderInitPort).initTusUpload(captor.capture());

            InitProviderUploadCommand capturedCommand = captor.getValue();
            assertEquals(CHAPTER_TITLE, capturedCommand.title());
            assertEquals(SIZE_BYTES, capturedCommand.sizeBytes());
            assertEquals(VideoUseCases.DESCRIPTION, capturedCommand.description());
            assertEquals(VideoUseCases.PRIVACY, capturedCommand.privacy());
        }

        @Test
        @DisplayName("should use default title when chapter title is null")
        void init_whenChapterTitleIsNull_shouldUseDefaultTitle() {
            // GIVEN
            var command = createValidInitCommand();
            var chapter = createChapterWithNullTitle();
            var providerResult = createProviderInitResult(Instant.now().plusSeconds(3600));
            var savedVideo = createPendingVideo();

            when(loadChapterForVideoOpsPort.loadChapterForVideoOps(any(Id.class)))
                    .thenReturn(Optional.of(chapter));
            when(videoProviderInitPort.initTusUpload(any(InitProviderUploadCommand.class)))
                    .thenReturn(providerResult);
            when(createPendingVideoPort.createPendingVideo(any(Id.class), anyString()))
                    .thenReturn(savedVideo);

            // WHEN
            videoUseCases.init(command);

            // THEN
            ArgumentCaptor<InitProviderUploadCommand> captor = ArgumentCaptor.forClass(InitProviderUploadCommand.class);
            verify(videoProviderInitPort).initTusUpload(captor.capture());

            assertEquals(VideoUseCases.DEFAULT_TITLE, captor.getValue().title());
        }

        @Test
        @DisplayName("should use default title when chapter title is blank")
        void init_whenChapterTitleIsBlank_shouldUseDefaultTitle() {
            // GIVEN
            var command = createValidInitCommand();
            var chapter = createChapterWithBlankTitle();
            var providerResult = createProviderInitResult(Instant.now().plusSeconds(3600));
            var savedVideo = createPendingVideo();

            when(loadChapterForVideoOpsPort.loadChapterForVideoOps(any(Id.class)))
                    .thenReturn(Optional.of(chapter));
            when(videoProviderInitPort.initTusUpload(any(InitProviderUploadCommand.class)))
                    .thenReturn(providerResult);
            when(createPendingVideoPort.createPendingVideo(any(Id.class), anyString()))
                    .thenReturn(savedVideo);

            // WHEN
            videoUseCases.init(command);

            // THEN
            ArgumentCaptor<InitProviderUploadCommand> captor = ArgumentCaptor.forClass(InitProviderUploadCommand.class);
            verify(videoProviderInitPort).initTusUpload(captor.capture());

            assertEquals(VideoUseCases.DEFAULT_TITLE, captor.getValue().title());
        }

        @Test
        @DisplayName("should use default expiration when provider returns null expiresAt")
        void init_whenProviderReturnsNullExpiresAt_shouldUseDefaultExpiration() {
            // GIVEN
            var command = createValidInitCommand();
            var chapter = createChapterWithCourseRelation();
            var providerResult = createProviderInitResult(null); // null expiresAt
            var savedVideo = createPendingVideo();

            when(loadChapterForVideoOpsPort.loadChapterForVideoOps(any(Id.class)))
                    .thenReturn(Optional.of(chapter));
            when(videoProviderInitPort.initTusUpload(any(InitProviderUploadCommand.class)))
                    .thenReturn(providerResult);
            when(createPendingVideoPort.createPendingVideo(any(Id.class), anyString()))
                    .thenReturn(savedVideo);

            // WHEN
            Instant beforeCall = Instant.now();
            VideoUploadInit result = videoUseCases.init(command);

            // THEN
            assertNotNull(result.upload().expiresAt());
            // Should be approximately 1 hour from now
            Instant expectedExpiration = beforeCall.plus(1, ChronoUnit.HOURS);
            assertThat(result.upload().expiresAt())
                    .isCloseTo(expectedExpiration, within(5, ChronoUnit.SECONDS));
        }

        @Test
        @DisplayName("should throw exception when command is null")
        void init_whenCommandIsNull_shouldThrowException() {
            // WHEN + THEN
            var exception = assertThrows(IllegalArgumentException.class,
                    () -> videoUseCases.init(null));

            assertEquals("command must not be null", exception.getMessage());
            verifyNoInteractions(loadChapterForVideoOpsPort);
        }

        @Test
        @DisplayName("should throw exception when chapter not found")
        void init_whenChapterNotFound_shouldThrowException() {
            // GIVEN
            var command = createValidInitCommand();

            when(loadChapterForVideoOpsPort.loadChapterForVideoOps(any(Id.class)))
                    .thenReturn(Optional.empty());

            // WHEN + THEN
            var exception = assertThrows(IllegalStateException.class,
                    () -> videoUseCases.init(command));

            assertThat(exception.getMessage()).contains("Chapter not found");
            verify(loadChapterForVideoOpsPort).loadChapterForVideoOps(Id.of(CHAPTER_ID));
            verifyNoInteractions(videoProviderInitPort);
        }

        @Test
        @DisplayName("should throw exception when chapter does not belong to course")
        void init_whenChapterDoesNotBelongToCourse_shouldThrowException() {
            // GIVEN
            var command = createValidInitCommand();
            var chapter = createChapterFromDifferentCourse();

            when(loadChapterForVideoOpsPort.loadChapterForVideoOps(any(Id.class)))
                    .thenReturn(Optional.of(chapter));

            // WHEN + THEN
            var exception = assertThrows(IllegalStateException.class,
                    () -> videoUseCases.init(command));

            assertThat(exception.getMessage()).contains("does not belong to course");
            verifyNoInteractions(videoProviderInitPort);
        }

        @Test
        @DisplayName("should throw exception when chapter already has a video")
        void init_whenChapterAlreadyHasVideo_shouldThrowException() {
            // GIVEN
            var command = createValidInitCommand();
            var chapter = createChapterWithExistingVideo();

            when(loadChapterForVideoOpsPort.loadChapterForVideoOps(any(Id.class)))
                    .thenReturn(Optional.of(chapter));

            // WHEN + THEN
            var exception = assertThrows(IllegalStateException.class,
                    () -> videoUseCases.init(command));

            assertThat(exception.getMessage()).contains("already has a video");
            verifyNoInteractions(videoProviderInitPort);
        }

        @Test
        @DisplayName("should throw exception when chapter is missing course relation")
        void init_whenChapterMissingCourseRelation_shouldThrowException() {
            // GIVEN
            var command = createValidInitCommand();
            var chapter = createChapterWithoutCourseRelation();

            when(loadChapterForVideoOpsPort.loadChapterForVideoOps(any(Id.class)))
                    .thenReturn(Optional.of(chapter));

            // WHEN + THEN
            var exception = assertThrows(IllegalStateException.class,
                    () -> videoUseCases.init(command));

            assertThat(exception.getMessage()).contains("missing section/course relation");
        }
    }

    // ========================================================================
    // CONFIRM VIDEO UPLOAD TESTS
    // ========================================================================
    @Nested
    @DisplayName("confirm() method")
    class ConfirmVideoUpload {

        @Test
        @DisplayName("should confirm video upload successfully")
        void confirm_withValidCommand_shouldReturnProcessingVideo() {
            // GIVEN
            var command = createValidConfirmCommand();
            var chapter = createChapterWithExistingVideo();
            var processingVideo = createProcessingVideo();

            when(loadChapterForVideoOpsPort.loadChapterForVideoOps(any(Id.class)))
                    .thenReturn(Optional.of(chapter));
            when(saveVideoInfoPort.save(any(VideoInfo.class)))
                    .thenReturn(processingVideo);

            // WHEN
            VideoInfo result = videoUseCases.confirm(command);

            // THEN
            assertNotNull(result);
            assertEquals(VideoStatusEnum.PROCESSING, result.status());

            verify(loadChapterForVideoOpsPort).loadChapterForVideoOps(Id.of(CHAPTER_ID));
            verify(saveVideoInfoPort).save(any(VideoInfo.class));
            verify(enqueueVideoPollingRequestPort).enqueue(eq(VIDEO_ID), eq(1), any(Instant.class), eq(0L));
        }

        @Test
        @DisplayName("should transition video from PENDING to PROCESSING")
        void confirm_shouldTransitionVideoStatus() {
            // GIVEN
            var command = createValidConfirmCommand();
            var chapter = createChapterWithExistingVideo();

            when(loadChapterForVideoOpsPort.loadChapterForVideoOps(any(Id.class)))
                    .thenReturn(Optional.of(chapter));
            when(saveVideoInfoPort.save(any(VideoInfo.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // WHEN
            VideoInfo result = videoUseCases.confirm(command);

            // THEN
            assertNotNull(result);
            assertEquals(VideoStatusEnum.PROCESSING, result.status());

            ArgumentCaptor<VideoInfo> captor = ArgumentCaptor.forClass(VideoInfo.class);
            verify(saveVideoInfoPort).save(captor.capture());

            VideoInfo savedVideo = captor.getValue();
            assertEquals(VideoStatusEnum.PROCESSING, savedVideo.status());
        }

        @Test
        @DisplayName("should enqueue polling request after confirmation")
        void confirm_shouldEnqueuePollingRequest() {
            // GIVEN
            var command = createValidConfirmCommand();
            var chapter = createChapterWithExistingVideo();
            var processingVideo = createProcessingVideo();

            when(loadChapterForVideoOpsPort.loadChapterForVideoOps(any(Id.class)))
                    .thenReturn(Optional.of(chapter));
            when(saveVideoInfoPort.save(any(VideoInfo.class)))
                    .thenReturn(processingVideo);

            // WHEN
            Instant beforeCall = Instant.now();
            VideoInfo result = videoUseCases.confirm(command);
            Instant afterCall = Instant.now();

            // THEN
            assertNotNull(result);
            ArgumentCaptor<Instant> instantCaptor = ArgumentCaptor.forClass(Instant.class);
            verify(enqueueVideoPollingRequestPort).enqueue(
                    eq(VIDEO_ID),
                    eq(1),
                    instantCaptor.capture(),
                    eq(0L)
            );

            Instant capturedInstant = instantCaptor.getValue();
            assertThat(capturedInstant).isBetween(beforeCall, afterCall);
        }

        @Test
        @DisplayName("should throw exception when command is null")
        void confirm_whenCommandIsNull_shouldThrowException() {
            // WHEN + THEN
            var exception = assertThrows(IllegalArgumentException.class,
                    () -> videoUseCases.confirm(null));

            assertEquals("command must not be null", exception.getMessage());
            verifyNoInteractions(loadChapterForVideoOpsPort);
        }

        @Test
        @DisplayName("should throw exception when chapter not found")
        void confirm_whenChapterNotFound_shouldThrowException() {
            // GIVEN
            var command = createValidConfirmCommand();

            when(loadChapterForVideoOpsPort.loadChapterForVideoOps(any(Id.class)))
                    .thenReturn(Optional.empty());

            // WHEN + THEN
            var exception = assertThrows(IllegalStateException.class,
                    () -> videoUseCases.confirm(command));

            assertThat(exception.getMessage()).contains("Chapter not found");
            verifyNoInteractions(saveVideoInfoPort);
            verifyNoInteractions(enqueueVideoPollingRequestPort);
        }

        @Test
        @DisplayName("should throw exception when chapter does not belong to course")
        void confirm_whenChapterDoesNotBelongToCourse_shouldThrowException() {
            // GIVEN
            var command = createValidConfirmCommand();
            var chapter = createChapterFromDifferentCourse();
            // Add a video to this chapter
            chapter.setVideo(createPendingVideo());

            when(loadChapterForVideoOpsPort.loadChapterForVideoOps(any(Id.class)))
                    .thenReturn(Optional.of(chapter));

            // WHEN + THEN
            var exception = assertThrows(IllegalStateException.class,
                    () -> videoUseCases.confirm(command));

            assertThat(exception.getMessage()).contains("does not belong to course");
            verifyNoInteractions(saveVideoInfoPort);
        }

        @Test
        @DisplayName("should throw exception when no video initialized for chapter")
        void confirm_whenNoVideoInitialized_shouldThrowException() {
            // GIVEN
            var command = createValidConfirmCommand();
            var chapter = createChapterWithCourseRelation(); // No video

            when(loadChapterForVideoOpsPort.loadChapterForVideoOps(any(Id.class)))
                    .thenReturn(Optional.of(chapter));

            // WHEN + THEN
            var exception = assertThrows(IllegalStateException.class,
                    () -> videoUseCases.confirm(command));

            assertThat(exception.getMessage()).contains("No initialized video for chapter");
            verifyNoInteractions(saveVideoInfoPort);
        }

        @Test
        @DisplayName("should throw exception when video has no sourceUri")
        void confirm_whenVideoHasNoSourceUri_shouldThrowException() {
            // GIVEN
            var command = createValidConfirmCommand();
            var chapter = createChapterWithCourseRelation();
            var videoWithoutSourceUri = VideoInfo.builder()
                    .id(Id.of(VIDEO_ID))
                    .sourceUri(null)
                    .status(VideoStatusEnum.PENDING)
                    .externalDeletionStatus(ExternalDeletionStatus.NONE)
                    .build();
            chapter.setVideo(videoWithoutSourceUri);

            when(loadChapterForVideoOpsPort.loadChapterForVideoOps(any(Id.class)))
                    .thenReturn(Optional.of(chapter));

            // WHEN + THEN
            var exception = assertThrows(IllegalStateException.class,
                    () -> videoUseCases.confirm(command));

            assertThat(exception.getMessage()).contains("has no sourceUri");
            verifyNoInteractions(saveVideoInfoPort);
        }

        @Test
        @DisplayName("should throw exception when video has blank sourceUri")
        void confirm_whenVideoHasBlankSourceUri_shouldThrowException() {
            // GIVEN
            var command = createValidConfirmCommand();
            var chapter = createChapterWithCourseRelation();
            var videoWithBlankSourceUri = VideoInfo.builder()
                    .id(Id.of(VIDEO_ID))
                    .sourceUri("   ")
                    .status(VideoStatusEnum.PENDING)
                    .externalDeletionStatus(ExternalDeletionStatus.NONE)
                    .build();
            chapter.setVideo(videoWithBlankSourceUri);

            when(loadChapterForVideoOpsPort.loadChapterForVideoOps(any(Id.class)))
                    .thenReturn(Optional.of(chapter));

            // WHEN + THEN
            var exception = assertThrows(IllegalStateException.class,
                    () -> videoUseCases.confirm(command));

            assertThat(exception.getMessage()).contains("has no sourceUri");
            verifyNoInteractions(saveVideoInfoPort);
        }
    }

    // ========================================================================
    // HELPER METHOD TESTS (via integration)
    // ========================================================================
    @Nested
    @DisplayName("Helper methods (tested via public methods)")
    class HelperMethods {

        @Test
        @DisplayName("normalizeNullable should handle various input")
        void normalizeNullable_shouldHandleVariousInput() {
            // This is tested indirectly through init()
            // When chapter title is "  trimmed  ", it should be "trimmed"
            var command = createValidInitCommand();
            var chapter = createChapterWithCourseRelation();
            chapter.setTitle("  Trimmed Title  ");
            var providerResult = createProviderInitResult(Instant.now().plusSeconds(3600));
            var savedVideo = createPendingVideo();

            when(loadChapterForVideoOpsPort.loadChapterForVideoOps(any(Id.class)))
                    .thenReturn(Optional.of(chapter));
            when(videoProviderInitPort.initTusUpload(any(InitProviderUploadCommand.class)))
                    .thenReturn(providerResult);
            when(createPendingVideoPort.createPendingVideo(any(Id.class), anyString()))
                    .thenReturn(savedVideo);

            // WHEN
            videoUseCases.init(command);

            // THEN
            ArgumentCaptor<InitProviderUploadCommand> captor = ArgumentCaptor.forClass(InitProviderUploadCommand.class);
            verify(videoProviderInitPort).initTusUpload(captor.capture());

            // Title should be trimmed
            assertEquals("Trimmed Title", captor.getValue().title());
        }
    }
}
