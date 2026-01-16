package com.simplon_project.skillhub.skillhub.course.application.port.out;

import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.course.domain.model.Chapter;

import java.util.Optional;

public interface ChapterRepository {

    Optional<Chapter> findById(EntityId chapterId);

    Optional<Chapter> findByIdWithSectionAndCourse(EntityId chapterId);

    boolean belongsToCourse(EntityId chapterId, EntityId courseId);
}
