package com.masterlearning.platform.modules.course.entity;

import com.masterlearning.platform.common.entity.BaseEntity;
import com.masterlearning.platform.modules.organization.entity.Organization;
import com.masterlearning.platform.modules.user.entity.User;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name="courses")
public class Course extends BaseEntity {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @Column(nullable=false,length=180) private String title;
    @Column(unique=true,length=220) private String slug;
    @Column(length=2000) private String description;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private CourseStatus status=CourseStatus.DRAFT;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="organization_id") private Organization organization;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="created_by") private User createdBy;

    protected Course(){}
    public Course(String t,String s,String d,Organization o){this(t,s,d,o,null);}
    public Course(String t,String s,String d,Organization o,User u){title=t;slug=s;description=d;organization=o;createdBy=u;}

    public UUID getId(){return id;}
    public String getTitle(){return title;}
    public String getSlug(){return slug;}
    public String getDescription(){return description;}
    public CourseStatus getStatus(){return status;}
    public Organization getOrganization(){return organization;}
    public User getCreatedBy(){return createdBy;}

    public void updateDetails(String title,String slug,String description,Organization organization){
        this.title=title;
        this.slug=slug;
        this.description=description;
        this.organization=organization;
    }

    public void publish(){status=CourseStatus.PUBLISHED;}
    public void archive(){status=CourseStatus.ARCHIVED;}
}
