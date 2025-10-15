package com.simplon_project.skillhub.skillhub.storage.application.port.out;

import java.nio.file.Path;

public interface DownloadStoragePort {
    Path download(String objectKey);
}
