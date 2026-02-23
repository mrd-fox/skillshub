package com.simplon_project.skillhub.skillhub.course.application.port.in;

import com.simplon_project.skillhub.skillhub.course.application.port.in.command.CreateChapterCommand;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;

public interface CreateChapterPort {
    Course createChapter(CreateChapterCommand command);
}
