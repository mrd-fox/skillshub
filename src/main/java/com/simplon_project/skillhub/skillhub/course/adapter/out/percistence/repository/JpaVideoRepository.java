package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.repository;

import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.VideoEntity;
import com.simplon_project.skillhub.skillhub.course.domain.enums.VideoStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface JpaVideoRepository extends JpaRepository<VideoEntity, EntityId> {


    @Query("""
                SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END
                FROM VideoEntity v
                WHERE v.chapter.chapterId = :chapterId
                  AND v.deletedAt IS NULL
                  AND v.chapter.deletedAt IS NULL
            """)
    boolean existsByChapterId(@Param("chapterId") EntityId chapterId);

    /**
     * Check if the course contains at least one video with status in the provided set.
     * Respects soft deletion: only non-soft-deleted videos, chapters, sections, and courses are checked.
     *
     * @param courseId the course ID
     * @param statuses the set of video statuses to check (e.g., PENDING, PROCESSING)
     * @return true if at least one video exists with matching status, false otherwise
     */
    @Query("""
                SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END
                FROM VideoEntity v
                JOIN v.chapter c
                JOIN c.section s
                JOIN s.course co
                WHERE co.courseId = :courseId
                  AND v.status IN :statuses
                  AND v.deletedAt IS NULL
                  AND c.deletedAt IS NULL
                  AND s.deletedAt IS NULL
                  AND co.deletedAt IS NULL
            """)
    boolean existsInFlightVideoForCourse(@Param("courseId") EntityId courseId,
                                         @Param("statuses") Set<VideoStatusEnum> statuses);
}
