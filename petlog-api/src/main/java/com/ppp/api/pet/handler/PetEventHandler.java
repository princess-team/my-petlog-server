package com.ppp.api.pet.handler;

import com.ppp.api.diary.service.DiarySearchService;
import com.ppp.api.diary.service.DiaryService;
import com.ppp.api.pet.dto.event.PetDeletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@RequiredArgsConstructor
public class PetEventHandler {
    private final DiarySearchService diarySearchService;
    private final DiaryService diaryService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePetDeletedEvent(PetDeletedEvent event) {
        CompletableFuture.runAsync(() -> diarySearchService.deleteAllByPetId(event.getPetId()))
                .thenRunAsync(() -> diaryService.deleteAllByPetId(event.getPetId()));
    }

}
