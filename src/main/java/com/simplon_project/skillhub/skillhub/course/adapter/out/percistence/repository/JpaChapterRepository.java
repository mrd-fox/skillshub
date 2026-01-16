package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.repository;

import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.ChapterEntity;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JpaChapterRepository extends JpaRepository<ChapterEntity, EntityId> {
    @Query("""
                SELECT c
                FROM ChapterEntity c
                JOIN FETCH c.section s
                JOIN FETCH s.course cr
                LEFT JOIN FETCH c.video            
                WHERE c.chapterId = :chapterId
            """)
    Optional<ChapterEntity> findByIdWithSectionAndCourse(@Param("chapterId") EntityId chapterId);

    @Query("""
                SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
                FROM ChapterEntity c
                WHERE c.chapterId = :chapterId
                  AND c.section.course.courseId = :courseId
            """)
    boolean belongsToCourse(@Param("chapterId") EntityId chapterId,
                            @Param("courseId") EntityId courseId);

}
