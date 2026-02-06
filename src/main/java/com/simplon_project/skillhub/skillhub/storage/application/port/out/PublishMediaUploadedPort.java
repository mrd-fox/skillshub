package com.simplon_project.skillhub.skillhub.storage.application.port.out;

import com.simplon_project.skillhub.skillhub.storage.domaine.model.MediaContent;

/**
 * Port OUT pour publier l'événement de media uploadé.
 * SRP : une seule responsabilité = publier l'événement d'upload.
 */
public interface PublishMediaUploadedPort {
    /**
     * Publie un événement indiquant qu'un media a été uploadé.
     *
     * @param content le contenu média uploadé
     */
    void publishMediaUploaded(MediaContent content);
}
