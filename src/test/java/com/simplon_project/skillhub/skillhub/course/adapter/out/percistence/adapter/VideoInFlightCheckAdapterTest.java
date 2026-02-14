package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.adapter;

import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.repository.JpaVideoRepository;
import com.simplon_project.skillhub.skillhub.course.domain.enums.VideoStatusEnum;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit test for VideoInFlightCheckAdapter.
 * Tests the adapter logic for checking in-flight videos.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VideoInFlightCheckAdapter Unit Tests")
class VideoInFlightCheckAdapterTest {

    @Mock
    private JpaVideoRepository jpaVideoRepository;

    @InjectMocks
    private VideoInFlightCheckAdapter adapter;

    @Test
    @DisplayName("existsInFlightVideoForCourse should return true when repository finds in-flight video")
    void existsInFlightVideoForCourse_shouldReturnTrue_whenVideoExists() {
        // GIVEN
        UUID courseIdUuid = UUID.randomUUID();
        Id courseId = Id.of(courseIdUuid.toString());
        Set<VideoStatusEnum> statuses = Set.of(VideoStatusEnum.PENDING, VideoStatusEnum.PROCESSING);

        when(jpaVideoRepository.existsInFlightVideoForCourse(any(EntityId.class), eq(statuses)))
                .thenReturn(true);

        // WHEN
        boolean result = adapter.existsInFlightVideoForCourse(courseId, statuses);

        // THEN
        assertTrue(result);
    }

    @Test
    @DisplayName("existsInFlightVideoForCourse should return false when repository finds no in-flight video")
    void existsInFlightVideoForCourse_shouldReturnFalse_whenNoVideoExists() {
        // GIVEN
        UUID courseIdUuid = UUID.randomUUID();
        Id courseId = Id.of(courseIdUuid.toString());
        Set<VideoStatusEnum> statuses = Set.of(VideoStatusEnum.PENDING, VideoStatusEnum.PROCESSING);

        when(jpaVideoRepository.existsInFlightVideoForCourse(any(EntityId.class), eq(statuses)))
                .thenReturn(false);

        // WHEN
        boolean result = adapter.existsInFlightVideoForCourse(courseId, statuses);

        // THEN
        assertFalse(result);
    }

    @Test
    @DisplayName("existsInFlightVideoForCourse should check for PENDING status")
    void existsInFlightVideoForCourse_shouldCheckForPendingStatus() {
        // GIVEN
        UUID courseIdUuid = UUID.randomUUID();
        Id courseId = Id.of(courseIdUuid.toString());
        Set<VideoStatusEnum> statuses = Set.of(VideoStatusEnum.PENDING);

        when(jpaVideoRepository.existsInFlightVideoForCourse(any(EntityId.class), eq(statuses)))
                .thenReturn(true);

        // WHEN
        boolean result = adapter.existsInFlightVideoForCourse(courseId, statuses);

        // THEN
        assertTrue(result);
    }

    @Test
    @DisplayName("existsInFlightVideoForCourse should check for PROCESSING status")
    void existsInFlightVideoForCourse_shouldCheckForProcessingStatus() {
        // GIVEN
        UUID courseIdUuid = UUID.randomUUID();
        Id courseId = Id.of(courseIdUuid.toString());
        Set<VideoStatusEnum> statuses = Set.of(VideoStatusEnum.PROCESSING);

        when(jpaVideoRepository.existsInFlightVideoForCourse(any(EntityId.class), eq(statuses)))
                .thenReturn(true);

        // WHEN
        boolean result = adapter.existsInFlightVideoForCourse(courseId, statuses);

        // THEN
        assertTrue(result);
    }

    @Test
    @DisplayName("existsInFlightVideoForCourse should return false for READY status")
    void existsInFlightVideoForCourse_shouldReturnFalse_forReadyStatus() {
        // GIVEN
        UUID courseIdUuid = UUID.randomUUID();
        Id courseId = Id.of(courseIdUuid.toString());
        Set<VideoStatusEnum> statuses = Set.of(VideoStatusEnum.READY);

        when(jpaVideoRepository.existsInFlightVideoForCourse(any(EntityId.class), eq(statuses)))
                .thenReturn(false);

        // WHEN
        boolean result = adapter.existsInFlightVideoForCourse(courseId, statuses);

        // THEN
        assertFalse(result);
    }

    @Test
    @DisplayName("existsInFlightVideoForCourse should return false for FAILED status")
    void existsInFlightVideoForCourse_shouldReturnFalse_forFailedStatus() {
        // GIVEN
        UUID courseIdUuid = UUID.randomUUID();
        Id courseId = Id.of(courseIdUuid.toString());
        Set<VideoStatusEnum> statuses = Set.of(VideoStatusEnum.FAILED);

        when(jpaVideoRepository.existsInFlightVideoForCourse(any(EntityId.class), eq(statuses)))
                .thenReturn(false);

        // WHEN
        boolean result = adapter.existsInFlightVideoForCourse(courseId, statuses);

        // THEN
        assertFalse(result);
    }

    @Test
    @DisplayName("existsInFlightVideoForCourse should handle multiple statuses")
    void existsInFlightVideoForCourse_shouldHandleMultipleStatuses() {
        // GIVEN
        UUID courseIdUuid = UUID.randomUUID();
        Id courseId = Id.of(courseIdUuid.toString());
        Set<VideoStatusEnum> statuses = Set.of(
                VideoStatusEnum.PENDING,
                VideoStatusEnum.PROCESSING,
                VideoStatusEnum.UPLOADED
        );

        when(jpaVideoRepository.existsInFlightVideoForCourse(any(EntityId.class), eq(statuses)))
                .thenReturn(true);

        // WHEN
        boolean result = adapter.existsInFlightVideoForCourse(courseId, statuses);

        // THEN
        assertTrue(result);
    }
}

