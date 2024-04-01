package com.ppp.domain.diary;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@RedisHash(value = "diary-drafts")
public class DiaryDraft implements Serializable {
    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private Long petId;

    @Column
    private String title;

    @Column
    private String content;

    @Column
    private LocalDate date;

    @Column
    private boolean isPublic;

    @Column
    private LocalDateTime createdAt;

    @Column
    @Getter(value = AccessLevel.NONE)
    private List<String> images;

    @Column
    @Getter(value = AccessLevel.NONE)
    private List<String> videos;

    @Builder
    public DiaryDraft(String userId, Long petId, String title, String content, LocalDate date, boolean isPublic, List<String> images, List<String> videos) {
        this.userId = userId;
        this.petId = petId;
        this.title = title;
        this.content = content;
        this.date = date;
        this.isPublic = isPublic;
        this.images = images;
        this.videos = videos;
        this.createdAt = LocalDateTime.now();
    }

    public void update(String title, String content, LocalDate date, boolean isPublic, List<String> images, List<String> videos) {
        this.title = title;
        this.content = content;
        this.date = date;
        this.isPublic = isPublic;
        this.images = images;
        this.videos = videos;
        this.createdAt = LocalDateTime.now();
    }

    public List<String> getMedias() {
        getImages().addAll(getVideos());
        return images;
    }

    public List<String> getImages() {
        return images == null ? new ArrayList<>() : images;
    }

    public List<String> getVideos() {
        return videos == null ? new ArrayList<>() : videos;
    }
}
