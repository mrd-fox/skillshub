package com.simplon_project.skillhub.skillhub.course.application.usecase;

import com.simplon_project.skillhub.skillhub.course.application.exception.CourseNotFoundException;
import com.simplon_project.skillhub.skillhub.course.application.port.in.CreateSectionPort;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.CreateSectionCommand;
import com.simplon_project.skillhub.skillhub.course.application.port.out.course.LoadCourseByIdPort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.course.UpdateCourseStructurePort;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class SectionUseCases implements CreateSectionPort {
    private final LoadCourseByIdPort loadCourseByIdPort;
    private final UpdateCourseStructurePort updateCourseStructurePort;

    @Override
    @Transactional("courseTxManager")
    public Course createSection(CreateSectionCommand command) {
        var course = findCourseById(Id.of(command.courseId()));
        var section = command.mapToDomain();
        course.addSection(section);
        return updateCourseStructurePort.updateCourseStructure(course);
    }

    private Course findCourseById(Id id) {
        return loadCourseByIdPort.loadCourseById(id)
                .orElseThrow(() -> new CourseNotFoundException(id));
    }
}
