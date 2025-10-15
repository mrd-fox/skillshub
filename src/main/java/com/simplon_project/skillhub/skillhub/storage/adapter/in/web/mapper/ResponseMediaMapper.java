package com.simplon_project.skillhub.skillhub.storage.adapter.in.web.mapper;

import com.simplon_project.skillhub.skillhub.storage.adapter.in.web.response.MediaContentResponse;
import com.simplon_project.skillhub.skillhub.storage.adapter.in.web.response.ObjectMetadataResponse;
import com.simplon_project.skillhub.skillhub.storage.domaine.model.MediaContent;
import com.simplon_project.skillhub.skillhub.storage.domaine.model.MediaMetadata;

public final class ResponseMediaMapper {
    private ResponseMediaMapper() {
    }

    //        var location = URI.create("/storage/media/" + media.getId().asString());
//        return ResponseEntity.created(location).body(body);


    public static MediaContentResponse mapToMediaResponse(MediaContent mediaContent) {
        return new MediaContentResponse(
                mediaContent.getId().asString(),
                mediaContent.getCourseId(),
                mediaContent.getChapterId(),
                mediaContent.getFilename(),
                mediaContent.getContentType(),
                mediaContent.getSize(),
                mediaContent.getUrl(),        // storageKey
                mediaContent.getCreatedAt()
        );
    }

    public static ObjectMetadataResponse mapToResponse(MediaMetadata mediaMetadata) {
        return new ObjectMetadataResponse(mediaMetadata.sizeBytes(), mediaMetadata.eTag(), mediaMetadata.contentType(), mediaMetadata.lastModified(), mediaMetadata.userMetadata());
    }
}
