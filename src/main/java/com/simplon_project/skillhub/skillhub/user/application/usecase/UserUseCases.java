package com.simplon_project.skillhub.skillhub.user.application.usecase;

import com.simplon_project.skillhub.skillhub.user.application.port.in.CreateUserPort;
import com.simplon_project.skillhub.skillhub.user.application.port.in.command.CreateUserCommand;
import com.simplon_project.skillhub.skillhub.user.application.port.out.SaveUserPort;
import com.simplon_project.skillhub.skillhub.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserUseCases implements CreateUserPort {
    private final SaveUserPort saveUserPort;

    @Override
    public User create(CreateUserCommand command) {
        User user = command.mapToDomain();
        return saveUserPort.saveUser(user);
    }
}
