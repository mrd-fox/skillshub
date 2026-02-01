package com.simplon_project.skillhub.skillhub.course.application.port.out.chapter;

import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;

public interface CheckChapterBelongsToCoursePort {

    boolean checkChapterBelongsToCoursePort(EntityId chapterId, EntityId courseId);

}
