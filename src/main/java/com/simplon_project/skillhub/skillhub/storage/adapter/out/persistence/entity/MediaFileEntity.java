package com.simplon_project.skillhub.skillhub.storage.adapter.out.persistence.entity;

import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "media_files")
public class MediaFileEntity {
    @EmbeddedId
    private EntityId mediaId;

    private String filename;
    private String contentType;
    private long size;
    private String storagePath;
    private LocalDateTime createdAt;

    // Getters/setters

    public EntityId getId() {
        return mediaId;
    }
}
