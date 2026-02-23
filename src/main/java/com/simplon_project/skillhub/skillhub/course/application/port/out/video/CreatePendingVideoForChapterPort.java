package com.simplon_project.skillhub.skillhub.course.application.port.out.video;

import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import com.simplon_project.skillhub.skillhub.course.domain.model.VideoInfo;

public interface CreatePendingVideoForChapterPort {

    VideoInfo createPendingVideo(Id chapterId, String sourceUri);
}
