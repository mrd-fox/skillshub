package com.simplon_project.skillhub.skillhub.course.adapter.out.response.vimeo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReviewPage(
        Boolean active,
        String link,
        @JsonProperty("is_shareable")
        Boolean isShareable
) {
}
