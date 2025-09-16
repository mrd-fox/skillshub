package com.simplon_project.skillhub.skillhub.storage.application.port.out.minIo;

import java.io.InputStream;

public interface DownloadFromStoragePort {
    InputStream download(String bucket, String key);

}
