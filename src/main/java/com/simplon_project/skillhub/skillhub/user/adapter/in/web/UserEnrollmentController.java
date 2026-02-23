package com.simplon_project.skillhub.skillhub.user.adapter.in.web;

import com.simplon_project.skillhub.skillhub.user.application.port.in.EnrollInCoursePort;
import com.simplon_project.skillhub.skillhub.user.application.port.in.command.EnrollInCourseCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Internal REST controller for user enrollment operations.
 * This controller is designed for internal service-to-service communication.
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserEnrollmentController {

    private final EnrollInCoursePort enrollInCoursePort;

    @PostMapping("/external/{externalUserId}/enrollments/{courseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enrollInCourse(
            @PathVariable String externalUserId,
            @PathVariable String courseId,
            @RequestHeader("X-User-Roles") String rawRoles
    ) {
        EnrollInCourseCommand command = EnrollInCourseCommand.of(externalUserId, courseId, rawRoles);
        enrollInCoursePort.enroll(command);
    }
}