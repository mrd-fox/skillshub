package com.simplon_project.skillhub.skillhub.user.application.usecase;

import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.adapter.UserRepositoryAdapter;
import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.mapper.UserEntityMapper;
import com.simplon_project.skillhub.skillhub.user.application.port.in.CreateUserPort;
import com.simplon_project.skillhub.skillhub.user.application.port.in.GetUserByExternalIdPort;
import com.simplon_project.skillhub.skillhub.user.application.port.in.GetUserByIdPort;
import com.simplon_project.skillhub.skillhub.user.application.port.in.command.CreateUserCommand;
import com.simplon_project.skillhub.skillhub.user.application.port.in.command.GetUserByExternalIdCommand;
import com.simplon_project.skillhub.skillhub.user.application.port.in.command.GetUserByIdCommand;
import com.simplon_project.skillhub.skillhub.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserUseCases implements CreateUserPort, GetUserByIdPort , GetUserByExternalIdPort {
    private final UserRepositoryAdapter userRepositoryAdapter;

    @Override
    public User create(CreateUserCommand command) {
        User user = command.mapToDomain();
        var saved = userRepositoryAdapter.save(user);
        return UserEntityMapper.mapToDomain(saved);
    }

    @Override
    public User getUserById(GetUserByIdCommand command) {
        return userRepositoryAdapter.loadUserById(command.toDomainId());
    }

    @Override
    public User getUserByExternalId(GetUserByExternalIdCommand command) {
        return userRepositoryAdapter.findByExternalId(command.toExternalId());
    }
}
