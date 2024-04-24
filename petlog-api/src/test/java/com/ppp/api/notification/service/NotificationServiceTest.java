package com.ppp.api.notification.service;

import com.ppp.api.notification.dto.response.NotificationResponse;
import com.ppp.domain.notification.constant.Type;
import com.ppp.domain.notification.dto.NotificationDto;
import com.ppp.domain.notification.repository.NotificationQuerydslRepository;
import com.ppp.domain.notification.repository.NotificationRepository;
import com.ppp.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationQuerydslRepository notificationQuerydslRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User user;

    @BeforeEach
    public void init() {
        user = User.builder()
                .id("abcde1234")
                .nickname("hi")
                .build();
    }

    @Test
    @DisplayName("알림목록리스트")
    void displayNotifications_returnPage() {
        //give
        int page = 0;
        int size = 2;

        Pageable pageable = PageRequest.of(page, size);
        List<NotificationDto> notificationDtoList = new ArrayList<>();
        notificationDtoList.add(NotificationDto.builder()
                .id(1L)
                .type(Type.INVITATION)
                .message("message")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build());
        notificationDtoList.add(NotificationDto.builder()
                .id(2L)
                .type(Type.INVITATION)
                .message("message")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build());
        Page<NotificationDto> notificationDtoPage = new PageImpl<>(notificationDtoList, pageable, notificationDtoList.size());

        //when
        when(notificationQuerydslRepository.findAllPageByReceiverId(user, pageable)).thenReturn(notificationDtoPage);
        Page<NotificationResponse> notificationResponsePage = notificationService.displayNotifications(user, pageable);

        //then
        assertEquals(notificationResponsePage.getSize(), notificationResponsePage.getContent().size());
    }
}