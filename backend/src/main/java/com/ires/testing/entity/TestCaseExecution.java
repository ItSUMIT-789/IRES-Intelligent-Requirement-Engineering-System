package com.ires.testing.entity;

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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "test_case_executions")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TestCaseExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_case_id", nullable = false)
    private TestCase testCase;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "executed_by", nullable = false)
    private User executedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "execution_status", nullable = false, length = 10)
    private ExecutionStatus executionStatus = ExecutionStatus.NOT_RUN;

    @Column(name = "actual_result", length = 10000)
    private String actualResult;

    @Column(name = "executed_at")
    private Instant executedAt;

    @Column(length = 10000)
    private String notes;

    public TestCaseExecution(TestCase testCase, User executedBy, ExecutionStatus executionStatus,
                             String actualResult, String notes) {
        this.testCase = testCase;
        this.executedBy = executedBy;
        this.executionStatus = executionStatus;
        this.actualResult = actualResult;
        this.notes = notes;
        this.executedAt = Instant.now();
    }
}
