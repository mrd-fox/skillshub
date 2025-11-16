package com.simplon_project.skillhub.skillhub.user.application.port.in;

import com.simplon_project.skillhub.skillhub.user.application.port.in.command.CreateUserCommand;
import com.simplon_project.skillhub.skillhub.user.domain.model.User;

public interface CreateUserPort {
    User create(CreateUserCommand command);
}
