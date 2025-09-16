package com.simplon_project.skillhub.skillhub.storage.application.port.in;

import com.simplon_project.skillhub.skillhub.storage.domaine.model.MediaContent;
import com.simplon_project.skillhub.skillhub.storage.domaine.model.MediaId;

import java.util.Optional;

public interface FindMediaContentByIdPort {
    Optional<MediaContent> findById(MediaId mediaId);
}
