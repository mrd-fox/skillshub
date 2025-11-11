package com.simplon_project.skillhub.skillhub.user.application.port.out;

import com.simplon_project.skillhub.skillhub.user.domain.model.User;

public interface SaveUserPort {


    User saveUser(User user);
}
