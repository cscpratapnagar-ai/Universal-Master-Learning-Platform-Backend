package com.masterlearning.platform.modules.course.service;

import com.masterlearning.platform.modules.course.dto.request.*;
import com.masterlearning.platform.modules.course.dto.response.*;
import java.util.*;

public interface CourseService {
    CourseResponse create(CreateCourseRequest request);
    CourseResponse update(UUID id, UpdateCourseRequest request);
    List<CourseResponse> published();
    CourseResponse publish(UUID id);
    CourseResponse archive(UUID id);
    ModuleResponse updateModule(UUID moduleId, UpdateModuleRequest request);
    LessonResponse updateLesson(UUID lessonId, UpdateLessonRequest request);
}
