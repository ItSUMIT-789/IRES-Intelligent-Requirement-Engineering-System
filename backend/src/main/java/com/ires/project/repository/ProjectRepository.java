package com.ires.project.repository;

import com.ires.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, UUID>, JpaSpecificationExecutor<Project> {
    @Query("select count(distinct p) from Project p left join p.members m where p.client.id = :userId or m.user.id = :userId")
    long countAccessibleTo(@Param("userId") UUID userId);
}
