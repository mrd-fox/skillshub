package com.simplon_project.skillhub.skillhub.course.adapter.common.exception;

import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class SectionNotFoundException extends AbstractThrowableProblem {
    public SectionNotFoundException(Id sectionId) {
        super(null, "section-not-found", Status.BAD_REQUEST, String.format("section with id %s not found", sectionId.asString()));

    }
}
