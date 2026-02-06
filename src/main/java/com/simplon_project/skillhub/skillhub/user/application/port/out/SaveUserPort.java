package com.simplon_project.skillhub.skillhub.user.application.port.out;

import com.simplon_project.skillhub.skillhub.user.domain.model.User;

/**
 * Port de sortie pour sauvegarder un utilisateur.
 * Responsabilité unique : persistance d'un utilisateur (création ou mise à jour).
 */
public interface SaveUserPort {
    /**
     * Persiste ou met à jour un utilisateur.
     *
     * @param user Le modèle de domaine User
     * @return Le User persisté (avec ID, timestamps, etc.)
     */
    User saveUser(User user);
}
