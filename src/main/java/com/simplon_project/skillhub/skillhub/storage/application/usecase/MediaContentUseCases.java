package com.simplon_project.skillhub.skillhub.storage.application.usecase;

//@Service
//@RequiredArgsConstructor
//public class MediaContentUseCases implements CompleteUploadMediaContentPort {
//    private final UploadStoragePort uploadStoragePort;         // adapter MinIO (port de sortie)
//    private final SaveMediaContentPort saveMediaContentPort; // adapter JPA (port de sortie)
//    //    private final EventPublisher eventPublisher;     // adapter Rabbit (port de sortie)
//    private final StorageProperties storageProperties;           // bucket/prefix
//    private final Clock clock;
//
//
//    @Override
//    @Transactional("storageTxManager")
//    public MediaContent upload(UploadMediaCommand command) {
//        var mediaContent = command.mapToDomain(clock);
//        var storageKey = buildStorageKey(mediaContent);
//
//// 3) Upload vers MinIO (streaming)
//        ObjectPutResult putResult;
//
//
//        try (InputStream inputStream = command.dataSupplier().get()) {
//            putResult = uploadStoragePort.upload(
//                    storageProperties.bucket(),
//                    storageKey,
//                    inputStream,
//                    mediaContent.getSize(),
//                    mediaContent.getContentType()
//            );
//        } catch (Exception exception) {
//            throw new RuntimeException("Storage upload failed.", exception);
//        }
//
////        try {
////            eventPublisher.publishMediaUploaded(media);
////        } catch (Exception e) {
////            throw new RuntimeException("Failed to publish MediaUploaded event.", e);
////        }
//
//        // 4) Mettre à jour l’agrégat avec la storageKey (dans url pour le MVP)
//        mediaContent = mediaContent.withUrl(putResult.storageKey());
//
//        // 5) Persister en Postgres
//        try {
//            mediaContent = saveMediaContentPort.save(mediaContent);
//        } catch (Exception exception) {
//            throw new RuntimeException("Failed to persist media metadata.", exception);
//        }
//
//        // 6) Retourner le domaine — le contrôleur le mappera en response DTO
//        return mediaContent;
//    }
//
//
//    private String buildStorageKey(MediaContent mediaContent) {
//        String safeFileName = slugify(mediaContent.getFilename());
//        // ex: videos/courses/{courseId}/chapters/{chapterId}/{mediaId}/{filename}
//        return "%s/courses/%s/chapters/%s/%s/%s".formatted(
//                storageProperties.prefix(),
//                mediaContent.getCourseId(),
//                mediaContent.getChapterId(),
//                mediaContent.getId().asString(),
//                safeFileName
//        );
//    }
//
//    private static String slugify(String filename) {
//        String normalized = Normalizer.normalize(filename, Normalizer.Form.NFD)
//                .replaceAll("\\p{M}", "");
//        return normalized.toLowerCase()
//                .replaceAll("[^a-z0-9._-]", "-")
//                .replaceAll("-{2,}", "-");
//    }
//}
