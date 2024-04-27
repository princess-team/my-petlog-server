package com.ppp.api.diary.service;

import com.ppp.api.diary.dto.request.DiaryDraftCreateRequest;
import com.ppp.api.diary.dto.response.DiaryDraftCheckResponse;
import com.ppp.api.diary.dto.response.DiaryDraftResponse;
import com.ppp.api.diary.exception.DiaryException;
import com.ppp.common.service.FileStorageManageService;
import com.ppp.domain.diary.DiaryDraft;
import com.ppp.domain.diary.repository.DiaryDraftRedisRepository;
import com.ppp.domain.pet.Pet;
import com.ppp.domain.user.User;
import com.ppp.domain.video.TempVideo;
import com.ppp.domain.video.repository.TempVideoRedisRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.ppp.api.diary.exception.ErrorCode.DIARY_DRAFT_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DiaryDraftServiceTest {
    @Mock
    private DiaryDraftRedisRepository diaryDraftRedisRepository;
    @Mock
    private FileStorageManageService fileStorageManageService;
    @Mock
    private TempVideoRedisRepository tempVideoRedisRepository;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @InjectMocks
    private DiaryDraftService diaryDraftService;

    User user = User.builder()
            .id("randomstring")
            .nickname("hi")
            .profilePath("USER/12345678/1232132313dsfadskfakfsa.jpg")
            .build();

    Pet pet = Pet.builder()
            .id(1L)
            .birth(LocalDateTime.of(2023, 2, 8, 0, 0))
            .build();

    List<MultipartFile> images = List.of(
            new MockMultipartFile("images", "image.jpg",
                    MediaType.IMAGE_JPEG_VALUE, "abcde".getBytes()),
            new MockMultipartFile("images", "image.jpg",
                    MediaType.IMAGE_JPEG_VALUE, "abcde".getBytes())
    );

    @Test
    @DisplayName("임시 저장 일기 생성 성공")
    void createDiaryDraft_success() {
        //given
        DiaryDraftCreateRequest request = DiaryDraftCreateRequest.builder()
                .title("우리 강아지")
                .content("너무 귀엽당")
                .isPublic(true)
                .uploadedVideoIds(List.of("c8e8f796-8e29-4067-86c4-0eae419a054e"))
                .date(LocalDate.now().toString())
                .build();
        given(diaryDraftRedisRepository.findByPetIdAndUserId(anyLong(), anyString()))
                .willReturn(Optional.empty());
        given(fileStorageManageService.uploadImages(anyList(), any()))
                .willReturn(List.of("DIARY/2024-01-31/805496ad51ee46ab94394c5635a2abd820240131183104956.jpg",
                        "DIARY/2024-01-31/805496ad51ee46ab94394c5635a2abd820240131183104956.jpg"));
        given(tempVideoRedisRepository.findById(anyString()))
                .willReturn(Optional.of(TempVideo.builder()
                        .filePath("temp/encoded/2024021313/267d730ad30d4c8da5560e9b3cc0581820240213130549683.mp4")
                        .userId(user.getId())
                        .build()));
        given(fileStorageManageService.uploadVideos(any(), any()))
                .willReturn(List.of("VIDEO/2024-01-31/805496ad51ee46ab94394c5635a2abd820240131183104956.mp4"));

        //when
        diaryDraftService.createDiaryDraft(1L, request, images, user);
        //then
        ArgumentCaptor<DiaryDraft> captor = ArgumentCaptor.forClass(DiaryDraft.class);
        verify(diaryDraftRedisRepository, times(1)).save(captor.capture());
        assertEquals(request.getTitle(), captor.getValue().getTitle());
        assertEquals(request.getContent(), captor.getValue().getContent());
        assertEquals(request.getDate().toString(), captor.getValue().getDate().toString());
        assertEquals(user.getId(), captor.getValue().getUserId());
        assertEquals(pet.getId(), captor.getValue().getPetId());
        assertEquals(2, captor.getValue().getImages().size());
        assertEquals(1, captor.getValue().getVideos().size());
        assertTrue(captor.getValue().isPublic());
    }

    @Test
    @DisplayName("임시 저장 일기 생성 성공-기 저장된 임시 저장 일기 있는 경우")
    void createDiaryDraft_success_WhenDiaryDraftAlreadyExist() {
        //given
        DiaryDraftCreateRequest request = DiaryDraftCreateRequest.builder()
                .title("우리 강아지")
                .content("너무 귀엽당")
                .isPublic(true)
                .uploadedVideoIds(List.of("c8e8f796-8e29-4067-86c4-0eae419a054e"))
                .date(LocalDate.now().toString())
                .build();
        DiaryDraft diaryDraft = DiaryDraft.builder()
                .title("우리 고양이")
                .content("정말 귀엽다")
                .petId(1L)
                .userId("randomstring")
                .isPublic(false)
                .images(new ArrayList<>())
                .videos(new ArrayList<>())
                .build();
        given(diaryDraftRedisRepository.findByPetIdAndUserId(anyLong(), anyString()))
                .willReturn(Optional.of(diaryDraft));
        given(fileStorageManageService.uploadImages(anyList(), any()))
                .willReturn(List.of("DIARY/2024-01-31/805496ad51ee46ab94394c5635a2abd820240131183104956.jpg",
                        "DIARY/2024-01-31/805496ad51ee46ab94394c5635a2abd820240131183104956.jpg"));
        given(tempVideoRedisRepository.findById(anyString()))
                .willReturn(Optional.of(TempVideo.builder()
                        .filePath("temp/encoded/2024021313/267d730ad30d4c8da5560e9b3cc0581820240213130549683.mp4")
                        .userId(user.getId())
                        .build()));
        given(fileStorageManageService.uploadVideos(any(), any()))
                .willReturn(List.of("VIDEO/2024-01-31/805496ad51ee46ab94394c5635a2abd820240131183104956.mp4"));

        //when
        diaryDraftService.createDiaryDraft(1L, request, images, user);
        //then
        verify(diaryDraftRedisRepository, times(1)).save(any());
        assertEquals(request.getTitle(), diaryDraft.getTitle());
        assertEquals(request.getContent(), diaryDraft.getContent());
        assertEquals(request.getDate(), diaryDraft.getDate());
        assertEquals(user.getId(), diaryDraft.getUserId());
        assertEquals(pet.getId(), diaryDraft.getPetId());
        assertEquals(2, diaryDraft.getImages().size());
        assertEquals(1, diaryDraft.getVideos().size());
        assertTrue(diaryDraft.isPublic());
    }

    @Test
    @DisplayName("임시 저장 일기 존재 여부 조회 성공")
    void checkHasDiaryDraft_success() {
        //given
        given(diaryDraftRedisRepository.existsByPetIdAndUserId(anyLong(), anyString()))
                .willReturn(true);
        //when
        DiaryDraftCheckResponse response = diaryDraftService.checkHasDiaryDraft(1L, user);
        //then
        assertTrue(response.hasDiaryDraft());
    }

    @Test
    @DisplayName("임시 저장 일기 조회 성공")
    void retrieveDiaryDraft_success() {
        //given
        DiaryDraft diaryDraft = DiaryDraft.builder()
                .title("우리 고양이")
                .content("정말 귀엽다")
                .petId(1L)
                .date(LocalDate.EPOCH)
                .userId("randomstring")
                .isPublic(false)
                .images(List.of("DIARY/2024-01-31/805496ad51ee46ab94394c5635a2abd820240131183104956.jpg",
                        "DIARY/2024-01-31/805496ad51ee46ab94394c5635a2abd820240131183104956.jpg"))
                .videos(new ArrayList<>())
                .build();
        given(diaryDraftRedisRepository.findByPetIdAndUserId(anyLong(), anyString()))
                .willReturn(Optional.of(diaryDraft));
        //when
        DiaryDraftResponse response = diaryDraftService.retrieveDiaryDraft(1L, user);
        //then
        assertEquals(2, response.images().size());
        assertEquals(0, response.videos().size());
        assertEquals("우리 고양이", response.title());
        assertEquals("정말 귀엽다", response.content());
        assertEquals(LocalDate.EPOCH, response.date());
        assertFalse(response.isPublic());
    }

    @Test
    @DisplayName("임시 저장 일기 조회 실패-DIARY_DRAFT_NOT_FOUND")
    void retrieveDiaryDraft_fail_DIARY_DRAFT_NOT_FOUND() {
        //given
        given(diaryDraftRedisRepository.findByPetIdAndUserId(anyLong(), anyString()))
                .willReturn(Optional.empty());
        //when
        DiaryException exception = assertThrows(DiaryException.class, () -> diaryDraftService.retrieveDiaryDraft(1L, user));
        //then
        assertEquals(DIARY_DRAFT_NOT_FOUND.getCode(), exception.getCode());
    }


    @Test
    @DisplayName("임시 저장 일기 삭제 성공")
    void deleteDiaryDraft_success() {
        //given
        DiaryDraft diaryDraft = DiaryDraft.builder()
                .title("우리 고양이")
                .content("정말 귀엽다")
                .petId(1L)
                .date(LocalDate.EPOCH)
                .userId("randomstring")
                .isPublic(false)
                .images(List.of("DIARY/2024-01-31/805496ad51ee46ab94394c5635a2abd820240131183104956.jpg",
                        "DIARY/2024-01-31/805496ad51ee46ab94394c5635a2abd820240131183104956.jpg"))
                .videos(new ArrayList<>())
                .build();
        given(diaryDraftRedisRepository.findByPetIdAndUserId(anyLong(), anyString()))
                .willReturn(Optional.of(diaryDraft));
        //when
        diaryDraftService.deleteDiaryDraft(1L, user);
        //then
        verify(diaryDraftRedisRepository, times(1)).delete(any());
    }

    @Test
    @DisplayName("임시 저장 일기 삭제 실패-DIARY_DRAFT_NOT_FOUND")
    void deleteDiaryDraft_fail_DIARY_DRAFT_NOT_FOUND() {
        //given
        given(diaryDraftRedisRepository.findByPetIdAndUserId(anyLong(), anyString()))
                .willReturn(Optional.empty());
        //when
        DiaryException exception = assertThrows(DiaryException.class, () -> diaryDraftService.deleteDiaryDraft(1L, user));
        //then
        assertEquals(DIARY_DRAFT_NOT_FOUND.getCode(), exception.getCode());
    }

}