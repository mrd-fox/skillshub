package com.simplon_project.skillhub.skillhub.course.application.port.out.chapter;

import com.simplon_project.skillhub.skillhub.course.domain.model.Id;

public interface CheckChapterBelongsToCoursePort {

    boolean checkChapterBelongsToCoursePort(Id chapterId, Id courseId);

}
