package com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.repository;

import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.entity.UserCourseEntity;
import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.entity.UserCourseId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface JpaUserCourseRepository extends JpaRepository<UserCourseEntity, UserCourseId> {

    default boolean existsEnrollment(UUID userId, UUID courseId) {
        return existsById(UserCourseId.of(userId, courseId));
    }

    default Optional<UserCourseEntity> findEnrollment(UUID userId, UUID courseId) {
        return findById(UserCourseId.of(userId, courseId));
    }

    @Query("SELECT uc.id.courseId FROM UserCourseEntity uc WHERE uc.id.userId = :userId")
    List<UUID> findAllCourseIdsByUserId(@Param("userId") UUID userId);

    @Query("SELECT uc.id.userId FROM UserCourseEntity uc WHERE uc.id.courseId = :courseId")
    List<UUID> findAllUserIdsByCourseId(@Param("courseId") UUID courseId);
}