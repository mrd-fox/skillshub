package com.simplon_project.skillhub.skillhub.course.application.port.out.chapter;

import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;

public interface CheckVideoExistsForChapterPort {

    boolean checkVideoExistsForChapter(EntityId chapterId);
}
