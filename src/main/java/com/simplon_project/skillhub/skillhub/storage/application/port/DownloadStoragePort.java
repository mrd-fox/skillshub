package com.simplon_project.skillhub.skillhub.storage.application.port;

import java.io.InputStream;

public interface DownloadStoragePort {
    InputStream download(String bucket, String key);

}
