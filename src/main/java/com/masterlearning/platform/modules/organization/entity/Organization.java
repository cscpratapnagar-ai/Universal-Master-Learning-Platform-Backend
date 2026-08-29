package com.masterlearning.platform.modules.organization.entity;

import com.masterlearning.platform.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "organizations", uniqueConstraints = {
        @UniqueConstraint(name = "uk_organizations_code", columnNames = "code")
})
public class Organization extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(nullable=false,length=100) private String code;
    @Column(nullable=false,length=200) private String name;
    @Column(length=500) private String description;
    @Column(nullable=false) private boolean active=true;
    protected Organization(){}
    public Organization(String code,String name,String description){this.code=code;this.name=name;this.description=description;}
    public UUID getId(){return id;} public String getCode(){return code;} public String getName(){return name;}
    public String getDescription(){return description;} public boolean isActive(){return active;}
    public void update(String name,String description){this.name=name;this.description=description;}
    public void deactivate(){this.active=false;} public void activate(){this.active=true;}
}