package com.ppp.common.service;

import com.ppp.common.client.FileStorageClient;
import com.ppp.common.util.FilePathUtil;
import com.ppp.domain.common.constant.Domain;
import com.ppp.domain.video.TempVideo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileStorageManageService {
    private final FileStorageClient fileStorageClient;
    public static final List<String> ALLOW_IMAGE_CODES = List.of(".jpeg", ".png", ".jpg", ".gif", ".svg");
    public static final List<String> ALLOW_VIDEO_CODES = List.of(".mp4");

    public Optional<String> uploadImage(MultipartFile multipartFile, Domain domain) {
        Optional<String> maybeExtension = FilePathUtil.getFileExtension(
                Objects.requireNonNull(multipartFile.getOriginalFilename()));
        if (maybeExtension.isEmpty() || !ALLOW_IMAGE_CODES.contains(maybeExtension.get().toLowerCase(Locale.ROOT)))
            return Optional.empty();
        return Optional.of(fileStorageClient.upload(multipartFile, domain));
    }

    public List<String> uploadImages(List<MultipartFile> multipartFiles, Domain domain) {
        return multipartFiles.stream()
                .filter(multipartFile -> {
                    Optional<String> maybeExtension = FilePathUtil.getFileExtension(
                            Objects.requireNonNull(multipartFile.getOriginalFilename()));
                    return maybeExtension.isPresent() && ALLOW_IMAGE_CODES.contains(maybeExtension.get().toLowerCase(Locale.ROOT));
                })
                .map(multipartFile -> fileStorageClient.upload(multipartFile, domain))
                .collect(Collectors.toList());
    }

    public Optional<String> uploadImage(File file, Domain domain) {
        Optional<String> maybeExtension = FilePathUtil.getFileExtension(file.getName());
        if (maybeExtension.isEmpty() || !ALLOW_IMAGE_CODES.contains(maybeExtension.get().toLowerCase(Locale.ROOT)))
            return Optional.empty();
        return Optional.of(fileStorageClient.upload(file, domain));
    }

    public Optional<String> uploadVideo(TempVideo video, Domain domain) {
        File file = Path.of(video.getFilePath()).toFile();
        Optional<String> maybeExtension = FilePathUtil.getFileExtension(file.getName());
        if (maybeExtension.isEmpty() || !ALLOW_VIDEO_CODES.contains(maybeExtension.get().toLowerCase(Locale.ROOT)))
            return Optional.empty();
        return Optional.of(fileStorageClient.upload(file, domain));
    }

    public List<String> uploadVideos(List<TempVideo> videos, Domain domain) {
        return videos.stream()
                .filter(video -> {
                    Optional<String> maybeExtension = FilePathUtil.getFileExtension(video.getFilePath());
                    return maybeExtension.isPresent() && ALLOW_VIDEO_CODES.contains(maybeExtension.get());
                })
                .map(video -> fileStorageClient.upload(Path.of(video.getFilePath()).toFile(), domain))
                .collect(Collectors.toList());
    }

    public void deleteImage(String imagePath) {
        fileStorageClient.delete(imagePath);
    }

    public void deleteImages(List<String> imagePaths) {
        imagePaths.forEach(fileStorageClient::delete);
    }
}
