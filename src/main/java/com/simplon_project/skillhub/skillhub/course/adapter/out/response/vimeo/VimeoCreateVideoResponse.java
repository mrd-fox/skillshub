package com.simplon_project.skillhub.skillhub.course.adapter.out.response.vimeo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * Vimeo API response for video creation (POST /me/videos).
 *
 * <p>Provider-specific HTTP response DTO.
 * Used only inside the Vimeo outbound adapter.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VimeoCreateVideoResponse(


        String uri,


        Upload upload

) {

    @JsonIgnoreProperties(ignoreUnknown = true)

    public record Upload(

            @Getter
            String approach,

            @JsonProperty("upload_link")
            @Getter
            String uploadLink

    ) {
    }
}