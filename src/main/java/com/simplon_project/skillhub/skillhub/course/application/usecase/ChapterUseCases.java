package com.simplon_project.skillhub.skillhub.course.application.usecase;

import com.simplon_project.skillhub.skillhub.course.adapter.common.exception.CourseNotFoundException;
import com.simplon_project.skillhub.skillhub.course.application.port.in.CreateChapterPort;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.CreateChapterCommand;
import com.simplon_project.skillhub.skillhub.course.application.port.out.CourseRepository;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;
import org.springframework.stereotype.Service;


@Service
public class ChapterUseCases implements CreateChapterPort {

    private final CourseRepository courseRepository;

    public ChapterUseCases(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public Course createChapter(CreateChapterCommand command) {
        var course = findCourseById(Id.of(command.courseId()));
        var section = course.getSectionById(Id.of(command.sectionId()));
        var chapter = command.mapToDomain();
        section.addChapter(chapter);
        return courseRepository.save(course);
    }

    private Course findCourseById(Id id) {
        return courseRepository.findById(id.asString())
                .orElseThrow(() -> new CourseNotFoundException(id));
    }
}
