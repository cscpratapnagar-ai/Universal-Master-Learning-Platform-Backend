package com.masterlearning.platform.modules.organization.entity;

import com.masterlearning.platform.common.entity.BaseEntity;
import com.masterlearning.platform.modules.user.entity.User;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name="organization_members", uniqueConstraints=@UniqueConstraint(name="uk_organization_member",columnNames={"organization_id","user_id"}))
public class OrganizationMember extends BaseEntity {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="organization_id",nullable=false) private Organization organization;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private User user;
    @Column(nullable=false) private boolean active=true;
    protected OrganizationMember(){}
    public OrganizationMember(Organization organization,User user){this.organization=organization;this.user=user;}
    public UUID getId(){return id;} public Organization getOrganization(){return organization;} public User getUser(){return user;} public boolean isActive(){return active;}
    public void deactivate(){active=false;}
}