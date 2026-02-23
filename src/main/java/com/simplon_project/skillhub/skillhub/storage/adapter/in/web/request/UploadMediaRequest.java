//package com.simplon_project.skillhub.skillhub.storage.adapter.in.web.request;
//
//import com.simplon_project.skillhub.skillhub.storage.application.port.in.command.UploadMediaCommand;
//import io.swagger.v3.oas.annotations.media.Schema;
//import jakarta.validation.constraints.NotNull;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.UncheckedIOException;
//import java.util.Optional;
//import java.util.function.Supplier;
//
//public record UploadMediaRequest(
//        @NotNull
//        @Schema(type = "string", format = "binary")
//        MultipartFile file
//) {
//    public UploadMediaCommand mapToCommand(String authenticatedUserId, String courseId, String chapterId) {
//
//        if (file == null || file.isEmpty()) {
//            throw new IllegalArgumentException("File is missing or empty.");
//        }
//
//
//        String filename = Optional.ofNullable(file.getOriginalFilename()).orElse("upload.bin");
//        String contentType = resolveContentType(file, filename);
//        long size = file.getSize();
//
//        Supplier<InputStream> dataSupplier = () -> {
//            try {
//                return file.getInputStream();
//            } catch (IOException e) {
//                throw new UncheckedIOException("Failed to open input stream.", e);
//            }
//        };
//
//        return new UploadMediaCommand(
//                authenticatedUserId, // ← depuis SecurityContext
//                courseId,
//                chapterId,
//                filename,
//                contentType,
//                size,
//                dataSupplier
//        );
//    }
//
//    private static String resolveContentType(MultipartFile file, String filename) {
//        var fromMultipart = file.getContentType();
//        if (fromMultipart != null && !fromMultipart.isBlank()) return fromMultipart;
//
//        var lower = filename.toLowerCase();
//        if (lower.endsWith(".mp4")) return "video/mp4";
//        if (lower.endsWith(".webm")) return "video/webm";
//        if (lower.endsWith(".ogv") || lower.endsWith(".ogg")) return "video/ogg";
//        if (lower.endsWith(".pdf")) return "application/pdf";
//
//        throw new IllegalArgumentException("Unable to infer content type; expected mp4/webm/ogg/pdf.");
//    }
//}
