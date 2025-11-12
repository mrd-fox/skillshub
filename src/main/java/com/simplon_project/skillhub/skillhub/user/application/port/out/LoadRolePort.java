package com.simplon_project.skillhub.skillhub.user.application.port.out;

import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.entity.RoleEntity;
import com.simplon_project.skillhub.skillhub.user.domain.enums.RolesEnum;

import java.util.Set;

public interface LoadRolePort {
    Set<RoleEntity> loadRolesByNames(Set<RolesEnum> names);
}
