package com.simplon_project.skillhub.skillhub.course.application.usecase;

import com.simplon_project.skillhub.skillhub.course.application.dto.UploadInstructions;
import com.simplon_project.skillhub.skillhub.course.application.dto.VideoUploadInit;
import com.simplon_project.skillhub.skillhub.course.application.port.in.ConfirmVideoUploadPort;
import com.simplon_project.skillhub.skillhub.course.application.port.in.InitVideoInChapterPort;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.ConfirmVideoUploadCommand;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.InitProviderUploadCommand;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.InitVideoCommand;
import com.simplon_project.skillhub.skillhub.course.application.port.out.chapter.LoadChapterForVideoOpsPort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.video.CreatePendingVideoForChapterPort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.video.EnqueueVideoPollingRequestPort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.video.SaveVideoInfoPort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.video.VideoProviderInitPort;
import com.simplon_project.skillhub.skillhub.course.domain.model.Chapter;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import com.simplon_project.skillhub.skillhub.course.domain.model.VideoInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoUseCases implements InitVideoInChapterPort, ConfirmVideoUploadPort {

    public static final String PROVIDER_NAME = "VIMEO";
    public static final String DESCRIPTION = "";
    public static final String PRIVACY = "unlisted";
    public static final String DEFAULT_TITLE = "";

    private final LoadChapterForVideoOpsPort loadChapterForVideoOpsPort;
    private final CreatePendingVideoForChapterPort createPendingVideoPort;
    private final VideoProviderInitPort videoProviderInitPort;

    private final SaveVideoInfoPort saveVideoInfoPort;
    private final EnqueueVideoPollingRequestPort enqueueVideoPollingRequestPort;

    @Override
    @Transactional("courseTxManager")
    public VideoUploadInit init(InitVideoCommand command) {

        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }

        var chapter = loadChapterForVideoOpsPort.loadChapterForVideoOps(Id.of(command.chapterId()))
                .orElseThrow(() -> new IllegalStateException("Chapter not found: " + command.chapterId()));

        var fetchedCourseId = extractCourseIdFromDomain(chapter);

        if (!(Id.of(command.courseId())).equals(fetchedCourseId)) {
            throw new IllegalStateException(
                    "Chapter %s does not belong to course %s".formatted(command.chapterId(), command.courseId())
            );
        }

        if (chapter.getVideo() != null) {
            throw new IllegalStateException("Chapter already has a video. Replace flow is not implemented yet.");
        }

        var title = normalizeNullable(chapter.getTitle());
        if (title == null) {
            title = DEFAULT_TITLE;
        }

        var providerCommand = new InitProviderUploadCommand(
                command.sizeBytes(),
                title,
                DESCRIPTION,
                PRIVACY
        );

        var providerResult = videoProviderInitPort.initTusUpload(providerCommand);

        var savedVideo = createPendingVideoPort.createPendingVideo(
                chapter.getId(),
                providerResult.sourceUri()
        );

        var expiresAt = providerResult.expiresAt();
        if (expiresAt == null) {
            expiresAt = Instant.now().plus(java.time.Duration.ofHours(1));
        }

        return new VideoUploadInit(
                savedVideo,
                new UploadInstructions(
                        PROVIDER_NAME,
                        providerResult.uploadUrl(),
                        expiresAt
                )
        );
    }


// -----------------------
    // CONFIRM
    // -----------------------

    @Override
    @Transactional("courseTxManager")
    public VideoInfo confirm(ConfirmVideoUploadCommand command) {

        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }

        var courseId = Id.of(command.courseId());
        var chapterId = Id.of(command.chapterId());

        var chapter = loadChapterForVideoOpsPort.loadChapterForVideoOps(chapterId)
                .orElseThrow(() -> new IllegalStateException("Chapter not found: " + command.chapterId()));

        var fetchedCourseId = extractCourseIdFromDomain(chapter);

        if (!courseId.equals(fetchedCourseId)) {
            throw new IllegalStateException(
                    "Chapter %s does not belong to course %s".formatted(command.chapterId(), command.courseId())
            );
        }

        var current = chapter.getVideo();
        if (current == null) {
            throw new IllegalStateException("No initialized video for chapter " + command.chapterId());
        }

        // Domain transition: PENDING -> PROCESSING (and validates sourceUri)
        var processing = current.markProcessing(command.sourceUri());

        var saved = saveVideoInfoPort.save(processing);

        // First poll is immediate (attempt=0). requestedAt=now. delayMs=0.
        enqueueVideoPollingRequestPort.enqueue(
                saved.id().asString(),
                0,
                Instant.now(),
                0L
        );

        return saved;
    }

    // -----------------------
    // Helpers
    // -----------------------
    private static Id extractCourseIdFromDomain(Chapter chapter) {
        if (chapter.getSection() == null
                || chapter.getSection().getCourse() == null
                || chapter.getSection().getCourse().getId() == null) {
            throw new IllegalStateException("Chapter is missing section/course relation (data integrity issue)");
        }
        return chapter.getSection().getCourse().getId();
    }

    private static String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed;
    }
}