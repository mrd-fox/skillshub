package com.simplon_project.skillhub.skillhub.course.adapter.out.response.vimeo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Pictures(
        String uri,
        Boolean active,
        String type,
        @JsonProperty("base_link")
        String baseLink,
        List<PictureSize> sizes,
        @JsonProperty("resource_key")
        String resourceKey,
        @JsonProperty("default_picture")
        Boolean defaultPicture
) {
}
