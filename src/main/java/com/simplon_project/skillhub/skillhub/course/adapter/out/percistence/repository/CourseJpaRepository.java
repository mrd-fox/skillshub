package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.repository;

import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.CourseEntity;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseJpaRepository extends JpaRepository<CourseEntity, EntityId> {
    Optional<CourseEntity> findById(EntityId id);

    Optional<CourseEntity> findByTitle(String title);

    @Query("""
                select distinct c
                from CourseEntity c
                left join fetch c.sections s
                left join fetch s.chapters ch
                left join fetch ch.video v
                where c.courseId = :id
            """)
    Optional<CourseEntity> findByIdWithTree(@Param("id") EntityId id);
}
