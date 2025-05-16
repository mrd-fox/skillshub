package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence;

import com.simplon_project.skillhub.skillhub.course.adapter.common.mapper.CycleAvoidingMappingContext;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.mapper.CreateCourseEntityMapper;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.repository.CourseRepository;
import com.simplon_project.skillhub.skillhub.course.application.port.out.SaveCoursePort;
import com.simplon_project.skillhub.skillhub.course.domain.exception.CourseAlreadyExistsException;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
@RequiredArgsConstructor
public class CourseAdapter implements SaveCoursePort {
    private final CourseRepository courseRepository;
    private final CreateCourseEntityMapper createCourseEntityMapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void assertCourseNotExists(Course course) {
        //comparer avec to lower case
        var existingCourse = courseRepository.findByTitle(course.getTitle().toLowerCase());
        if (existingCourse.isPresent()) {
            var found = existingCourse.get();
            throw new CourseAlreadyExistsException(found.getTitle());
        }
    }

    @Transactional
    public Course saveCourse(Course course) {
        var courseEntity = createCourseEntityMapper.toDto(course, new CycleAvoidingMappingContext());
        var saved = courseRepository.saveAndFlush(courseEntity);
        entityManager.refresh(saved);
        return createCourseEntityMapper.toDomain(saved, new CycleAvoidingMappingContext());
    }


}
