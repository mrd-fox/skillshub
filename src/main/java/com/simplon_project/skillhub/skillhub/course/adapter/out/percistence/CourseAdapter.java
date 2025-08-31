package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence;

import com.simplon_project.skillhub.skillhub.course.adapter.common.exception.CourseNotFoundException;
import com.simplon_project.skillhub.skillhub.course.adapter.common.mapper.CycleAvoidingMappingContext;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.mapper.CourseEntityMapper;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.repository.CourseJpaRepository;
import com.simplon_project.skillhub.skillhub.course.application.port.out.FindCoursePort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.SaveCoursePort;
import com.simplon_project.skillhub.skillhub.course.domain.exception.CourseAlreadyExistsException;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
@RequiredArgsConstructor
public class CourseAdapter implements SaveCoursePort, FindCoursePort {
    private final CourseJpaRepository courseJpaRepository;


    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void assertCourseNotExists(Course course) {
        var existingCourse = this.findByTitle(course);
        if (existingCourse != null) {
            throw new CourseAlreadyExistsException(existingCourse.getTitle());
        }
    }

    @Transactional
    public Course saveCourse(Course course) {
        var courseEntity = CourseEntityMapper.mapToEntity(course, new CycleAvoidingMappingContext());
        var saved = courseJpaRepository.saveAndFlush(courseEntity);
        entityManager.refresh(saved);
        return CourseEntityMapper.mapToDomain(saved, new CycleAvoidingMappingContext());
    }


    @Override
    public Course find(Id id) {
        var courseEntity = courseJpaRepository.findById(id.asString())
                .orElseThrow(() -> new CourseNotFoundException(id));

        return CourseEntityMapper.mapToDomain(courseEntity, new CycleAvoidingMappingContext());

    }


    @Override
    public Course findByTitle(Course course) {
        String normalizedTitle = course.getTitle().replaceAll("\\s+", "").toLowerCase();

        var existingCourse = courseJpaRepository.findAll().stream()
                .filter(c -> c.getTitle().replaceAll("\\s+", "").toLowerCase().equals(normalizedTitle))
                .findFirst().orElse(null);

        return CourseEntityMapper.mapToDomain(existingCourse, new CycleAvoidingMappingContext());
    }
}
