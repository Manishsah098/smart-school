package com.smartschool.service;

import com.smartschool.dto.NoticeCreateRequest;
import com.smartschool.dto.NoticeResponseDTO;
import com.smartschool.dto.NotificationDTO;
import com.smartschool.entity.Notice;
import com.smartschool.entity.Notification;
import com.smartschool.entity.Section;
import com.smartschool.entity.User;
import com.smartschool.entity.enums.NoticeAudience;
import com.smartschool.exception.ResourceNotFoundException;
import com.smartschool.repository.NoticeRepository;
import com.smartschool.repository.NotificationRepository;
import com.smartschool.repository.SectionRepository;
import com.smartschool.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoticeNotificationService {

    private final NoticeRepository noticeRepository;
    private final NotificationRepository notificationRepository;
    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public NoticeNotificationService(NoticeRepository noticeRepository,
                                     NotificationRepository notificationRepository,
                                     SectionRepository sectionRepository,
                                     UserRepository userRepository,
                                     AuditService auditService) {
        this.noticeRepository = noticeRepository;
        this.notificationRepository = notificationRepository;
        this.sectionRepository = sectionRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional
    public NoticeResponseDTO createNotice(NoticeCreateRequest request, Long authorUserId, String ipAddress) {
        User user = userRepository.findById(authorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Section section = null;
        if (request.getSectionId() != null) {
            section = sectionRepository.findById(request.getSectionId()).orElse(null);
        }

        Notice notice = new Notice();
        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice.setAudience(request.getAudience() != null ? request.getAudience() : NoticeAudience.ALL);
        notice.setSection(section);
        notice.setPublishedDate(request.getPublishedDate() != null ? request.getPublishedDate() : LocalDate.now());
        notice.setExpiryDate(request.getExpiryDate());
        notice.setAttachmentUrl(request.getAttachmentUrl());
        notice.setCreatedByUser(user);

        notice = noticeRepository.save(notice);

        auditService.log(user.getId(), user.getUsername(), "CREATE_NOTICE", "Notice", notice.getId(),
                "Published notice: " + notice.getTitle(), ipAddress);

        return convertNoticeToDTO(notice);
    }

    @Transactional(readOnly = true)
    public List<NoticeResponseDTO> getAllNotices() {
        return noticeRepository.findAllByOrderByPublishedDateDesc().stream()
                .map(this::convertNoticeToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NoticeResponseDTO> getNoticesForAudienceAndSection(NoticeAudience audience, Long sectionId) {
        if (sectionId != null) {
            return noticeRepository.findNoticesForSectionAndAudience(sectionId, audience).stream()
                    .map(this::convertNoticeToDTO)
                    .collect(Collectors.toList());
        }
        return noticeRepository.findNoticesForAudience(audience).stream()
                .map(this::convertNoticeToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createNotification(Long userId, String title, String message, String type, String refUrl) {
        userRepository.findById(userId).ifPresent(user -> {
            Notification notification = new Notification(user, title, message, type, refUrl);
            notificationRepository.save(notification);
        });
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(n -> new NotificationDTO(n.getId(), n.getTitle(), n.getMessage(), n.getType(), n.isRead(), n.getReferenceUrl(), n.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void markNotificationAsRead(Long notificationId, Long userId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getUser().getId().equals(userId)) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        });
    }

    @Transactional(readOnly = true)
    public long getUnreadNotificationCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    public NoticeResponseDTO convertNoticeToDTO(Notice notice) {
        NoticeResponseDTO dto = new NoticeResponseDTO();
        dto.setId(notice.getId());
        dto.setTitle(notice.getTitle());
        dto.setContent(notice.getContent());
        dto.setAudience(notice.getAudience());
        dto.setPublishedDate(notice.getPublishedDate());
        dto.setExpiryDate(notice.getExpiryDate());
        dto.setAttachmentUrl(notice.getAttachmentUrl());
        dto.setCreatedAt(notice.getCreatedAt());
        if (notice.getCreatedByUser() != null) {
            dto.setAuthorName(notice.getCreatedByUser().getUsername());
        }
        if (notice.getSection() != null) {
            dto.setSectionId(notice.getSection().getId());
            dto.setSectionFullName(notice.getSection().getFullName());
        }
        return dto;
    }
}
