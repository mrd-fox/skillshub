package com.simplon_project.skillhub.skillhub.storage.application.usecase;

import com.simplon_project.skillhub.skillhub.storage.application.port.in.command.GetObjectMetadataCommand;
import com.simplon_project.skillhub.skillhub.storage.application.port.out.GetObjectMetadataPort;
import com.simplon_project.skillhub.skillhub.storage.application.port.out.minIo.StoragePort;
import com.simplon_project.skillhub.skillhub.storage.domaine.model.MediaMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ObjectMetadataUseCase implements GetObjectMetadataPort {

    private final StoragePort storagePort;

    @Override
    public MediaMetadata get(GetObjectMetadataCommand command) {
        return storagePort.head(command.objectKey());
    }
}
