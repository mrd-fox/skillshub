package com.simplon_project.skillhub.skillhub.user.application.port.out;

import com.simplon_project.skillhub.skillhub.user.domain.model.Id;
import com.simplon_project.skillhub.skillhub.user.domain.model.User;

public interface LoadUserPort {
    User loadUserById(Id userId);
}
