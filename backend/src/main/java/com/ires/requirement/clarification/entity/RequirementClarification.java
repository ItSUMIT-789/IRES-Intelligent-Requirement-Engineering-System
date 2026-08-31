package com.ires.requirement.clarification.entity;

import com.ires.requirement.entity.Requirement;
import com.ires.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "requirement_clarifications")
@Getter @Setter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RequirementClarification {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "requirement_id") private Requirement requirement;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "requested_by") private User requestedBy;
    @Column(nullable = false, length = 10000) private String question;
    @Column(name = "requested_at", nullable = false) private Instant requestedAt;
    @Column(length = 10000) private String response;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "responded_by") private User respondedBy;
    @Column(name = "responded_at") private Instant respondedAt;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private ClarificationStatus status;

    public RequirementClarification(Requirement requirement, User requestedBy, String question) {
        this.requirement = requirement; this.requestedBy = requestedBy; this.question = question;
        this.requestedAt = Instant.now(); this.status = ClarificationStatus.OPEN;
    }
}
