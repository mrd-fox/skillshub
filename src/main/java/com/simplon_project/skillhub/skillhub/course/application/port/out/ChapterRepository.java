package com.simplon_project.skillhub.skillhub.course.application.port.out;

import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.ChapterEntity;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;

import java.util.Optional;

public interface ChapterRepository {

    Optional<ChapterEntity> findById(EntityId chapterId);

    Optional<ChapterEntity> findByIdWithSectionAndCourse(EntityId chapterId);

    boolean belongsToCourse(EntityId chapterId, EntityId courseId);
}
