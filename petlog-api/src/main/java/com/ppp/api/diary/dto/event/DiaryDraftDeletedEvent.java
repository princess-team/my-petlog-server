package com.ppp.api.diary.dto.event;

import lombok.Getter;

import java.util.List;

@Getter
public class DiaryDraftDeletedEvent {
    private final List<String> deletedPaths;

    public DiaryDraftDeletedEvent(List<String> deletedMedias) {
        this.deletedPaths = deletedMedias;
    }
}
