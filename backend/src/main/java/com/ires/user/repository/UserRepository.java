package com.ires.user.repository;

import com.ires.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmail(String email);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findByUsernameIgnoreCase(String username);

    boolean existsByEmail(String email);

    boolean existsByUsernameIgnoreCase(String username);

    @EntityGraph(attributePaths = "roles")
    List<User> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "roles")
    @Query("select distinct u from ProjectMember pm join pm.user u join u.roles r " +
            "where pm.project.id = :projectId and u.active = true and r.name = 'DEVELOPER' " +
            "order by u.firstName, u.lastName")
    List<User> findEligibleDevelopersForProject(@Param("projectId") UUID projectId);

    @EntityGraph(attributePaths = "roles")
    @Query("select distinct u from ProjectMember pm join pm.user u join u.roles r " +
            "where pm.project.id = :projectId and u.active = true and r.name = 'TESTER' " +
            "order by u.firstName, u.lastName")
    List<User> findEligibleTestersForProject(@Param("projectId") UUID projectId);

    @EntityGraph(attributePaths = "roles")
    @Query("select distinct u from User u join u.roles r where u.active = true and r.name = 'BUSINESS_ANALYST' " +
            "and not exists (select pm.id from ProjectMember pm where pm.project.id = :projectId and pm.user.id = u.id) " +
            "order by u.firstName, u.lastName")
    List<User> findEligibleBusinessAnalystsForProject(@Param("projectId") UUID projectId);

    @EntityGraph(attributePaths = "roles")
    @Query("select distinct u from User u join u.roles r where u.active = true and r.name = :role order by u.createdAt")
    List<User> findActiveByRole(@Param("role") String role);
}
