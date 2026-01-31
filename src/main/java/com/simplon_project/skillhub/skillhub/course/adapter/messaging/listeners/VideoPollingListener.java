package com.simplon_project.skillhub.skillhub.course.adapter.messaging.listeners;

import com.simplon_project.skillhub.skillhub.course.adapter.messaging.VideoPollingMessage;
import com.simplon_project.skillhub.skillhub.course.application.dto.ProviderPollingSnapshot;
import com.simplon_project.skillhub.skillhub.course.application.dto.ProviderPollingStateEnum;
import com.simplon_project.skillhub.skillhub.course.application.exception.VideoProviderPollingException;
import com.simplon_project.skillhub.skillhub.course.application.port.out.video.LoadVideoInfoByIdPort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.video.SaveVideoInfoPort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.video.VideoProviderPollingPort;
import com.simplon_project.skillhub.skillhub.course.domain.enums.VideoStatusEnum;
import com.simplon_project.skillhub.skillhub.course.domain.model.VideoInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Rabbit consumer that polls the external provider for video processing status
 * and updates the local VideoInfo state accordingly.
 *
 * <p>This is orchestration code (application boundary). It must NOT call JPA repositories directly.
 */
@Slf4j
@Component
public class VideoPollingListener {

    private static final int MAX_ATTEMPTS = 30;

    private final LoadVideoInfoByIdPort loadVideoInfoByIdPort;
    private final SaveVideoInfoPort saveVideoInfoPort;
    private final VideoProviderPollingPort videoProviderPollingPort;
    private final RabbitTemplate rabbitTemplate;

    @Value("${course.rabbitmq.exchange}")
    private String exchange;

    @Value("${course.rabbitmq.video-polling.polling-delay-routing-key}")
    private String delayRoutingKey;

    @Value("${course.rabbitmq.video-polling.timeout-minutes:15}")
    private int timeoutMinutes;

    public VideoPollingListener(
            LoadVideoInfoByIdPort loadVideoInfoByIdPort,
            SaveVideoInfoPort saveVideoInfoPort,
            VideoProviderPollingPort videoProviderPollingPort,
            @Qualifier("courseRabbitTemplate") RabbitTemplate rabbitTemplate
    ) {
        this.loadVideoInfoByIdPort = loadVideoInfoByIdPort;
        this.saveVideoInfoPort = saveVideoInfoPort;
        this.videoProviderPollingPort = videoProviderPollingPort;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(
            queues = "${course.rabbitmq.video-polling.polling-queue}",
            containerFactory = "courseRabbitListenerContainerFactory"
    )
    public void onMessage(VideoPollingMessage message) {

        if (message == null) {
            log.warn("Video polling message is null - ignoring");
            return;
        }

        log.info("Video polling received: videoId={} attempt={}", message.videoId(), message.attempt());

        Optional<VideoInfo> maybeVideo = loadVideoInfoByIdPort.loadVideoInfoById(message.videoId());
        if (maybeVideo.isEmpty()) {
            log.warn("Video not found for polling: videoId={}", message.videoId());
            return;
        }

        VideoInfo video = maybeVideo.get();

        if (video.status() != VideoStatusEnum.PROCESSING) {
            log.info("Video polling skipped (not processing): videoId={} status={}", message.videoId(), video.status());
            return;
        }

        if (message.attempt() >= MAX_ATTEMPTS) {
            String error = "Polling timeout after " + message.attempt() + " attempts (~" + timeoutMinutes + " minutes)";
            VideoInfo failed = video.markFailed(error);
            saveVideoInfoPort.save(failed);
            log.error(
                    "Video polling timeout -> marked FAILED: videoId={} attempt={} timeoutMinutes={}",
                    message.videoId(),
                    message.attempt(),
                    timeoutMinutes
            );
            return;
        }

        try {
            Optional<ProviderPollingSnapshot> maybeSnapshot = videoProviderPollingPort.poll(video.sourceUri());

            if (maybeSnapshot.isEmpty()) {
                VideoInfo failed = video.markFailed("Provider video not found");
                saveVideoInfoPort.save(failed);
                log.warn("Provider video not found -> marked FAILED: videoId={}", message.videoId());
                return;
            }

            ProviderPollingSnapshot snapshot = maybeSnapshot.get();

            if (snapshot.state() == ProviderPollingStateEnum.AVAILABLE) {
                // When READY, we merge metadata from current poll and previous accumulated metadata in the message
                VideoInfo ready = video.markReady(
                        coalesce(snapshot.thumbnailUrl(), message.thumbnailUrl()),
                        coalesce(snapshot.durationSeconds(), message.duration()),
                        coalesce(snapshot.width(), message.width()),
                        coalesce(snapshot.height(), message.height()),
                        snapshot.format(),
                        coalesce(snapshot.sizeBytes(), message.size()),
                        coalesce(snapshot.embedHash(), message.embedHash())
                );
                saveVideoInfoPort.save(ready);
                log.info("Video marked READY: videoId={}", message.videoId());
                return;
            }

            if (snapshot.state() == ProviderPollingStateEnum.ERROR) {
                String error = snapshot.errorMessage() != null ? snapshot.errorMessage() : "Provider error";
                VideoInfo failed = video.markFailed(error);
                saveVideoInfoPort.save(failed);
                log.warn("Video marked FAILED: videoId={} error={}", message.videoId(), error);
                return;
            }

            // Still processing: re-enqueue with updated metadata carrier
            VideoPollingMessage next = message.nextAttempt(
                    snapshot.sizeBytes(),
                    snapshot.durationSeconds(),
                    snapshot.width(),
                    snapshot.height(),
                    snapshot.thumbnailUrl(),
                    snapshot.embedHash()
            );
            enqueueNext(next);
            log.info("Video still processing -> re-enqueued: videoId={} attempt={}", next.videoId(), next.attempt());

        } catch (VideoProviderPollingException ex) {
            VideoPollingMessage next = message.nextAttempt(null, null, null, null, null, null);
            enqueueNext(next);
            log.warn(
                    "Provider polling failed -> re-enqueued: videoId={} attempt={} err={}",
                    next.videoId(),
                    next.attempt(),
                    ex.getMessage()
            );
        } catch (Exception ex) {
            VideoPollingMessage next = message.nextAttempt(null, null, null, null, null, null);
            enqueueNext(next);
            log.error("Unexpected polling error -> re-enqueued: videoId={} attempt={}", next.videoId(), next.attempt(), ex);
        }
    }

    private <T> T coalesce(T current, T previous) {
        return current != null ? current : previous;
    }

    private void enqueueNext(VideoPollingMessage message) {
        long delayMs = computeDelayMs(message.attempt());

        log.debug("Enqueuing next polling attempt: exchange={}, routingKey={}, videoId={}, attempt={}, delayMs={}",
                exchange, delayRoutingKey, message.videoId(), message.attempt(), delayMs);

        rabbitTemplate.convertAndSend(exchange, delayRoutingKey, message, msg -> {
            // Per-message TTL (works with a DLX-based delay queue).
            // IMPORTANT: ensure the delay queue doesn't enforce a smaller x-message-ttl than this value.
            msg.getMessageProperties().setExpiration(Long.toString(delayMs));
            // Explicitly set content type and encoding to ensure correct deserialization when the message returns from the delay queue
            msg.getMessageProperties().setContentType("application/json");
            msg.getMessageProperties().setContentEncoding("UTF-8");
            return msg;
        });

        log.info("Video polling scheduled: videoId={} attempt={} delayMs={}", message.videoId(), message.attempt(), delayMs);
    }

    private long computeDelayMs(int attempt) {
        // Backoff tuned for MVP: reduces spam but keeps feedback reasonably fast.
        // Total worst-case time (30 attempts) ~ 12-13 minutes.
        if (attempt <= 5) {
            return 5_000L;   // 5s
        } else if (attempt <= 10) {
            return 10_000L;  // 10s
        } else if (attempt <= 20) {
            return 20_000L;  // 20s
        } else {
            return 50_000L;  // 50s
        }
    }
}