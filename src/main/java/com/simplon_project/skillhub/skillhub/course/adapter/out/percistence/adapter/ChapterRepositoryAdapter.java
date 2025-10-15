package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.adapter;

import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.ChapterEntity;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.repository.JpaChapterRepository;
import com.simplon_project.skillhub.skillhub.course.application.port.out.ChapterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ChapterRepositoryAdapter implements ChapterRepository {

    private final JpaChapterRepository jpaRepository;

    @Override
    public Optional<ChapterEntity> findById(EntityId chapterId) {
        return jpaRepository.findById(chapterId);
    }

    @Override
    public Optional<ChapterEntity> findByIdWithSectionAndCourse(EntityId chapterId) {
        return jpaRepository.findByIdWithSectionAndCourse(chapterId);
    }

    @Override
    public boolean belongsToCourse(EntityId chapterId, EntityId courseId) {
        return jpaRepository.belongsToCourse(chapterId, courseId);
    }
}
