package com.ppp.api.notification.handler;

import com.ppp.api.notification.dto.event.DiaryNotificationEvent;
import com.ppp.api.notification.dto.event.DiaryTagNotificationEvent;
import com.ppp.api.notification.dto.event.InvitationNotificationEvent;
import com.ppp.api.notification.dto.event.SubscribeNotificationEvent;
import com.ppp.api.notification.service.NotificationService;
import com.ppp.domain.diary.DiaryLike;
import com.ppp.domain.diary.repository.DiaryLikeRepository;
import com.ppp.domain.notification.constant.Type;
import com.ppp.domain.pet.Pet;
import com.ppp.domain.pet.PetImage;
import com.ppp.domain.pet.repository.PetImageRepository;
import com.ppp.domain.subscription.SubscriptionLog;
import com.ppp.domain.subscription.repository.SubscriptionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationHandler {
    private final NotificationService notificationService;
    private final PetImageRepository petImageRepository;
    private final DiaryLikeRepository diaryLikeRepository;
    private final SubscriptionLogRepository subscriptionLogRepository;

    private String findThumbnailPath(Pet pet) {
        Optional<PetImage> maybePetImage = petImageRepository.findByPet(pet);
        return maybePetImage.map(PetImage::findThumbnailPath).orElse(null);
    }
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleInvitationNotification(InvitationNotificationEvent event) {
        String message = "";
        String thumbnailPath = "";
        switch (event.getMessageCode()) {
            case INVITATION_REQUEST:
                message = String.format("%s님이 %s의 펫메이트로 초대했습니다.", event.getActor().getNickname(), event.getPet().getName());
                thumbnailPath = findThumbnailPath(event.getPet());
                break;
            case INVITATION_ACCEPT:
                message = String.format("%s님이 %s 펫메이트 초대를 수락하셨습니다.", event.getActor().getNickname(), event.getPet().getName());
                thumbnailPath = event.getActor().getThumbnailPath();
                break;
            case INVITATION_REJECT:
                message = String.format("%s님이 %s의 초대를 거절하셨습니다.", event.getActor().getNickname(), event.getPet().getName());
                thumbnailPath = event.getActor().getThumbnailPath();
                break;
            case INVITATION_GUARDIAN_KICK:
                message = String.format("%s님이 %s 펫메이트 멤버에서 회원님을 삭제했습니다.", event.getActor().getNickname(), event.getPet().getName());
                thumbnailPath = findThumbnailPath(event.getPet());
                break;
        }
        notificationService.createNotification(Type.INVITATION, event.getActor().getId(), event.getReceiverId(), thumbnailPath, message);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDiaryNotification(DiaryNotificationEvent event) {
        String message = "";
        switch (event.getMessageCode()) {
            case DIARY_COMMENT_CREATE:
                message = String.format("%s의 일기에 %s님이 댓글을 달았습니다.", event.getDiary().getPet().getName(), event.getActor().getNickname());
                notificationService.createNotification(Type.DIARY, event.getActor().getId(), event.getDiary().getUser().getId(), event.getActor().getThumbnailPath(), message);
                break;
            case DIARY_LIKE:
                message = String.format("%s님이 %s의 일기를 좋아합니다.",  event.getActor().getNickname(),  event.getDiary().getPet().getName());
                if (!diaryLikeRepository.existsByDiaryIdAndUserId(event.getDiary().getId(), event.getActor().getId())) {
                    notificationService.createNotification(Type.DIARY, event.getActor().getId(), event.getDiary().getUser().getId(), event.getActor().getThumbnailPath(), message);
                    diaryLikeRepository.save(new DiaryLike(event.getDiary().getId(), event.getActor().getId()));
                }
                break;
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSubscriptionNotification(SubscribeNotificationEvent event) {
        String message = "";
        switch (event.getMessageCode()) {
            case SUBSCRIBE:
                message = String.format("%s님이 %s의 일기를 구독하기 시작했습니다.", event.getActor().getNickname(), event.getPet().getName());
                if (!subscriptionLogRepository.existsByPetIdAndUserId(event.getPet().getId(), event.getActor().getId())) {
                    notificationService.createNotification(Type.SUBSCRIBE, event.getActor().getId(), event.getReceiverId(), event.getActor().getThumbnailPath(), message);
                    subscriptionLogRepository.save(new SubscriptionLog(event.getPet().getId(), event.getActor().getId()));
                }
                break;
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDiaryTagNotification(DiaryTagNotificationEvent event) {
        String message = "";
        switch (event.getMessageCode()) {
            case DIARY_TAG:
                for (String taggedId : event.getTaggedIds()) {
                    message = String.format("%s의 일기에 %s님이 회원님을 태그했습니다.", event.getDiary().getPet().getName(), event.getActor().getNickname());
                    notificationService.createNotification(Type.DIARY, event.getActor().getId(), taggedId, event.getActor().getThumbnailPath(), message);
                }
                break;
        }
    }
}
