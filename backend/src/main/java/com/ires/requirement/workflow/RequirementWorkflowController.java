package com.ires.requirement.workflow;

import com.ires.common.response.ApiResponse;
import com.ires.requirement.clarification.dto.*;
import com.ires.requirement.dto.*;
import com.ires.requirement.entity.RequirementStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/v1/requirements/{id}") @RequiredArgsConstructor
public class RequirementWorkflowController {
    private final RequirementWorkflowService workflow;

    @PostMapping("/submit") @PreAuthorize("hasAnyRole('ADMIN','CLIENT')")
    public ApiResponse<RequirementResponse> submit(@PathVariable UUID id, @AuthenticationPrincipal UserDetails p) { return action(id, RequirementStatus.SUBMITTED, p); }
    @PostMapping("/start-analysis") @PreAuthorize("hasAnyRole('ADMIN','BUSINESS_ANALYST')")
    public ApiResponse<RequirementResponse> startAnalysis(@PathVariable UUID id, @AuthenticationPrincipal UserDetails p) { return action(id, RequirementStatus.IN_ANALYSIS, p); }
    @PostMapping("/approve") @PreAuthorize("hasAnyRole('ADMIN','BUSINESS_ANALYST')")
    public ApiResponse<RequirementResponse> approve(@PathVariable UUID id, @AuthenticationPrincipal UserDetails p) { return action(id, RequirementStatus.APPROVED_FOR_DEVELOPMENT, p); }
    @PostMapping("/assign-developer") @PreAuthorize("hasAnyRole('ADMIN','BUSINESS_ANALYST')")
    public ApiResponse<RequirementResponse> assignDeveloper(@PathVariable UUID id, @Valid @RequestBody RequirementAssignmentRequest r, @AuthenticationPrincipal UserDetails p) { return ApiResponse.success("Developer assigned.", workflow.assignDeveloper(id, r.developerId(), p)); }
    @PostMapping("/start-development") @PreAuthorize("hasAnyRole('ADMIN','DEVELOPER')")
    public ApiResponse<RequirementResponse> startDevelopment(@PathVariable UUID id, @AuthenticationPrincipal UserDetails p) { return action(id, RequirementStatus.IN_DEVELOPMENT, p); }
    @PostMapping("/ready-for-testing") @PreAuthorize("hasAnyRole('ADMIN','DEVELOPER')")
    public ApiResponse<RequirementResponse> ready(@PathVariable UUID id, @AuthenticationPrincipal UserDetails p) { return action(id, RequirementStatus.READY_FOR_TESTING, p); }
    @PostMapping("/start-testing") @PreAuthorize("hasAnyRole('ADMIN','TESTER')")
    public ApiResponse<RequirementResponse> startTesting(@PathVariable UUID id, @AuthenticationPrincipal UserDetails p) { return action(id, RequirementStatus.IN_TESTING, p); }
    @PostMapping("/pass") @PreAuthorize("hasAnyRole('ADMIN','TESTER')")
    public ApiResponse<RequirementResponse> pass(@PathVariable UUID id, @AuthenticationPrincipal UserDetails p) { return action(id, RequirementStatus.PASSED, p); }
    @PostMapping("/fail") @PreAuthorize("hasAnyRole('ADMIN','TESTER')")
    public ApiResponse<RequirementResponse> fail(@PathVariable UUID id, @AuthenticationPrincipal UserDetails p) { return action(id, RequirementStatus.FAILED, p); }
    @PostMapping("/complete") @PreAuthorize("hasAnyRole('ADMIN','TESTER')")
    public ApiResponse<RequirementResponse> complete(@PathVariable UUID id, @AuthenticationPrincipal UserDetails p) { return action(id, RequirementStatus.COMPLETED, p); }

    @PostMapping("/request-clarification") @PreAuthorize("hasAnyRole('ADMIN','BUSINESS_ANALYST')")
    public ApiResponse<ClarificationResponse> request(@PathVariable UUID id, @Valid @RequestBody ClarificationRequest r, @AuthenticationPrincipal UserDetails p) { return ApiResponse.success("Clarification requested.", workflow.requestClarification(id, r, p)); }
    @PostMapping("/clarifications") @PreAuthorize("hasAnyRole('ADMIN','CLIENT')")
    public ApiResponse<ClarificationResponse> respond(@PathVariable UUID id, @Valid @RequestBody ClarificationRequest r, @AuthenticationPrincipal UserDetails p) { return ApiResponse.success("Clarification submitted.", workflow.respond(id, r, p)); }
    @GetMapping("/clarifications") @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ClarificationResponse>> list(@PathVariable UUID id, @AuthenticationPrincipal UserDetails p) { return ApiResponse.success("Clarifications loaded.", workflow.clarifications(id, p)); }

    private ApiResponse<RequirementResponse> action(UUID id, RequirementStatus target, UserDetails p) { return ApiResponse.success("Requirement workflow updated.", workflow.transition(id, target, p)); }
}
