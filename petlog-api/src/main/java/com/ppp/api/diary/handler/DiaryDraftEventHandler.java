package com.ppp.api.diary.handler;

import com.ppp.api.diary.dto.event.DiaryDraftDeletedEvent;
import com.ppp.api.diary.dto.event.DiaryDraftUpdatedEvent;
import com.ppp.common.service.FileStorageManageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class DiaryDraftEventHandler {
    private final FileStorageManageService fileStorageManageService;

    @Async
    @EventListener
    public void handleDiaryUpdatedEvent(DiaryDraftUpdatedEvent event) {
        fileStorageManageService.deleteImages(event.getDeletedPaths());
    }

    @Async
    @EventListener
    public void handleDiaryDeletedEvent(DiaryDraftDeletedEvent event) {
        fileStorageManageService.deleteImages(event.getDeletedPaths());
    }

}
