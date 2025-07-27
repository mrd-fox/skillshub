package com.simplon_project.skillhub.skillhub.course.application.usecase;

import com.simplon_project.skillhub.skillhub.course.adapter.out.percistence.repository.CourseRepository;
import com.simplon_project.skillhub.skillhub.course.application.port.in.CreateChapterPort;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.CreateChapterCommand;

public class ChapterUseCases implements CreateChapterPort {

    private final CourseRepository courseRepository;

    public ChapterUseCases(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }


    @Override
    public void createChapter(CreateChapterCommand command) {
        //domain chapter
        var chapter = command.mapToDomain();
        //verifications
        var course = courseRepository.findById(chapter.getSection().getCourse().getId().asString());
        //section exists?
        //
        //persistance chapter


    }
}
