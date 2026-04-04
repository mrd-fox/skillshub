package com.simplon_project.skillhub.skillhub.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Helper - Sanitization Tests")
class HelperTest {

    @Test
    @DisplayName("sanitize() should remove HTML tags")
    void sanitize_shouldRemoveHtmlTags() {
        // Given
        String input = "<script>alert('XSS')</script>Hello World";

        // When
        String result = Helper.sanitize(input);

        // Then
        assertThat(result).isEqualTo("Hello World");
    }

    @Test
    @DisplayName("sanitize() should handle null input")
    void sanitize_shouldHandleNull() {
        // Given
        String input = null;

        // When
        String result = Helper.sanitize(input);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("sanitize() should remove complex HTML")
    void sanitize_shouldRemoveComplexHtml() {
        // Given
        String input = "<div><p>Test <b>bold</b> and <i>italic</i></p></div>";

        // When
        String result = Helper.sanitize(input);

        // Then
        assertThat(result).isEqualTo("Test bold and italic");
    }

    @Test
    @DisplayName("normalizeOptional() should sanitize and trim")
    void normalizeOptional_shouldSanitizeAndTrim() {
        // Given
        String input = "  <script>alert('test')</script>Hello  ";

        // When
        String result = Helper.normalizeOptional(input);

        // Then
        assertThat(result).isEqualTo("Hello");
    }

    @Test
    @DisplayName("normalizeOptional() should return null for blank strings")
    void normalizeOptional_shouldReturnNullForBlank() {
        // Given
        String input = "   ";

        // When
        String result = Helper.normalizeOptional(input);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("normalizeOptional() should return null for null input")
    void normalizeOptional_shouldReturnNullForNullInput() {
        // Given
        String input = null;

        // When
        String result = Helper.normalizeOptional(input);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("sanitize() should return null for empty HTML tags")
    void sanitize_shouldReturnNullForEmptyHtmlTags() {
        // Given
        String input = "<div></div>";

        // When
        String result = Helper.sanitize(input);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("sanitize() should return null for empty script tag")
    void sanitize_shouldReturnNullForEmptyScript() {
        // Given
        String input = "<script></script>";

        // When
        String result = Helper.sanitize(input);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("sanitize() should return null for multiple empty tags")
    void sanitize_shouldReturnNullForMultipleEmptyTags() {
        // Given
        String input = "<div></div><span></span><p></p>";

        // When
        String result = Helper.sanitize(input);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("sanitize() should return null for blank string")
    void sanitize_shouldReturnNullForBlankString() {
        // Given
        String input = "   ";

        // When
        String result = Helper.sanitize(input);

        // Then
        assertThat(result).isNull();
    }
}

