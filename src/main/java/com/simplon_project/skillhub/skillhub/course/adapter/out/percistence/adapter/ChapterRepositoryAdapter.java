package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.adapter;

import com.simplon_project.skillhub.skillhub.course.adapter.common.mapper.CycleAvoidingMappingContext;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.mapper.ChapterEntityMapper;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.repository.JpaChapterRepository;
import com.simplon_project.skillhub.skillhub.course.application.port.out.chapter.CheckChapterBelongsToCoursePort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.chapter.LoadChapterByIdPort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.chapter.LoadChapterForVideoOpsPort;
import com.simplon_project.skillhub.skillhub.course.domain.model.Chapter;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ChapterRepositoryAdapter implements LoadChapterByIdPort, LoadChapterForVideoOpsPort, CheckChapterBelongsToCoursePort {

    private final JpaChapterRepository jpaRepository;

    @Override
    public Optional<Chapter> loadChapterById(EntityId chapterId) {
        return jpaRepository.findById(chapterId).map(
                entity -> ChapterEntityMapper.mapToDomain(entity, new CycleAvoidingMappingContext())
        );
    }

    @Override
    public Optional<Chapter> loadChapterForVideoOps(Id chapterId) {
        return jpaRepository.findByIdWithSectionAndCourse(EntityId.of(chapterId.asUUID())).map(
                entity -> ChapterEntityMapper.mapToDomainWithSectionAndCourseLight(entity, new CycleAvoidingMappingContext())
        );
    }

    @Override
    public boolean checkChapterBelongsToCoursePort(EntityId chapterId, EntityId courseId) {
        return jpaRepository.belongsToCourse(chapterId, courseId);

    }
}
