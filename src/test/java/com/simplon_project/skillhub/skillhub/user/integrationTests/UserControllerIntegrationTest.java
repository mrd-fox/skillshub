package com.simplon_project.skillhub.skillhub.user.integrationTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.mapper.UserEntityMapper;
import com.simplon_project.skillhub.skillhub.user.adapter.out.percistence.repository.JpaUserRepository;
import com.simplon_project.skillhub.skillhub.user.domain.enums.RolesEnum;
import com.simplon_project.skillhub.skillhub.user.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional("userTxManager")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JpaUserRepository userJpaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateUserSuccessfully_whenHeadersAreValid() throws Exception {
        // Arrange
        String externalId = UUID.randomUUID().toString();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Id", externalId);
        headers.add("X-User-FirstName", "Marina");
        headers.add("X-User-LastName", "Darde");
        headers.add("X-User-Email", "marina.darde@3wa.io");
        headers.add("X-User-Address", "12 rue des Lilas");
        headers.add("X-User-City", "Paris");
        headers.add("X-User-Country", "France");
        headers.add("X-User-PostalCode", "75000");
        headers.add("X-User-PhoneNumber", "+33612345678");
        headers.add("X-User-Roles", "STUDENT,TUTOR");

        // Act & Assert
        mockMvc.perform(post("/api/users/create")
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("marina.darde@3wa.io"))
                .andExpect(jsonPath("$.firstName").value("Marina"))
                .andExpect(jsonPath("$.roles[0]").isNotEmpty());

        // Verify DB persistence
        User saved = userJpaRepository.findByExternalId(UUID.fromString(externalId))
                .map(UserEntityMapper::mapToDomain) // selon ton mapper ou méthode utilitaire
                .orElseThrow();

        assertThat(saved.getEmail()).isEqualTo("marina.darde@3wa.io");
        assertThat(saved.getFirstName()).isEqualTo("Marina");

        var roleNames = saved.getRoles().stream()
                .filter(Objects::nonNull)
                .map(RolesEnum::name)
                .toList();
        assertTrue(roleNames.contains("STUDENT"));
        assertTrue(roleNames.contains("TUTOR"));
    }

    @Test
    void shouldReturnBadRequest_whenMissingRequiredHeaders() throws Exception {
        mockMvc.perform(post("/api/users/create"))
                .andExpect(status().isBadRequest());
    }
}
