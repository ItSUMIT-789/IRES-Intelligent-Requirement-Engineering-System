package com.ires.notification.service;

import com.ires.common.exception.NotFoundException;
import com.ires.notification.dto.NotificationResponse;
import com.ires.notification.entity.*;
import com.ires.notification.repository.NotificationRepository;
import com.ires.project.entity.ProjectMemberRole;
import com.ires.project.repository.ProjectMemberRepository;
import com.ires.project.service.ProjectService;
import com.ires.requirement.entity.Requirement;
import com.ires.testing.repository.TestCaseRepository;
import com.ires.user.entity.*;
import com.ires.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class NotificationService {
 private final NotificationRepository repository; private final ProjectService projectService; private final UserRepository userRepository; private final ProjectMemberRepository memberRepository; private final TestCaseRepository testCaseRepository;
 public Page<NotificationResponse> list(Boolean read,Pageable pageable,UserDetails principal){UUID id=current(principal).getId();Page<Notification> page=read==null?repository.findByUserId(id,pageable):repository.findByUserIdAndRead(id,read,pageable);return page.map(NotificationResponse::from);}
 public long unread(UserDetails principal){return repository.countByUserIdAndReadFalse(current(principal).getId());}
 @Transactional public NotificationResponse markRead(UUID id,UserDetails principal){Notification n=repository.findByIdAndUserId(id,current(principal).getId()).orElseThrow(()->new NotFoundException("Notification not found."));n.setRead(true);return NotificationResponse.from(n);}
 @Transactional public long markAllRead(UserDetails principal){List<Notification> values=repository.findByUserIdAndReadFalse(current(principal).getId());values.forEach(n->n.setRead(true));repository.saveAll(values);return values.size();}
 @Transactional public void notifyUser(User user,NotificationType type,String title,String message,Requirement requirement){if(user!=null&&user.isActive())repository.save(new Notification(user,type,title,message,requirement));}
 @Transactional public void notifyAdmins(NotificationType type,String title,String message,Requirement r){userRepository.findActiveByRole(RoleName.ADMIN.name()).forEach(u->notifyUser(u,type,title,message,r));}
 @Transactional public void notifyProjectRole(Requirement r,ProjectMemberRole role,NotificationType type,String title,String message){memberRepository.findByProjectIdAndProjectRole(r.getProject().getId(),role).stream().map(m->m.getUser()).filter(User::isActive).forEach(u->notifyUser(u,type,title,message,r));}
 @Transactional public void notifyAssignedTesters(Requirement r,NotificationType type,String title,String message){testCaseRepository.findByRequirementId(r.getId()).stream().map(tc->tc.getAssignedTo()).filter(Objects::nonNull).distinct().forEach(u->notifyUser(u,type,title,message,r));}
 private User current(UserDetails principal){return projectService.currentUser(principal);}
}
