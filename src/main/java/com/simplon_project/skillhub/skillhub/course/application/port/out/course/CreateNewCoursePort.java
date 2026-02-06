package com.simplon_project.skillhub.skillhub.course.application.port.out.course;

import com.simplon_project.skillhub.skillhub.course.domain.model.Course;

/**
 * Port de sortie pour créer un nouveau cours (création initiale).
 * Responsabilité unique : Persistance d'un nouveau cours sans structure complexe.
 * Utilisé lors de la création initiale du cours (mode DRAFT).
 */
public interface CreateNewCoursePort {
    /**
     * Vérifie qu'un cours n'existe pas déjà (validation métier).
     *
     * @param course Le cours à vérifier
     * @throws com.simplon_project.skillhub.skillhub.course.domain.exception.CourseAlreadyExistsException si un cours avec le même titre existe
     */
    void assertCourseNotExists(Course course);

    /**
     * Crée un nouveau cours dans la base de données.
     * Utilisé pour la création initiale (sans sections/chapitres).
     *
     * @param course Le cours à créer (nouveau domaine)
     * @return Le cours créé avec son ID généré
     */
    Course createNewCourse(Course course);
}
