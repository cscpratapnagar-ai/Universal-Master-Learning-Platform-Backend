package com.masterlearning.platform.modules.course.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.course.repository.CourseModuleRepository;
import com.masterlearning.platform.modules.course.repository.CourseRepository;
import com.masterlearning.platform.modules.course.repository.LessonRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/learning")
public class AdminLearningCatalogController {
    private final CourseRepository courses;
    private final CourseModuleRepository modules;
    private final LessonRepository lessons;

    public AdminLearningCatalogController(CourseRepository courses,
                                           CourseModuleRepository modules,
                                           LessonRepository lessons) {
        this.courses = courses;
        this.modules = modules;
        this.lessons = lessons;
    }

    @GetMapping("/catalog")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','INSTRUCTOR')")
    @Transactional(readOnly = true)
    public ApiResponse<List<Map<String, Object>>> catalog() {
        List<Map<String, Object>> data = courses.findAll().stream().map(course -> {
            List<Map<String, Object>> moduleViews = modules.findByCourseIdOrderBySortOrderAsc(course.getId()).stream().map(module -> {
                List<Map<String, Object>> lessonViews = lessons.findByModuleIdOrderBySortOrderAsc(module.getId()).stream().map(lesson -> {
                    Map<String, Object> view = new LinkedHashMap<>();
                    view.put("id", lesson.getId());
                    view.put("title", lesson.getTitle());
                    view.put("sortOrder", lesson.getSortOrder());
                    view.put("completionMode", lesson.getCompletionMode());
                    return view;
                }).toList();

                Map<String, Object> view = new LinkedHashMap<>();
                view.put("id", module.getId());
                view.put("title", module.getTitle());
                view.put("sortOrder", module.getSortOrder());
                view.put("lessons", lessonViews);
                return view;
            }).toList();

            Map<String, Object> view = new LinkedHashMap<>();
            view.put("id", course.getId());
            view.put("title", course.getTitle());
            view.put("slug", course.getSlug());
            view.put("status", course.getStatus().name());
            view.put("modules", moduleViews);
            return view;
        }).toList();

        return ApiResponse.success("Learning catalog retrieved", data);
    }
}
