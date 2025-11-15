package com.simplon_project.skillhub.skillhub.user.integrationTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.mapper.UserEntityMapper;
import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.repository.JpaUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                .andExpect(jsonPath("$.roles.length()").value(2));

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
}
