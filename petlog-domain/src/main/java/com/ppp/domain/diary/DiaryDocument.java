package com.ppp.domain.diary;

import com.ppp.domain.common.BaseDocument;
import com.ppp.domain.user.UserDocument;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;

@Getter
@Document(indexName = "diaries")
public class DiaryDocument extends BaseDocument {
    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String content;

    private LocalDate date;

    private String thumbnailPath;

    @Field(type = FieldType.Nested)
    private UserDocument user;

    private long petId;

    @Builder
    public DiaryDocument(String id, String title, String content, LocalDate date, String thumbnailPath, long petId, UserDocument user) {
        super(id);
        this.title = title;
        this.content = content;
        this.date = date;
        this.thumbnailPath = thumbnailPath;
        this.user = user;
        this.petId = petId;
    }

    public static DiaryDocument from(Diary diary) {
        return DiaryDocument.builder()
                .id(diary.getId() + "")
                .content(diary.getContent())
                .title(diary.getTitle())
                .date(diary.getDate())
                .thumbnailPath(diary.getThumbnailPath())
                .petId(diary.getPet().getId())
                .user(UserDocument.from(diary.getUser()))
                .build();
    }
}
