package com.simplon_project.skillhub.skillhub.storage.application.worker;

import com.simplon_project.skillhub.skillhub.storage.application.port.out.DownloadStoragePort;
import com.simplon_project.skillhub.skillhub.storage.application.port.out.PublishMetadataExtractedPort;
import com.simplon_project.skillhub.skillhub.storage.application.port.out.UpdateMediaPort;
import com.simplon_project.skillhub.skillhub.storage.domaine.model.MediaContent;
import com.simplon_project.skillhub.skillhub.storage.domaine.model.VideoStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "storage", name = "enabled", havingValue = "true")
@Slf4j
public class VideoProcessingWorker {
    private final UpdateMediaPort updateMediaPort;
    private final DownloadStoragePort downloadStoragePort;
    private final VideoMetadataExtractor metadataExtractor;
    private final DiskAwareQueue diskQueue; // ✅ injection de la queue
    private final PublishMetadataExtractedPort publishMetadataExtractedPort;

    @Async("videoExecutor")
    @Transactional("storageTxManager")
    public void process(MediaContent media) {
        Path localFile = null;
        try {
            // 1️⃣ Attendre un slot libre (en fonction de l'espace disque)
            diskQueue.acquireSlot();

            log.info("▶️ Démarrage du traitement pour {}", media.getFilename());
            localFile = downloadStoragePort.download(media.getUrl());

            // 2️⃣ Analyse des métadonnées
            var metadata = metadataExtractor.extract(localFile);
            updateMediaPort.updateMetadata(media.getId(),
                    metadata.width(),
                    metadata.height(),
                    metadata.duration(),
                    VideoStatusEnum.READY);

            log.info("✅ Traitement terminé pour {}", media.getFilename());
            // 🔥 Publish event extracted
            publishMetadataExtractedPort.publishMetadataExtracted(media, metadata);
        } catch (Exception e) {
            log.error("❌ Erreur pendant le traitement vidéo {}", media.getFilename(), e);
        } finally {
            try {
                if (localFile != null) {
                    Files.deleteIfExists(localFile);
                    log.debug("🧹 Fichier temporaire supprimé : {}", localFile);
                }
            } catch (Exception ex) {
                log.warn("⚠️ Impossible de supprimer le fichier temporaire {}", localFile, ex);
            }
            diskQueue.releaseSlot(); // ✅ libération du slot
        }
    }
}
