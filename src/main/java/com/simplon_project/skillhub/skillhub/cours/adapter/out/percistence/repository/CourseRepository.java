package com.simplon_project.skillhub.skillhub.cours.adapter.out.percistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, String> {
    boolean existsByTitle(String title);
}
