package com.simplon_project.skillhub.skillhub.user.application.port.out;

import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.entity.UserEntity;
import com.simplon_project.skillhub.skillhub.user.domain.model.User;

public interface SaveUserPort {

    User saveUser(UserEntity userEntity);
}
