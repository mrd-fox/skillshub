package com.simplon_project.skillhub.skillhub.course.application.usecase;

import com.simplon_project.skillhub.skillhub.course.application.exception.CourseNotFoundException;
import com.simplon_project.skillhub.skillhub.course.application.port.in.CreateSectionPort;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.CreateSectionCommand;
import com.simplon_project.skillhub.skillhub.course.application.port.out.course.CourseRepository;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class SectionUseCases implements CreateSectionPort {
    private final CourseRepository courseRepository;

    @Override
    @Transactional("courseTxManager")
    public Course createSection(CreateSectionCommand command) {
        var course = findCourseById(Id.of(command.courseId()));
        var section = command.mapToDomain();
        course.addSection(section);
        return courseRepository.save(course);
    }

    private Course findCourseById(Id id) {
        return courseRepository.findById(id.asString())
                .orElseThrow(() -> new CourseNotFoundException(id));
    }
}
