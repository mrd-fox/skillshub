package com.simplon_project.skillhub.skillhub.storage.application.port.out;

import com.simplon_project.skillhub.skillhub.storage.domaine.model.MediaId;
import com.simplon_project.skillhub.skillhub.storage.domaine.model.VideoStatusEnum;

public interface UpdateMediaPort {
    void updateMetadata(MediaId id, int width, int height, long duration, VideoStatusEnum status);
}
