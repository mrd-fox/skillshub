package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.repository;

import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.ChapterEntity;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JpaChapterRepository extends JpaRepository<ChapterEntity, EntityId> {

    /**
     * Load a chapter with its section and course (for ownership validation).
     * Soft-delete filtering is enforced by:
     * - explicit checks on Chapter/Section/Course in WHERE
     * - @SQLRestriction on VideoEntity (so deleted video won't be joined)
     * <p>
     * IMPORTANT: do NOT put (v IS NULL OR v.deletedAt IS NULL) in WHERE,
     * otherwise the chapter disappears when a deleted video exists.
     */
    @Query("""
                SELECT c
                FROM ChapterEntity c
                JOIN FETCH c.section s
                JOIN FETCH s.course cr
                LEFT JOIN FETCH c.video v
                WHERE c.chapterId = :chapterId
                  AND c.deletedAt IS NULL
                  AND s.deletedAt IS NULL
                  AND cr.deletedAt IS NULL
            """)
    Optional<ChapterEntity> findByIdWithSectionAndCourse(@Param("chapterId") EntityId chapterId);

    @Query("""
                SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
                FROM ChapterEntity c
                WHERE c.chapterId = :chapterId
                  AND c.section.course.courseId = :courseId
                  AND c.deletedAt IS NULL
                  AND c.section.deletedAt IS NULL
                  AND c.section.course.deletedAt IS NULL
            """)
    boolean belongsToCourse(@Param("chapterId") EntityId chapterId,
                            @Param("courseId") EntityId courseId);
}