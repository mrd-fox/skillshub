package com.simplon_project.skillhub.skillhub.course.application.usecase;

import com.simplon_project.skillhub.skillhub.course.application.exception.CourseNotFoundException;
import com.simplon_project.skillhub.skillhub.course.application.port.in.CreateChapterPort;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.CreateChapterCommand;
import com.simplon_project.skillhub.skillhub.course.application.port.out.course.LoadCourseByIdPort;
import com.simplon_project.skillhub.skillhub.course.application.port.out.course.UpdateCourseStructurePort;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class ChapterUseCases implements CreateChapterPort {

    private final LoadCourseByIdPort loadCourseByIdPort;
    private final UpdateCourseStructurePort updateCourseStructurePort;

    public ChapterUseCases(LoadCourseByIdPort loadCourseByIdPort, UpdateCourseStructurePort updateCourseStructurePort) {
        this.loadCourseByIdPort = loadCourseByIdPort;
        this.updateCourseStructurePort = updateCourseStructurePort;
    }

    @Override
    @Transactional("courseTxManager")
    public Course createChapter(CreateChapterCommand command) {
        var course = findCourseById(Id.of(command.courseId()));
        var section = course.getSectionById(Id.of(command.sectionId()));
        var chapter = command.mapToDomain();
        section.addChapter(chapter);
        return updateCourseStructurePort.updateCourseStructure(course);
    }

    private Course findCourseById(Id id) {
        return loadCourseByIdPort.loadCourseById(id)
                .orElseThrow(() -> new CourseNotFoundException(id));
    }
}
