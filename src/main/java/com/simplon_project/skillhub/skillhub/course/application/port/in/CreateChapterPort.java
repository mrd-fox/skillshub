package com.simplon_project.skillhub.skillhub.course.application.port.in;

import com.simplon_project.skillhub.skillhub.course.application.port.in.command.CreateChapterCommand;

public interface CreateChapterPort {
    void createChapter(CreateChapterCommand command);
}
