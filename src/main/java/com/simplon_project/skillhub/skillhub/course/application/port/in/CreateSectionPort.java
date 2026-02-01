package com.simplon_project.skillhub.skillhub.course.application.port.in;

import com.simplon_project.skillhub.skillhub.course.application.port.in.command.CreateChapterCommand;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.CreateSectionCommand;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;

public interface CreateSectionPort {
    Course createSection(CreateSectionCommand command);
}
