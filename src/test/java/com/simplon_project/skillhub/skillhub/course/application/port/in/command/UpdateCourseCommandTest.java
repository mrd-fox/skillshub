package com.simplon_project.skillhub.skillhub.course.application.port.in.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests pour UpdateCourseCommand : sanitisation et validation
 */
@DisplayName("UpdateCourseCommand Tests")
class UpdateCourseCommandTest {

    @Test
    @DisplayName("Should reject title with ONLY HTML tags (empty after sanitization)")
    void shouldRejectTitleWithOnlyHtmlTags() {
        // Given - Title avec SEULEMENT du HTML, pas de texte
        String htmlOnlyTitle = "<div></div>";

        // When & Then - Doit être rejeté car après sanitisation = chaîne vide
        assertThatThrownBy(() ->
                UpdateCourseCommand.builder()
                        .externalAuthorId("user-123")
                        .rawRoles("TUTOR")
                        .courseId("course-456")
                        .title(htmlOnlyTitle)
                        .description("Valid description")
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("title cannot be blank");
    }

    @Test
    @DisplayName("Should reject title with script tags only")
    void shouldRejectTitleWithScriptTagsOnly() {
        // Given
        String scriptOnlyTitle = "<script>alert('xss')</script>";

        // When & Then
        assertThatThrownBy(() ->
                UpdateCourseCommand.builder()
                        .externalAuthorId("user-123")
                        .rawRoles("TUTOR")
                        .courseId("course-456")
                        .title(scriptOnlyTitle)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("title cannot be blank");
    }

    @Test
    @DisplayName("Should reject title with multiple empty tags and spaces")
    void shouldRejectTitleWithMultipleEmptyTags() {
        // Given
        String emptyTagsTitle = "  <div></div>  <span></span>  <b></b>  ";

        // When & Then
        assertThatThrownBy(() ->
                UpdateCourseCommand.builder()
                        .externalAuthorId("user-123")
                        .rawRoles("TUTOR")
                        .courseId("course-456")
                        .title(emptyTagsTitle)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("title cannot be blank");
    }

    @Test
    @DisplayName("Should reject title with iframe tag only")
    void shouldRejectTitleWithIframeOnly() {
        // Given
        String iframeOnlyTitle = "<iframe src='evil.com'></iframe>";

        // When & Then
        assertThatThrownBy(() ->
                UpdateCourseCommand.builder()
                        .externalAuthorId("user-123")
                        .rawRoles("ADMIN")
                        .courseId("course-456")
                        .title(iframeOnlyTitle)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("title cannot be blank");
    }

    @Test
    @DisplayName("Should ACCEPT title with HTML AND text (sanitizes to valid text)")
    void shouldAcceptTitleWithHtmlAndText() {
        // Given - Title avec HTML + texte valide
        String titleWithHtmlAndText = "<div>Valid Course Title</div>";

        // When
        UpdateCourseCommand command = UpdateCourseCommand.builder()
                .externalAuthorId("user-123")
                .rawRoles("TUTOR")
                .courseId("course-456")
                .title(titleWithHtmlAndText)
                .build();

        // Then - Le titre est sanitisé mais conserve le texte
        assertThat(command.title()).isEqualTo("Valid Course Title");
        assertThat(command.title()).doesNotContain("<div>");
    }

    @Test
    @DisplayName("Should ACCEPT title with spaces, HTML tags, and text")
    void shouldAcceptTitleWithSpacesHtmlAndText() {
        // Given
        String complexTitle = "  <script>evil()</script>  Mon Cours  <b>de</b> Java  ";

        // When
        UpdateCourseCommand command = UpdateCourseCommand.builder()
                .externalAuthorId("user-123")
                .rawRoles("TUTOR")
                .courseId("course-456")
                .title(complexTitle)
                .build();

        // Then
        assertThat(command.title()).isEqualTo("Mon Cours de Java");
        assertThat(command.title()).doesNotContain("<script>");
        assertThat(command.title()).doesNotContain("<b>");
    }

    @Test
    @DisplayName("Should reject blank title (normal case)")
    void shouldRejectNormalBlankTitle() {
        // Given
        String blankTitle = "    ";

        // When & Then
        assertThatThrownBy(() ->
                UpdateCourseCommand.builder()
                        .externalAuthorId("user-123")
                        .rawRoles("TUTOR")
                        .courseId("course-456")
                        .title(blankTitle)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("title cannot be blank");
    }

    @Test
    @DisplayName("Should accept null title (means no update)")
    void shouldAcceptNullTitle() {
        // Given
        String nullTitle = null;

        // When
        UpdateCourseCommand command = UpdateCourseCommand.builder()
                .externalAuthorId("user-123")
                .rawRoles("TUTOR")
                .courseId("course-456")
                .title(nullTitle)
                .description("Update only description")
                .build();

        // Then
        assertThat(command.title()).isNull();
    }
}


