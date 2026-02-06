package com.simplon_project.skillhub.skillhub.course.application.port.out.chapter;

import com.simplon_project.skillhub.skillhub.course.domain.model.Chapter;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;

import java.util.Optional;

public interface LoadChapterByIdPort {

    Optional<Chapter> loadChapterById(Id chapterId);
}
