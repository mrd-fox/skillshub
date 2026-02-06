package com.simplon_project.skillhub.skillhub.storage.application.usecase;

import com.simplon_project.skillhub.skillhub.storage.application.port.in.CompleteUploadMediaContentPort;
import com.simplon_project.skillhub.skillhub.storage.application.port.in.command.CompleteUploadCommand;
import com.simplon_project.skillhub.skillhub.storage.application.port.out.PublishMediaUploadedPort;
import com.simplon_project.skillhub.skillhub.storage.application.port.out.SaveMediaContentPort;
import com.simplon_project.skillhub.skillhub.storage.application.port.out.StatObjectPort;
import com.simplon_project.skillhub.skillhub.storage.application.worker.VideoProcessingWorker;
import com.simplon_project.skillhub.skillhub.storage.config.StorageProperties;
import com.simplon_project.skillhub.skillhub.storage.domaine.model.MediaContent;
import com.simplon_project.skillhub.skillhub.storage.domaine.model.MediaId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CompleteUploadUseCase implements CompleteUploadMediaContentPort {

    private final StatObjectPort statObjectPort;
    private final SaveMediaContentPort saveMediaContentPort;
    private final StorageProperties props;
    private final Clock clock;
    private final PublishMediaUploadedPort publishMediaUploadedPort;
    private final VideoProcessingWorker videoProcessingWorker;

    @Override
    @Transactional("storageTxManager")
    public MediaContent complete(CompleteUploadCommand command) {
        // 1) verify object existing in minIo et extract size
        var metadata = statObjectPort.stat(props.bucket(), command.storageKey());
        if (metadata == null) {
            throw new IllegalStateException("Object not found in storage.");
        }

        // 2) meta data
        var media = MediaContent.builder()
                .id(MediaId.of(command.mediaId()))
                .filename(command.filename())
                .contentType(metadata.getContentType())
                .size(metadata.getSize())
                .createdAt(LocalDateTime.now(clock))
                .chapterId(command.chapterId())
                .courseId(command.courseId())
                .uploaderId(command.uploaderId())
                .url(command.storageKey()) // ici "url" = storageKey dans ton modèle actuel
                .build();

        // 3) persist
        final var savedMedia = saveMediaContentPort.save(media);

        // 4) publish
        //todo we can evaluate here with "Outbox pattern"
        try {
            publishMediaUploadedPort.publishMediaUploaded(savedMedia);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to publish media event", e);
        }

        // 5️⃣ Déclencher le traitement asynchrone (worker)
        videoProcessingWorker.process(savedMedia); // ✅ non bloquant
        return media;
    }
}