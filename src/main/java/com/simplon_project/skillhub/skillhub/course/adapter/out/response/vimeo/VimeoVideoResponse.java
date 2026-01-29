package com.simplon_project.skillhub.skillhub.course.adapter.out.response.vimeo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Vimeo API response for GET /videos/{id}.
 *
 * <p>Provider-specific HTTP DTO.
 * Used ONLY inside the Vimeo outbound adapter.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VimeoVideoResponse(
        // Core identity / links
        String uri,
        String name,
        String type,
        String link,
        @JsonProperty("player_embed_url")
        String playerEmbedUrl,

        // Media metadata
        Long duration,
        Integer width,
        Integer height,
        Long size,

        // Processing / availability
        String status,

        @JsonProperty("is_playable")
        Boolean isPlayable,

        @JsonProperty("has_audio")
        Boolean hasAudio,

        // Nested objects we actually use
        Pictures pictures,
        Upload upload,
        Transcode transcode,
        Play play,
        ReviewPage reviewPage,

        // Error payload (may be null)
        ErrorVimeo error

) {
}