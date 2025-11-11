package com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.adapter;

import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.entity.RoleEntity;
import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.entity.UserEntity;
import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.repository.JpaRoleRepository;
import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter {

    private final JpaUserRepository userRepository;
    private final JpaRoleRepository roleRepository;

    /**
     * Save or update user entity in database.
     */
    public UserEntity save(UserEntity user, Set<String> roleNames) {
        Set<RoleEntity> roleEntities = roleRepository.findByNameIn(roleNames);
        user.setRoles(roleEntities);
        return userRepository.save(user);
    }

    /**
     * Find user by ID.
     */
    public Optional<UserEntity> findById(EntityId userId) {
        return userRepository.findById(userId);
    }

    /**
     * Find user by external UUID (Keycloak ID).
     */
    public Optional<UserEntity> findByExternalId(UUID externalId) {
        return userRepository.findByExternalId(externalId);
    }

    /**
     * Find user by email.
     */
    public Optional<UserEntity> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Delete user by ID.
     */
    public void deleteById(EntityId userId) {
        userRepository.deleteById(userId);
    }

    /**
     * Check if a user exists by email.
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Load all roles from DB.
     */
    public Set<RoleEntity> getAllRoles() {
        return roleRepository.findAll().stream().collect(Collectors.toSet());
    }

    /**
     * Get a specific role by its name.
     */
    public Optional<RoleEntity> findRoleByName(String name) {
        return roleRepository.findByName(name);
    }
}