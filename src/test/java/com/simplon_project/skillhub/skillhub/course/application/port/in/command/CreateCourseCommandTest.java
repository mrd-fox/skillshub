package com.simplon_project.skillhub.skillhub.course.application.port.in.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CreateCourseCommand Tests")
class CreateCourseCommandTest {

    @Test
    @DisplayName("Should reject title with only HTML tags (empty after sanitization)")
    void shouldRejectTitleWithOnlyHtmlTags() {
        // Given
        String htmlOnlyTitle = "<div></div>";

        // When & Then
        assertThatThrownBy(() ->
                CreateCourseCommand.builder()
                        .externalAuthorId("user-123")
                        .rawRoles("TUTOR")
                        .title(htmlOnlyTitle)
                        .description("Description")
                        .price(100L)
                        .sections(List.of())
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("title is missing or blank");
    }

    @Test
    @DisplayName("Should reject blank title")
    void shouldRejectBlankTitle() {
        // Given
        String blankTitle = "   ";

        // When & Then
        assertThatThrownBy(() ->
                CreateCourseCommand.builder()
                        .externalAuthorId("user-123")
                        .rawRoles("TUTOR")
                        .title(blankTitle)
                        .description("Description")
                        .price(100L)
                        .sections(List.of())
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("title is missing or blank");
    }

    @Test
    @DisplayName("Should sanitize title with HTML tags")
    void shouldSanitizeTitleWithHtmlTags() {
        // Given
        String maliciousTitle = "<script>alert('XSS')</script>Mon Super Cours";

        // When
        CreateCourseCommand command = CreateCourseCommand.builder()
                .externalAuthorId("user-123")
                .rawRoles("TUTOR")
                .title(maliciousTitle)
                .description("Description normale")
                .price(100L)
                .sections(List.of())
                .build();

        // Then
        assertThat(command.title()).isEqualTo("Mon Super Cours");
        assertThat(command.title()).doesNotContain("<script>");
        assertThat(command.title()).doesNotContain("alert");
    }

    @Test
    @DisplayName("Should sanitize description with complex HTML")
    void shouldSanitizeDescriptionWithComplexHtml() {
        // Given
        String maliciousDescription = "<div><p>Description <b>avec</b> HTML</p><img src=x onerror=alert(1)></div>";

        // When
        CreateCourseCommand command = CreateCourseCommand.builder()
                .externalAuthorId("user-123")
                .rawRoles("ADMIN")
                .title("Cours de Java")
                .description(maliciousDescription)
                .price(0L)
                .sections(List.of())
                .build();

        // Then
        assertThat(command.description()).isEqualTo("Description avec HTML");
        assertThat(command.description()).doesNotContain("<div>");
        assertThat(command.description()).doesNotContain("<img");
        assertThat(command.description()).doesNotContain("onerror");
    }

    @Test
    @DisplayName("Should preserve normal text in title")
    void shouldPreserveNormalTextInTitle() {
        // Given
        String normalTitle = "Introduction à Spring Boot";

        // When
        CreateCourseCommand command = CreateCourseCommand.builder()
                .externalAuthorId("user-123")
                .rawRoles("TUTOR")
                .title(normalTitle)
                .description("Description")
                .price(50L)
                .sections(List.of())
                .build();

        // Then
        assertThat(command.title()).isEqualTo(normalTitle);
    }

    @Test
    @DisplayName("Should sanitize both title and description")
    void shouldSanitizeBothTitleAndDescription() {
        // Given
        String maliciousTitle = "Cours <iframe src='evil.com'></iframe>de Sécurité";
        String maliciousDescription = "<a href='javascript:alert(1)'>Click me</a> Description";

        // When
        CreateCourseCommand command = CreateCourseCommand.builder()
                .externalAuthorId("user-123")
                .rawRoles("TUTOR,ADMIN")
                .title(maliciousTitle)
                .description(maliciousDescription)
                .price(100L)
                .sections(List.of())
                .build();

        // Then
        assertThat(command.title()).isEqualTo("Cours de Sécurité");
        assertThat(command.title()).doesNotContain("<iframe>");

        assertThat(command.description()).isEqualTo("Click me Description");
        assertThat(command.description()).doesNotContain("<a ");
        assertThat(command.description()).doesNotContain("javascript:");
    }

    @Test
    @DisplayName("Should handle null description")
    void shouldHandleNullDescription() {
        // When
        CreateCourseCommand command = CreateCourseCommand.builder()
                .externalAuthorId("user-123")
                .rawRoles("TUTOR")
                .title("Cours sans description")
                .description(null)
                .price(0L)
                .sections(List.of())
                .build();

        // Then
        assertThat(command.description()).isNull();
    }

    @Test
    @DisplayName("Should sanitize SQL injection attempts in title")
    void shouldSanitizeSqlInjectionAttempts() {
        // Given - Même si ce n'est pas du HTML, on teste la robustesse
        String titleWithSql = "Cours de Base de Données'; DROP TABLE courses; --";

        // When
        CreateCourseCommand command = CreateCourseCommand.builder()
                .externalAuthorId("user-123")
                .rawRoles("TUTOR")
                .title(titleWithSql)
                .description("Description")
                .price(100L)
                .sections(List.of())
                .build();

        // Then - Le texte est préservé (la protection SQL injection est faite ailleurs)
        assertThat(command.title()).isEqualTo(titleWithSql);
    }
}



