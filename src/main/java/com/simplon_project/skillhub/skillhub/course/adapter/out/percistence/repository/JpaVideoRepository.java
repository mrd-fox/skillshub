package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.repository;

import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.VideoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaVideoRepository extends JpaRepository<VideoEntity, EntityId> {


    @Query("""
                SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END
                FROM VideoEntity v
                WHERE v.chapter.chapterId = :chapterId
                  AND v.deletedAt IS NULL
                  AND v.chapter.deletedAt IS NULL
            """)
    boolean existsByChapterId(@Param("chapterId") EntityId chapterId);
}
