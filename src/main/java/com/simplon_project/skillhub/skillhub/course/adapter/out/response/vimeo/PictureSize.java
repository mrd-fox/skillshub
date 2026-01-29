package com.simplon_project.skillhub.skillhub.course.adapter.out.response.vimeo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PictureSize(
        Integer width,
        Integer height,
        String link,
        @JsonProperty("link_with_play_button")
        String linkWithPlayButton
) {
}
