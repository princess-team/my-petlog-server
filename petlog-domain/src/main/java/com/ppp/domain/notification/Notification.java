package com.ppp.domain.notification;

import com.ppp.domain.common.BaseTimeEntity;
import com.ppp.domain.notification.constant.Type;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "notifications",
indexes = {
        @Index(name = "idx_createdAt", columnList = "createdAt desc")
})
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Type type;

    @Column(nullable = false, length = 100)
    private String actorId;

    @Column(nullable = false, length = 100)
    private String receiverId;

    @Column(nullable = false, length = 100)
    private String message;

    @Column(columnDefinition = "bit(1) default 0")
    private Boolean isRead;

    private String thumbnailPath;

    public static Notification of(Type type, String actorId, String receiverId, String thumbnailPath, String message) {
        return Notification.builder()
                .type(type)
                .actorId(actorId)
                .receiverId(receiverId)
                .message(message)
                .isRead(false)
                .thumbnailPath(thumbnailPath)
                .build();
    }
}
