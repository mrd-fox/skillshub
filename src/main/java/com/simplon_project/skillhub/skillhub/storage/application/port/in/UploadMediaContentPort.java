package com.simplon_project.skillhub.skillhub.storage.application.port.in;

import com.simplon_project.skillhub.skillhub.storage.domaine.model.MediaContent;

import java.io.InputStream;

public interface UploadMediaContentPort {
    MediaContent upload(MediaContent mediaContent, InputStream data);
}
