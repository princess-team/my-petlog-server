package com.ppp.domain.user;


import com.ppp.domain.common.BaseTimeEntity;
import com.ppp.domain.common.util.GenerationUtil;
import com.ppp.domain.pet.Pet;
import com.ppp.domain.user.constant.LoginType;
import com.ppp.domain.user.constant.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User extends BaseTimeEntity {

    @Id
    @Column(length = 100, unique = true)
    private String id;

    @Column(unique = true)
    private String email;

    @Column(unique = true, length = 20)
    private String nickname;

    private String password;

    @Column(columnDefinition = "BIT default 0")
    private Boolean isDeleted;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "user")
    private List<Pet> pets = new ArrayList<>();

    private String profilePath;

    private String thumbnailPath;

    @Enumerated(EnumType.STRING)
    private LoginType loginType;

    public static User createUserByEmail(String email, String password, Role role) {
        return User.builder()
                .id(GenerationUtil.generateIdFromEmail(email))
                .email(email)
                .password(password)
                .role(role)
                .isDeleted(false)
                .loginType(LoginType.EMAIL)
                .build();
    }

    public static User createUserBySocial(String email, Role role, LoginType loginType) {
        return User.builder()
                .id(GenerationUtil.generateIdFromEmail(email))
                .email(email)
                .role(role)
                .isDeleted(false)
                .loginType(loginType)
                .build();
    }

    public void updateProfilePath(String path) {
        this.profilePath = path;
    }

    public void updateThumbnailPath(String path) {
        this.thumbnailPath = path;
    }

    public void deleteProfilePath() {
        this.profilePath = null;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
