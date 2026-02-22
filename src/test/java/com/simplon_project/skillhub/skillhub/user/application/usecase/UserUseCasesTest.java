package com.simplon_project.skillhub.skillhub.user.application.usecase;

import com.simplon_project.skillhub.skillhub.user.application.port.in.command.CreateUserCommand;
import com.simplon_project.skillhub.skillhub.user.application.port.in.command.GetUserByExternalIdCommand;
import com.simplon_project.skillhub.skillhub.user.application.port.in.command.GetUserByIdCommand;
import com.simplon_project.skillhub.skillhub.user.application.port.in.command.UpdateUserCommand;
import com.simplon_project.skillhub.skillhub.user.application.port.in.dto.GetUserByExternalIdResult;
import com.simplon_project.skillhub.skillhub.user.application.port.out.FindUserByExternalIdPort;
import com.simplon_project.skillhub.skillhub.user.application.port.out.LoadEnrolledCourseIdsPort;
import com.simplon_project.skillhub.skillhub.user.application.port.out.LoadUserPort;
import com.simplon_project.skillhub.skillhub.user.application.port.out.SaveUserPort;
import com.simplon_project.skillhub.skillhub.user.domain.enums.RolesEnum;
import com.simplon_project.skillhub.skillhub.user.domain.model.Id;
import com.simplon_project.skillhub.skillhub.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserUseCases Unit Tests")
class UserUseCasesTest {

    @Mock
    private SaveUserPort saveUserPort;

    @Mock
    private LoadUserPort loadUserPort;

    @Mock
    private FindUserByExternalIdPort findUserByExternalIdPort;

    @Mock
    private LoadEnrolledCourseIdsPort loadEnrolledCourseIdsPort;

    @InjectMocks
    private UserUseCases userUseCases;

    private UUID externalUserId;
    private UUID internalUserId;
    private String email;
    private User testUser;

    @BeforeEach
    void setUp() {
        externalUserId = UUID.randomUUID();
        internalUserId = UUID.randomUUID();
        email = "test.user@example.com";

        testUser = User.builder()
                .id(Id.of(internalUserId.toString()))
                .externalId(externalUserId)
                .email(email)
                .firstName("John")
                .lastName("Doe")
                .address("123 Main St")
                .postalCode("75000")
                .city("Paris")
                .country("France")
                .phoneNumber("+33612345678")
                .active(true)
                .roles(new HashSet<>(Set.of(RolesEnum.STUDENT)))
                .build();
    }

    @Nested
    @DisplayName("create() method")
    class CreateUser {

        @Test
        @DisplayName("should call saveUserPort.saveUser and return saved user")
        void create_shouldCallSaveUserPortAndReturnUser() {
            // GIVEN
            CreateUserCommand command = new CreateUserCommand(
                    externalUserId.toString(),
                    "Jane",
                    "Smith",
                    email,
                    "456 Oak Ave",
                    "Paris",
                    "France",
                    "+33698765432",
                    "75001",
                    Set.of("STUDENT")
            );

            User expectedUser = User.builder()
                    .id(Id.of(internalUserId.toString()))
                    .externalId(externalUserId)
                    .email(email)
                    .firstName("Jane")
                    .lastName("Smith")
                    .active(true)
                    .roles(Set.of(RolesEnum.STUDENT))
                    .build();

            when(saveUserPort.saveUser(any(User.class))).thenReturn(expectedUser);

            // WHEN
            User result = userUseCases.create(command);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(expectedUser);
            assertThat(result.getEmail()).isEqualTo(email);
            assertThat(result.getFirstName()).isEqualTo("Jane");

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(saveUserPort, times(1)).saveUser(userCaptor.capture());

            User capturedUser = userCaptor.getValue();
            assertThat(capturedUser.getExternalId()).isEqualTo(externalUserId);
            assertThat(capturedUser.getEmail()).isEqualTo(email);

            verifyNoMoreInteractions(saveUserPort);
            verifyNoInteractions(loadUserPort, findUserByExternalIdPort, loadEnrolledCourseIdsPort);
        }

        @Test
        @DisplayName("should handle user with multiple roles")
        void create_shouldHandleMultipleRoles() {
            // GIVEN
            CreateUserCommand command = new CreateUserCommand(
                    externalUserId.toString(),
                    "Admin",
                    "User",
                    email,
                    null,
                    null,
                    null,
                    null,
                    null,
                    Set.of("STUDENT", "TUTOR")
            );

            User savedUser = User.builder()
                    .id(Id.of(internalUserId.toString()))
                    .externalId(externalUserId)
                    .email(email)
                    .roles(Set.of(RolesEnum.STUDENT, RolesEnum.TUTOR))
                    .build();

            when(saveUserPort.saveUser(any(User.class))).thenReturn(savedUser);

            // WHEN
            User result = userUseCases.create(command);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.getRoles()).hasSize(2);
            assertThat(result.getRoles()).contains(RolesEnum.STUDENT, RolesEnum.TUTOR);

            verify(saveUserPort, times(1)).saveUser(any(User.class));
        }
    }

    @Nested
    @DisplayName("getUserById() method")
    class GetUserById {

        @Test
        @DisplayName("should call loadUserPort.loadUserById with expected domain id")
        void getUserById_shouldCallLoadUserPortWithDomainId() {
            // GIVEN
            GetUserByIdCommand command = new GetUserByIdCommand(internalUserId.toString());

            when(loadUserPort.loadUserById(any(Id.class))).thenReturn(testUser);

            // WHEN
            User result = userUseCases.getUserById(command);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(testUser);
            assertThat(result.getId().asString()).isEqualTo(internalUserId.toString());
            assertThat(result.getEmail()).isEqualTo(email);

            ArgumentCaptor<Id> idCaptor = ArgumentCaptor.forClass(Id.class);
            verify(loadUserPort, times(1)).loadUserById(idCaptor.capture());

            Id capturedId = idCaptor.getValue();
            assertThat(capturedId.asString()).isEqualTo(internalUserId.toString());

            verifyNoMoreInteractions(loadUserPort);
            verifyNoInteractions(saveUserPort, findUserByExternalIdPort, loadEnrolledCourseIdsPort);
        }

        @Test
        @DisplayName("should return user with all fields populated")
        void getUserById_shouldReturnUserWithAllFields() {
            // GIVEN
            GetUserByIdCommand command = new GetUserByIdCommand(internalUserId.toString());

            when(loadUserPort.loadUserById(any(Id.class))).thenReturn(testUser);

            // WHEN
            User result = userUseCases.getUserById(command);

            // THEN
            assertThat(result.getFirstName()).isEqualTo("John");
            assertThat(result.getLastName()).isEqualTo("Doe");
            assertThat(result.getAddress()).isEqualTo("123 Main St");
            assertThat(result.getPostalCode()).isEqualTo("75000");
            assertThat(result.getCity()).isEqualTo("Paris");
            assertThat(result.getCountry()).isEqualTo("France");
            assertThat(result.getPhoneNumber()).isEqualTo("+33612345678");
            assertThat(result.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("getUserByExternalId() method")
    class GetUserByExternalId {

        @Test
        @DisplayName("should call findUserByExternalIdPort and loadEnrolledCourseIdsPort")
        void getUserByExternalId_shouldCallBothPorts() {
            // GIVEN
            GetUserByExternalIdCommand command = new GetUserByExternalIdCommand(externalUserId.toString());

            when(findUserByExternalIdPort.findUserByExternalId(externalUserId)).thenReturn(testUser);
            when(loadEnrolledCourseIdsPort.loadCourseIds(internalUserId)).thenReturn(List.of());

            // WHEN
            GetUserByExternalIdResult result = userUseCases.getUserByExternalId(command);

            // THEN
            assertThat(result).isNotNull();

            verify(findUserByExternalIdPort, times(1)).findUserByExternalId(externalUserId);
            verify(loadEnrolledCourseIdsPort, times(1)).loadCourseIds(internalUserId);

            verifyNoMoreInteractions(findUserByExternalIdPort, loadEnrolledCourseIdsPort);
            verifyNoInteractions(saveUserPort, loadUserPort);
        }

        @Test
        @DisplayName("should return result with user and empty enrolledCourseIds when none")
        void getUserByExternalId_shouldReturnEmptyEnrolledCourseIdsWhenNone() {
            // GIVEN
            GetUserByExternalIdCommand command = new GetUserByExternalIdCommand(externalUserId.toString());

            when(findUserByExternalIdPort.findUserByExternalId(externalUserId)).thenReturn(testUser);
            when(loadEnrolledCourseIdsPort.loadCourseIds(internalUserId)).thenReturn(List.of());

            // WHEN
            GetUserByExternalIdResult result = userUseCases.getUserByExternalId(command);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.user()).isEqualTo(testUser);
            assertThat(result.enrolledCourseIds()).isNotNull();
            assertThat(result.enrolledCourseIds()).isEmpty();
        }

        @Test
        @DisplayName("should return result with user and enrolledCourseIds when present")
        void getUserByExternalId_shouldReturnEnrolledCourseIdsWhenPresent() {
            // GIVEN
            GetUserByExternalIdCommand command = new GetUserByExternalIdCommand(externalUserId.toString());

            UUID courseId1 = UUID.randomUUID();
            UUID courseId2 = UUID.randomUUID();
            List<UUID> expectedCourseIds = List.of(courseId1, courseId2);

            when(findUserByExternalIdPort.findUserByExternalId(externalUserId)).thenReturn(testUser);
            when(loadEnrolledCourseIdsPort.loadCourseIds(internalUserId)).thenReturn(expectedCourseIds);

            // WHEN
            GetUserByExternalIdResult result = userUseCases.getUserByExternalId(command);

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.user()).isEqualTo(testUser);
            assertThat(result.enrolledCourseIds()).isNotNull();
            assertThat(result.enrolledCourseIds()).hasSize(2);
            assertThat(result.enrolledCourseIds()).containsExactly(courseId1, courseId2);
        }

        @Test
        @DisplayName("should return result with user and three enrolledCourseIds")
        void getUserByExternalId_shouldHandleMultipleEnrollments() {
            // GIVEN
            GetUserByExternalIdCommand command = new GetUserByExternalIdCommand(externalUserId.toString());

            UUID courseId1 = UUID.randomUUID();
            UUID courseId2 = UUID.randomUUID();
            UUID courseId3 = UUID.randomUUID();
            List<UUID> expectedCourseIds = List.of(courseId1, courseId2, courseId3);

            when(findUserByExternalIdPort.findUserByExternalId(externalUserId)).thenReturn(testUser);
            when(loadEnrolledCourseIdsPort.loadCourseIds(internalUserId)).thenReturn(expectedCourseIds);

            // WHEN
            GetUserByExternalIdResult result = userUseCases.getUserByExternalId(command);

            // THEN
            assertThat(result.enrolledCourseIds()).hasSize(3);
            assertThat(result.enrolledCourseIds()).containsExactly(courseId1, courseId2, courseId3);
        }

        @Test
        @DisplayName("should use user's internal id to load course ids")
        void getUserByExternalId_shouldUseInternalIdToLoadCourseIds() {
            // GIVEN
            GetUserByExternalIdCommand command = new GetUserByExternalIdCommand(externalUserId.toString());

            when(findUserByExternalIdPort.findUserByExternalId(externalUserId)).thenReturn(testUser);
            when(loadEnrolledCourseIdsPort.loadCourseIds(internalUserId)).thenReturn(List.of());

            // WHEN
            userUseCases.getUserByExternalId(command);

            // THEN
            ArgumentCaptor<UUID> internalIdCaptor = ArgumentCaptor.forClass(UUID.class);
            verify(loadEnrolledCourseIdsPort, times(1)).loadCourseIds(internalIdCaptor.capture());

            UUID capturedInternalId = internalIdCaptor.getValue();
            assertThat(capturedInternalId).isEqualTo(internalUserId);
        }
    }

    @Nested
    @DisplayName("update() method")
    class UpdateUser {

        @Test
        @DisplayName("should call findUserByExternalIdPort and saveUserPort")
        void update_shouldCallFindAndSavePorts() {
            // GIVEN
            UpdateUserCommand command = new UpdateUserCommand(
                    externalUserId.toString(),
                    "Updated",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            User updatedUser = User.builder()
                    .id(testUser.getId())
                    .externalId(testUser.getExternalId())
                    .email(testUser.getEmail())
                    .firstName("Updated")
                    .lastName(testUser.getLastName())
                    .active(true)
                    .build();

            when(findUserByExternalIdPort.findUserByExternalId(externalUserId)).thenReturn(testUser);
            when(saveUserPort.saveUser(any(User.class))).thenReturn(updatedUser);

            // WHEN
            User result = userUseCases.update(command);

            // THEN
            assertThat(result).isNotNull();

            verify(findUserByExternalIdPort, times(1)).findUserByExternalId(externalUserId);
            verify(saveUserPort, times(1)).saveUser(any(User.class));

            verifyNoMoreInteractions(findUserByExternalIdPort, saveUserPort);
            verifyNoInteractions(loadUserPort, loadEnrolledCourseIdsPort);
        }

        @Test
        @DisplayName("should only update firstName when only firstName provided")
        void update_shouldOnlyUpdateProvidedFields() {
            // GIVEN
            UpdateUserCommand command = new UpdateUserCommand(
                    externalUserId.toString(),
                    "UpdatedFirstName",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            when(findUserByExternalIdPort.findUserByExternalId(externalUserId)).thenReturn(testUser);
            when(saveUserPort.saveUser(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // WHEN
            User result = userUseCases.update(command);

            // THEN
            assertThat(result.getFirstName()).isEqualTo("UpdatedFirstName");
            assertThat(result.getLastName()).isEqualTo("Doe");
            assertThat(result.getAddress()).isEqualTo("123 Main St");
            assertThat(result.getPostalCode()).isEqualTo("75000");
            assertThat(result.getCity()).isEqualTo("Paris");
            assertThat(result.getCountry()).isEqualTo("France");
            assertThat(result.getPhoneNumber()).isEqualTo("+33612345678");

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(saveUserPort).saveUser(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getFirstName()).isEqualTo("UpdatedFirstName");
            assertThat(savedUser.getLastName()).isEqualTo("Doe");
        }

        @Test
        @DisplayName("should update multiple fields when multiple fields provided")
        void update_shouldUpdateMultipleFields() {
            // GIVEN
            UpdateUserCommand command = new UpdateUserCommand(
                    externalUserId.toString(),
                    "NewFirst",
                    "NewLast",
                    "New Address",
                    null,
                    "Lyon",
                    null,
                    null,
                    null
            );

            when(findUserByExternalIdPort.findUserByExternalId(externalUserId)).thenReturn(testUser);
            when(saveUserPort.saveUser(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // WHEN
            User result = userUseCases.update(command);

            // THEN
            assertThat(result.getFirstName()).isEqualTo("NewFirst");
            assertThat(result.getLastName()).isEqualTo("NewLast");
            assertThat(result.getAddress()).isEqualTo("New Address");
            assertThat(result.getCity()).isEqualTo("Lyon");
            assertThat(result.getPostalCode()).isEqualTo("75000");
            assertThat(result.getCountry()).isEqualTo("France");
            assertThat(result.getPhoneNumber()).isEqualTo("+33612345678");
        }

        @Test
        @DisplayName("should not update fields when null (PATCH behavior)")
        void update_shouldNotUpdateFieldsWhenNull() {
            // GIVEN
            UpdateUserCommand command = new UpdateUserCommand(
                    externalUserId.toString(),
                    "OnlyThis",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            when(findUserByExternalIdPort.findUserByExternalId(externalUserId)).thenReturn(testUser);
            when(saveUserPort.saveUser(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // WHEN
            User result = userUseCases.update(command);

            // THEN
            assertThat(result.getFirstName()).isEqualTo("OnlyThis");
            assertThat(result.getLastName()).isEqualTo("Doe");
            assertThat(result.getAddress()).isEqualTo("123 Main St");
        }

        @Test
        @DisplayName("should add TUTOR role when rolesToAdd contains TUTOR")
        void update_shouldAddTutorRoleWhenProvided() {
            // GIVEN
            UpdateUserCommand command = new UpdateUserCommand(
                    externalUserId.toString(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    Set.of("TUTOR")
            );

            when(findUserByExternalIdPort.findUserByExternalId(externalUserId)).thenReturn(testUser);
            when(saveUserPort.saveUser(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // WHEN
            User result = userUseCases.update(command);

            // THEN
            assertThat(result.getRoles()).isNotNull();
            assertThat(result.getRoles()).hasSize(2);
            assertThat(result.getRoles()).contains(RolesEnum.STUDENT, RolesEnum.TUTOR);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(saveUserPort).saveUser(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getRoles()).contains(RolesEnum.STUDENT);
            assertThat(savedUser.getRoles()).contains(RolesEnum.TUTOR);
        }

        @Test
        @DisplayName("should not remove existing roles when adding TUTOR")
        void update_shouldNotRemoveExistingRolesWhenAddingTutor() {
            // GIVEN
            testUser.setRoles(new HashSet<>(Set.of(RolesEnum.STUDENT, RolesEnum.ADMIN)));

            UpdateUserCommand command = new UpdateUserCommand(
                    externalUserId.toString(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    Set.of("TUTOR")
            );

            when(findUserByExternalIdPort.findUserByExternalId(externalUserId)).thenReturn(testUser);
            when(saveUserPort.saveUser(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // WHEN
            User result = userUseCases.update(command);

            // THEN
            assertThat(result.getRoles()).hasSize(3);
            assertThat(result.getRoles()).contains(RolesEnum.STUDENT, RolesEnum.ADMIN, RolesEnum.TUTOR);
        }

        @Test
        @DisplayName("should not modify roles when rolesToAdd is null")
        void update_shouldNotModifyRolesWhenRolesToAddIsNull() {
            // GIVEN
            UpdateUserCommand command = new UpdateUserCommand(
                    externalUserId.toString(),
                    "Updated",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            when(findUserByExternalIdPort.findUserByExternalId(externalUserId)).thenReturn(testUser);
            when(saveUserPort.saveUser(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // WHEN
            User result = userUseCases.update(command);

            // THEN
            assertThat(result.getRoles()).hasSize(1);
            assertThat(result.getRoles()).containsExactly(RolesEnum.STUDENT);
        }

        @Test
        @DisplayName("should not modify roles when rolesToAdd is empty")
        void update_shouldNotModifyRolesWhenRolesToAddIsEmpty() {
            // GIVEN
            UpdateUserCommand command = new UpdateUserCommand(
                    externalUserId.toString(),
                    "Updated",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    Set.of()
            );

            when(findUserByExternalIdPort.findUserByExternalId(externalUserId)).thenReturn(testUser);
            when(saveUserPort.saveUser(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // WHEN
            User result = userUseCases.update(command);

            // THEN
            assertThat(result.getRoles()).hasSize(1);
            assertThat(result.getRoles()).containsExactly(RolesEnum.STUDENT);
        }

        @Test
        @DisplayName("should initialize roles set if null before adding TUTOR")
        void update_shouldInitializeRolesIfNullBeforeAdding() {
            // GIVEN
            testUser.setRoles(null);

            UpdateUserCommand command = new UpdateUserCommand(
                    externalUserId.toString(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    Set.of("TUTOR")
            );

            when(findUserByExternalIdPort.findUserByExternalId(externalUserId)).thenReturn(testUser);
            when(saveUserPort.saveUser(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // WHEN
            User result = userUseCases.update(command);

            // THEN
            assertThat(result.getRoles()).isNotNull();
            assertThat(result.getRoles()).hasSize(1);
            assertThat(result.getRoles()).containsExactly(RolesEnum.TUTOR);
        }

        @Test
        @DisplayName("should update all address fields together")
        void update_shouldUpdateAllAddressFields() {
            // GIVEN
            UpdateUserCommand command = new UpdateUserCommand(
                    externalUserId.toString(),
                    null,
                    null,
                    "New Street",
                    "69000",
                    "Lyon",
                    "France",
                    null,
                    null
            );

            when(findUserByExternalIdPort.findUserByExternalId(externalUserId)).thenReturn(testUser);
            when(saveUserPort.saveUser(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // WHEN
            User result = userUseCases.update(command);

            // THEN
            assertThat(result.getAddress()).isEqualTo("New Street");
            assertThat(result.getPostalCode()).isEqualTo("69000");
            assertThat(result.getCity()).isEqualTo("Lyon");
            assertThat(result.getCountry()).isEqualTo("France");
        }

        @Test
        @DisplayName("should update phoneNumber when provided")
        void update_shouldUpdatePhoneNumber() {
            // GIVEN
            UpdateUserCommand command = new UpdateUserCommand(
                    externalUserId.toString(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "+33601020304",
                    null
            );

            when(findUserByExternalIdPort.findUserByExternalId(externalUserId)).thenReturn(testUser);
            when(saveUserPort.saveUser(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // WHEN
            User result = userUseCases.update(command);

            // THEN
            assertThat(result.getPhoneNumber()).isEqualTo("+33601020304");
        }
    }
}



