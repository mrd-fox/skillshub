package com.simplon_project.skillhub.skillhub.user.application.port.out;

import com.simplon_project.skillhub.skillhub.user.domain.model.User;

import java.util.UUID;

/**
 * Port de sortie pour rechercher un utilisateur par son identifiant externe.
 * Responsabilité unique : recherche par external ID (Keycloak UUID).
 */
public interface FindUserByExternalIdPort {
    /**
     * Recherche un utilisateur par son identifiant externe (Keycloak UUID).
     *
     * @param externalId L'UUID externe (Keycloak)
     * @return Le User du domaine
     * @throws com.simplon_project.skillhub.skillhub.user.domain.exception.UserNotFoundException si non trouvé
     */
    User findUserByExternalId(UUID externalId);
}
