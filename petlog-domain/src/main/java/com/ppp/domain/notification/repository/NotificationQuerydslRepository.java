package com.ppp.domain.notification.repository;

import com.ppp.domain.notification.dto.NotificationDto;
import com.ppp.domain.user.User;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.ppp.domain.notification.QNotification.notification;
import static com.ppp.domain.user.QUser.user;

@Repository
@RequiredArgsConstructor
public class NotificationQuerydslRepository {
    private final JPAQueryFactory queryFactory;

    public Page<NotificationDto> findAllPageByReceiverId(User receiver, Pageable pageable) {
        List<NotificationDto> notificationDtos = getNotificationDtos(receiver, pageable);
        Long count = getCount(receiver);

        return new PageImpl<>(notificationDtos, pageable, count);
    }

    private List<NotificationDto> getNotificationDtos(User receiver, Pageable pageable) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        return queryFactory.select(Projections.fields(
                        NotificationDto.class,
                        notification.id,
                        notification.type,
                        notification.message,
                        notification.isRead,
                        notification.createdAt,
                        notification.thumbnailPath
                ))
                .from(notification)
                .where(receiverContains(receiver)
                        .and(notification.createdAt.goe(thirtyDaysAgo)))
                .innerJoin(user).on(notification.actorId.eq(user.id))
                .orderBy(createOrderSpecifier())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private Long getCount(User receiver) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        return queryFactory
                .select(notification.count())
                .from(notification)
                .where(receiverContains(receiver)
                        .and(notification.createdAt.goe(thirtyDaysAgo)))
                .innerJoin(user).on(notification.actorId.eq(user.id))
                .fetchOne();
    }

    private BooleanExpression receiverContains(User receiver) {
        return notification.receiverId.eq(receiver.getId());
    }

    private OrderSpecifier[] createOrderSpecifier() {
        List<OrderSpecifier> orderSpecifiers = new ArrayList<>();

        orderSpecifiers.add(new OrderSpecifier(Order.ASC, notification.isRead));
        orderSpecifiers.add(new OrderSpecifier(Order.DESC, notification.createdAt));
        return orderSpecifiers.toArray(new OrderSpecifier[orderSpecifiers.size()]);
    }

    @Transactional
    public void readNotification(String receiverId) {
        queryFactory
                .update(notification)
                .where(notification.receiverId.eq(receiverId)
                    .and(notification.isRead.eq(false)))
                .set(notification.isRead, true)
                .execute();
    }

    @Transactional
    public void deleteNotification(String receiverId) {
        queryFactory
                .delete(notification)
                .where(notification.receiverId.eq(receiverId))
                .execute();
    }
}