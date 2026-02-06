package com.simplon_project.skillhub.skillhub.course.application.port.out.chapter;

import com.simplon_project.skillhub.skillhub.course.domain.model.Id;

public interface CheckVideoExistsForChapterPort {

    boolean checkVideoExistsForChapter(Id chapterId);
}
