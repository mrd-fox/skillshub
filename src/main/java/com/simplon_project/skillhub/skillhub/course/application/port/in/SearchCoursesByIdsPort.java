package com.simplon_project.skillhub.skillhub.course.application.port.in;

import com.simplon_project.skillhub.skillhub.course.application.port.in.command.SearchCoursesByIdsCommand;
import com.simplon_project.skillhub.skillhub.course.domain.model.CourseSummary;

import java.util.List;

/**
 * Port for searching courses by IDs (student dashboard).
 * Returns lightweight summaries without sections/chapters/videos.
 */
public interface SearchCoursesByIdsPort {

    List<CourseSummary> searchByIds(SearchCoursesByIdsCommand command);
}

