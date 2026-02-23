package com.simplon_project.skillhub.skillhub.helpers.mothers;

import com.simplon_project.skillhub.skillhub.course.domain.enums.CourseStatusEnum;
import com.simplon_project.skillhub.skillhub.course.domain.enums.ExternalDeletionStatus;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import com.simplon_project.skillhub.skillhub.course.domain.model.VideoInfo;
import com.simplon_project.skillhub.skillhub.helpers.builders.ChapterBuilder;
import com.simplon_project.skillhub.skillhub.helpers.builders.CourseBuilder;
import com.simplon_project.skillhub.skillhub.helpers.builders.SectionBuilder;
import com.simplon_project.skillhub.skillhub.helpers.builders.VideoBuilder;

public final class CourseMother {

    private CourseMother() {
    }

    // --------------------------------------------------------
    // BASIC COURSES
    // --------------------------------------------------------

    public static Course draftOwnedBy(String externalUserId) {
        return CourseBuilder.aCourse()
                .withExternalUserId(externalUserId)
                .withStatus(CourseStatusEnum.DRAFT)
                .withPrice(0L)
                .build();
    }

    public static Course publishedOwnedBy(String externalUserId) {
        return CourseBuilder.aCourse()
                .withExternalUserId(externalUserId)
                .withStatus(CourseStatusEnum.PUBLISHED)
                .withPrice(100L)
                .build();
    }

    // --------------------------------------------------------
    // STRUCTURE WITHOUT VIDEO
    // --------------------------------------------------------

    public static Course withSingleSectionAndChapter(String externalUserId) {
        var chapter = ChapterBuilder.aChapter().build();
        var section = SectionBuilder.aSection()
                .withChapter(chapter)
                .build();

        return CourseBuilder.aCourse()
                .withExternalUserId(externalUserId)
                .withPrice(4999L)
                .withSection(section)
                .build();
    }

    // --------------------------------------------------------
    // VIDEO STATES (IMPORTANT FOR DELETE TESTS)
    // --------------------------------------------------------

    public static Course withVideoNone(String externalUserId) {
        VideoInfo video = VideoBuilder.aVideo()
                .withExternalDeletionStatus(ExternalDeletionStatus.NONE)
                .build();

        var chapter = ChapterBuilder.aChapter()
                .withVideo(video)
                .build();

        var section = SectionBuilder.aSection()
                .withChapter(chapter)
                .build();

        return CourseBuilder.aCourse()
                .withExternalUserId(externalUserId)
                .withSection(section)
                .build();
    }

    public static Course withVideoAlreadyRequested(String externalUserId) {
        VideoInfo video = VideoBuilder.aVideo()
                .withExternalDeletionStatus(ExternalDeletionStatus.REQUESTED)
                .build();

        var chapter = ChapterBuilder.aChapter()
                .withVideo(video)
                .build();

        var section = SectionBuilder.aSection()
                .withChapter(chapter)
                .build();

        return CourseBuilder.aCourse()
                .withExternalUserId(externalUserId)
                .withSection(section)
                .build();
    }

    public static Course withVideoDeleted(String externalUserId) {
        VideoInfo video = VideoBuilder.aVideo()
                .withExternalDeletionStatus(ExternalDeletionStatus.DELETED)
                .build();

        var chapter = ChapterBuilder.aChapter()
                .withVideo(video)
                .build();

        var section = SectionBuilder.aSection()
                .withChapter(chapter)
                .build();

        return CourseBuilder.aCourse()
                .withExternalUserId(externalUserId)
                .withSection(section)
                .build();
    }

    // --------------------------------------------------------
    // COMPLEX CASE: MULTIPLE VIDEOS
    // --------------------------------------------------------

    public static Course withTwoVideos(String externalUserId) {
        var video1 = VideoBuilder.aVideo().build();
        var video2 = VideoBuilder.aVideo().build();

        var chapter1 = ChapterBuilder.aChapter().withVideo(video1).build();
        var chapter2 = ChapterBuilder.aChapter().withVideo(video2).build();

        var section = SectionBuilder.aSection()
                .withChapter(chapter1)
                .withChapter(chapter2)
                .build();

        return CourseBuilder.aCourse()
                .withExternalUserId(externalUserId)
                .withSection(section)
                .build();
    }
}