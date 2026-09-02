package com.ires.notification.entity;

import com.ires.requirement.entity.Requirement;
import com.ires.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="notifications") @Getter @Setter
@NoArgsConstructor(access=AccessLevel.PROTECTED)
public class Notification {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private User user;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=40) private NotificationType type;
    @Column(nullable=false,length=200) private String title;
    @Column(nullable=false,length=1000) private String message;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="related_requirement_id") private Requirement relatedRequirement;
    @Column(name="is_read",nullable=false) private boolean read;
    @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
    public Notification(User user,NotificationType type,String title,String message,Requirement requirement){this.user=user;this.type=type;this.title=title;this.message=message;this.relatedRequirement=requirement;}
    @PrePersist void create(){createdAt=Instant.now();}
}
