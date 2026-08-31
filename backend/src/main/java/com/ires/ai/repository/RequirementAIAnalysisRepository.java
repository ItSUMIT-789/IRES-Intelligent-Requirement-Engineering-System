package com.ires.ai.repository;

import com.ires.ai.entity.RequirementAIAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;
import com.ires.ai.entity.AnalysisStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RequirementAIAnalysisRepository extends JpaRepository<RequirementAIAnalysis, UUID> {

    Optional<RequirementAIAnalysis> findByRequirementId(UUID requirementId);

    @Query("select count(distinct a) from RequirementAIAnalysis a left join a.requirement.project.members m " +
            "where (a.requirement.project.client.id = :userId or m.user.id = :userId) and a.analysisStatus in :statuses")
    long countAccessibleToByStatuses(@Param("userId") UUID userId, @Param("statuses") java.util.Collection<AnalysisStatus> statuses);
}
