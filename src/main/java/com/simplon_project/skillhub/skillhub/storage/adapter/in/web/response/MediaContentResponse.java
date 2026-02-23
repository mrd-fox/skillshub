package com.simplon_project.skillhub.skillhub.storage.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(name = "MediaContentResponse", description = "Métadonnées du média persisté après upload.")
public record MediaContentResponse(

        @Schema(description = "Identifiant du média (UUID).",
                example = "9f6b4a3e-7f0a-4f0a-9a1c-2a1b9f6c3d2e")
        String mediaId,

        @Schema(description = "Identifiant du cours auquel le média est rattaché.",
                example = "5c8f1b2a-3d4e-5f6a-7b8c-9d0e1f2a3b4c")
        String courseId,

        @Schema(description = "Identifiant du chapitre du cours.",
                example = "1a2b3c4d-5e6f-7081-92a3-b4c5d6e7f809")
        String chapterId,

        @Schema(description = "Nom de fichier original (nettoyé le cas échéant).",
                example = "big-buck-bunny-1080p.mp4")
        String filename,

        @Schema(description = "Type MIME du média.",
                example = "video/mp4")
        String contentType,

        @Schema(description = "Taille du fichier en octets.",
                example = "73400320")
        long size,

        @Schema(description = "Clé objet de stockage (MinIO/S3).",
                example = "videos/courses/5c8f.../chapters/1a2b.../9f6b.../big-buck-bunny-1080p.mp4")
        String storageKey,

        @Schema(description = "Horodatage de création (UTC), au format ISO-8601.",
                type = "string", format = "date-time",
                example = "2025-09-18T12:34:56Z")
        LocalDateTime createdAt
) {
}