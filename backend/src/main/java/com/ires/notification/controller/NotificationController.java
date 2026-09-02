package com.ires.notification.controller;
import com.ires.common.response.ApiResponse;import com.ires.notification.dto.NotificationResponse;import com.ires.notification.service.NotificationService;import lombok.RequiredArgsConstructor;import org.springframework.data.domain.*;import org.springframework.data.web.PageableDefault;import org.springframework.security.access.prepost.PreAuthorize;import org.springframework.security.core.annotation.AuthenticationPrincipal;import org.springframework.security.core.userdetails.UserDetails;import org.springframework.web.bind.annotation.*;import java.util.UUID;
@RestController @RequestMapping("/api/v1/notifications") @RequiredArgsConstructor @PreAuthorize("isAuthenticated()")
public class NotificationController {private final NotificationService service;
 @GetMapping public ApiResponse<Page<NotificationResponse>> list(@RequestParam(required=false)Boolean read,@PageableDefault(size=20,sort="createdAt",direction=Sort.Direction.DESC)Pageable pageable,@AuthenticationPrincipal UserDetails p){return ApiResponse.success("Notifications loaded.",service.list(read,pageable,p));}
 @GetMapping("/unread-count") public ApiResponse<Long> unread(@AuthenticationPrincipal UserDetails p){return ApiResponse.success("Unread notification count loaded.",service.unread(p));}
 @PostMapping("/{id}/read") public ApiResponse<NotificationResponse> read(@PathVariable UUID id,@AuthenticationPrincipal UserDetails p){return ApiResponse.success("Notification marked read.",service.markRead(id,p));}
 @PostMapping("/read-all") public ApiResponse<Long> readAll(@AuthenticationPrincipal UserDetails p){return ApiResponse.success("Notifications marked read.",service.markAllRead(p));}
}
