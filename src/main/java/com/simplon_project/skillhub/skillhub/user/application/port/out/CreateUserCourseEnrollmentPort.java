package com.simplon_project.skillhub.skillhub.user.application.port.out;

import java.util.UUID;

/**
 * Output port for creating user-course enrollment.
 */
public interface CreateUserCourseEnrollmentPort {

    /**
     * Create a user-course enrollment if it does not already exist (idempotent).
     *
     * @param internalUserId the internal user UUID (user_account.id)
     * @param courseId       the course UUID
     */
    void createIfAbsent(UUID internalUserId, UUID courseId);
}

