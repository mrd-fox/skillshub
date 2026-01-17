package com.simplon_project.skillhub.skillhub.course.adapter.out.external.vimeo;

import com.simplon_project.skillhub.skillhub.course.adapter.out.response.vimeo.VimeoCreateVideoResponse;
import com.simplon_project.skillhub.skillhub.course.adapter.out.response.vimeo.VimeoVideoResponse;
import com.simplon_project.skillhub.skillhub.course.application.dto.ProviderPollingSnapshot;
import com.simplon_project.skillhub.skillhub.course.application.dto.ProviderPollingStateEnum;
import com.simplon_project.skillhub.skillhub.course.application.dto.VideoUploadInitResult;
import com.simplon_project.skillhub.skillhub.course.application.exception.VideoProviderPollingException;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.InitProviderUploadCommand;
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
public class VimeoVideoProviderAdapter implements VideoProviderInitPort, VideoProviderPollingPort {

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

            // Vimeo does not consistently provide an expiry for the upload_link.
            Instant expiresAt = null;

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

            return Optional.of(mapToSnapshot(vimeoId, response));

        } catch (WebClientResponseException ex) {

            int status = ex.getStatusCode().value();

            if (status == 404) {
                log.info("Vimeo poll: video not found id={}", vimeoId);
                return Optional.empty();
            }

            String body = safeBody(ex);

            if (status >= 400 && status < 500) {
                log.warn(
                        "Vimeo poll failed (client error): status={} id={} body={}",
                        status,
                        vimeoId,
                        body
                );
            } else {
                log.error(
                        "Vimeo poll failed (server error): status={} id={} body={}",
                        status,
                        vimeoId,
                        body
                );
            }

            throw new VideoProviderPollingException(
                    "Vimeo polling failed: status=" + status + " id=" + vimeoId,
                    ex
            );
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
            // Vimeo uses "anybody" to indicate public visibility.
            return "anybody";
        } else {
            return "private";
        }
    }

    private String extractVideoIdFromVimeoUri(String uri) {
        // Expected format: "/videos/{id}"
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

        var state = mapProviderState(response);

        String thumbnailUrl = null;
        if (response.pictures() != null && hasText(response.pictures().baseLink())) {
            thumbnailUrl = response.pictures().baseLink();
        }

        var durationSeconds = response.duration();

        Integer width = response.width();
        Integer height = response.height();

        var sizeBytes = response.size();

        String format = null;
        if (hasText(response.type())) {
            format = response.type();
        }

        String errorMessage = null;
        if (state == ProviderPollingStateEnum.ERROR) {
            if (response.error() != null && hasText(response.error().message())) {
                errorMessage = response.error().message();
            }
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
                errorMessage
        );
    }

    private ProviderPollingStateEnum mapProviderState(VimeoVideoResponse response) {

        String status = response.status();

        if (!hasText(status)) {
            return ProviderPollingStateEnum.PROCESSING;
        }

        String s = status.trim().toLowerCase();

        if (s.equals("available")) {
            return ProviderPollingStateEnum.AVAILABLE;
        } else if (s.equals("error") || s.equals("failed")) {
            return ProviderPollingStateEnum.ERROR;
        } else {
            return ProviderPollingStateEnum.PROCESSING;
        }
    }
}