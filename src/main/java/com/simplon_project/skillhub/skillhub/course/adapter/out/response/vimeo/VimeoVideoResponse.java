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

        String uri,
        String name,
        String type,
        String link,
        @JsonProperty("player_embed_url")
        String playerEmbedUrl,
        String h, // embed hash (for unlisted/private videos)


        Long duration,
        Integer width,
        Integer height,
        Long size,


        String status,

        @JsonProperty("is_playable")
        Boolean isPlayable,

        @JsonProperty("has_audio")
        Boolean hasAudio,


        Pictures pictures,
        Upload upload,
        Transcode transcode,
        Play play,
        ReviewPage reviewPage,


        ErrorVimeo error

) {
}