package com.simplon_project.skillhub.skillhub.user.application.port.out;

import com.simplon_project.skillhub.skillhub.user.domain.enums.RolesEnum;
import com.simplon_project.skillhub.skillhub.user.domain.model.Role;

import java.util.Set;

public interface LoadRolePort {
    Set<Role> loadRolesByNames(Set<RolesEnum> names);
}
