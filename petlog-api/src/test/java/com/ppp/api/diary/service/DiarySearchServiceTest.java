package com.ppp.api.diary.service;

import com.ppp.api.diary.dto.response.DiaryGroupByDateResponse;
import com.ppp.api.diary.dto.response.DiaryMostUsedTermsResponse;
import com.ppp.api.diary.exception.DiaryException;
import com.ppp.api.user.exception.UserException;
import com.ppp.domain.diary.Diary;
import com.ppp.domain.diary.DiaryDocument;
import com.ppp.domain.diary.dto.DiaryMostUsedTermsDto;
import com.ppp.domain.diary.repository.DiarySearchQuerydslRepository;
import com.ppp.domain.diary.repository.DiarySearchRepository;
import com.ppp.domain.guardian.repository.GuardianRepository;
import com.ppp.domain.pet.Pet;
import com.ppp.domain.user.User;
import com.ppp.domain.user.UserDocument;
import com.ppp.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.ppp.api.diary.exception.ErrorCode.FORBIDDEN_PET_SPACE;
import static com.ppp.api.user.exception.ErrorCode.NOT_FOUND_USER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DiarySearchServiceTest {
    @Mock
    private DiarySearchRepository diarySearchRepository;

    @Mock
    private DiaryCommentRedisService diaryCommentRedisService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GuardianRepository guardianRepository;
    @Mock
    private DiarySearchQuerydslRepository diarySearchQuerydslRepository;
    @InjectMocks
    private DiarySearchService diarySearchService;

    User user = User.builder()
            .id("abcde1234")
            .nickname("엄마")
            .build();

    Pet pet = Pet.builder()
            .id(1L).build();

    Diary diary = Diary.builder()
            .title("우리집 고양이")
            .content("츄르를 좋아해")
            .date(LocalDate.of(2020, 11, 11))
            .user(user)
            .pet(pet).build();

    @Test
    @DisplayName("저장 성공")
    void save_success() {
        //when
        diarySearchService.save(diary);
        ArgumentCaptor<DiaryDocument> captor = ArgumentCaptor.forClass(DiaryDocument.class);
        //then
        verify(diarySearchRepository, times(1))
                .save(captor.capture());
        assertEquals(captor.getValue().getUser().getNickname(), "엄마");
        assertEquals(captor.getValue().getTitle(), "우리집 고양이");
        assertEquals(captor.getValue().getContent(), "츄르를 좋아해");
        assertEquals(captor.getValue().getDate(), LocalDate.of(2020, 11, 11).toEpochDay());
        assertEquals(captor.getValue().getPetId(), 1L);
    }

    @Test
    @DisplayName("업데이트 성공")
    void update_success() {
        diarySearchService.update(diary);
        ArgumentCaptor<DiaryDocument> captor = ArgumentCaptor.forClass(DiaryDocument.class);
        //then
        verify(diarySearchRepository, times(1))
                .save(captor.capture());
        assertEquals(captor.getValue().getUser().getNickname(), "엄마");
        assertEquals(captor.getValue().getTitle(), "우리집 고양이");
        assertEquals(captor.getValue().getContent(), "츄르를 좋아해");
        assertEquals(captor.getValue().getDate(), LocalDate.of(2020, 11, 11).toEpochDay());
        assertEquals(captor.getValue().getPetId(), 1L);
    }

    @Test
    @DisplayName("검색 성공")
    void search_success() {
        //given
        given(guardianRepository.existsByUserIdAndPetId(anyString(), anyLong()))
                .willReturn(true);
        given(diaryCommentRedisService.getDiaryCommentCountByDiaryId(any()))
                .willReturn(3);
        given(diarySearchRepository.findByTitleContainsOrContentContainsAndPetIdOrderByDateDesc(anyString(), anyLong(), anySet(), any()))
                .willReturn(new PageImpl<>(
                        List.of(
                                DiaryDocument.builder()
                                        .id("1")
                                        .title("우리집 고양이")
                                        .content("츄르를 좋아해")
                                        .thumbnailPath("/DIARY/2024-01-31/805496ad51ee46ab94394c5635a2abd820240131183104956.jpg")
                                        .date(LocalDate.MAX.toEpochDay())
                                        .user(UserDocument.builder()
                                                .nickname("첫째누나")
                                                .id("qwert1234")
                                                .build())
                                        .petId(1L).build(),
                                DiaryDocument.builder()
                                        .id("2")
                                        .title("우리집 고양이")
                                        .content("츄르를 먹는")
                                        .thumbnailPath("/DIARY/2024-01-31/805496ad51ee46ab94394c5635a2abd820240131183104956.jpg")
                                        .date(LocalDate.MAX.toEpochDay())
                                        .user(UserDocument.builder()
                                                .nickname("첫째누나")
                                                .id("qwert1234")
                                                .build())
                                        .petId(1L).build(),
                                DiaryDocument.builder()
                                        .id("3")
                                        .title("우리집 강아지")
                                        .content("츄르를 좋아해")
                                        .thumbnailPath("/DIARY/2024-01-31/805496ad51ee46ab94394c5635a2abd820240131183104956.jpg")
                                        .date(LocalDate.MIN.toEpochDay())
                                        .user(UserDocument.builder()
                                                .nickname("엄마")
                                                .id("abcde1234")
                                                .build())
                                        .petId(1L).build())
                ));
        //when
        Page<DiaryGroupByDateResponse> response = diarySearchService.searchInPetSpace(user, "우리집", 1L, 1, 10);
        //then
        assertEquals(response.getContent().size(), 2);
        assertEquals(response.getContent().get(0).date(), LocalDate.MAX);
        assertEquals(response.getContent().get(0).diaries().size(), 2);
        assertEquals(response.getContent().get(0).diaries().get(0).diaryId(), 1L);
        assertEquals(response.getContent().get(0).diaries().get(0).title(), "우리집 고양이");
        assertEquals(response.getContent().get(0).diaries().get(0).content(), "츄르를 좋아해");
        assertEquals(response.getContent().get(0).diaries().get(0).writer().id(), "qwert1234");
        assertEquals(response.getContent().get(0).diaries().get(0).writer().nickname(), "첫째누나");
        assertFalse(response.getContent().get(0).diaries().get(0).writer().isCurrentUser());
        assertEquals(response.getContent().get(0).diaries().get(1).diaryId(), 2L);
        assertEquals(response.getContent().get(1).date(), LocalDate.MIN);
        assertEquals(response.getContent().get(1).diaries().size(), 1);
        assertEquals(response.getContent().get(1).diaries().get(0).title(), "우리집 강아지");
        assertEquals(response.getContent().get(1).diaries().get(0).content(), "츄르를 좋아해");
        assertEquals(response.getContent().get(1).diaries().get(0).writer().id(), "abcde1234");
        assertEquals(response.getContent().get(1).diaries().get(0).writer().nickname(), "엄마");
        assertTrue(response.getContent().get(1).diaries().get(0).writer().isCurrentUser());
    }

    @Test
    @DisplayName("유저 정보 elastic search 에 저장 성공")
    void updateUser_success() {
        //given
        List<DiaryDocument> documents = List.of(DiaryDocument.builder()
                .id("3")
                .title("우리집 강아지")
                .content("츄르를 좋아해")
                .thumbnailPath("/DIARY/2024-01-31/805496ad51ee46ab94394c5635a2abd820240131183104956.jpg")
                .date(LocalDate.MIN.toEpochDay())
                .user(UserDocument.builder()
                        .nickname("첫째누나")
                        .id("abcde1234")
                        .build())
                .petId(1L).build());
        given(userRepository.findByIdAndIsDeletedFalse(anyString()))
                .willReturn(Optional.of(user));
        given(diarySearchRepository.findByUser_Id(anyString()))
                .willReturn(documents);
        //when
        diarySearchService.updateUser("abcde1234");
        //then
        verify(diarySearchRepository, times(1)).saveAll(anyCollection());
        assertEquals("abcde1234", documents.get(0).getUser().getId());
        assertEquals("엄마", documents.get(0).getUser().getNickname());
    }

    @Test
    @DisplayName("유저 정보 elastic search 에 저장 실패_not found user")
    void update_fail_NOT_FOUND_USER() {
        //given
        given(userRepository.findByIdAndIsDeletedFalse(anyString()))
                .willReturn(Optional.empty());
        //when
        UserException exception = assertThrows(UserException.class, () -> diarySearchService.updateUser("abcde1234"));
        //then
        assertEquals(NOT_FOUND_USER.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("자주 사용한 용어 조회 성공")
    void findMostUsedTermsByPetId_success() {
        //given
        given(guardianRepository.existsByUserIdAndPetId(anyString(), anyLong()))
                .willReturn(true);
        given(diarySearchQuerydslRepository.findMostUsedTermsByPetId(anyLong()))
                .willReturn(new DiaryMostUsedTermsDto(Set.of("우리집", "강아지", "미용", "산책을")));
        //when
        DiaryMostUsedTermsResponse response = diarySearchService.findMostUsedTermsByPetId(user, 1L);
        //then
        assertEquals(response.terms().size(), 4);
        assertTrue(response.terms().contains("우리집"));
    }

    @Test
    @DisplayName("자주 사용한 용어 조회 실패_forbidden pet space")
    void findMostUsedTermsByPetId_fail_FORBIDDEN_PET_SPACE() {
        //given
        given(guardianRepository.existsByUserIdAndPetId(anyString(), anyLong()))
                .willReturn(false);
        //when
        DiaryException exception = assertThrows(DiaryException.class, () -> diarySearchService.findMostUsedTermsByPetId(user, 1L));
        //then
        assertEquals(FORBIDDEN_PET_SPACE.getCode(), exception.getCode());
    }
}