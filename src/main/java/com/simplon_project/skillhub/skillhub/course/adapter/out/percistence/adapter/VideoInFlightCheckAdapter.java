package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.adapter;

import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.repository.JpaVideoRepository;
import com.simplon_project.skillhub.skillhub.course.application.port.out.video.ExistsInFlightVideoForCoursePort;
import com.simplon_project.skillhub.skillhub.course.domain.enums.VideoStatusEnum;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Adapter implementation for checking if a course contains in-flight videos.
 */
@Component
@RequiredArgsConstructor
@Transactional(value = "courseTxManager", readOnly = true)
public class VideoInFlightCheckAdapter implements ExistsInFlightVideoForCoursePort {

    private final JpaVideoRepository jpaVideoRepository;

    @Override
    public boolean existsInFlightVideoForCourse(Id courseId, Set<VideoStatusEnum> statuses) {
        return jpaVideoRepository.existsInFlightVideoForCourse(
                EntityId.of(courseId.asUUID()),
                statuses
        );
    }
}

