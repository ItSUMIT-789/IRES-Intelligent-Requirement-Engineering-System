package com.ires.story.repository;

import com.ires.story.entity.UserStory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserStoryRepository extends JpaRepository<UserStory, UUID> {

    Page<UserStory> findByRequirementId(UUID requirementId, Pageable pageable);
}
