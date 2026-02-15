package com.simplon_project.skillhub.skillhub.course.application.usecase;

import com.simplon_project.skillhub.skillhub.course.application.port.in.SearchCoursesByIdsPort;
import com.simplon_project.skillhub.skillhub.course.application.port.in.command.SearchCoursesByIdsCommand;
import com.simplon_project.skillhub.skillhub.course.application.port.out.course.LoadCoursesByIdsPort;
import com.simplon_project.skillhub.skillhub.course.domain.model.Course;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchCoursesByIdsUseCase implements SearchCoursesByIdsPort {

    private final LoadCoursesByIdsPort loadCoursesByIdsPort;

    @Override
    @Transactional(readOnly = true, transactionManager = "courseTxManager")
    public List<Course> searchByIds(SearchCoursesByIdsCommand command) {
        return loadCoursesByIdsPort.loadCoursesByIds(command.courseIds());
    }

}

