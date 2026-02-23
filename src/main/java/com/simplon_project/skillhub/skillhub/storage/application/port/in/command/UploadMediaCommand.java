//package com.simplon_project.skillhub.skillhub.storage.application.port.in.command;
//
//import com.simplon_project.skillhub.skillhub.storage.domaine.model.MediaContent;
//import com.simplon_project.skillhub.skillhub.storage.domaine.model.MediaId;
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.Positive;
//
//import java.io.InputStream;
//import java.time.Clock;
//import java.time.LocalDateTime;
//import java.util.Objects;
//import java.util.Set;
//import java.util.function.Supplier;
//
//public record UploadMediaCommand(
//        @NotBlank String uploaderId,
//        @NotBlank String courseId,
//        @NotBlank String chapterId,
//        @NotBlank String filename,
//        @NotBlank String contentType,
//        @Positive long size,
//        Supplier<InputStream> dataSupplier
//) {
//    private static final Set<String> ALLOWED_MIME = Set.of(
//            "video/mp4", "video/webm", "video/ogg", "application/pdf"
//    );
//
//    public UploadMediaCommand {
//        if (!ALLOWED_MIME.contains(contentType)) {
//            throw new IllegalArgumentException("Unsupported content type: " + contentType);
//        }
//        Objects.requireNonNull(dataSupplier, "dataSupplier must not be null");
//    }
//
//
//    public MediaContent mapToDomain(Clock clock) {
//        return MediaContent.builder()
//                .id(MediaId.random())
//                .filename(filename)
//                .contentType(contentType)
//                .size(size)
//                .createdAt(LocalDateTime.now(clock))
//                .url(null) // complété après le stockage (MinIO)
//                .build();
//    }
//}
