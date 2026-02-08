package com.simplon_project.skillhub.skillhub.course.adapter.out.external.vimeo;

import com.simplon_project.skillhub.skillhub.course.adapter.out.response.vimeo.VimeoCreateVideoResponse;
import com.simplon_project.skillhub.skillhub.course.adapter.out.response.vimeo.VimeoVideoResponse;
import com.simplon_project.skillhub.skillhub.course.application.dto.ProviderPollingSnapshot;
import com.simplon_project.skillhub.skillhub.course.application.dto.ProviderPollingStateEnum;
import com.simplon_project.skillhub.skillhub.course.application.dto.VideoUploadInitResult;
import com.simplon_project.skillhub.skillhub.course.application.exception.VideoProviderDeletionException;
import com.simplon_project.skillhub.skillhub.course.application.exception.VideoProviderDeletionPermanentException;
import com.simplon_project.skillhub.skillhub.course.application.exception.VideoProviderPollingException;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.InitProviderUploadCommand;
import com.simplon_project.skillhub.skillhub.course.application.port.out.video.VideoProviderDeletionPort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.video.VideoProviderInitPort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.video.VideoProviderPollingPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.springframework.util.StringUtils.hasText;

@Slf4j
@Component
@RequiredArgsConstructor
public class VimeoVideoProviderAdapter implements VideoProviderInitPort, VideoProviderPollingPort, VideoProviderDeletionPort {

    public static final String PREFIX_VIMEO = "vimeo://";
    public static final String PATH_POLL_VIDEO_BY_ID = "/videos/{id}";
    public static final String BEARER = "Bearer ";
    public static final String PATH_INIT_ME_VIDEOS = "/me/videos";

    private final WebClient webClient;

    @Value("${vimeo.base-url}")
    private String vimeoApiBase;

    @Value("${vimeo.accept}")
    private String vimeoAccept;

    @Value("${vimeo.access-token}")
    private String accessToken;

    @Override
    public VideoUploadInitResult initTusUpload(InitProviderUploadCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        if (command.sizeBytes() <= 0) {
            throw new IllegalArgumentException("sizeBytes must be > 0");
        }

        Map<String, Object> payload = buildCreateVideoPayload(command);

        try {
            VimeoCreateVideoResponse body = webClient
                    .post()
                    .uri(vimeoApiBase + PATH_INIT_ME_VIDEOS)
                    .header(HttpHeaders.AUTHORIZATION, BEARER + accessToken)
                    .header(HttpHeaders.ACCEPT, vimeoAccept)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(VimeoCreateVideoResponse.class)
                    .block();

            if (body == null || isBlank(body.uri())) {
                throw new IllegalStateException("Vimeo init failed: missing 'uri' in response");
            }
            if (body.upload() == null || isBlank(body.upload().getUploadLink())) {
                throw new IllegalStateException("Vimeo init failed: missing 'upload.upload_link' in response");
            }

            String providerVideoId = extractVideoIdFromVimeoUri(body.uri()); // "/videos/{id}"
            String sourceUri = PREFIX_VIMEO + providerVideoId;

            Instant expiresAt = null; // Vimeo does not consistently provide expiry

            return new VideoUploadInitResult(
                    providerVideoId,
                    body.upload().getUploadLink(),
                    expiresAt,
                    sourceUri
            );

        } catch (WebClientResponseException e) {

            int status = e.getStatusCode().value();
            String body = safeBody(e);

            if (status >= 400 && status < 500) {
                log.warn("Vimeo init failed (client error): status={} body={}", status, body);
            } else {
                log.error("Vimeo init failed (server error): status={} body={}", status, body);
            }

            throw new IllegalStateException("Vimeo init upload failed: HTTP " + status, e);

        } catch (Exception ex) {
            log.error("Vimeo init upload failed (unexpected error): sizeBytes={}", command.sizeBytes(), ex);
            throw new IllegalStateException("Vimeo init upload failed", ex);
        }
    }

    @Override
    public Optional<ProviderPollingSnapshot> poll(String sourceUri) {
        String vimeoId = extractVimeoIdFromSourceUri(sourceUri);

        log.info(
                "Vimeo poll start: sourceUri={} vimeoId={} endpoint={}/videos/{}",
                sourceUri, vimeoId, vimeoApiBase, vimeoId
        );

        try {
            VimeoVideoResponse response = webClient
                    .get()
                    .uri(vimeoApiBase + PATH_POLL_VIDEO_BY_ID, vimeoId)
                    .header(HttpHeaders.ACCEPT, vimeoAccept)
                    .header(HttpHeaders.AUTHORIZATION, BEARER + accessToken)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(VimeoVideoResponse.class)
                    .block();

            if (response == null) {
                throw new VideoProviderPollingException("Vimeo returned an empty response for id=" + vimeoId);
            }

            String picturesBaseLink = (response.pictures() != null) ? response.pictures().baseLink() : null;

            String uploadStatus = (response.upload() != null) ? response.upload().status() : null;
            Long uploadSize = (response.upload() != null) ? response.upload().size() : null;
            String transcodeStatus = (response.transcode() != null) ? response.transcode().status() : null;
            String playStatus = (response.play() != null) ? response.play().status() : null;

            String errorMsg = (response.error() != null) ? response.error().message() : null;

            log.info(
                    "Vimeo poll HTTP 200: id={} status={} upload.status={} upload.size={} transcode.status={} play.status={} isPlayable={} duration={} size={} width={} height={} pictures.base_link={} error={}",
                    vimeoId,
                    response.status(),
                    uploadStatus,
                    uploadSize,
                    transcodeStatus,
                    playStatus,
                    response.isPlayable(),
                    response.duration(),
                    response.size(),
                    response.width(),
                    response.height(),
                    picturesBaseLink,
                    errorMsg
            );

            ProviderPollingSnapshot mapped = mapToSnapshot(vimeoId, response);

            log.info(
                    "Vimeo poll mapped: id={} state={} durationSeconds={} sizeBytes={} width={} height={} thumbnailUrl={} errorMessage={}",
                    vimeoId,
                    mapped.state(),
                    mapped.durationSeconds(),
                    mapped.sizeBytes(),
                    mapped.width(),
                    mapped.height(),
                    mapped.thumbnailUrl(),
                    mapped.errorMessage()
            );

            return Optional.of(mapped);

        } catch (WebClientResponseException ex) {

            int status = ex.getStatusCode().value();

            if (status == 404) {
                log.info("Vimeo poll: video not found id={}", vimeoId);
                return Optional.empty();
            }

            String body = safeBody(ex);

            if (status >= 400 && status < 500) {
                log.warn("Vimeo poll failed (client error): status={} id={} body={}", status, vimeoId, body);
            } else {
                log.error("Vimeo poll failed (server error): status={} id={} body={}", status, vimeoId, body);
            }

            throw new VideoProviderPollingException("Vimeo polling failed: status=" + status + " id=" + vimeoId, ex);

        } catch (Exception ex) {
            log.error("Vimeo poll failed (unexpected error): id={}", vimeoId, ex);
            throw new VideoProviderPollingException("Vimeo polling failed for id=" + vimeoId, ex);
        }
    }

    private Map<String, Object> buildCreateVideoPayload(InitProviderUploadCommand command) {
        return Map.of(
                "upload", Map.of(
                        "approach", "tus",
                        "size", command.sizeBytes()
                ),
                "name", nullToEmpty(command.title()),
                "description", nullToEmpty(command.description()),
                "privacy", Map.of(
                        "view", mapPrivacy(command.privacy())
                )
        );
    }

    private String mapPrivacy(String privacy) {
        if (privacy == null) {
            return "private";
        }
        String p = privacy.trim().toLowerCase();

        if (p.equals("private")) {
            return "private";
        } else if (p.equals("unlisted")) {
            return "unlisted";
        } else if (p.equals("public")) {
            return "anybody";
        } else {
            return "private";
        }
    }

    private String extractVideoIdFromVimeoUri(String uri) {
        String trimmed = uri.trim();
        int idx = trimmed.lastIndexOf('/');
        if (idx < 0 || idx == trimmed.length() - 1) {
            throw new IllegalStateException("Unexpected Vimeo uri format: " + uri);
        }
        return trimmed.substring(idx + 1);
    }

    private String extractVimeoIdFromSourceUri(String sourceUri) {
        if (!hasText(sourceUri) || !sourceUri.startsWith(PREFIX_VIMEO)) {
            throw new VideoProviderPollingException("Invalid sourceUri: " + sourceUri);
        }
        return sourceUri.substring(PREFIX_VIMEO.length()).trim();
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private String safeBody(WebClientResponseException e) {
        try {
            return e.getResponseBodyAsString();
        } catch (Exception ex) {
            return "<unavailable>";
        }
    }

    private ProviderPollingSnapshot mapToSnapshot(String vimeoId, VimeoVideoResponse response) {

        ProviderPollingStateEnum state = mapProviderState(response);

        String thumbnailUrl = null;
        if (response.pictures() != null && hasText(response.pictures().baseLink())) {
            thumbnailUrl = response.pictures().baseLink();
        }

        Long durationSeconds = response.duration();
        Integer width = response.width();
        Integer height = response.height();

        // size can be at root or inside upload object (Vimeo moves it once upload is finished)
        Long sizeBytes = response.size();
        if (sizeBytes == null && response.upload() != null) {
            sizeBytes = response.upload().size();
        }

        String format = null;
        if (hasText(response.type())) {
            format = response.type();
        }

        // Extract embed hash (explicit field 'h' OR from player_embed_url)
        String embedHash = response.h();
        if (!hasText(embedHash) && hasText(response.playerEmbedUrl())) {
            embedHash = extractHashFromEmbedUrl(response.playerEmbedUrl());
        }

        String errorMessage = null;
        if (response.error() != null && hasText(response.error().message())) {
            errorMessage = response.error().message();
        }

        return new ProviderPollingSnapshot(
                state,
                vimeoId,
                thumbnailUrl,
                durationSeconds,
                width,
                height,
                sizeBytes,
                format,
                embedHash,
                errorMessage
        );
    }

    private String extractHashFromEmbedUrl(String url) {
        if (url == null || !url.contains("?h=")) {
            return null;
        }
        try {
            int start = url.indexOf("?h=") + 3;
            int end = url.indexOf("&", start);
            if (end == -1) {
                return url.substring(start);
            }
            return url.substring(start, end);
        } catch (Exception e) {
            log.debug("Failed to extract hash from embed url: {}", url);
            return null;
        }
    }

    private ProviderPollingStateEnum mapProviderState(VimeoVideoResponse response) {

        // 1) Hard error signal from Vimeo
        if (response.error() != null && hasText(response.error().message())) {
            log.debug("Vimeo poll: ERROR state detected. error={}", response.error().message());
            return ProviderPollingStateEnum.ERROR;
        }

        // 2) Strong "available" signal based on the fields you pasted from Vimeo
        boolean playable = Boolean.TRUE.equals(response.isPlayable());

        String playStatus = (response.play() != null) ? response.play().status() : null;
        String transcodeStatus = (response.transcode() != null) ? response.transcode().status() : null;
        String uploadStatus = (response.upload() != null) ? response.upload().status() : null;

        boolean playOk = hasText(playStatus) && playStatus.trim().equalsIgnoreCase("playable");
        boolean transcodeOk = hasText(transcodeStatus) && transcodeStatus.trim().equalsIgnoreCase("complete");
        boolean uploadOk = hasText(uploadStatus) && uploadStatus.trim().equalsIgnoreCase("complete");

        if (playable && playOk && transcodeOk && uploadOk) {
            log.debug("Vimeo poll: AVAILABLE state detected (all criteria met).");
            return ProviderPollingStateEnum.AVAILABLE;
        }

        // 3) Fallback on root status if present
        String status = response.status();
        if (hasText(status) && status.trim().equalsIgnoreCase("available")) {
            log.debug("Vimeo poll: AVAILABLE state detected (root status is available).");
            return ProviderPollingStateEnum.AVAILABLE;
        }

        // Detailed log to understand which criteria is blocking the transition to AVAILABLE
        log.debug("Vimeo poll: PROCESSING state. playable={} playOk={} transcodeOk={} uploadOk={} status={}",
                playable, playOk, transcodeOk, uploadOk, status);

        // Otherwise still processing
        return ProviderPollingStateEnum.PROCESSING;
    }

    @Override
    public void delete(String sourceUri) {
        if (sourceUri == null || sourceUri.isBlank()) {
            throw new IllegalArgumentException("sourceUri must not be blank");
        }

        String vimeoId = extractVimeoIdFromSourceUri(sourceUri);

        log.info(
                "Vimeo delete start: sourceUri={} vimeoId={} endpoint={}/videos/{}",
                sourceUri, vimeoId, vimeoApiBase, vimeoId
        );

        try {
            // NOTE:
            // - Vimeo returns 204 on success.
            // - retrieve() will throw WebClientResponseException on 4xx/5xx by default.
            // - We keep .toBodilessEntity() to avoid buffering any body.
            var response = webClient
                    .delete()
                    .uri(vimeoApiBase + PATH_POLL_VIDEO_BY_ID, vimeoId)
                    .header(HttpHeaders.ACCEPT, vimeoAccept)
                    .header(HttpHeaders.AUTHORIZATION, BEARER + accessToken)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            // Defensive: if block() returned null, treat as transient
            if (response == null) {
                String msg = "Vimeo delete transient error (retry): null response entity vimeoId=" + vimeoId;
                log.warn(msg);
                throw new VideoProviderDeletionException(msg);
            }

            // Defensive: if status isn't 2xx, classify it
            int status = response.getStatusCode().value();
            if (status == 204 || (status >= 200 && status < 300)) {
                log.info("Vimeo delete SUCCESS: sourceUri={} vimeoId={} (HTTP {})", sourceUri, vimeoId, status);
                return;
            }

            if (status == 404) {
                log.info("Vimeo delete SUCCESS (idempotent): sourceUri={} vimeoId={} (HTTP 404)", sourceUri, vimeoId);
                return;
            }

            if (status >= 500) {
                String msg = "Vimeo delete transient error (retry): status=" + status + " vimeoId=" + vimeoId;
                log.warn(msg);
                throw new VideoProviderDeletionException(msg);
            }

            String msg = "Vimeo delete permanent client error (no retry): status=" + status + " vimeoId=" + vimeoId;
            log.error(msg);
            throw new VideoProviderDeletionPermanentException(msg);

        } catch (WebClientResponseException ex) {

            int status = ex.getStatusCode().value();
            String body = safeBody(ex);

            if (status == 404) {
                log.info(
                        "Vimeo delete SUCCESS (idempotent): sourceUri={} vimeoId={} (HTTP 404)",
                        sourceUri, vimeoId
                );
                return;
            }

            // 5xx => transient => retry
            if (status >= 500) {
                String msg = "Vimeo delete transient error (retry): status=" + status + " vimeoId=" + vimeoId + " body=" + body;
                log.warn(msg);
                throw new VideoProviderDeletionException(msg, ex);
            }

            // 401/403 are permanent (token/config issue)
            if (status == 401 || status == 403) {
                String msg = "Vimeo delete permanent auth error (no retry): status=" + status + " vimeoId=" + vimeoId + " body=" + body;
                log.error(msg);
                throw new VideoProviderDeletionPermanentException(msg, ex);
            }

            // 4xx (except 404) => permanent (NO retry)
            String msg = "Vimeo delete permanent client error (no retry): status=" + status + " vimeoId=" + vimeoId + " body=" + body;
            log.error(msg);
            throw new VideoProviderDeletionPermanentException(msg, ex);

        } catch (VideoProviderDeletionException | VideoProviderDeletionPermanentException ex) {
            // passthrough: already classified
            throw ex;

        } catch (Exception ex) {
            // network/timeout etc => transient => retry
            String msg = "Vimeo delete network error (retry): vimeoId=" + vimeoId + " error=" + ex.getMessage();
            log.warn(msg, ex);
            throw new VideoProviderDeletionException(msg, ex);
        }
    }
}