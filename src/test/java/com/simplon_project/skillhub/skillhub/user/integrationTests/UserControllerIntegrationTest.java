package com.simplon_project.skillhub.skillhub.user.integrationTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.entity.RoleEntity;
import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.entity.UserEntity;
import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.mapper.UserEntityMapper;
import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.repository.JpaUserRepository;
import com.simplon_project.skillhub.skillhub.user.domain.enums.RolesEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest extends DatabaseTestConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JpaUserRepository userJpaRepository;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void shouldCreateUserSuccessfully_whenHeadersAreValid() throws Exception {
        String externalId = UUID.randomUUID().toString();
        String email = "john+" + UUID.randomUUID() + "@mail.io";

        mockMvc.perform(post("/api/users/create")
                        .header("X-User-Id", externalId)
                        .header("X-User-FirstName", "John")
                        .header("X-User-LastName", "Doe")
                        .header("X-User-Email", email)
                        .header("X-User-Address", "12 rue des Lilas")
                        .header("X-User-City", "Paris")
                        .header("X-User-Country", "France")
                        .header("X-User-PostalCode", "75000")
                        .header("X-User-PhoneNumber", "+33612345678")
                        .header("X-User-Roles", "STUDENT,TUTOR")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.roles.length()").value(2))
                .andExpect(jsonPath("$.enrolledCourseIds").exists())
                .andExpect(jsonPath("$.enrolledCourseIds").isArray())
                .andExpect(jsonPath("$.enrolledCourseIds", hasSize(0)));

        var saved = userJpaRepository.findByExternalId(UUID.fromString(externalId))
                .map(UserEntityMapper::mapToDomain)
                .orElseThrow();

        assertThat(saved.getEmail()).isEqualTo(email);
    }

    @Test
    void shouldReturnBadRequest_whenMissingRequiredHeaders() throws Exception {
        mockMvc.perform(post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Nested
    @DisplayName("GET /api/users/external/{externalId}")
    class GetUserByExternalId {

        @Test
        @DisplayName("should return user with empty enrolledCourseIds when user has no enrollments")
        void getUserByExternalId_withNoEnrollments_shouldReturnEmptyEnrolledCourseIds() throws Exception {
            UUID externalId = UUID.randomUUID();

            UserEntity user = UserEntity.builder()
                    .id(EntityId.random())
                    .externalId(externalId)
                    .firstName("Jane")
                    .lastName("Doe")
                    .email("jane.doe+" + UUID.randomUUID() + "@test.com")
                    .active(true)
                    .roles(Set.of(RoleEntity.builder().id(EntityId.random()).name(RolesEnum.STUDENT).build()))
                    .build();

            userJpaRepository.save(user);

            mockMvc.perform(get("/api/users/external/" + externalId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.externalId").value(externalId.toString()))
                    .andExpect(jsonPath("$.email").value(user.getEmail()))
                    .andExpect(jsonPath("$.enrolledCourseIds").exists())
                    .andExpect(jsonPath("$.enrolledCourseIds").isArray())
                    .andExpect(jsonPath("$.enrolledCourseIds", hasSize(0)));
        }

        @Test
        @DisplayName("should return user with enrolledCourseIds as array of UUID strings")
        void getUserByExternalId_shouldReturnEnrolledCourseIdsAsArrayOfStrings() throws Exception {
            UUID externalId = UUID.randomUUID();

            UserEntity user = UserEntity.builder()
                    .id(EntityId.random())
                    .externalId(externalId)
                    .firstName("John")
                    .lastName("Smith")
                    .email("john.smith+" + UUID.randomUUID() + "@test.com")
                    .active(true)
                    .roles(Set.of(RoleEntity.builder().id(EntityId.random()).name(RolesEnum.STUDENT).build()))
                    .build();

            userJpaRepository.save(user);

            mockMvc.perform(get("/api/users/external/" + externalId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.enrolledCourseIds").exists())
                    .andExpect(jsonPath("$.enrolledCourseIds").isArray());
        }

        @Test
        @DisplayName("should return 404 when user with externalId not found")
        void getUserByExternalId_withNonExistentId_shouldReturn404() throws Exception {
            UUID nonExistentId = UUID.randomUUID();

            mockMvc.perform(get("/api/users/external/" + nonExistentId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 400 when externalId is not a valid UUID")
        void getUserByExternalId_withInvalidUuid_shouldReturn400() throws Exception {
            mockMvc.perform(get("/api/users/external/invalid-uuid"))
                    .andExpect(status().isBadRequest());
        }
    }
}
