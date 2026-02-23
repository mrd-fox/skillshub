package com.simplon_project.skillhub.skillhub.course.application.port.out.course;

import com.simplon_project.skillhub.skillhub.course.domain.model.Course;

/**
 * Port de sortie pour mettre à jour la structure d'un cours existant.
 * Responsabilité unique : Mise à jour d'un cours avec sa structure complète (sections, chapitres, vidéos).
 * Utilisé lors de l'ajout de sections/chapitres ou de la mise à jour de la structure.
 */
public interface UpdateCourseStructurePort {
    /**
     * Met à jour un cours existant avec sa structure complète.
     * Gère les modifications de sections, chapitres et vidéos via un patch en place.
     *
     * @param course Le cours à mettre à jour (avec ID existant)
     * @return Le cours mis à jour
     * @throws IllegalStateException si le cours n'existe pas
     */
    Course updateCourseStructure(Course course);
}
