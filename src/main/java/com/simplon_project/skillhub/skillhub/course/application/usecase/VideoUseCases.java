package com.simplon_project.skillhub.skillhub.course.application.usecase;

import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.course.application.dto.UploadInstructions;
import com.simplon_project.skillhub.skillhub.course.application.dto.VideoUploadInit;
import com.simplon_project.skillhub.skillhub.course.application.port.in.InitVideoInChapterPort;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.InitProviderUploadCommand;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.InitVideoCommand;
import com.simplon_project.skillhub.skillhub.course.application.port.out.ChapterRepository;
import com.simplon_project.skillhub.skillhub.course.application.port.out.VideoProviderInitPort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.VideoRepository;
import com.simplon_project.skillhub.skillhub.course.domain.enums.VideoStatusEnum;
import com.simplon_project.skillhub.skillhub.course.domain.model.Chapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoUseCases implements InitVideoInChapterPort {

    public static final String PROVIDER_NAME = "VIMEO";
    public static final String DESCRIPTION = "";
    public static final String PRIVACY = "private";
    public static final String DEFAULT_TITLE = "";
    private final ChapterRepository chapterRepository; // your port (currently returns entities)
    private final VideoRepository videoRepository;     // your port (currently returns entities)
    private final VideoProviderInitPort videoProviderInitPort;

    @Override
    public VideoUploadInit init(InitVideoCommand command) {

        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }

        var courseId = EntityId.fromString(command.courseId());
        var chapterId = EntityId.fromString(command.chapterId());

        var chapter = chapterRepository.findByIdWithSectionAndCourse(chapterId)
                .orElseThrow(() -> new IllegalStateException("Chapter not found: " + command.chapterId()));

        var fetchedCourseId = extractCourseIdFromDomain(chapter);

        if (!courseId.equals(fetchedCourseId)) {
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

        var savedVideo = videoRepository.createPendingVideo(
                chapterId,
                providerResult.sourceUri(),
                VideoStatusEnum.PENDING
        );

        return new VideoUploadInit(
                savedVideo,
                new UploadInstructions(
                        PROVIDER_NAME,
                        providerResult.uploadUrl(),
                        providerResult.expiresAt()
                )
        );
    }

    private static EntityId extractCourseIdFromDomain(Chapter chapter) {
        if (chapter.getSection() == null
                || chapter.getSection().getCourse() == null
                || chapter.getSection().getCourse().getId() == null) {
            throw new IllegalStateException("Chapter is missing section/course relation (data integrity issue)");
        }
        return EntityId.fromString(chapter.getSection().getCourse().getId().asString());
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