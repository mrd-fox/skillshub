package com.simplon_project.skillhub.skillhub.user.application.usecase;

import com.simplon_project.skillhub.skillhub.user.application.port.in.CreateUserPort;
import com.simplon_project.skillhub.skillhub.user.application.port.in.command.CreateUserCommand;
import com.simplon_project.skillhub.skillhub.user.domain.model.User;

public class UserUseCases implements CreateUserPort {
    @Override
    public User create(CreateUserCommand command) {
        return null;
    }
}
