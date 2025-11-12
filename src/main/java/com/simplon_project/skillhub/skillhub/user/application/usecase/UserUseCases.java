package com.simplon_project.skillhub.skillhub.user.application.usecase;

import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.adapter.UserRepositoryAdapter;
import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.mapper.UserEntityMapper;
import com.simplon_project.skillhub.skillhub.user.application.port.in.CreateUserPort;
import com.simplon_project.skillhub.skillhub.user.application.port.in.command.CreateUserCommand;
import com.simplon_project.skillhub.skillhub.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserUseCases implements CreateUserPort {
    //    private final SaveUserPort saveUserPort;
//    private final LoadRolePort loadRolePort;
    private final UserRepositoryAdapter userRepositoryAdapter;

    @Override
    public User create(CreateUserCommand command) {
        User user = command.mapToDomain();

//        var roleEntities = loadRolePort.loadRolesByNames(user.getRoles());

//        var userEntity = UserEntityMapper.mapToEntity(user);
//        userEntity.setRoles(roleEntities);
//
//        var saved = saveUserPort.saveUser(userEntity);
//        return saved;

        var saved = userRepositoryAdapter.save(user);

        return UserEntityMapper.mapToDomain(saved);
    }
}
