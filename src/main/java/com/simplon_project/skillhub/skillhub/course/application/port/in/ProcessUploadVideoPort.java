package com.simplon_project.skillhub.skillhub.course.application.port.in;


import com.simplon_project.skillhub.skillhub.course.adapter.messaging.events.VideoUploadedEvent;

public interface ProcessUploadVideoPort {
    void processUploadedVideo(VideoUploadedEvent event);
}
