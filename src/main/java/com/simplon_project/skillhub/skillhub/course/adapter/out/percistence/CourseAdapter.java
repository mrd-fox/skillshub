package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence;

import com.simplon_project.skillhub.skillhub.course.adapter.common.mapper.CycleAvoidingMappingContext;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.CourseEntity;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.entity.EntityId;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.mapper.CourseEntityMapper;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.mapper.PublicCourseEntityDetailMapper;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.mapper.PublicCourseSummaryMapper;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.repository.JpaCourseRepository;
import com.simplon_project.skillhub.skillhub.course.application.port.out.course.*;
import com.simplon_project.skillhub.skillhub.course.domain.exception.CourseAlreadyExistsException;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import com.simplon_project.skillhub.skillhub.course.domain.model.PublicCourseDetail;
import com.simplon_project.skillhub.skillhub.course.domain.model.PublicCourseSummary;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Component
@Transactional("courseTxManager")
public class CourseAdapter implements
        CreateNewCoursePort,
        FindCoursePort,
        LoadPublicCoursesPort,
        LoadPublicCourseDetailPort,
        LoadCoursesByIdsPort {


    private final JpaCourseRepository courseJpaRepository;
    private final EntityManager entityManager;

    public CourseAdapter(JpaCourseRepository jpaCourseRepository,
                         @Qualifier("courseEntityManager") EntityManager entityManager) {
        this.courseJpaRepository = jpaCourseRepository;
        this.entityManager = entityManager;
    }

    @Override
    public void assertCourseNotExists(Course course) {
        var existingCourse = this.findByTitle(course);
        if (existingCourse != null) {
            throw new CourseAlreadyExistsException(existingCourse.getTitle());
        }
    }

    @Override
    @Transactional("courseTxManager")
    public Course createNewCourse(Course course) {
        var courseEntity = CourseEntityMapper.mapToEntity(course, new CycleAvoidingMappingContext());
        var saved = courseJpaRepository.saveAndFlush(courseEntity);
        entityManager.refresh(saved);
        return CourseEntityMapper.mapToDomain(saved, new CycleAvoidingMappingContext());
    }


    @Override
    public Course findByTitle(Course course) {
        String normalizedTitle = course.getTitle().replaceAll("\\s+", "").toLowerCase();

        var existingCourse = courseJpaRepository.findAll().stream()
                .filter(c -> c.getTitle().replaceAll("\\s+", "").toLowerCase().equals(normalizedTitle))
                .findFirst().orElse(null);

        return existingCourse != null ? CourseEntityMapper.mapToDomain(existingCourse, new CycleAvoidingMappingContext()) : null;
    }

    @Override
    public List<Course> findByExternalUserId(String externalUserId) {

        List<CourseEntity> entities =
                courseJpaRepository.findByExternalUserId(externalUserId);

        return CourseEntityMapper.mapToDomain(entities, new CycleAvoidingMappingContext());
    }

    // =========================
    // PUBLIC CATALOG
    // =========================

    @Override
    public List<PublicCourseSummary> loadPublicCourses() {
        return courseJpaRepository.findAllForPublicCatalog().stream()
                .map(PublicCourseSummaryMapper::mapToDomain)
                .toList();
    }

    @Override
    public Optional<PublicCourseDetail> loadPublicCourseDetail(Id courseId) {
        return courseJpaRepository.findByIdWithPublicTree(
                        EntityId.fromString(courseId.asString())
                )
                .map(PublicCourseEntityDetailMapper::mapToDomain);
    }

    // =========================
    // BATCH SEARCH
    // =========================

    @Override
    public List<Course> loadCoursesByIds(List<Id> courseIds) {

        List<UUID> uuids = courseIds.stream()
                .map(Id::asUUID)
                .toList();

        List<CourseEntity> entities = courseJpaRepository.findAllByIdIn(uuids);

        return CourseEntityMapper.mapToDomain(entities, new CycleAvoidingMappingContext());
    }

}
