package com.ppp.api.diary.service;

import com.ppp.api.diary.dto.request.DiaryCommentRequest;
import com.ppp.api.diary.dto.response.DiaryCommentResponse;
import com.ppp.api.diary.dto.response.DiaryReCommentResponse;
import com.ppp.api.diary.exception.DiaryException;
import com.ppp.common.util.TimeUtil;
import com.ppp.domain.diary.Diary;
import com.ppp.domain.diary.DiaryComment;
import com.ppp.domain.diary.repository.DiaryCommentRepository;
import com.ppp.domain.diary.repository.DiaryRepository;
import com.ppp.domain.guardian.repository.GuardianRepository;
import com.ppp.domain.pet.Pet;
import com.ppp.domain.user.User;
import com.ppp.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.LocalDate;
import java.util.*;

import static com.ppp.api.diary.exception.ErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiaryCommentServiceTest {
    @Mock
    private DiaryCommentRepository diaryCommentRepository;
    @Mock
    private DiaryRepository diaryRepository;
    @Mock
    private GuardianRepository guardianRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DiaryCommentRedisService diaryCommentRedisService;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @InjectMocks
    private DiaryCommentService diaryCommentService;

    static User user = User.builder()
            .id("abcde1234")
            .profilePath("USER/12345678/1232132313dsfadskfakfsa.jpg")
            .nickname("hi")
            .build();

    static User userA = User.builder()
            .id("abc123")
            .profilePath("USER/12345678/1232132313dsfadskfakfsa.jpg")
            .nickname("첫째누나")
            .build();

    static Pet pet = Pet.builder()
            .id(1L).build();

    static Diary diary = Diary.builder()
            .title("우리집 고양이")
            .content("츄르를 좋아해")
            .isPublic(false)
            .date(LocalDate.of(2020, 11, 11))
            .user(user)
            .pet(pet).build();

    static Map<String, String> taggedUserIdNicknameMap = Map.of("ljf123", "둘째누나");

    static MockedStatic<TimeUtil> mockTimeUtil = mockStatic(TimeUtil.class);
    static DiaryComment mockDiaryComment = mock(DiaryComment.class);
    static Diary mockDiary = mock(Diary.class);

    @BeforeAll
    static void init() {
        mockTimeUtil.when(() -> TimeUtil.calculateTerm(any())).thenReturn("2분");
        given(mockDiaryComment.getDiary()).willReturn(mockDiary);
        given(mockDiary.getId()).willReturn(1L);
        given(mockDiaryComment.getAncestorCommentId()).willReturn(1L);
        given(mockDiaryComment.getUser()).willReturn(user);
        given(mockDiaryComment.getParent()).willReturn(DiaryComment.builder()
                .content("오늘은 바다로 산책을 갔어요")
                .taggedUsersIdNicknameMap(taggedUserIdNicknameMap)
                .diary(diary)
                .user(user)
                .build());
        given(mockDiaryComment.getContent()).willReturn("오늘은 바다로 산책을 갔어요");
    }

    @AfterAll
    static void tearDown() {
        mockTimeUtil.close();
    }

    @Test
    @DisplayName("다이어리 댓글 생성 성공")
    void createComment_success() {
        //given
        DiaryCommentRequest request = new DiaryCommentRequest("오늘은 산으로 산책을 갔어요", List.of("abc123", "dab456"));

        given(diaryRepository.findByIdAndIsDeletedFalse(anyLong()))
                .willReturn(Optional.of(diary));
        given(guardianRepository.existsByUserIdAndPetId(anyString(), anyLong()))
                .willReturn(true);
        given(userRepository.findByGuardianUsersByPetIdAndUserIdsContaining(anyLong(), anyList())).willReturn(List.of(userA));
        given(mockDiaryComment.getTaggedUsersIdNicknameMap()).willReturn(taggedUserIdNicknameMap);
        given(diaryCommentRepository.save(any()))
                .willReturn(mockDiaryComment);
        //when
        DiaryCommentResponse response = diaryCommentService.createComment(user, 1L, 1L, request);
        ArgumentCaptor<DiaryComment> diaryCommentArgumentCaptor = ArgumentCaptor.forClass(DiaryComment.class);
        //then
        verify(diaryCommentRepository, times(1)).save(diaryCommentArgumentCaptor.capture());
        assertEquals(diaryCommentArgumentCaptor.getValue().getDiary(), diary);
        assertEquals(diaryCommentArgumentCaptor.getValue().getUser(), user);
        assertEquals(diaryCommentArgumentCaptor.getValue().getContent(), "오늘은 산으로 산책을 갔어요");
        assertEquals(diaryCommentArgumentCaptor.getValue().getTaggedUsersIdNicknameMap().size(), 1);
        assertTrue(diaryCommentArgumentCaptor.getValue().getTaggedUsersIdNicknameMap().containsKey("abc123"));
        assertFalse(diaryCommentArgumentCaptor.getValue().getTaggedUsersIdNicknameMap().containsKey("dab456"));
        assertTrue(diaryCommentArgumentCaptor.getValue().getTaggedUsersIdNicknameMap().containsValue("첫째누나"));
        assertEquals(response.writer().id(), "abcde1234");
        assertEquals(response.writer().nickname(), "hi");
        assertFalse(response.isCurrentUserLiked());
        assertEquals(response.likeCount(), 0);
        assertEquals(response.content(), "오늘은 바다로 산책을 갔어요");
        assertEquals(response.taggedUsers().size(), 1);
        assertEquals(response.taggedUsers().get(0).nickname(), "둘째누나");
        assertEquals(response.taggedUsers().get(0).id(), "ljf123");
    }

    @Test
    @DisplayName("다이어리 댓글 생성 성공-빈 태깅 유저 리스트가 주어졌을 때")
    void createComment_success_whenTaggedUserIdsEmpty() {
        //given
        DiaryCommentRequest request = new DiaryCommentRequest("오늘은 산으로 산책을 갔어요", new ArrayList<>());

        given(diaryRepository.findByIdAndIsDeletedFalse(anyLong()))
                .willReturn(Optional.of(diary));
        given(guardianRepository.existsByUserIdAndPetId(anyString(), anyLong()))
                .willReturn(true);
        given(mockDiaryComment.getTaggedUsersIdNicknameMap()).willReturn(new HashMap<>());
        given(diaryCommentRepository.save(any()))
                .willReturn(mockDiaryComment);
        //when
        DiaryCommentResponse response = diaryCommentService.createComment(user, 1L, 1L, request);
        ArgumentCaptor<DiaryComment> diaryCommentArgumentCaptor = ArgumentCaptor.forClass(DiaryComment.class);
        //then
        verify(diaryCommentRepository, times(1)).save(diaryCommentArgumentCaptor.capture());
        assertEquals(diaryCommentArgumentCaptor.getValue().getDiary(), diary);
        assertEquals(diaryCommentArgumentCaptor.getValue().getUser(), user);
        assertEquals(diaryCommentArgumentCaptor.getValue().getContent(), "오늘은 산으로 산책을 갔어요");
        assertEquals(diaryCommentArgumentCaptor.getValue().getTaggedUsersIdNicknameMap().size(), 0);
        assertEquals(response.writer().id(), "abcde1234");
        assertEquals(response.writer().nickname(), "hi");
        assertFalse(response.isCurrentUserLiked());
        assertEquals(response.likeCount(), 0);
        assertEquals(response.content(), "오늘은 바다로 산책을 갔어요");
        assertEquals(response.taggedUsers().size(), 0);
    }

    @Test
    @DisplayName("다이어리 댓글 생성 실패-diary not found")
    void createComment_fail_DIARY_NOT_FOUND() {
        //given
        DiaryCommentRequest request = new DiaryCommentRequest("오늘은 산으로 산책을 갔어요", List.of("abc123", "dab456"));

        given(diaryRepository.findByIdAndIsDeletedFalse(anyLong()))
                .willReturn(Optional.empty());
        //when
        DiaryException exception = assertThrows(DiaryException.class, () -> diaryCommentService.createComment(user, 1L, 1L, request));
        //then
        assertEquals(DIARY_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("다이어리 댓글 생성 실패-diary not found-다이어리 pet id와 주어진 pet id가 다름")
    void createComment_fail_DIARY_NOT_FOUND_WhenGivenPetIdIsNotDiaryPetId() {
        //given
        DiaryCommentRequest request = new DiaryCommentRequest("오늘은 산으로 산책을 갔어요", List.of("abc123", "dab456"));

        given(diaryRepository.findByIdAndIsDeletedFalse(anyLong()))
                .willReturn(Optional.of(diary));
        //when
        DiaryException exception = assertThrows(DiaryException.class, () -> diaryCommentService.createComment(user, 2L, 1L, request));
        //then
        assertEquals(DIARY_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("다이어리 댓글 생성 실패-forbidden pet space")
    void createComment_fail_FORBIDDEN_PET_SPACE() {
        //given
        DiaryCommentRequest request = new DiaryCommentRequest("오늘은 산으로 산책을 갔어요", List.of("abc123", "dab456"));

        given(diaryRepository.findByIdAndIsDeletedFalse(anyLong()))
                .willReturn(Optional.of(diary));
        given(guardianRepository.existsByUserIdAndPetId(anyString(), anyLong()))
                .willReturn(false);
        //when
        DiaryException exception = assertThrows(DiaryException.class, () -> diaryCommentService.createComment(user, 1L, 1L, request));
        //then
        assertEquals(FORBIDDEN_PET_SPACE.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("다이어리 댓글 수정 성공")
    void updateComment_success() {
        //given
        DiaryCommentRequest request = new DiaryCommentRequest("오늘은 산으로 산책을 갔어요", List.of("abc123", "dab456"));

        DiaryComment diaryComment = DiaryComment.builder()
                .content("오늘은 바다로 산책을 갔어요")
                .taggedUsersIdNicknameMap(taggedUserIdNicknameMap)
                .diary(diary)
                .user(user)
                .build();
        given(diaryCommentRepository.findByIdAndPetIdAndIsDeletedFalse(anyLong(), anyLong()))
                .willReturn(Optional.of(diaryComment));
        given(userRepository.findByGuardianUsersByPetIdAndUserIdsContaining(anyLong(), anyList())).willReturn(List.of(userA));
        given(guardianRepository.existsByUserIdAndPetId(anyString(), anyLong()))
                .willReturn(true);
        //when
        diaryCommentService.updateComment(user, 1L, 1L, request);
        ArgumentCaptor<DiaryComment> diaryCommentArgumentCaptor = ArgumentCaptor.forClass(DiaryComment.class);
        //then
        assertEquals(diaryComment.getDiary(), diary);
        assertEquals(diaryComment.getUser(), user);
        assertEquals(diaryComment.getContent(), "오늘은 산으로 산책을 갔어요");
        assertEquals(diaryComment.getTaggedUsersIdNicknameMap().size(), 1);
        assertTrue(diaryComment.getTaggedUsersIdNicknameMap().containsKey("abc123"));
        assertFalse(diaryComment.getTaggedUsersIdNicknameMap().containsKey("dab456"));
        assertTrue(diaryComment.getTaggedUsersIdNicknameMap().containsValue("첫째누나"));
    }

    @Test
    @DisplayName("다이어리 댓글 수정 실패-diary comment not found")
    void updateComment_success_DIARY_COMMENT_NOT_FOUND() {
        //given
        DiaryCommentRequest request = new DiaryCommentRequest("오늘은 산으로 산책을 갔어요", List.of("abc123", "dab456"));

        given(diaryCommentRepository.findByIdAndPetIdAndIsDeletedFalse(anyLong(), anyLong()))
                .willReturn(Optional.empty());
        //when
        DiaryException exception = assertThrows(DiaryException.class, () -> diaryCommentService.updateComment(user, 1L, 1L, request));
        //then
        assertEquals(DIARY_COMMENT_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("다이어리 댓글 수정 실패-not diary comment owner")
    void updateComment_fail_NOT_DIARY_COMMENT_OWNER() {
        //given
        DiaryCommentRequest request = new DiaryCommentRequest("오늘은 산으로 산책을 갔어요", List.of("abc123", "dab456"));

        User otherUser = User.builder()
                .id("other-user").build();
        DiaryComment diaryComment = DiaryComment.builder()
                .content("오늘은 바다로 산책을 갔어요")
                .taggedUsersIdNicknameMap(taggedUserIdNicknameMap)
                .diary(diary)
                .user(otherUser)
                .build();
        given(diaryCommentRepository.findByIdAndPetIdAndIsDeletedFalse(anyLong(), anyLong()))
                .willReturn(Optional.of(diaryComment));
        //when
        DiaryException exception = assertThrows(DiaryException.class, () -> diaryCommentService.updateComment(user, 1L, 1L, request));
        //then
        assertEquals(NOT_DIARY_COMMENT_OWNER.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("다이어리 댓글 수정 실패-forbidden pet space")
    void updateComment_success_FORBIDDEN_PET_SPACE() {
        //given
        DiaryCommentRequest request = new DiaryCommentRequest("오늘은 산으로 산책을 갔어요", List.of("abc123", "dab456"));

        DiaryComment diaryComment = DiaryComment.builder()
                .content("오늘은 바다로 산책을 갔어요")
                .taggedUsersIdNicknameMap(taggedUserIdNicknameMap)
                .diary(diary)
                .user(user)
                .build();
        given(diaryCommentRepository.findByIdAndPetIdAndIsDeletedFalse(anyLong(), anyLong()))
                .willReturn(Optional.of(diaryComment));
        given(guardianRepository.existsByUserIdAndPetId(anyString(), anyLong()))
                .willReturn(false);
        //when
        DiaryException exception = assertThrows(DiaryException.class, () -> diaryCommentService.updateComment(user, 1L, 1L, request));
        //then
        assertEquals(FORBIDDEN_PET_SPACE.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("다이어리 댓글 삭제 성공")
    void deleteComment_success() {
        //given
        Diary diary = mock(Diary.class);
        DiaryComment diaryComment = DiaryComment.builder()
                .content("오늘은 바다로 산책을 갔어요")
                .taggedUsersIdNicknameMap(taggedUserIdNicknameMap)
                .diary(diary)
                .user(user)
                .build();
        given(diaryCommentRepository.findByIdAndPetIdAndIsDeletedFalse(anyLong(), anyLong()))
                .willReturn(Optional.of(diaryComment));
        given(guardianRepository.existsByUserIdAndPetId(anyString(), anyLong()))
                .willReturn(true);
        //when
        diaryCommentService.deleteComment(user, 1L, 1L);
        //then
        assertTrue(diaryComment.isDeleted());
    }

    @Test
    @DisplayName("다이어리 댓글 삭제 실패-comment not found")
    void deleteComment_success_DIARY_COMMENT_NOT_FOUND() {
        //given
        given(diaryCommentRepository.findByIdAndPetIdAndIsDeletedFalse(anyLong(), anyLong()))
                .willReturn(Optional.empty());
        //when
        DiaryException exception = assertThrows(DiaryException.class, () -> diaryCommentService.deleteComment(user, 1L, 1L));
        //then
        assertEquals(DIARY_COMMENT_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("다이어리 댓글 삭제 실패-diary comment not found")
    void deleteComment_fail_DIARY_COMMENT_NOT_FOUND() {
        //given
        User otherUser = User.builder()
                .id("other-user").build();
        DiaryComment diaryComment = DiaryComment.builder()
                .content("오늘은 바다로 산책을 갔어요")
                .taggedUsersIdNicknameMap(taggedUserIdNicknameMap)
                .diary(diary)
                .user(otherUser)
                .build();
        given(diaryCommentRepository.findByIdAndPetIdAndIsDeletedFalse(anyLong(), anyLong()))
                .willReturn(Optional.of(diaryComment));
        //when
        DiaryException exception = assertThrows(DiaryException.class, () -> diaryCommentService.deleteComment(user, 1L, 1L));
        //then
        assertEquals(NOT_DIARY_COMMENT_OWNER.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("다이어리 댓글 삭제 실패-forbidden pet space")
    void deleteComment_fail_FORBIDDEN_PET_SPACE() {
        //given
        DiaryComment diaryComment = DiaryComment.builder()
                .content("오늘은 바다로 산책을 갔어요")
                .taggedUsersIdNicknameMap(taggedUserIdNicknameMap)
                .diary(diary)
                .user(user)
                .build();
        given(diaryCommentRepository.findByIdAndPetIdAndIsDeletedFalse(anyLong(), anyLong()))
                .willReturn(Optional.of(diaryComment));
        given(guardianRepository.existsByUserIdAndPetId(anyString(), anyLong()))
                .willReturn(false);
        //when
        DiaryException exception = assertThrows(DiaryException.class, () -> diaryCommentService.deleteComment(user, 1L, 1L));
        //then
        assertEquals(FORBIDDEN_PET_SPACE.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("다이어리 댓글 조회 성공")
    void displayComments_success() {
        //given
        given(diaryRepository.findByIdAndIsDeletedFalse(anyLong()))
                .willReturn(Optional.of(diary));
        given(guardianRepository.existsByUserIdAndPetId(anyString(), anyLong()))
                .willReturn(true);
        given(diaryCommentRepository.findByDiaryAndAncestorCommentIdIsNullAndIsDeletedFalse(any(), any()))
                .willReturn(new SliceImpl<>(List.of(
                        DiaryComment.builder()
                                .content("우리 체리 귀엽다 ~ ^^")
                                .diary(diary)
                                .user(userA)
                                .taggedUsersIdNicknameMap(taggedUserIdNicknameMap)
                                .build(),
                        DiaryComment.builder()
                                .content("체리짱귀")
                                .diary(diary)
                                .user(user)
                                .taggedUsersIdNicknameMap(new HashMap<>())
                                .build())));
        //when
        Slice<DiaryCommentResponse> response = diaryCommentService.displayComments(user, 1L, 1L, 1, 10);
        //then
        assertEquals(response.getContent().get(0).content(), "우리 체리 귀엽다 ~ ^^");
        assertEquals(response.getContent().get(1).content(), "체리짱귀");
        assertEquals(response.getContent().get(0).writer().profilePath(), userA.getProfilePath());
        assertEquals(response.getContent().get(0).writer().nickname(), userA.getNickname());
        assertEquals(response.getContent().get(1).writer().nickname(), user.getNickname());
        assertEquals(response.getContent().get(1).writer().profilePath(), user.getProfilePath());
        assertFalse(response.getContent().get(0).writer().isCurrentUser());
        assertTrue(response.getContent().get(1).writer().isCurrentUser());
        assertFalse(response.getContent().get(0).taggedUsers().get(0).isCurrentUser());
        assertEquals(response.getContent().get(0).taggedUsers().get(0).id(), "ljf123");
        assertEquals(response.getContent().get(0).taggedUsers().get(0).nickname(), "둘째누나");
    }

    @Test
    @DisplayName("다이어리 댓글 조회 실패-diary not found")
    void displayComments_fail_DIARY_NOT_FOUND() {
        //given
        given(diaryRepository.findByIdAndIsDeletedFalse(anyLong()))
                .willReturn(Optional.empty());
        //when
        DiaryException exception = assertThrows(DiaryException.class, () -> diaryCommentService.displayComments(user, 1L, 1L, 1, 10));
        //then
        assertEquals(DIARY_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("다이어리 댓글 조회 실패-diary not found-다이어리 pet id와 주어진 pet id가 다름")
    void displayComments_fail_DIARY_NOT_FOUND_WhenGivenPetIdIsNotDiaryPetId() {
        //given
        given(diaryRepository.findByIdAndIsDeletedFalse(anyLong()))
                .willReturn(Optional.of(diary));
        //when
        DiaryException exception = assertThrows(DiaryException.class, () -> diaryCommentService.displayComments(user, 2L, 1L, 1, 10));
        //then
        assertEquals(DIARY_NOT_FOUND.getCode(), exception.getCode());
    }


    @Test
    @DisplayName("다이어리 댓글 조회 실패-forbidden pet space")
    void displayComments_fail_FORBIDDEN_PET_SPACE() {
        //given
        given(diaryRepository.findByIdAndIsDeletedFalse(anyLong()))
                .willReturn(Optional.of(diary));
        given(guardianRepository.existsByUserIdAndPetId(anyString(), anyLong()))
                .willReturn(false);
        //when
        DiaryException exception = assertThrows(DiaryException.class, () -> diaryCommentService.displayComments(user, 1L, 1L, 1, 10));
        //then
        assertEquals(FORBIDDEN_PET_SPACE.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("다이어리 댓글 좋아요 성공-좋아요 등록")
    void likeComment_success() {
        //given
        DiaryComment diaryComment = DiaryComment.builder()
                .content("오늘은 바다로 산책을 갔어요")
                .taggedUsersIdNicknameMap(taggedUserIdNicknameMap)
                .diary(diary)
                .user(user)
                .build();
        given(diaryCommentRepository.findByIdAndPetIdAndIsDeletedFalse(anyLong(), anyLong()))
                .willReturn(Optional.of(diaryComment));
        given(guardianRepository.existsByUserIdAndPetId(anyString(), anyLong()))
                .willReturn(true);
        given(diaryCommentRedisService.isDiaryCommentLikeExistByCommentIdAndUserId(anyLong(), anyString()))
                .willReturn(false);

        //when
        diaryCommentService.likeComment(user, 1L, 1L);
        //then
        verify(diaryCommentRedisService, times(1)).registerLikeByCommentIdAndUserId(anyLong(), anyString());
    }

    @Test
    @DisplayName("다이어리 댓글 좋아요 성공-좋아요 취소")
    void likeComment_success_WhenLikeAlreadyExists() {
        //given
        DiaryComment diaryComment = DiaryComment.builder()
                .content("오늘은 바다로 산책을 갔어요")
                .taggedUsersIdNicknameMap(taggedUserIdNicknameMap)
                .diary(diary)
                .user(user)
                .build();
        given(diaryCommentRepository.findByIdAndPetIdAndIsDeletedFalse(anyLong(), anyLong()))
                .willReturn(Optional.of(diaryComment));
        given(guardianRepository.existsByUserIdAndPetId(anyString(), anyLong()))
                .willReturn(true);
        given(diaryCommentRedisService.isDiaryCommentLikeExistByCommentIdAndUserId(anyLong(), anyString()))
                .willReturn(true);

        //when
        diaryCommentService.likeComment(user, 1L, 1L);
        //then
        verify(diaryCommentRedisService, times(1)).cancelLikeByCommentIdAndUserId(anyLong(), anyString());
    }

    @Test
    @DisplayName("다이어리 댓글 좋아요 실패-diary comment not found")
    void likeComment_fail_DIARY_COMMENT_NOT_FOUND() {
        //given
        given(diaryCommentRepository.findByIdAndPetIdAndIsDeletedFalse(anyLong(), anyLong()))
                .willReturn(Optional.empty());
        //when
        DiaryException exception = assertThrows(DiaryException.class, () -> diaryCommentService.likeComment(user, 1L, 1L));
        //then
        assertEquals(DIARY_COMMENT_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("다이어리 댓글 좋아요 실패-forbidden pet space")
    void likeComment_fail_FORBIDDEN_PET_SPACE() {
        //given
        DiaryComment diaryComment = DiaryComment.builder()
                .content("오늘은 바다로 산책을 갔어요")
                .taggedUsersIdNicknameMap(taggedUserIdNicknameMap)
                .diary(diary)
                .user(user)
                .build();
        given(diaryCommentRepository.findByIdAndPetIdAndIsDeletedFalse(anyLong(), anyLong()))
                .willReturn(Optional.of(diaryComment));
        given(guardianRepository.existsByUserIdAndPetId(anyString(), anyLong()))
                .willReturn(false);
        //when
        DiaryException exception = assertThrows(DiaryException.class, () -> diaryCommentService.likeComment(user, 1L, 1L));
        //then
        assertEquals(FORBIDDEN_PET_SPACE.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("다이어리 대댓글 생성 성공")
    void createReComment_success() {
        //given
        DiaryCommentRequest request = new DiaryCommentRequest("오늘은 산으로 산책을 갔어요", List.of("abc123", "dab456"));
        given(diaryCommentRepository.findByIdAndPetIdAndIsDeletedFalse(anyLong(), anyLong()))
                .willReturn(Optional.of(DiaryComment.builder()
                        .content("오늘은 바다로 산책을 갔어요")
                        .taggedUsersIdNicknameMap(taggedUserIdNicknameMap)
                        .diary(diary)
                        .user(user)
                        .build()));
        given(guardianRepository.existsByUserIdAndPetId(anyString(), anyLong()))
                .willReturn(true);
        given(userRepository.findByGuardianUsersByPetIdAndUserIdsContaining(anyLong(), anyList())).willReturn(List.of(userA));
        given(mockDiaryComment.getTaggedUsersIdNicknameMap()).willReturn(taggedUserIdNicknameMap);
        given(diaryCommentRepository.save(any()))
                .willReturn(mockDiaryComment);
        //when
        DiaryReCommentResponse response = diaryCommentService.createReComment(user, 1L, 1L, request);
        ArgumentCaptor<DiaryComment> diaryCommentArgumentCaptor = ArgumentCaptor.forClass(DiaryComment.class);
        //then
        verify(diaryCommentRepository, times(1)).save(diaryCommentArgumentCaptor.capture());
        assertEquals(diaryCommentArgumentCaptor.getValue().getDiary(), diary);
        assertEquals(diaryCommentArgumentCaptor.getValue().getUser(), user);
        assertEquals(diaryCommentArgumentCaptor.getValue().getContent(), "오늘은 산으로 산책을 갔어요");
        assertEquals(diaryCommentArgumentCaptor.getValue().getTaggedUsersIdNicknameMap().size(), 1);
        assertTrue(diaryCommentArgumentCaptor.getValue().getTaggedUsersIdNicknameMap().containsKey("abc123"));
        assertFalse(diaryCommentArgumentCaptor.getValue().getTaggedUsersIdNicknameMap().containsKey("dab456"));
        assertTrue(diaryCommentArgumentCaptor.getValue().getTaggedUsersIdNicknameMap().containsValue("첫째누나"));
        assertEquals(response.writer().id(), "abcde1234");
        assertEquals(response.writer().nickname(), "hi");
        assertFalse(response.isCurrentUserLiked());
        assertEquals(response.likeCount(), 0);
        assertEquals(response.content(), "오늘은 바다로 산책을 갔어요");
        assertEquals(response.taggedUsers().size(), 1);
        assertEquals(response.taggedUsers().get(0).nickname(), "둘째누나");
        assertEquals(response.taggedUsers().get(0).id(), "ljf123");
    }

    @Test
    @DisplayName("다이어리 대댓글 생성 실패-DIARY_COMMENT_NOT_FOUND")
    void createReComment_fail_DIARY_COMMENT_NOT_FOUND() {
        //given
        DiaryCommentRequest request = new DiaryCommentRequest("오늘은 산으로 산책을 갔어요", List.of("abc123", "dab456"));
        given(diaryCommentRepository.findByIdAndPetIdAndIsDeletedFalse(anyLong(), anyLong()))
                .willReturn(Optional.empty());
        //when
        DiaryException exception = assertThrows(DiaryException.class, () -> diaryCommentService.createReComment(user, 1L, 1L, request));
        //then
        assertEquals(DIARY_COMMENT_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("다이어리 대댓글 생성 실패-FORBIDDEN_PET_SPACE")
    void createReComment_fail_FORBIDDEN_PET_SPACE() {
        //given
        DiaryCommentRequest request = new DiaryCommentRequest("오늘은 산으로 산책을 갔어요", List.of("abc123", "dab456"));
        given(diaryCommentRepository.findByIdAndPetIdAndIsDeletedFalse(anyLong(), anyLong()))
                .willReturn(Optional.of(DiaryComment.builder()
                        .content("오늘은 바다로 산책을 갔어요")
                        .taggedUsersIdNicknameMap(taggedUserIdNicknameMap)
                        .diary(diary)
                        .user(user)
                        .build()));
        given(guardianRepository.existsByUserIdAndPetId(anyString(), anyLong()))
                .willReturn(false);
        //when
        DiaryException exception = assertThrows(DiaryException.class, () -> diaryCommentService.createReComment(user, 1L, 1L, request));
        //then
        assertEquals(FORBIDDEN_PET_SPACE.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("다이어리 대댓글 조회 성공")
    void displayReComments_success() {
        //given
        DiaryComment ancestor = DiaryComment.builder()
                .content("우리 체리 귀엽다")
                .user(userA)
                .diary(diary)
                .taggedUsersIdNicknameMap(new HashMap<>())
                .build();
        DiaryComment parent = DiaryComment.builder()
                .content("체리 진짜 귀엽지")
                .diary(diary)
                .parent(ancestor)
                .user(user)
                .taggedUsersIdNicknameMap(taggedUserIdNicknameMap)
                .build();
        given(diaryCommentRepository.findByAncestorCommentIdAndIsDeletedFalseOrderByIdDesc(anyLong()))
                .willReturn(List.of(
                        parent,
                        DiaryComment.builder()
                                .content("그니까 체리 짱귀")
                                .diary(diary)
                                .user(userA)
                                .parent(parent)
                                .taggedUsersIdNicknameMap(new HashMap<>())
                                .build()));//when
        List<DiaryReCommentResponse> responses = diaryCommentService.displayReComments(user, 1L, 1L);
        //then
        assertEquals(responses.get(0).content(), "체리 진짜 귀엽지");
        assertEquals(responses.get(1).content(), "그니까 체리 짱귀");
        assertEquals(responses.get(0).writer().profilePath(), user.getProfilePath());
        assertEquals(responses.get(0).writer().nickname(), user.getNickname());
        assertEquals(responses.get(1).writer().nickname(), userA.getNickname());
        assertEquals(responses.get(1).writer().profilePath(), userA.getProfilePath());
        assertTrue(responses.get(0).writer().isCurrentUser());
        assertFalse(responses.get(1).writer().isCurrentUser());
        assertEquals(responses.get(0).receiver().id(), userA.getId());
        assertEquals(responses.get(0).receiver().nickname(), userA.getNickname());
        assertEquals(responses.get(1).receiver().id(), user.getId());
        assertEquals(responses.get(1).receiver().nickname(), user.getNickname());
        assertFalse(responses.get(0).taggedUsers().get(0).isCurrentUser());
        assertEquals(responses.get(0).taggedUsers().get(0).id(), "ljf123");
        assertEquals(responses.get(0).taggedUsers().get(0).nickname(), "둘째누나");
    }
    

}