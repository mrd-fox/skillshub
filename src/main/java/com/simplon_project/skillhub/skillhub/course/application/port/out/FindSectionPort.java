package com.simplon_project.skillhub.skillhub.course.application.port.out;

import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import com.simplon_project.skillhub.skillhub.course.domain.model.Section;

public interface FindSectionPort {
    Section find(Id id);
}
