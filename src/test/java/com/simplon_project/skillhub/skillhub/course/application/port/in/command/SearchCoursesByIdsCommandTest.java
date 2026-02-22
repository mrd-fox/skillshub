package com.simplon_project.skillhub.skillhub.course.application.port.in.command;

import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SearchCoursesByIdsCommand")
class SearchCoursesByIdsCommandTest {

    private static final String VALID_EXTERNAL_USER_ID = UUID.randomUUID().toString();

    @Nested
    @DisplayName("of() factory method validation")
    class OfFactoryMethod {

        @Test
        @DisplayName("should throw IllegalArgumentException when ids is null")
        void of_withNullIds_shouldThrowIllegalArgumentException() {
            assertThatThrownBy(() -> SearchCoursesByIdsCommand.of(null, VALID_EXTERNAL_USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Course IDs list cannot be null or empty");
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when ids is empty list")
        void of_withEmptyList_shouldThrowIllegalArgumentException() {
            assertThatThrownBy(() -> SearchCoursesByIdsCommand.of(Collections.emptyList(), VALID_EXTERNAL_USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Course IDs list cannot be null or empty");
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when list contains null")
        void of_withListContainingNull_shouldThrowIllegalArgumentException() {
            List<String> idsWithNull = Arrays.asList((String) null);

            assertThatThrownBy(() -> SearchCoursesByIdsCommand.of(idsWithNull, VALID_EXTERNAL_USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Course ID cannot be null or blank");
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when list contains blank string")
        void of_withListContainingBlank_shouldThrowIllegalArgumentException() {
            List<String> idsWithBlank = List.of("   ");

            assertThatThrownBy(() -> SearchCoursesByIdsCommand.of(idsWithBlank, VALID_EXTERNAL_USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Course ID cannot be null or blank");
        }

        @Test
        @DisplayName("should throw IllegalArgumentException with invalid UUID value in message")
        void of_withInvalidUuid_shouldThrowIllegalArgumentExceptionWithValue() {
            String invalidUuid = "not-a-uuid";
            List<String> idsWithInvalidUuid = List.of(invalidUuid);

            assertThatThrownBy(() -> SearchCoursesByIdsCommand.of(idsWithInvalidUuid, VALID_EXTERNAL_USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid UUID format for course ID")
                    .hasMessageContaining(invalidUuid);
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when externalUserId is null")
        void of_withNullExternalUserId_shouldThrowIllegalArgumentException() {
            String validUuid = UUID.randomUUID().toString();
            List<String> ids = List.of(validUuid);

            assertThatThrownBy(() -> SearchCoursesByIdsCommand.of(ids, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("External user ID cannot be null or blank");
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when externalUserId is blank")
        void of_withBlankExternalUserId_shouldThrowIllegalArgumentException() {
            String validUuid = UUID.randomUUID().toString();
            List<String> ids = List.of(validUuid);

            assertThatThrownBy(() -> SearchCoursesByIdsCommand.of(ids, "   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("External user ID cannot be null or blank");
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when externalUserId is invalid UUID")
        void of_withInvalidExternalUserId_shouldThrowIllegalArgumentException() {
            String validUuid = UUID.randomUUID().toString();
            List<String> ids = List.of(validUuid);
            String invalidExternalUserId = "not-a-uuid";

            assertThatThrownBy(() -> SearchCoursesByIdsCommand.of(ids, invalidExternalUserId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid UUID format for external user ID")
                    .hasMessageContaining(invalidExternalUserId);
        }

        @Test
        @DisplayName("should create command with single valid UUID")
        void of_withValidUuid_shouldCreateCommand() {
            String validUuid = UUID.randomUUID().toString();
            List<String> ids = List.of(validUuid);

            SearchCoursesByIdsCommand command = SearchCoursesByIdsCommand.of(ids, VALID_EXTERNAL_USER_ID);

            assertThat(command).isNotNull();
            assertThat(command.courseIds()).hasSize(1);
            assertThat(command.courseIds().get(0)).isEqualTo(Id.of(validUuid));
            assertThat(command.courseIds().get(0).asString()).isEqualTo(validUuid);
            assertThat(command.externalUserId()).isNotNull();
            assertThat(command.externalUserId()).isEqualTo(UUID.fromString(VALID_EXTERNAL_USER_ID));
        }

        @Test
        @DisplayName("should create command with multiple valid UUIDs")
        void of_withMultipleValidUuids_shouldCreateCommand() {
            String uuid1 = UUID.randomUUID().toString();
            String uuid2 = UUID.randomUUID().toString();
            String uuid3 = UUID.randomUUID().toString();
            List<String> ids = List.of(uuid1, uuid2, uuid3);

            SearchCoursesByIdsCommand command = SearchCoursesByIdsCommand.of(ids, VALID_EXTERNAL_USER_ID);

            assertThat(command).isNotNull();
            assertThat(command.courseIds()).hasSize(3);
            assertThat(command.courseIds().get(0).asString()).isEqualTo(uuid1);
            assertThat(command.courseIds().get(1).asString()).isEqualTo(uuid2);
            assertThat(command.courseIds().get(2).asString()).isEqualTo(uuid3);
            assertThat(command.externalUserId()).isNotNull();
            assertThat(command.externalUserId()).isEqualTo(UUID.fromString(VALID_EXTERNAL_USER_ID));
        }
    }

    @Nested
    @DisplayName("Record constructor validation")
    class RecordConstructorValidation {

        @Test
        @DisplayName("should throw IllegalArgumentException when courseIds is null in constructor")
        void constructor_withNullCourseIds_shouldThrowIllegalArgumentException() {
            UUID externalUserId = UUID.fromString(VALID_EXTERNAL_USER_ID);

            assertThatThrownBy(() -> new SearchCoursesByIdsCommand(null, externalUserId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Course IDs list cannot be null or empty");
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when courseIds is empty in constructor")
        void constructor_withEmptyCourseIds_shouldThrowIllegalArgumentException() {
            UUID externalUserId = UUID.fromString(VALID_EXTERNAL_USER_ID);

            assertThatThrownBy(() -> new SearchCoursesByIdsCommand(Collections.emptyList(), externalUserId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Course IDs list cannot be null or empty");
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when courseIds contains null in constructor")
        void constructor_withCourseIdsContainingNull_shouldThrowIllegalArgumentException() {
            List<Id> idsWithNull = Arrays.asList((Id) null);
            UUID externalUserId = UUID.fromString(VALID_EXTERNAL_USER_ID);

            assertThatThrownBy(() -> new SearchCoursesByIdsCommand(idsWithNull, externalUserId))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Course ID cannot be null");
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when externalUserId is null in constructor")
        void constructor_withNullExternalUserId_shouldThrowIllegalArgumentException() {
            Id id1 = Id.of(UUID.randomUUID().toString());
            List<Id> courseIds = List.of(id1);

            assertThatThrownBy(() -> new SearchCoursesByIdsCommand(courseIds, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("External user ID cannot be null");
        }

        @Test
        @DisplayName("should create command with valid courseIds in constructor")
        void constructor_withValidCourseIds_shouldCreateCommand() {
            Id id1 = Id.of(UUID.randomUUID().toString());
            Id id2 = Id.of(UUID.randomUUID().toString());
            List<Id> courseIds = List.of(id1, id2);
            UUID externalUserId = UUID.fromString(VALID_EXTERNAL_USER_ID);

            SearchCoursesByIdsCommand command = new SearchCoursesByIdsCommand(courseIds, externalUserId);

            assertThat(command).isNotNull();
            assertThat(command.courseIds()).hasSize(2);
            assertThat(command.courseIds()).containsExactly(id1, id2);
            assertThat(command.externalUserId()).isNotNull();
            assertThat(command.externalUserId()).isEqualTo(externalUserId);
        }
    }
}




