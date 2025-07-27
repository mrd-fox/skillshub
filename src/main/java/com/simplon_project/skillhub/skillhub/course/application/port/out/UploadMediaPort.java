package com.simplon_project.skillhub.skillhub.course.application.port.out;

import java.io.InputStream;

public interface UploadMediaPort {
    void uploadMedia(String bucket, String key, InputStream stream, long size, String contentType);
}
