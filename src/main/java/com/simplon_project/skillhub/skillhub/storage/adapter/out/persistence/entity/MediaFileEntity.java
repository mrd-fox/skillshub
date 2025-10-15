package com.simplon_project.skillhub.skillhub.storage.adapter.out.persistence.entity;


import com.simplon_project.skillhub.skillhub.storage.domaine.model.VideoStatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "media_files")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MediaFileEntity {

    @EmbeddedId
    @EqualsAndHashCode.Include
    private EntityId id;

    @Column(nullable = false, length = 200)
    private String filename;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "size", nullable = false)
    private long size;

    @Column(name = "uploader_id", nullable = false, length = 200)
    private String uploaderId;

    @Column(name = "course_id", nullable = false, length = 100)
    private String courseId;

    @Column(name = "chapter_id", nullable = false, length = 100)
    private String chapterId;

    /**
     * storageKey MinIO (clé objet)
     */
    @Column(name = "storage_path", nullable = false, length = 512)
    private String storagePath;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;
    //seconds
    @Column(name = "duration")
    private Long duration;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private VideoStatusEnum status;

}
