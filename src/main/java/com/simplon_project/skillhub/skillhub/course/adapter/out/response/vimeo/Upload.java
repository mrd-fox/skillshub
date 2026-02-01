package com.simplon_project.skillhub.skillhub.course.adapter.out.response.vimeo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Upload(
        String status,
        String link,
        @JsonProperty("upload_link")
        String uploadLink,
        String approach,
        Long size,
        @JsonProperty("redirect_url")
        String redirectUrl
) {
}