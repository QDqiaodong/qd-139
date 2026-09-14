package com.polar.experience.service;

import com.polar.experience.dto.NotificationDTO;
import com.polar.experience.entity.Notification;
import com.polar.experience.entity.Session;
import com.polar.experience.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final NotificationRepository notificationRepository;

    /**
     * 场次停用后给当班馆务推送站内通知，通知中写明场次名称和开始时间。
     */
    @Transactional
    public void notifySessionDisabled(Session session) {
        Notification notification = new Notification();
        notification.setTitle("场次停用通知");
        notification.setContent("场次【" + session.getName() + "】（开始时间："
                + session.getStartTime().format(TIME_FORMATTER)
                + "）已停用，请当班馆务知悉，勿再按该场次准备器材。");
        notification.setSessionId(session.getId());
        notification.setRecipientRole(Notification.RECIPIENT_ROLE_DUTY_STAFF);
        notification.setReadFlag(false);
        notificationRepository.save(notification);
        log.info("推送场次停用站内通知: sessionId={}, sessionName={}", session.getId(), session.getName());
    }

    public List<NotificationDTO> listForDutyStaff() {
        return notificationRepository
                .findByRecipientRoleOrderByCreatedAtDesc(Notification.RECIPIENT_ROLE_DUTY_STAFF)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public long unreadCountForDutyStaff() {
        return notificationRepository.countByRecipientRoleAndReadFlagFalse(Notification.RECIPIENT_ROLE_DUTY_STAFF);
    }

    /**
     * 标记已读：仅更新已读状态和已读时间，通知本身保留在列表中，未读通知不受影响。
     */
    @Transactional
    public NotificationDTO markRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("通知不存在"));
        if (!Boolean.TRUE.equals(notification.getReadFlag())) {
            notification.setReadFlag(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
            log.info("通知标记已读: id={}", id);
        }
        return convertToDTO(notification);
    }

    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setTitle(notification.getTitle());
        dto.setContent(notification.getContent());
        dto.setSessionId(notification.getSessionId());
        dto.setReadFlag(notification.getReadFlag());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setReadAt(notification.getReadAt());
        return dto;
    }
}
