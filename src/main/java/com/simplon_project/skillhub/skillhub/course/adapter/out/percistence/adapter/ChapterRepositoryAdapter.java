package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.adapter;

import com.simplon_project.skillhub.skillhub.course.adapter.common.mapper.CycleAvoidingMappingContext;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.mapper.ChapterEntityMapper;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.repository.JpaChapterRepository;
import com.simplon_project.skillhub.skillhub.course.application.port.out.ChapterRepository;
import com.simplon_project.skillhub.skillhub.course.domain.model.Chapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ChapterRepositoryAdapter implements ChapterRepository {

    private final JpaChapterRepository jpaRepository;

    @Override
    public Optional<Chapter> findById(EntityId chapterId) {
        return jpaRepository.findById(chapterId).map(
                entity -> ChapterEntityMapper.mapToDomain(entity, new CycleAvoidingMappingContext())
        );
    }

    @Override
    public Optional<Chapter> findByIdWithSectionAndCourse(EntityId chapterId) {
        return jpaRepository.findByIdWithSectionAndCourse(chapterId).map(
                entity -> ChapterEntityMapper.mapToDomainWithSectionAndCourseLight(entity, new CycleAvoidingMappingContext())
        );
    }

    @Override
    public boolean belongsToCourse(EntityId chapterId, EntityId courseId) {
        return jpaRepository.belongsToCourse(chapterId, courseId);
    }
}
