package com.simplon_project.skillhub.skillhub.course.adapter.out.external.vimeo;

import com.simplon_project.skillhub.skillhub.course.application.dto.VideoUploadInitResult;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.InitProviderUploadCommand;
import com.simplon_project.skillhub.skillhub.course.application.port.out.VideoProviderInitPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class VimeoVideoProviderInitAdapter implements VideoProviderInitPort {

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
                    .uri(vimeoApiBase + "/me/videos")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .header(HttpHeaders.ACCEPT, vimeoAccept)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .onStatus(
                            status -> status.isError(),
                            resp -> resp.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .flatMap(raw -> Mono.error(new IllegalStateException(
                                            "Vimeo init failed: HTTP " + resp.statusCode().value() + " body=" + raw
                                    )))
                    )
                    .bodyToMono(VimeoCreateVideoResponse.class)
                    .block();

            if (body == null || isBlank(body.uri)) {
                throw new IllegalStateException("Vimeo init failed: missing 'uri' in response");
            }
            if (body.upload == null || isBlank(body.upload.upload_link)) {
                throw new IllegalStateException("Vimeo init failed: missing 'upload.upload_link' in response");
            }

            String providerVideoId = extractVideoId(body.uri); // "/videos/{id}"
            String sourceUri = "vimeo://" + providerVideoId;

            // Vimeo does not consistently provide an expiry for the upload_link.
            Instant expiresAt = null;

            return new VideoUploadInitResult(
                    providerVideoId,
                    body.upload.upload_link,
                    expiresAt,
                    sourceUri
            );

        } catch (WebClientResponseException e) {
            String responseBody = safeBody(e);
            log.error("Vimeo init upload failed: status={} body={}", e.getRawStatusCode(), responseBody);
            throw new IllegalStateException("Vimeo init upload failed: HTTP " + e.getRawStatusCode(), e);
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

    private String extractVideoId(String uri) {
        // Expected format: "/videos/{id}"
        String trimmed = uri.trim();
        int idx = trimmed.lastIndexOf('/');
        if (idx < 0 || idx == trimmed.length() - 1) {
            throw new IllegalStateException("Unexpected Vimeo uri format: " + uri);
        }
        return trimmed.substring(idx + 1);
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

    private static final class VimeoCreateVideoResponse {
        public String uri;
        public Upload upload;

        private static final class Upload {
            public String approach;
            public String upload_link;
        }
    }
}