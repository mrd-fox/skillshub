package com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.adapter;

import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.entity.RoleEntity;
import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.entity.UserEntity;
import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.mapper.UserEntityMapper;
import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.repository.JpaRoleRepository;
import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.repository.JpaUserRepository;
import com.simplon_project.skillhub.skillhub.user.application.port.out.LoadUserPort;
import com.simplon_project.skillhub.skillhub.user.domain.exception.UserNotFoundException;
import com.simplon_project.skillhub.skillhub.user.domain.model.Id;
import com.simplon_project.skillhub.skillhub.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements LoadUserPort {

    private final JpaUserRepository userRepository;
    private final JpaRoleRepository roleRepository;

    /**
     * Save or update user entity in database.
     */
    public UserEntity save(User user) {
        var roles = user.getRoles();
        Set<RoleEntity> roleEntities = roleRepository.findByNameIn(roles);
        var userEntity = UserEntityMapper.mapToEntity(user);
        userEntity.setRoles(roleEntities);
        return userRepository.saveAndFlush(userEntity);
    }

    /**
     * Find user by ID.
     */

    @Override
    public User loadUserById(Id userId) {
        UserEntity found = userRepository.findById(EntityId.of(userId.asUUID()))
                .orElseThrow(() -> new UserNotFoundException("User not found with id:", userId.asString()));
        return UserEntityMapper.mapToDomain(found);
    }



    /**
     * Find user by external UUID (Keycloak ID).
     */
    public User findByExternalId(UUID externalId) {
        UserEntity found = userRepository.findByExternalId(externalId)
                .orElseThrow(() -> new UserNotFoundException("User not found with external id:", externalId.toString()));
        return UserEntityMapper.mapToDomain(found);
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
//    public Optional<RoleEntity> findRoleByName(String name) {
//        return roleRepository.findByName(name);
//    }
}