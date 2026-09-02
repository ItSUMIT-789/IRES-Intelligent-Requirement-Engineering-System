package com.ires.requirement.entity;

import com.ires.project.entity.Project;
import com.ires.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "requirements")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Requirement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(length = 10000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "requirement_type", nullable = false, length = 30)
    private RequirementType requirementType = RequirementType.FUNCTIONAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private RequirementPriority priority = RequirementPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RequirementStatus status = RequirementStatus.DRAFT;

    @Column(length = 100)
    private String source;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Requirement(Project project, String title, String description, RequirementType requirementType,
                       RequirementPriority priority, RequirementStatus status, String source,
                       User createdBy, User assignedTo) {
        this.project = project;
        this.title = title;
        this.description = description;
        this.requirementType = requirementType;
        this.priority = priority;
        this.status = status;
        this.source = source;
        this.createdBy = createdBy;
        this.assignedTo = assignedTo;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
