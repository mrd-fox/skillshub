package com.simplon_project.skillhub.skillhub.user.application.usecase;

import com.simplon_project.skillhub.skillhub.user.api.LoadEnrolledCourseIdsByExternalUserIdPort;
import com.simplon_project.skillhub.skillhub.user.application.port.in.CreateUserPort;
import com.simplon_project.skillhub.skillhub.user.application.port.in.GetUserByExternalIdPort;
import com.simplon_project.skillhub.skillhub.user.application.port.in.GetUserByIdPort;
import com.simplon_project.skillhub.skillhub.user.application.port.in.UpdateUserPort;
import com.simplon_project.skillhub.skillhub.user.application.port.in.command.CreateUserCommand;
import com.simplon_project.skillhub.skillhub.user.application.port.in.command.GetUserByExternalIdCommand;
import com.simplon_project.skillhub.skillhub.user.application.port.in.command.GetUserByIdCommand;
import com.simplon_project.skillhub.skillhub.user.application.port.in.command.UpdateUserCommand;
import com.simplon_project.skillhub.skillhub.user.application.port.in.dto.GetUserByExternalIdResult;
import com.simplon_project.skillhub.skillhub.user.application.port.out.*;
import com.simplon_project.skillhub.skillhub.user.domain.enums.RolesEnum;
import com.simplon_project.skillhub.skillhub.user.domain.exception.UserNotFoundException;
import com.simplon_project.skillhub.skillhub.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(transactionManager = "userTxManager")
public class UserUseCases implements CreateUserPort, GetUserByIdPort, GetUserByExternalIdPort, UpdateUserPort, LoadEnrolledCourseIdsByExternalUserIdPort {
    private final SaveUserPort saveUserPort;
    private final LoadUserPort loadUserPort;
    private final FindUserByExternalIdPort findUserByExternalIdPort;
    private final LoadEnrolledCourseIdsPort loadEnrolledCourseIdsPort;
    private final LoadInternalUserIdByExternalIdPort loadInternalUserIdPort;

    @Override
    public User create(CreateUserCommand command) {
        User user = command.mapToDomain();
        return saveUserPort.saveUser(user);
    }

    @Override
    @Transactional(readOnly = true, transactionManager = "userTxManager")
    public User getUserById(GetUserByIdCommand command) {
        return loadUserPort.loadUserById(command.toDomainId());
    }

    @Override
    @Transactional(readOnly = true, transactionManager = "userTxManager")
    public GetUserByExternalIdResult getUserByExternalId(GetUserByExternalIdCommand command) {

        var externalId = command.toExternalId();
        var user = findUserByExternalIdPort.findUserByExternalId(externalId);

        List<UUID> enrolledCourseIds = loadEnrolledCourseIdsPort.loadCourseIds(user.getId().asUUID());

        return new GetUserByExternalIdResult(user, enrolledCourseIds);
    }

    @Override
    public User update(UpdateUserCommand command) {

        UUID externalId = UUID.fromString(command.externalId());
        User existing = findUserByExternalIdPort.findUserByExternalId(externalId);
        // Apply PATCH-like fields (command already normalized: null means "not provided")
        buildExisting(command, existing);
        return saveUserPort.saveUser(existing);
    }

    private static void buildExisting(UpdateUserCommand command, User existing) {
        if (command.firstName() != null) {
            existing.setFirstName(command.firstName());
        }

        if (command.lastName() != null) {
            existing.setLastName(command.lastName());
        }

        if (command.address() != null) {
            existing.setAddress(command.address());
        }

        if (command.postalCode() != null) {
            existing.setPostalCode(command.postalCode());
        }

        if (command.city() != null) {
            existing.setCity(command.city());
        }

        if (command.country() != null) {
            existing.setCountry(command.country());
        }

        if (command.phoneNumber() != null) {
            existing.setPhoneNumber(command.phoneNumber());
        }

        // Roles add-only (command already validated: only "TUTOR" allowed)
        if (command.rolesToAdd() != null && !command.rolesToAdd().isEmpty()) {
            if (existing.getRoles() == null) {
                existing.setRoles(new HashSet<>());
            }
            existing.getRoles().add(RolesEnum.TUTOR);
        }
    }

    @Override
    @Transactional(readOnly = true, transactionManager = "userTxManager")
    public Set<UUID> loadEnrolledCourseIds(UUID externalUserId) {
        if (externalUserId == null) {
            throw new IllegalArgumentException("External user ID cannot be null");
        }

        // Resolve internal user ID
        UUID internalUserId = loadInternalUserIdPort.loadInternalUserId(externalUserId)
                .orElseThrow(() -> new UserNotFoundException(
                        externalUserId.toString(),
                        "User not found with external ID: " + externalUserId
                ));

        // Load enrolled course IDs
        List<UUID> enrolledCourseIdsList = loadEnrolledCourseIdsPort.loadCourseIds(internalUserId);

        // Return as Set (non-null)
        return enrolledCourseIdsList != null ? new HashSet<>(enrolledCourseIdsList) : Set.of();
    }
}
