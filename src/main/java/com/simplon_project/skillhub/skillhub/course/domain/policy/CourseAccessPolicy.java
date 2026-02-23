package com.simplon_project.skillhub.skillhub.course.domain.policy;

import com.simplon_project.skillhub.skillhub.course.domain.enums.AccessLevelEnum;
import com.simplon_project.skillhub.skillhub.course.domain.enums.UserRole;

import java.util.Set;

public class CourseAccessPolicy {


    /**
     * Determines the access level for a given user and course.
     *
     * @param roles          roles injected by Gateway (may be empty, never null)
     * @param externalUserId authenticated user id (null for anonymous)
     * @param courseAuthorId author id stored on the course
     * @param isEnrolled     whether the user is enrolled to the course
     * @return resolved AccessLevel
     */
    public static AccessLevelEnum resolveAccess(
            Set<UserRole> roles,
            String externalUserId,
            String courseAuthorId,
            boolean isEnrolled
    ) {

        // ADMIN: transverse power, full visibility, never author
        if (roles.contains(UserRole.ADMIN)) {
            return AccessLevelEnum.ADMIN;
        }

        // AUTHOR: tutor owning the course
        if (roles.contains(UserRole.TUTOR)
                && externalUserId != null
                && externalUserId.equals(courseAuthorId)) {
            return AccessLevelEnum.AUTHOR;
        }

        // ENROLLED: student or tutor-as-student with valid enrollment
        if (isEnrolled) {
            return AccessLevelEnum.ENROLLED;
        }

        // PUBLIC: anonymous or non-enrolled user
        return AccessLevelEnum.PUBLIC;
    }

}
