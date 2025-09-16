package com.simplon_project.skillhub.skillhub.storage.application.service;

import com.simplon_project.skillhub.skillhub.storage.application.port.out.minIo.DeleteMediaContentFromStoragePort;
import com.simplon_project.skillhub.skillhub.storage.application.port.out.minIo.DownloadFromStoragePort;
import com.simplon_project.skillhub.skillhub.storage.application.port.out.minIo.UploadStoragePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MediaService {
    private final UploadStoragePort uploadPort;
    private final DownloadFromStoragePort downloadPort;
    private final DeleteMediaContentFromStoragePort deletePort;
//    private final MediaJpaRepository mediaRepository;
}
