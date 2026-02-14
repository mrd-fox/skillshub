package com.simplon_project.skillhub.skillhub.user.application.usecase;

import com.simplon_project.skillhub.skillhub.user.application.port.in.EnrollInCoursePort;
import com.simplon_project.skillhub.skillhub.user.application.port.in.command.EnrollInCourseCommand;
import com.simplon_project.skillhub.skillhub.user.application.port.out.CreateUserCourseEnrollmentPort;
import com.simplon_project.skillhub.skillhub.user.application.port.out.LoadInternalUserIdByExternalIdPort;
import com.simplon_project.skillhub.skillhub.user.domain.exception.ForbiddenException;
import com.simplon_project.skillhub.skillhub.user.domain.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case for enrolling a user in a course.
 * Validates STUDENT role and creates enrollment if user exists.
 */
@Service
@RequiredArgsConstructor
@Transactional(transactionManager = "userTxManager")
public class EnrollInCourseUseCase implements EnrollInCoursePort {

    private final LoadInternalUserIdByExternalIdPort loadInternalUserIdPort;
    private final CreateUserCourseEnrollmentPort createEnrollmentPort;

    @Override
    public void enroll(EnrollInCourseCommand command) {
        if (!command.hasStudentRole()) {
            throw new ForbiddenException("User must have STUDENT role to enroll in a course");
        }

        UUID externalUserId = command.externalUserId();

        UUID internalUserId = loadInternalUserIdPort
                .loadInternalUserId(externalUserId)
                .orElseThrow(() -> new UserNotFoundException(
                        externalUserId.toString(),
                        "User not found with external ID"
                ));

        UUID courseId = command.courseId();

        createEnrollmentPort.createIfAbsent(internalUserId, courseId);
    }
}