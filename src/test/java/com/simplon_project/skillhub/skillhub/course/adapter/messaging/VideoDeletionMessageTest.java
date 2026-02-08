package com.simplon_project.skillhub.skillhub.course.adapter.messaging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("VideoDeletionMessage")
class VideoDeletionMessageTest {

    @Test
    @DisplayName("firstAttempt should create attempt=1 with non-null enqueuedAt")
    void firstAttempt_shouldCreateAttempt1() {
        VideoDeletionMessage msg = VideoDeletionMessage.firstAttempt("vid-1", "vimeo://123");

        assertThat(msg.videoId()).isEqualTo("vid-1");
        assertThat(msg.sourceUri()).isEqualTo("vimeo://123");
        assertThat(msg.attempt()).isEqualTo(1);
        assertThat(msg.enqueuedAt()).isNotNull();
    }

    @Test
    @DisplayName("nextAttempt should increment attempt and refresh enqueuedAt")
    void nextAttempt_shouldIncrementAttempt() throws InterruptedException {
        VideoDeletionMessage msg1 = new VideoDeletionMessage("vid-1", "vimeo://123", 1, Instant.now());
        Instant t1 = msg1.enqueuedAt();

        Thread.sleep(2);

        VideoDeletionMessage msg2 = msg1.nextAttempt();

        assertThat(msg2.videoId()).isEqualTo("vid-1");
        assertThat(msg2.sourceUri()).isEqualTo("vimeo://123");
        assertThat(msg2.attempt()).isEqualTo(2);
        assertThat(msg2.enqueuedAt()).isAfterOrEqualTo(t1);
    }

    @Test
    @DisplayName("constructor should reject blank videoId")
    void ctor_shouldRejectBlankVideoId() {
        assertThatThrownBy(() -> new VideoDeletionMessage("  ", "vimeo://123", 1, Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("videoId must not be blank");
    }

    @Test
    @DisplayName("constructor should reject blank sourceUri")
    void ctor_shouldRejectBlankSourceUri() {
        assertThatThrownBy(() -> new VideoDeletionMessage("vid-1", "   ", 1, Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sourceUri must not be blank");
    }

    @Test
    @DisplayName("constructor should reject attempt < 0")
    void ctor_shouldRejectNegativeAttempt() {
        assertThatThrownBy(() -> new VideoDeletionMessage("vid-1", "vimeo://123", -1, Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("attempt must be >=");
    }

    @Test
    @DisplayName("constructor should reject null enqueuedAt")
    void ctor_shouldRejectNullEnqueuedAt() {
        assertThatThrownBy(() -> new VideoDeletionMessage("vid-1", "vimeo://123", 1, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("enqueuedAt must not be null");
    }
}