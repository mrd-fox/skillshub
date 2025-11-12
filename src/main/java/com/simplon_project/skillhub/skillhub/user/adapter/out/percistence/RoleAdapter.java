package com.simplon_project.skillhub.skillhub.user.adapter.out.percistence;

import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.entity.RoleEntity;
import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.repository.JpaRoleRepository;
import com.simplon_project.skillhub.skillhub.user.application.port.out.LoadRolePort;
import com.simplon_project.skillhub.skillhub.user.domain.enums.RolesEnum;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@Transactional("userTxManager")
public class RoleAdapter implements LoadRolePort {
    private final JpaRoleRepository roleJpaRepository;
    private final EntityManager entityManager;

    public RoleAdapter(JpaRoleRepository roleJpaRepository,
                       @Qualifier("userEntityManager") EntityManager entityManager) {
        this.roleJpaRepository = roleJpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    public Set<RoleEntity> loadRolesByNames(Set<RolesEnum> roles) {
        return roles.stream()
                .map(roleEnum -> roleJpaRepository.findByName(roleEnum)
                        .orElseThrow(() ->
                                new IllegalStateException("Role not found in database: " + roleEnum)))
                .collect(Collectors.toSet());
    }
}
