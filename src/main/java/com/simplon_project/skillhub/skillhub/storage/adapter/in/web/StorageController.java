package com.simplon_project.skillhub.skillhub.storage.adapter.in.web;

import com.simplon_project.skillhub.skillhub.storage.adapter.in.web.mapper.ResponseMediaMapper;
import com.simplon_project.skillhub.skillhub.storage.adapter.in.web.mapper.ResponseStorageMapper;
import com.simplon_project.skillhub.skillhub.storage.adapter.in.web.request.CommitUploadRequest;
import com.simplon_project.skillhub.skillhub.storage.adapter.in.web.request.PresignedUploadRequest;
import com.simplon_project.skillhub.skillhub.storage.adapter.in.web.response.MediaContentResponse;
import com.simplon_project.skillhub.skillhub.storage.adapter.in.web.response.PresignedUploadResponse;
import com.simplon_project.skillhub.skillhub.storage.application.port.in.AccessMediaPort;
import com.simplon_project.skillhub.skillhub.storage.application.port.in.CompleteUploadMediaContentPort;
import com.simplon_project.skillhub.skillhub.storage.application.port.in.InitiateUploadPort;
import com.simplon_project.skillhub.skillhub.storage.application.port.out.GetObjectMetadataPort;
import com.simplon_project.skillhub.skillhub.storage.config.UploaderId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/storage")
public class StorageController {

    private final InitiateUploadPort initiateUploadPort;
    private final CompleteUploadMediaContentPort completeUploadPort;
    private final AccessMediaPort accessMediaPort;
    private final GetObjectMetadataPort getObjectMetadataPort;

    @Operation(
            summary = "Fetch binary storage link",
            description = "Retrieve presigned link to save binary in storage"
    )
    @PostMapping("/media/request-upload")
    public PresignedUploadResponse requestUpload(
            @Valid @RequestBody PresignedUploadRequest request,
            @UploaderId String uploaderId
    ) {
        var cmd = request.mapToCommand(uploaderId);
        return ResponseStorageMapper.mapToResponse(initiateUploadPort.initiate(cmd));
    }

    @Operation(
            summary = "Save meta data of media",
            description = "publish event when saving media"
    )
    @ApiResponse(responseCode = "201", description = "Metadata saved")
    @PostMapping("/media/{mediaId}/commit")
    public MediaContentResponse commit(
            @PathVariable String mediaId,
            @Valid @RequestBody @NotNull CommitUploadRequest request,
            @UploaderId String uploaderId
    ) {
        var command = request.mapToCommand(uploaderId, mediaId);
        return ResponseMediaMapper.mapToMediaResponse(completeUploadPort.complete(command));
    }

//    @Operation(
//            summary = "Fetch object metadata",
//            description = "Pass-through endpoint returning metadata from MinIO for the given object key"
//    )
//    @ApiResponse(responseCode = "200", description = "Metadata retrieved")
//    @PostMapping("/objects/metadata")
//    public ObjectMetadataResponse getMetadataByQuery(@RequestBody GetMetadataRequest request) {
//        return ResponseMediaMapper.mapToResponse(
//                getObjectMetadataPort.get(request.mapToCommand())
//        );
//    }

//    @GetMapping("/media/access")
//    public String access(
//            @RequestParam String storageKey,
//            @RequestParam(defaultValue = "900") int ttlSeconds
//    ) {
//        return accessMediaPort.generateReadUrl(storageKey, ttlSeconds);
//    }
}

