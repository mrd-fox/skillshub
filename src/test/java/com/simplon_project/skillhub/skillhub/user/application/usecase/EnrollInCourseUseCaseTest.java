package com.simplon_project.skillhub.skillhub.user.application.usecase;

import com.simplon_project.skillhub.skillhub.user.application.port.in.command.EnrollInCourseCommand;
import com.simplon_project.skillhub.skillhub.user.application.port.out.CreateUserCourseEnrollmentPort;
import com.simplon_project.skillhub.skillhub.user.application.port.out.LoadInternalUserIdByExternalIdPort;
import com.simplon_project.skillhub.skillhub.user.domain.exception.ForbiddenException;
import com.simplon_project.skillhub.skillhub.user.domain.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EnrollInCourseUseCase.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EnrollInCourseUseCase Unit Tests")
class EnrollInCourseUseCaseTest {

    @Mock
    private LoadInternalUserIdByExternalIdPort loadInternalUserIdPort;

    @Mock
    private CreateUserCourseEnrollmentPort createEnrollmentPort;

    @InjectMocks
    private EnrollInCourseUseCase enrollInCourseUseCase;

    private String externalUserId;
    private String internalUserId;
    private String courseId;

    @BeforeEach
    void setUp() {
        externalUserId = UUID.randomUUID().toString();
        internalUserId = UUID.randomUUID().toString();
        courseId = UUID.randomUUID().toString();
    }

    @Nested
    @DisplayName("Successful Enrollment Tests")
    class SuccessfulEnrollment {

        @Test
        @DisplayName("Should create enrollment when user has STUDENT role")
        void shouldCreateEnrollmentWhenUserHasStudentRole() {
            // Given
            EnrollInCourseCommand command = EnrollInCourseCommand.of(externalUserId, courseId, "STUDENT");

            when(loadInternalUserIdPort.loadInternalUserId(UUID.fromString(externalUserId)))
                    .thenReturn(Optional.of(UUID.fromString(internalUserId)));

            // When
            enrollInCourseUseCase.enroll(command);

            // Then
            verify(loadInternalUserIdPort, times(1))
                    .loadInternalUserId(UUID.fromString(externalUserId));
            verify(createEnrollmentPort, times(1))
                    .createIfAbsent(UUID.fromString(internalUserId), UUID.fromString(courseId));
        }

        @Test
        @DisplayName("Should create enrollment when user has STUDENT and other roles")
        void shouldCreateEnrollmentWhenUserHasStudentAndOtherRoles() {
            // Given
            EnrollInCourseCommand command = EnrollInCourseCommand.of(externalUserId, courseId, "STUDENT, TUTOR");

            when(loadInternalUserIdPort.loadInternalUserId(UUID.fromString(externalUserId)))
                    .thenReturn(Optional.of(UUID.fromString(internalUserId)));

            // When
            enrollInCourseUseCase.enroll(command);

            // Then
            verify(loadInternalUserIdPort, times(1))
                    .loadInternalUserId(UUID.fromString(externalUserId));
            verify(createEnrollmentPort, times(1))
                    .createIfAbsent(UUID.fromString(internalUserId), UUID.fromString(courseId));
        }
    }

    @Nested
    @DisplayName("Forbidden Access Tests")
    class ForbiddenAccess {

        @Test
        @DisplayName("Should throw ForbiddenException when user lacks STUDENT role")
        void shouldThrowForbiddenExceptionWhenUserLacksStudentRole() {
            // Given
            EnrollInCourseCommand command = EnrollInCourseCommand.of(externalUserId, courseId, "TUTOR");

            // When & Then
            assertThrows(ForbiddenException.class, () -> {
                enrollInCourseUseCase.enroll(command);
            });

            // Verify no interactions with ports
            verify(loadInternalUserIdPort, never()).loadInternalUserId(any());
            verify(createEnrollmentPort, never()).createIfAbsent(any(), any());
        }

        @Test
        @DisplayName("Should throw ForbiddenException when user has ADMIN role only")
        void shouldThrowForbiddenExceptionWhenUserHasAdminRoleOnly() {
            // Given
            EnrollInCourseCommand command = EnrollInCourseCommand.of(externalUserId, courseId, "ADMIN");

            // When & Then
            assertThrows(ForbiddenException.class, () -> {
                enrollInCourseUseCase.enroll(command);
            });

            // Verify no interactions with ports
            verify(loadInternalUserIdPort, never()).loadInternalUserId(any());
            verify(createEnrollmentPort, never()).createIfAbsent(any(), any());
        }

        @Test
        @DisplayName("Should throw ForbiddenException when user has TUTOR and ADMIN roles")
        void shouldThrowForbiddenExceptionWhenUserHasTutorAndAdminRoles() {
            // Given
            EnrollInCourseCommand command = EnrollInCourseCommand.of(externalUserId, courseId, "TUTOR, ADMIN");

            // When & Then
            assertThrows(ForbiddenException.class, () -> {
                enrollInCourseUseCase.enroll(command);
            });

            // Verify no interactions with ports
            verify(loadInternalUserIdPort, never()).loadInternalUserId(any());
            verify(createEnrollmentPort, never()).createIfAbsent(any(), any());
        }
    }

    @Nested
    @DisplayName("User Not Found Tests")
    class UserNotFound {

        @Test
        @DisplayName("Should throw UserNotFoundException when user does not exist")
        void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
            // Given
            EnrollInCourseCommand command = EnrollInCourseCommand.of(externalUserId, courseId, "STUDENT");

            when(loadInternalUserIdPort.loadInternalUserId(UUID.fromString(externalUserId)))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThrows(UserNotFoundException.class, () -> {
                enrollInCourseUseCase.enroll(command);
            });

            // Verify port interactions
            verify(loadInternalUserIdPort, times(1))
                    .loadInternalUserId(UUID.fromString(externalUserId));
            verify(createEnrollmentPort, never())
                    .createIfAbsent(any(), any());
        }
    }

    @Nested
    @DisplayName("Command Validation Tests")
    class CommandValidation {

        @Test
        @DisplayName("Should throw IllegalArgumentException when externalUserId is null")
        void shouldThrowIllegalArgumentExceptionWhenExternalUserIdIsNull() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                EnrollInCourseCommand.of(null, courseId, "STUDENT");
            });
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when courseId is null")
        void shouldThrowIllegalArgumentExceptionWhenCourseIdIsNull() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                EnrollInCourseCommand.of(externalUserId, null, "STUDENT");

            });
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when rawRoles is null")
        void shouldThrowIllegalArgumentExceptionWhenRawRolesIsNull() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                EnrollInCourseCommand.of(externalUserId, courseId, null);

            });
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when rawRoles is blank")
        void shouldThrowIllegalArgumentExceptionWhenRawRolesIsBlank() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                EnrollInCourseCommand.of(externalUserId, courseId, "   ");
            });
        }
    }
}

