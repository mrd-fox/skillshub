package com.simplon_project.skillhub.skillhub.course.adapter.out.response.vimeo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Transcode(
        String status
) {
}