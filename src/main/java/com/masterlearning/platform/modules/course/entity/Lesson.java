package com.masterlearning.platform.modules.course.entity;

import com.masterlearning.platform.common.entity.BaseEntity;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "lessons")
public class Lesson extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private CourseModule module;

    @Column(nullable = false, length = 220)
    private String title;

    @Column(length = 30)
    private String contentType = "TEXT";

    @Column(length = 4000)
    private String content;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "completion_mode", nullable = false, length = 30)
    private String completionMode = "MANUAL_COMPLETE";

    protected Lesson() {}

    public Lesson(CourseModule module, String title, String contentType,
                  String content, int sortOrder) {
        this.module = module;
        this.title = title;
        this.contentType = contentType;
        this.content = content;
        this.sortOrder = sortOrder;
    }

    public UUID getId() { return id; }
    public CourseModule getModule() { return module; }
    public String getTitle() { return title; }
    public String getContentType() { return contentType; }
    public String getContent() { return content; }
    public int getSortOrder() { return sortOrder; }
    public String getCompletionMode() { return completionMode; }
    public void setCompletionMode(String completionMode) { this.completionMode = completionMode; }
}
