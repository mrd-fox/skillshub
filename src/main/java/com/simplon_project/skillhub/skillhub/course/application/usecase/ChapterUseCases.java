package com.simplon_project.skillhub.skillhub.course.application.usecase;

import com.simplon_project.skillhub.skillhub.course.adapter.common.exception.CourseNotFoundException;
import com.simplon_project.skillhub.skillhub.course.adapter.common.mapper.CycleAvoidingMappingContext;
import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.mapper.CourseEntityMapper;
import com.simplon_project.skillhub.skillhub.course.application.port.in.CreateChapterPort;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.CreateChapterCommand;
import com.simplon_project.skillhub.skillhub.course.application.port.out.CourseRepository;
import com.simplon_project.skillhub.skillhub.course.domain.model.Chapter;
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
        var chapter = buildChapter(command.chapterTitle(), command.position());
        section.addChapter(chapter);
        return courseRepository.save(course);
    }

    private Course findCourseById(Id id) {
        var courseEntity = courseRepository.findById(id.asString())
                .orElseThrow(() -> new CourseNotFoundException(id));

        return CourseEntityMapper.mapToDomain(courseEntity, new CycleAvoidingMappingContext());
    }

    private Chapter buildChapter(String title, int position) {
        return Chapter.builder()
                .title(title)
                .position(position)
                .build();
    }
}
