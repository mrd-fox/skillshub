package com.simplon_project.skillhub.skillhub.cours.adapter.out.percistence;

import com.simplon_project.skillhub.skillhub.cours.adapter.out.percistence.repository.CourseRepository;
import com.simplon_project.skillhub.skillhub.cours.application.port.out.FindCoursePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class CourseAdapter implements FindCoursePort {
    private final CourseRepository courseRepository;

    @Override
    public boolean existsByTitle(String title) {
        return courseRepository.existsByTitle(title);
    }
}
