package com.ires.requirement.comment.service;

import com.ires.common.exception.ForbiddenException;
import com.ires.common.exception.NotFoundException;
import com.ires.requirement.comment.dto.CommentCreateRequest;
import com.ires.requirement.comment.dto.CommentResponse;
import com.ires.requirement.comment.entity.RequirementComment;
import com.ires.requirement.comment.repository.RequirementCommentRepository;
import com.ires.requirement.entity.Requirement;
import com.ires.requirement.service.RequirementService;
import com.ires.project.service.ProjectService;
import com.ires.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequirementCommentService {

    private final RequirementCommentRepository commentRepository;
    private final RequirementService requirementService;
    private final ProjectService projectService;

    @Transactional
    public CommentResponse create(UUID requirementId, CommentCreateRequest request, UserDetails principal) {
        Requirement requirement = requirementService.findAccessibleRequirement(requirementId, principal);
        User author = projectService.currentUser(principal);
        RequirementComment comment = new RequirementComment(requirement, author, request.comment().trim());
        return CommentResponse.from(commentRepository.save(comment));
    }

    public Page<CommentResponse> list(UUID requirementId, Pageable pageable, UserDetails principal) {
        requirementService.findAccessibleRequirement(requirementId, principal);
        Pageable newestFirst = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return commentRepository.findByRequirementId(requirementId, newestFirst).map(CommentResponse::from);
    }

    @Transactional
    public CommentResponse update(UUID id, CommentCreateRequest request, UserDetails principal) {
        RequirementComment comment = findComment(id);
        assertCanModify(comment, principal);
        comment.setComment(request.comment().trim());
        return CommentResponse.from(comment);
    }

    @Transactional
    public void delete(UUID id, UserDetails principal) {
        RequirementComment comment = findComment(id);
        assertCanModify(comment, principal);
        commentRepository.delete(comment);
    }

    private RequirementComment findComment(UUID id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Comment not found."));
    }

    private void assertCanModify(RequirementComment comment, UserDetails principal) {
        if (isAdmin(principal)) {
            return;
        }
        User currentUser = projectService.currentUser(principal);
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only the comment author or an admin can modify this comment.");
        }
        requirementService.findAccessibleRequirement(comment.getRequirement().getId(), principal);
    }

    private boolean isAdmin(UserDetails principal) {
        return principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }
}
