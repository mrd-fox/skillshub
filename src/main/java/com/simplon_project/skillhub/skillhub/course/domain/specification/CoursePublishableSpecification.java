package com.simplon_project.skillhub.skillhub.course.domain.specification;

import com.simplon_project.skillhub.skillhub.course.domain.enums.VideoStatusEnum;
import com.simplon_project.skillhub.skillhub.course.domain.exception.CourseNotPublishableException;
import com.simplon_project.skillhub.skillhub.course.domain.model.Chapter;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import com.simplon_project.skillhub.skillhub.course.domain.model.Section;

import java.util.ArrayList;
import java.util.List;

/**
 * Specification to validate if a course is ready to be published.
 * A course is publishable if:
 * - It has at least one section with at least one chapter
 * - All chapters have a video
 * - All videos are in READY status
 */
public class CoursePublishableSpecification {

    private CoursePublishableSpecification() {
        // Utility class
    }

    /**
     * Validates if a course is publishable.
     *
     * @param course the course to validate
     * @throws CourseNotPublishableException if the course is not publishable
     */
    public static void check(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("Course cannot be null");
        }

        List<String> errors = new ArrayList<>();

        // Check if course has sections
        if (course.getSections() == null || course.getSections().isEmpty()) {
            errors.add("Course has no sections");
        } else {
            // Check each section has chapters and all chapters have ready videos
            boolean hasAtLeastOneChapter = false;

            for (Section section : course.getSections()) {
                if (section.getChapters() == null || section.getChapters().isEmpty()) {
                    continue;
                }

                for (Chapter chapter : section.getChapters()) {
                    hasAtLeastOneChapter = true;

                    // Check if chapter has a video
                    if (chapter.getVideo() == null) {
                        errors.add(String.format("Chapter '%s' (id: %s) has no video",
                                chapter.getTitle(),
                                chapter.getId() != null ? chapter.getId().asString() : "unknown"));
                        continue;
                    }

                    // Check if video is in READY status
                    var video = chapter.getVideo();
                    if (video.status() != VideoStatusEnum.READY) {
                        errors.add(String.format("Chapter '%s' (id: %s) has video with status %s (expected READY)",
                                chapter.getTitle(),
                                chapter.getId() != null ? chapter.getId().asString() : "unknown",
                                video.status()));
                    }
                }
            }

            if (!hasAtLeastOneChapter) {
                errors.add("Course has no chapters");
            }
        }

        if (!errors.isEmpty()) {
            throw new CourseNotPublishableException(String.join("; ", errors));
        }
    }

    /**
     * Check if a course satisfies the publishability criteria.
     *
     * @param course the course to check
     * @return true if the course is publishable, false otherwise
     */
    public static boolean isSatisfiedBy(Course course) {
        try {
            check(course);
            return true;
        } catch (CourseNotPublishableException e) {
            return false;
        }
    }
}
