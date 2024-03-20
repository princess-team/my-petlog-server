package com.ppp.api.notification.service;

import com.ppp.api.notification.dto.response.NotificationResponse;
import com.ppp.common.util.TimeUtil;
import com.ppp.domain.notification.Notification;
import com.ppp.domain.notification.constant.Type;
import com.ppp.domain.notification.dto.NotificationDto;
import com.ppp.domain.notification.repository.NotificationQuerydslRepository;
import com.ppp.domain.notification.repository.NotificationRepository;
import com.ppp.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationQuerydslRepository notificationQuerydslRepository;

    public void createNotification(Type type, String actorId, String receiverId, String message) {

        Notification notification = Notification.of(type, actorId, receiverId, message);
        notificationRepository.save(notification);
    }

    public Page<NotificationResponse> displayNotifications(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationDto> notificationDtoPage = notificationQuerydslRepository.findAllByReceiverId(user, pageable);

        return toResponsePage(notificationDtoPage);
    }

    private Page<NotificationResponse> toResponsePage(Page<NotificationDto> notificationDtoPage) {
        return notificationDtoPage.map(dto -> NotificationResponse.builder()
                    .id(dto.getId())
                    .type(dto.getType().getDescription())
                    .message(dto.getMessage())
                    .thumbnailPath(dto.getThumbnailPath())
                    .checked(dto.getIsRead())
                    .createdAt(TimeUtil.calculateTerm(dto.getCreatedAt()))
                    .build());
    }
}
