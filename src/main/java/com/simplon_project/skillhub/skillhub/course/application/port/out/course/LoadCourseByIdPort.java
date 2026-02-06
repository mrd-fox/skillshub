package com.simplon_project.skillhub.skillhub.course.application.port.out.course;

import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;

import java.util.Optional;

/**
 * Port de sortie pour charger un cours par son ID.
 * Responsabilité unique : chargement par identifiant interne.
 */
public interface LoadCourseByIdPort {
    /**
     * Charge un cours par son identifiant.
     *
     * @param courseId L'identifiant du cours
     * @return Le cours du domaine, ou Optional.empty() si non trouvé
     */
    Optional<Course> loadCourseById(Id courseId);
}
