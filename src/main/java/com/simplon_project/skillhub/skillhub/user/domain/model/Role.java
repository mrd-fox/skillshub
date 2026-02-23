package com.simplon_project.skillhub.skillhub.user.domain.model;

import com.simplon_project.skillhub.skillhub.user.domain.enums.RolesEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder(toBuilder = true)
@Setter
public class Role extends Base {
    private RolesEnum name;
}
