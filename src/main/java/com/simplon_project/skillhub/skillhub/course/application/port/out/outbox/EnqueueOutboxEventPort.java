package com.simplon_project.skillhub.skillhub.course.application.port.out.outbox;

import com.simplon_project.skillhub.skillhub.course.domain.model.Id;


public interface EnqueueOutboxEventPort {
    void enqueueVideoDeletionRequested(Id videoId, String sourceUri);
}
