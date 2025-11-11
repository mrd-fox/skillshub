package com.simplon_project.skillhub.skillhub.user.adapter.out.percistence;

import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.mapper.UserEntityMapper;
import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.repository.JpaUserRepository;
import com.simplon_project.skillhub.skillhub.user.application.port.out.SaveUserPort;
import com.simplon_project.skillhub.skillhub.user.domain.model.User;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional("userTxManager")
public class UserAdapter implements SaveUserPort {

    private final JpaUserRepository userJpaRepository;
    private final EntityManager entityManager;


    public UserAdapter(JpaUserRepository userJpaRepository,
                       @Qualifier("userEntityManager") EntityManager entityManager) {
        this.userJpaRepository = userJpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional("userTxManager")
    public User saveUser(User user) {
        var userEntity = UserEntityMapper.mapToEntity(user);
        var saved = userJpaRepository.save(userEntity);
        entityManager.refresh(saved);
        return UserEntityMapper.mapToDomain(saved);
    }
}
