package com.simplon_project.skillhub.skillhub.storage.application.port.out;

import com.simplon_project.skillhub.skillhub.storage.domaine.model.MediaContent;
import com.simplon_project.skillhub.skillhub.storage.domaine.model.VideoMetadata;

/**
 * Port OUT pour publier l'événement de métadonnées extraites.
 * SRP : une seule responsabilité = publier l'événement d'extraction de métadonnées.
 */
public interface PublishMetadataExtractedPort {
    /**
     * Publie un événement indiquant que les métadonnées d'une vidéo ont été extraites.
     *
     * @param content  le contenu média
     * @param metadata les métadonnées extraites
     */
    void publishMetadataExtracted(MediaContent content, VideoMetadata metadata);
}
