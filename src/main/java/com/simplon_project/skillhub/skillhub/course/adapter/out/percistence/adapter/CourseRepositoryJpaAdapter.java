package com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.adapter;


import com.simplon_project.skillhub.skillhub.course.adapter.common.mapper.CycleAvoidingMappingContext;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.mapper.CourseEntityMapper;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.repository.CourseJpaRepository;
import com.simplon_project.skillhub.skillhub.course.application.port.out.CourseRepository;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CourseRepositoryJpaAdapter implements CourseRepository {

    private final CourseJpaRepository jpa;

    @Override
    public Optional<Course> findById(String id) {
        UUID uuid = UUID.fromString(id);
        return jpa.findById(uuid)
                .map(entity -> CourseEntityMapper.mapToDomain(entity, new CycleAvoidingMappingContext()));
    }

    @Override
    public Optional<Course> findByTitle(String title) {
        return jpa.findByTitle(title)
                .map(entity -> CourseEntityMapper.mapToDomain(entity, new CycleAvoidingMappingContext()));
    }

    @Override
    public Course save(Course course) {
        var entity = CourseEntityMapper.mapToEntity(course, new CycleAvoidingMappingContext());
        var saved = jpa.save(entity);
        return CourseEntityMapper.mapToDomain(saved, new CycleAvoidingMappingContext());
    }
}
