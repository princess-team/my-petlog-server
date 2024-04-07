package com.ppp.domain.diary;

import com.ppp.domain.common.BaseDocument;
import com.ppp.domain.user.User;
import com.ppp.domain.user.UserDocument;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Document(indexName = "diaries")
public class DiaryDocument extends BaseDocument {
    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String content;

    private Long date;

    private String thumbnailPath;

    private boolean isPublic;

    @Field(type = FieldType.Nested)
    private UserDocument user;

    private long petId;

    @Builder
    public DiaryDocument(String id, String title, String content, Long date, String thumbnailPath, long petId, boolean isPublic, UserDocument user) {
        super(id);
        this.title = title;
        this.content = content;
        this.date = date;
        this.thumbnailPath = thumbnailPath;
        this.user = user;
        this.petId = petId;
        this.isPublic = isPublic;
    }

    public static DiaryDocument from(Diary diary) {
        return DiaryDocument.builder()
                .id(diary.getId() + "")
                .content(diary.getContent())
                .title(diary.getTitle())
                .isPublic(diary.isPublic())
                .date(diary.getDate().toEpochDay())
                .thumbnailPath(diary.getThumbnailPath())
                .petId(diary.getPet().getId())
                .user(UserDocument.from(diary.getUser()))
                .build();
    }

    public void updateUser(User user) {
        this.user = UserDocument.from(user);
    }
}
