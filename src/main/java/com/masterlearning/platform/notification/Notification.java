package com.masterlearning.platform.notification;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="notifications")
public class Notification {
    @Id private UUID id;
    @Column(name="user_id",nullable=false) private UUID userId;
    @Column(nullable=false,length=60) private String type;
    @Column(nullable=false,length=180) private String title;
    @Column(nullable=false,length=1000) private String message;
    @Column(name="action_url",length=500) private String actionUrl;
    @Column(name="read_at") private LocalDateTime readAt;
    @Column(name="created_at",nullable=false) private LocalDateTime createdAt;

    protected Notification() {}
    public Notification(UUID id,UUID userId,String type,String title,String message,String actionUrl){
        this.id=id;this.userId=userId;this.type=type;this.title=title;this.message=message;this.actionUrl=actionUrl;this.createdAt=LocalDateTime.now();
    }
    public void markRead(){this.readAt=LocalDateTime.now();}
    public UUID getId(){return id;} public UUID getUserId(){return userId;} public String getType(){return type;}
    public String getTitle(){return title;} public String getMessage(){return message;} public String getActionUrl(){return actionUrl;}
    public LocalDateTime getReadAt(){return readAt;} public LocalDateTime getCreatedAt(){return createdAt;}
}
