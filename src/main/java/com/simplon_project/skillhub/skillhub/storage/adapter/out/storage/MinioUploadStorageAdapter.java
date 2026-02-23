//package com.simplon_project.skillhub.skillhub.storage.adapter.out.storage;
//
//import com.simplon_project.skillhub.skillhub.storage.adapter.Exceptions.StorageException;
//import com.simplon_project.skillhub.skillhub.storage.application.port.out.ObjectPutResult;
//import com.simplon_project.skillhub.skillhub.storage.application.port.out.minIo.UploadStoragePort;
//import io.minio.MinioClient;
//import io.minio.ObjectWriteResponse;
//import io.minio.PutObjectArgs;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//
//import java.io.InputStream;
//
//@Component
//@RequiredArgsConstructor
//public class MinioUploadStorageAdapter implements UploadStoragePort {
//
//    private final MinioClient minioClient;
//
//    @Override
//    public ObjectPutResult upload(String bucket,
//                                  String objectKey,
//                                  InputStream dataStream,
//                                  long contentLength,
//                                  String contentType) {
//        try {
//            ObjectWriteResponse response = minioClient.putObject(
//                    PutObjectArgs.builder()
//                            .bucket(bucket)
//                            .object(objectKey)
//                            .contentType(contentType)
//                            .stream(dataStream, contentLength, -1)
//                            .build()
//            );
//            return new ObjectPutResult(objectKey, response.etag());
//        } catch (Exception exception) {
//            throw new StorageException("Storage upload failed.", exception);
//        }
//    }
//}
