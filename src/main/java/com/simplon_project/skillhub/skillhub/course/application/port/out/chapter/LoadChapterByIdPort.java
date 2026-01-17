package com.simplon_project.skillhub.skillhub.course.application.port.out.chapter;

import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.course.domain.model.Chapter;

import java.util.Optional;

public interface LoadChapterByIdPort {

    Optional<Chapter> loadChapterById(EntityId chapterId);
}
