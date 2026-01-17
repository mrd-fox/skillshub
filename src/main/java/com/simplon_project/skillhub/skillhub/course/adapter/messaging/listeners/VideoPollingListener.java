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

    @RabbitListener(queues = "${course.rabbitmq.video-polling.polling-queue}")
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
            log.error("Video polling timeout -> marked FAILED: videoId={} attempt={} timeoutMinutes={}", message.videoId(), message.attempt(), timeoutMinutes);
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
                VideoInfo ready = video.markReady(
                        snapshot.thumbnailUrl(),
                        snapshot.durationSeconds(),
                        snapshot.width(),
                        snapshot.height(),
                        snapshot.format(),
                        snapshot.sizeBytes()
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

            VideoPollingMessage next = message.nextAttempt();
            enqueueNext(next);
            log.info("Video still processing -> re-enqueued: videoId={} attempt={}", next.videoId(), next.attempt());

        } catch (VideoProviderPollingException ex) {
            VideoPollingMessage next = message.nextAttempt();
            enqueueNext(next);
            log.warn(
                    "Provider polling failed -> re-enqueued: videoId={} attempt={} err={}",
                    next.videoId(),
                    next.attempt(),
                    ex.getMessage()
            );
        } catch (Exception ex) {
            VideoPollingMessage next = message.nextAttempt();
            enqueueNext(next);
            log.error("Unexpected polling error -> re-enqueued: videoId={} attempt={}", next.videoId(), next.attempt(), ex);
        }
    }

    private void enqueueNext(VideoPollingMessage message) {
        rabbitTemplate.convertAndSend(exchange, delayRoutingKey, message);
    }
}