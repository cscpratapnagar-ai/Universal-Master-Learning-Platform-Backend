package com.masterlearning.platform.modules.course.service.impl;

import com.masterlearning.platform.modules.course.dto.request.*;
import com.masterlearning.platform.modules.course.dto.response.*;
import com.masterlearning.platform.modules.course.entity.*;
import com.masterlearning.platform.modules.course.repository.*;
import com.masterlearning.platform.modules.course.security.CourseAuthorizationService;
import com.masterlearning.platform.modules.course.service.CourseService;
import com.masterlearning.platform.modules.organization.repository.OrganizationRepository;
import com.masterlearning.platform.modules.user.repository.UserRepository;
import com.masterlearning.platform.security.util.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@Transactional
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courses;
    private final CourseModuleRepository modules;
    private final LessonRepository lessons;
    private final OrganizationRepository organizations;
    private final UserRepository users;
    private final CourseAuthorizationService authorization;

    public CourseServiceImpl(
            CourseRepository courses,
            CourseModuleRepository modules,
            LessonRepository lessons,
            OrganizationRepository organizations,
            UserRepository users,
            CourseAuthorizationService authorization) {
        this.courses=courses;
        this.modules=modules;
        this.lessons=lessons;
        this.organizations=organizations;
        this.users=users;
        this.authorization=authorization;
    }

    @Override
    public CourseResponse create(CreateCourseRequest r){
        String normalizedSlug = normalizeSlug(r.slug());
        if(courses.findBySlug(normalizedSlug).isPresent()) {
            throw new IllegalArgumentException("Course slug already exists");
        }
        var org=r.organizationId()==null?null:organizations.findById(r.organizationId())
                .orElseThrow(()->new EntityNotFoundException("Organization not found"));
        var user=users.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(()->new EntityNotFoundException("Authenticated user not found"));
        return map(courses.save(new Course(r.title().trim(),normalizedSlug,normalizeDescription(r.description()),org,user)));
    }

    @Override
    public CourseResponse update(UUID id, UpdateCourseRequest r){
        var course=findCourse(id);
        authorization.assertCanManage(course);

        String normalizedSlug = normalizeSlug(r.slug());
        var existing=courses.findBySlug(normalizedSlug);
        if(existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new IllegalArgumentException("Course slug already exists");
        }

        var org=r.organizationId()==null?null:organizations.findById(r.organizationId())
                .orElseThrow(()->new EntityNotFoundException("Organization not found"));

        if(course.getStatus()==CourseStatus.ARCHIVED) {
            throw new IllegalStateException("Archived courses cannot be edited");
        }

        course.updateDetails(r.title().trim(),normalizedSlug,normalizeDescription(r.description()),org);
        return map(course);
    }

    @Override
    @Transactional(readOnly=true)
    public List<CourseResponse> published(){
        return courses.findByStatus(CourseStatus.PUBLISHED).stream().map(this::map).toList();
    }

    @Override
    public CourseResponse publish(UUID id){
        var c=findCourse(id);
        authorization.assertCanManage(c);
        validatePublishable(c);
        c.publish();
        return map(c);
    }

    @Override
    public CourseResponse archive(UUID id){
        var c=findCourse(id);
        authorization.assertCanManage(c);
        c.archive();
        return map(c);
    }

    @Override
    public ModuleResponse updateModule(UUID moduleId, UpdateModuleRequest r){
        var module=modules.findById(moduleId)
                .orElseThrow(()->new EntityNotFoundException("Course module not found"));
        authorization.assertCanManage(module.getCourse());

        if(module.getCourse().getStatus()==CourseStatus.ARCHIVED) {
            throw new IllegalStateException("Archived courses cannot be edited");
        }

        module.updateDetails(r.title(),r.sortOrder());
        var lessonResponses=lessons.findByModuleIdOrderBySortOrderAsc(moduleId).stream()
                .map(this::mapLessonWithoutLearningState).toList();
        return new ModuleResponse(module.getId(),module.getTitle(),module.getSortOrder(),lessonResponses);
    }

    @Override
    public LessonResponse updateLesson(UUID lessonId, UpdateLessonRequest r){
        var lesson=lessons.findById(lessonId)
                .orElseThrow(()->new EntityNotFoundException("Lesson not found"));
        authorization.assertCanManage(lesson.getModule().getCourse());

        if(lesson.getModule().getCourse().getStatus()==CourseStatus.ARCHIVED) {
            throw new IllegalStateException("Archived courses cannot be edited");
        }

        lesson.updateDetails(
                r.title(),
                r.contentType()==null || r.contentType().isBlank() ? "TEXT" : r.contentType().trim().toUpperCase(),
                r.content(),
                r.sortOrder());
        return mapLessonWithoutLearningState(lesson);
    }

    private Course findCourse(UUID id){
        return courses.findById(id).orElseThrow(()->new EntityNotFoundException("Course not found"));
    }

    private void validatePublishable(Course course){
        var moduleList=modules.findByCourseIdOrderBySortOrderAsc(course.getId());
        if(course.getTitle()==null || course.getTitle().isBlank()) {
            throw new IllegalStateException("Course title is required before publishing");
        }
        if(course.getDescription()==null || course.getDescription().isBlank()) {
            throw new IllegalStateException("Course description is required before publishing");
        }
        if(moduleList.isEmpty()) {
            throw new IllegalStateException("Course must contain at least one module before publishing");
        }
        if(moduleList.stream().anyMatch(m -> m.getTitle()==null || m.getTitle().isBlank())) {
            throw new IllegalStateException("Every module must have a title before publishing");
        }
        var totalLessons=moduleList.stream()
                .mapToInt(m -> lessons.findByModuleIdOrderBySortOrderAsc(m.getId()).size())
                .sum();
        if(totalLessons==0) {
            throw new IllegalStateException("Course must contain at least one lesson before publishing");
        }
        for (var module : moduleList) {
            var moduleLessons = lessons.findByModuleIdOrderBySortOrderAsc(module.getId());
            if (moduleLessons.stream().anyMatch(l -> l.getTitle()==null || l.getTitle().isBlank())) {
                throw new IllegalStateException("Every lesson must have a title before publishing");
            }
            if (moduleLessons.stream().anyMatch(l -> l.getContent()==null || l.getContent().isBlank())) {
                throw new IllegalStateException("Every lesson must have content before publishing");
            }
        }
    }

    private String normalizeSlug(String slug) {
        return slug == null ? "" : slug.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeDescription(String description) {
        return description == null ? null : description.trim();
    }

    private CourseResponse map(Course c){
        return new CourseResponse(
                c.getId(),c.getTitle(),c.getSlug(),c.getDescription(),
                c.getStatus().name(),c.getOrganization()==null?null:c.getOrganization().getId());
    }

    private LessonResponse mapLessonWithoutLearningState(Lesson lesson){
        return new LessonResponse(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getContentType(),
                lesson.getContent(),
                lesson.getSortOrder(),
                false,
                false,
                List.of());
    }
}
