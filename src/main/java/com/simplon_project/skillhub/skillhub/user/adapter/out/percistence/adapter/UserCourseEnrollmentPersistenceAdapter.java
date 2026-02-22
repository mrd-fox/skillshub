package com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.adapter;

import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.entity.UserCourseEntity;
import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.entity.UserCourseId;
import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.repository.JpaUserCourseRepository;
import com.simplon_project.skillhub.skillhub.user.application.port.out.CreateUserCourseEnrollmentPort;
import com.simplon_project.skillhub.skillhub.user.application.port.out.LoadEnrolledCourseIdsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Persistence adapter for user-course enrollment operations.
 * Implements both creation and retrieval of enrollments.
 */
@Component
@RequiredArgsConstructor
public class UserCourseEnrollmentPersistenceAdapter implements CreateUserCourseEnrollmentPort, LoadEnrolledCourseIdsPort {

    private final JpaUserCourseRepository userCourseRepository;

    /**
     * Create a user-course enrollment if it does not already exist (idempotent).
     * Uses composite primary key (user_id, course_id).
     *
     * @param internalUserId the internal user UUID (user_account.id)
     * @param courseId       the course UUID
     */
    @Override
    public void createIfAbsent(UUID internalUserId, UUID courseId) {
        // Create composite primary key
        UserCourseId id = UserCourseId.of(internalUserId, courseId);

        // Check if enrollment already exists (idempotent)
        if (userCourseRepository.existsById(id)) {
            // Already enrolled - do nothing
        } else {
            // Create new enrollment
            UserCourseEntity enrollment = UserCourseEntity.builder()
                    .id(id)
                    .createdAt(Instant.now())
                    .build();

            userCourseRepository.save(enrollment);
        }
    }

    /**
     * Load all course IDs that a user is enrolled in.
     *
     * @param internalUserId the internal user UUID (user_account.id)
     * @return list of course UUIDs
     */
    @Override
    public List<UUID> loadCourseIds(UUID internalUserId) {
        return userCourseRepository.findAllCourseIdsByUserId(internalUserId);
    }
}


