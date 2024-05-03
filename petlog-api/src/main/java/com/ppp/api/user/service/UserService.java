package com.ppp.api.user.service;

import com.ppp.api.auth.exception.AuthException;
import com.ppp.api.auth.service.AuthService;
import com.ppp.api.user.dto.event.UserProfileUpdatedEvent;
import com.ppp.api.user.dto.response.ProfileResponse;
import com.ppp.api.user.exception.ErrorCode;
import com.ppp.api.user.exception.NotFoundUserException;
import com.ppp.api.user.exception.UserException;
import com.ppp.common.service.FileStorageManageService;
import com.ppp.common.service.ThumbnailService;
import com.ppp.domain.common.constant.Domain;
import com.ppp.domain.common.constant.FileType;
import com.ppp.domain.user.User;
import com.ppp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final FileStorageManageService fileStorageManageService;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final AuthService authService;
    private final ThumbnailService thumbnailService;

    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public void createProfile(User user, MultipartFile profileImage, String nickname) {
        User userFromDB = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new NotFoundUserException(ErrorCode.NOT_FOUND_USER));

        userFromDB.setNickname(nickname);
        saveProfileImage(userFromDB, profileImage);
    }

    private void saveProfileImage(User user, MultipartFile profileImage) {
        if (profileImage != null && !profileImage.isEmpty()) {
            fileStorageManageService.deleteImage(user.getProfilePath());
            String savedPath = uploadImageToS3(profileImage);
            user.updateProfilePath(savedPath);
            saveProfileThumbnail(user, savedPath);
        } else {
            if (user.getProfilePath() != null && !user.getProfilePath().isEmpty())
                fileStorageManageService.deleteImage(user.getProfilePath());
            user.deleteProfilePath();
        }
    }

    private void saveProfileThumbnail(User user, String savedPath) {
        try {
            if (user.getThumbnailPath() != null) {
                fileStorageManageService.deleteImage(user.getThumbnailPath());
            }
            String thumbnailPath = thumbnailService.uploadThumbnailFromStorageFile(savedPath, FileType.IMAGE, Domain.USER);
            user.updateThumbnailPath(thumbnailPath);
        } catch (Exception e) {
            log.warn("{} is thumbnail error", user.getId());
        }
    }

    private String uploadImageToS3(MultipartFile profileImage) {
        return fileStorageManageService.uploadImage(profileImage, Domain.USER)
                .orElseThrow(() -> new UserException(ErrorCode.PROFILE_REGISTRATION_FAILED));
    }

    @Transactional
    public void updateProfile(User user, String nickname, String password) {
        User userFromDb = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new NotFoundUserException(ErrorCode.NOT_FOUND_USER));

        if (nickname != null && !nickname.isEmpty())
            userFromDb.setNickname(nickname);
        if (password != null && !password.isEmpty())
            userFromDb.setPassword(authService.encodePassword(password));
        applicationEventPublisher.publishEvent(new UserProfileUpdatedEvent(user.getId()));
    }

    @Transactional
    public void updateImage(User user, MultipartFile profileImage) {
        User userFromDb = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new NotFoundUserException(ErrorCode.NOT_FOUND_USER));

        saveProfileImage(userFromDb, profileImage);
    }

    public ProfileResponse displayMe(User user) {
        User userFromDb = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new NotFoundUserException(ErrorCode.NOT_FOUND_USER));
        return ProfileResponse.builder()
                .id(userFromDb.getId())
                .nickname(userFromDb.getNickname())
                .email(userFromDb.getEmail())
                .profilePath(userFromDb.getProfilePath())
                .build();
    }

    public void validatePassword(String rawPassword, String encodedPassword) {
        if (!authService.checkPasswordMatches(rawPassword, encodedPassword))
            throw new AuthException(com.ppp.api.auth.exception.ErrorCode.NOTMATCH_PASSWORD);
    }
}
