package com.simplon_project.skillhub.skillhub.course.application.port.out.course;

import com.simplon_project.skillhub.skillhub.course.domain.model.CourseSummary;
import com.simplon_project.skillhub.skillhub.course.domain.model.Id;

import java.util.List;

/**
 * Port for loading lightweight course summaries without sections/chapters/videos.
 * Optimized for list views (student dashboard).
 */
public interface LoadCourseSummariesByIdsPort {

    List<CourseSummary> loadSummariesByIds(List<Id> courseIds);
}

