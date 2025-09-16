package com.simplon_project.skillhub.skillhub.storage.application.port.out;

import com.simplon_project.skillhub.skillhub.storage.domaine.model.MediaContent;
import com.simplon_project.skillhub.skillhub.storage.domaine.model.MediaId;

import java.util.Optional;

public interface LoadMediaContentPort {
    Optional<MediaContent> loadMediaContentById(MediaId id);
}
