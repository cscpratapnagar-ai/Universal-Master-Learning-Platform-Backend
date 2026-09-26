package com.masterlearning.platform.modules.course.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.course.dto.request.*;
import com.masterlearning.platform.modules.course.dto.response.*;
import com.masterlearning.platform.modules.course.repository.*;
import com.masterlearning.platform.modules.course.security.CourseAuthorizationService;
import com.masterlearning.platform.modules.course.service.CourseService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseController {
    private final CourseService service;
    private final CourseAuthorizationService authorization;
    private final CourseRepository courses;

    public CourseController(CourseService s, CourseAuthorizationService a, CourseRepository c){
        service=s;authorization=a;courses=c;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','ORG_ADMIN','INSTRUCTOR','TEACHER')")
    public ApiResponse<CourseResponse> create(@Valid @RequestBody CreateCourseRequest r){
        return ApiResponse.success("Course created successfully",service.create(r));
    }

    @GetMapping("/published")
    public ApiResponse<List<CourseResponse>> published(){
        return ApiResponse.success("Published courses retrieved successfully",service.published());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','INSTRUCTOR','TEACHER')")
    public ApiResponse<CourseResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCourseRequest r){
        return ApiResponse.success("Course updated successfully",service.update(id,r));
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','INSTRUCTOR','TEACHER')")
    public ApiResponse<CourseResponse> publish(@PathVariable UUID id){
        return ApiResponse.success("Course published successfully",service.publish(id));
    }

    @PutMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','INSTRUCTOR','TEACHER')")
    public ApiResponse<CourseResponse> archive(@PathVariable UUID id){
        return ApiResponse.success("Course archived successfully",service.archive(id));
    }

    @PutMapping("/modules/{moduleId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','INSTRUCTOR','TEACHER')")
    public ApiResponse<ModuleResponse> updateModule(
            @PathVariable UUID moduleId,
            @Valid @RequestBody UpdateModuleRequest r){
        return ApiResponse.success("Module updated successfully",service.updateModule(moduleId,r));
    }

    @PutMapping("/lessons/{lessonId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','INSTRUCTOR','TEACHER')")
    public ApiResponse<LessonResponse> updateLesson(
            @PathVariable UUID lessonId,
            @Valid @RequestBody UpdateLessonRequest r){
        return ApiResponse.success("Lesson updated successfully",service.updateLesson(lessonId,r));
    }
}
