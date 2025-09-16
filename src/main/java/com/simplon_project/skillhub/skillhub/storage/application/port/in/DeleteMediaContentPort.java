package com.simplon_project.skillhub.skillhub.storage.application.port.in;

import com.simplon_project.skillhub.skillhub.storage.domaine.model.MediaId;

public interface DeleteMediaContentPort {
    void delete(MediaId mediaId);
}
