package com.ppp.api.diary.dto.event;

import lombok.Getter;

import java.util.List;

@Getter
public class DiaryDraftUpdatedEvent {
    private final List<String> deletedPaths;

    public DiaryDraftUpdatedEvent(List<String> deletedMedias) {
        this.deletedPaths = deletedMedias;
    }
}
