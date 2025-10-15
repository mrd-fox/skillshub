package com.simplon_project.skillhub.skillhub.course.application.usecase;

import com.simplon_project.skillhub.skillhub.course.adapter.messaging.events.VideoMetadataExtractedEvent;
import com.simplon_project.skillhub.skillhub.course.adapter.messaging.events.VideoUploadedEvent;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.VideoEntity;
import com.simplon_project.skillhub.skillhub.course.application.port.in.ProcessExtractedMetadataPort;
import com.simplon_project.skillhub.skillhub.course.application.port.in.ProcessUploadVideoPort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.ChapterRepository;
import com.simplon_project.skillhub.skillhub.course.application.port.out.VideoRepository;
import com.simplon_project.skillhub.skillhub.course.domain.enums.VideoStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoUseCases implements ProcessUploadVideoPort, ProcessExtractedMetadataPort {

    private final ChapterRepository chapterRepository;
    private final VideoRepository videoRepository;

    @Override
    public void processUploadedVideo(VideoUploadedEvent event) {

        var courseId = EntityId.fromString(event.courseId());
        var chapterId = EntityId.fromString(event.chapterId());
        var videoId = EntityId.fromString(event.videoId());

        var chapter = chapterRepository.findByIdWithSectionAndCourse(chapterId)
                .orElseThrow(() -> new IllegalStateException("Chapter not found"));

        if (!chapter.getSection().getCourse().getId().equals(courseId)) {
            throw new IllegalStateException(
                    "Chapter %s does not belong to course %s".formatted(chapterId, courseId)
            );
        }
        var video = VideoEntity.builder()
                .videoId(videoId)
                .storageKey(event.storageKey())
                .format(event.format())
                .size(event.sizeBytes())
                .status(VideoStatusEnum.UPLOADED)
                .chapter(chapter)
                .build();
        videoRepository.save(video);
    }

    @Override
    public void processVideoMetadata(VideoMetadataExtractedEvent event) {

        var videoId = EntityId.fromString(event.videoId());

        var video = videoRepository.findById(videoId)
                .orElseThrow(() -> new IllegalStateException("Video not found: " + videoId));

        video.setWidth(event.width());
        video.setHeight(event.height());
        video.setDuration(event.duration());
        video.setStatus(VideoStatusEnum.valueOf(event.status()));

        videoRepository.save(video);
    }
}
