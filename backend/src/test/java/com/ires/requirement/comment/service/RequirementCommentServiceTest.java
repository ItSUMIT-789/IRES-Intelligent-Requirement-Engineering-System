package com.ires.requirement.comment.service;

import com.ires.common.exception.ForbiddenException;
import com.ires.requirement.comment.dto.CommentCreateRequest;
import com.ires.requirement.comment.entity.RequirementComment;
import com.ires.requirement.comment.repository.RequirementCommentRepository;
import com.ires.project.entity.Project;
import com.ires.project.entity.ProjectStatus;
import com.ires.project.service.ProjectService;
import com.ires.requirement.entity.Requirement;
import com.ires.requirement.entity.RequirementPriority;
import com.ires.requirement.entity.RequirementStatus;
import com.ires.requirement.entity.RequirementType;
import com.ires.requirement.service.RequirementService;
import com.ires.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequirementCommentServiceTest {

    @Mock
    private RequirementCommentRepository commentRepository;

    @Mock
    private RequirementService requirementService;

    @Mock
    private ProjectService projectService;

    @Mock
    private UserDetails principal;

    @InjectMocks
    private RequirementCommentService commentService;

    @Test
    void createsCommentForAVisibleRequirement() {
        User author = user("author@example.com");
        Requirement requirement = requirement(author);
        when(requirementService.findAccessibleRequirement(any(), any())).thenReturn(requirement);
        when(projectService.currentUser(principal)).thenReturn(author);
        when(commentRepository.save(any(RequirementComment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = commentService.create(UUID.randomUUID(), new CommentCreateRequest("  Needs clarification.  "), principal);

        assertThat(response.comment()).isEqualTo("Needs clarification.");
        assertThat(response.author().email()).isEqualTo("author@example.com");
    }

    @Test
    void preventsAnotherUserFromUpdatingComment() {
        User author = user("author@example.com");
        User outsider = user("outsider@example.com");
        RequirementComment comment = new RequirementComment(requirement(author), author, "Original");
        UUID commentId = UUID.randomUUID();
        comment.setId(commentId);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(projectService.currentUser(principal)).thenReturn(outsider);

        assertThatThrownBy(() -> commentService.update(commentId, new CommentCreateRequest("Changed"), principal))
                .isInstanceOf(ForbiddenException.class);
    }

    private Requirement requirement(User author) {
        User owner = user("owner@example.com");
        Project project = new Project("Checkout", "Revamp", ProjectStatus.ACTIVE, null, null, owner);
        Requirement requirement = new Requirement(project, "Guest checkout", "Details",
                RequirementType.FUNCTIONAL, RequirementPriority.MEDIUM, RequirementStatus.DRAFT,
                "client", author, null);
        requirement.setId(UUID.randomUUID());
        return requirement;
    }

    private User user(String email) {
        User user = new User("Test", "User", email, "hash", null);
        user.setId(UUID.randomUUID());
        return user;
    }
}
