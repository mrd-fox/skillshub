package com.simplon_project.skillhub.skillhub.storage.application.usecase;

import com.simplon_project.skillhub.skillhub.storage.application.port.in.InitiateUploadPort;
import com.simplon_project.skillhub.skillhub.storage.application.port.in.command.InitiateUploadCommand;
import com.simplon_project.skillhub.skillhub.storage.application.port.out.minIo.PresignedUrlPort;
import com.simplon_project.skillhub.skillhub.storage.config.StorageProperties;
import com.simplon_project.skillhub.skillhub.storage.domaine.model.MediaId;
import com.simplon_project.skillhub.skillhub.storage.domaine.model.StoragePresignedMedia;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "storage", name = "enabled", havingValue = "true")
public class InitiateUploadUseCase implements InitiateUploadPort {

    private final PresignedUrlPort presignedUrlPort;
    private final StorageProperties props;
    private final Clock clock;

    @Override
    public StoragePresignedMedia initiate(InitiateUploadCommand command) {
        // validations basiques
        if (command.size() <= 0) throw new IllegalArgumentException("File size must be > 0.");
        if (command.filename() == null || command.filename().isBlank())
            throw new IllegalArgumentException("Filename is required.");
        if (command.contentType() == null || command.contentType().isBlank())
            throw new IllegalArgumentException("Content-Type is required.");

        var mediaId = MediaId.random();
        var safeName = slug(command.filename());
        var key = "%s/courses/%s/chapters/%s/%s/%s".formatted(
                props.prefix(), command.courseId(), command.chapterId(), mediaId.asString(), safeName
        );

        String putUrl = presignedUrlPort.generatePutUrl(props.bucket(), key, props.presignTtlSeconds(), command.contentType());
        Instant expiresAt = Instant.now(clock).plusSeconds(props.presignTtlSeconds());

        return new StoragePresignedMedia(mediaId.asString(), key, putUrl, expiresAt);
    }

    private static String slug(String filename) {
        var norm = Normalizer.normalize(filename, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return norm.toLowerCase().replaceAll("[^a-z0-9._-]", "-").replaceAll("-{2,}", "-");
    }
}