package com.simplon_project.skillhub.skillhub.course.adapter.messaging.listeners;

import com.simplon_project.skillhub.skillhub.course.adapter.messaging.VideoDeletionMessage;
import com.simplon_project.skillhub.skillhub.course.application.dto.VideoDeletionTarget;
import com.simplon_project.skillhub.skillhub.course.application.exception.VideoProviderDeletionException;
import com.simplon_project.skillhub.skillhub.course.application.exception.VideoProviderDeletionPermanentException;
import com.simplon_project.skillhub.skillhub.course.application.port.out.video.LoadVideoEntityByIdPort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.video.SaveVideoEntityPort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.video.VideoProviderDeletionPort;
import com.simplon_project.skillhub.skillhub.course.domain.enums.ExternalDeletionStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;


@Slf4j
@Component
public class VideoDeletionListener {

    private final LoadVideoEntityByIdPort loadVideoEntityByIdPort;
    private final SaveVideoEntityPort saveVideoEntityPort;
    private final VideoProviderDeletionPort videoProviderDeletionPort;
    private final RabbitTemplate rabbitTemplate;

    @Value("${course.rabbitmq.exchange}")
    private String exchange;

    @Value("${course.rabbitmq.video-deletion.deletion-delay-routing-key}")
    private String delayRoutingKey;

    @Value("${course.rabbitmq.video-deletion.max-retry-attempts:5}")
    private int maxRetryAttempts;

    public VideoDeletionListener(
            LoadVideoEntityByIdPort loadVideoEntityByIdPort,
            SaveVideoEntityPort saveVideoEntityPort,
            VideoProviderDeletionPort videoProviderDeletionPort,
            @Qualifier("courseRabbitTemplate") RabbitTemplate rabbitTemplate
    ) {
        this.loadVideoEntityByIdPort = loadVideoEntityByIdPort;
        this.saveVideoEntityPort = saveVideoEntityPort;
        this.videoProviderDeletionPort = videoProviderDeletionPort;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(
            queues = "${course.rabbitmq.video-deletion.deletion-queue}",
            containerFactory = "courseRabbitListenerContainerFactory"
    )
    public void onMessage(VideoDeletionMessage message) {

        if (message == null) {
            log.warn("Video deletion message is null - ignoring");
            return;
        }

        log.info(
                "Video deletion received: videoId={} sourceUri={} attempt={}",
                message.videoId(),
                message.sourceUri(),
                message.attempt()
        );

        Optional<VideoDeletionTarget> maybeTarget =
                loadVideoEntityByIdPort.loadIncludingSoftDeleted(message.videoId());

        if (maybeTarget.isEmpty()) {
            log.warn("Video not found in DB - ACK and skip: videoId={}", message.videoId());
            return;
        }

        VideoDeletionTarget target = maybeTarget.get();

        if (target.externalDeletionStatus() == ExternalDeletionStatus.DELETED) {
            log.info("Video already DELETED - ACK and skip: videoId={}", message.videoId());
            return;
        }

        if (target.externalDeletionStatus() != ExternalDeletionStatus.REQUESTED) {
            log.warn(
                    "Video not REQUESTED - ACK and skip: videoId={} status={}",
                    message.videoId(),
                    target.externalDeletionStatus()
            );
            return;
        }

        // attempt is 1-based. If attempt > max => fail without calling provider
        if (message.attempt() > maxRetryAttempts) {
            saveVideoEntityPort.markFailed(
                    message.videoId(),
                    message.attempt(),
                    "Max retry attempts exceeded (" + maxRetryAttempts + ")"
            );
            log.error(
                    "Video deletion FAILED - max retries exceeded: videoId={} attempt={} maxRetries={}",
                    message.videoId(),
                    message.attempt(),
                    maxRetryAttempts
            );
            return;
        }

        // Provider call MUST be outside transaction
        try {
            videoProviderDeletionPort.delete(message.sourceUri());

            saveVideoEntityPort.markDeleted(message.videoId());

            log.info(
                    "Video deletion SUCCESS: videoId={} sourceUri={} attempt={}",
                    message.videoId(),
                    message.sourceUri(),
                    message.attempt()
            );

        } catch (VideoProviderDeletionPermanentException ex) {

            // No retry for permanent errors
            saveVideoEntityPort.markFailed(message.videoId(), message.attempt(), safe(ex.getMessage()));

            log.error(
                    "Video deletion FAILED (permanent, no retry): videoId={} attempt={} error={}",
                    message.videoId(),
                    message.attempt(),
                    safe(ex.getMessage())
            );

        } catch (VideoProviderDeletionException ex) {

            // Retry for transient errors
            scheduleRetry(message, ex.getMessage());

        } catch (Exception ex) {

            // Unknown: safer to retry (still capped by maxRetryAttempts)
            scheduleRetry(message, ex.getMessage());
        }
    }

    private void scheduleRetry(VideoDeletionMessage message, String errorMessage) {

        int nextAttempt = message.attempt() + 1;

        if (nextAttempt > maxRetryAttempts) {

            saveVideoEntityPort.markFailed(
                    message.videoId(),
                    message.attempt(),
                    "Max retry attempts exceeded (" + maxRetryAttempts + "): " + safe(errorMessage)
            );

            log.error(
                    "Video deletion FAILED - max retries exceeded: videoId={} attempt={} maxRetries={} error={}",
                    message.videoId(),
                    message.attempt(),
                    maxRetryAttempts,
                    safe(errorMessage)
            );

            return;
        }

        // IMPORTANT FIX:
        // Persist the NEXT attempt count (not the current one)
        saveVideoEntityPort.markRetryScheduled(
                message.videoId(),
                nextAttempt,
                safe(errorMessage)
        );

        VideoDeletionMessage nextMsg = message.nextAttempt();
        long delayMs = computeDelayMs(nextMsg.attempt());

        enqueueWithDelay(nextMsg, delayMs);

        log.warn(
                "Video deletion RETRY scheduled: videoId={} attempt={}/{} delayMs={} error={}",
                message.videoId(),
                nextMsg.attempt(),
                maxRetryAttempts,
                delayMs,
                safe(errorMessage)
        );
    }

    private void enqueueWithDelay(VideoDeletionMessage message, long delayMs) {
        rabbitTemplate.convertAndSend(
                exchange,
                delayRoutingKey,
                message,
                msg -> {
                    msg.getMessageProperties().setExpiration(String.valueOf(delayMs));
                    msg.getMessageProperties().setTimestamp(java.util.Date.from(Instant.now()));
                    return msg;
                }
        );
    }

    private long computeDelayMs(int attempt) {
        if (attempt <= 2) {
            return 10_000L;
        } else if (attempt <= 4) {
            return 30_000L;
        } else {
            return 60_000L;
        }
    }

    private String safe(String value) {
        if (value == null || value.isBlank()) {
            return "<no-message>";
        } else {
            return value;
        }
    }
}