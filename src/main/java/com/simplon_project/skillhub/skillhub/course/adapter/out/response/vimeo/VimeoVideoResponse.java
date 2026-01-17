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

        String status,

        Long duration,

        Integer width,

        Integer height,

        Long size,

        String type,

        Pictures pictures,

        Error error

) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Pictures(
            @JsonProperty("base_link")
            String baseLink
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Error(
            String message
    ) {
    }
}