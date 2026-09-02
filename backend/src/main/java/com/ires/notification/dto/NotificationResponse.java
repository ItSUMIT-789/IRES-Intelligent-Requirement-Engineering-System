package com.ires.notification.dto;
import com.ires.notification.entity.*;
import java.time.Instant;
import java.util.UUID;
public record NotificationResponse(UUID id,NotificationType type,String title,String message,UUID relatedRequirementId,boolean read,Instant createdAt){
 public static NotificationResponse from(Notification n){return new NotificationResponse(n.getId(),n.getType(),n.getTitle(),n.getMessage(),n.getRelatedRequirement()==null?null:n.getRelatedRequirement().getId(),n.isRead(),n.getCreatedAt());}
}
