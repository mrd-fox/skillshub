package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.repository;

import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.CourseEntity;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaCourseRepository extends JpaRepository<CourseEntity, EntityId> {

    Optional<CourseEntity> findById(EntityId id);

    Optional<CourseEntity> findByTitle(String title);

    /**
     * Tree load (sections + chapters + video).
     * Child filtering is enforced by @SQLRestriction on Section/Chapter/Video.
     * DO NOT filter children in WHERE, otherwise parent rows disappear when deleted children exist.
     */
    @Query("""
                select distinct c
                from CourseEntity c
                left join fetch c.sections s
                left join fetch s.chapters ch
                left join fetch ch.video v
                where c.courseId = :id
                  and c.deletedAt is null
            """)
    Optional<CourseEntity> findByIdWithTree(@Param("id") EntityId id);

    @Query("""
                select c
                from CourseEntity c
                where c.status = 'PUBLISHED'
                  and c.deletedAt is null
                order by c.createdAt desc
            """)
    List<CourseEntity> findAllForPublicCatalog();

    /**
     * Public tree (sections + chapters, NO video).
     * Child filtering is enforced by @SQLRestriction on Section/Chapter.
     */
    @Query("""
                select distinct c
                from CourseEntity c
                left join fetch c.sections s
                left join fetch s.chapters ch
                where c.courseId = :id
                  and c.deletedAt is null
            """)
    Optional<CourseEntity> findByIdWithPublicTree(@Param("id") EntityId id);

    @Query("""
                select c
                from CourseEntity c
                where c.externalUserId = :externalUserId
                  and c.deletedAt is null
                order by c.createdAt desc
            """)
    List<CourseEntity> findByExternalUserId(@Param("externalUserId") String externalUserId);
}