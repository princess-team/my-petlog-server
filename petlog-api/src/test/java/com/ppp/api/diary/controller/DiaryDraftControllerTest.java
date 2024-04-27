package com.ppp.api.diary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ppp.api.diary.dto.request.DiaryDraftCreateRequest;
import com.ppp.api.diary.service.DiaryDraftService;
import com.ppp.api.test.WithMockCustomUser;
import com.ppp.common.security.UserDetailsServiceImpl;
import com.ppp.common.security.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DiaryDraftController.class)
@AutoConfigureMockMvc(addFilters = false)
class DiaryDraftControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private DiaryDraftService diaryDraftService;

    private static final String TOKEN = "Bearer token";

    @Test
    @WithMockCustomUser
    @DisplayName("임시 저장 일기 생성 성공")
    void createDiaryDraft_success() throws Exception {
        //given
        DiaryDraftCreateRequest request = DiaryDraftCreateRequest.builder()
                .title("우리 강아지")
                .content("너무 귀엽당")
                .uploadedVideoIds(List.of("c8e8f796-8e29-4067-86c4-0eae419a054e"))
                .date(LocalDate.now().toString())
                .build();
        //when
        mockMvc.perform(multipart("/api/v1/pets/{petId}/diaries/drafts", 1L)
                        .file(new MockMultipartFile("request", "json",
                                MediaType.APPLICATION_JSON_VALUE,
                                objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)))
                        .file(new MockMultipartFile("images", "image.jpg",
                                MediaType.IMAGE_JPEG_VALUE, "abcde".getBytes()))
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                ).andDo(print())
                .andExpect(status().isOk());
        //then
    }

    @Test
    @WithMockCustomUser
    @DisplayName("임시 저장 일기 존재 여부 조회 성공")
    void checkHasDiaryDraft_success() throws Exception {
        //given
        //when
        mockMvc.perform(get("/api/v1/pets/{petId}/diaries/drafts/check", 1L)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                ).andDo(print())
                .andExpect(status().isOk());
        //then
    }

    @Test
    @WithMockCustomUser
    @DisplayName("임시 저장 일기 조회 성공")
    void retrieveDiaryDraft_success() throws Exception {
        //given
        //when
        mockMvc.perform(get("/api/v1/pets/{petId}/diaries/drafts", 1L)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                ).andDo(print())
                .andExpect(status().isOk());
        //then
    }

    @Test
    @WithMockCustomUser
    @DisplayName("임시 저장 일기 삭제 성공")
    void deleteDiaryDraft_success() throws Exception {
        //given
        //when
        mockMvc.perform(get("/api/v1/pets/{petId}/diaries/drafts", 1L)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                ).andDo(print())
                .andExpect(status().isOk());
        //then
    }

}