package com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.repository;

import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.entity.RoleEntity;
import com.simplon_project.skillhub.skillhub.user.domain.enums.RolesEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface JpaRoleRepository extends JpaRepository<RoleEntity, EntityId> {

    /**
     * Find a role by its enum name.
     */
    Optional<RoleEntity> findByName(RolesEnum name);

    /**
     * Find multiple roles by their enum names.
     */
    Set<RoleEntity> findByNameIn(Set<RolesEnum> names);
}