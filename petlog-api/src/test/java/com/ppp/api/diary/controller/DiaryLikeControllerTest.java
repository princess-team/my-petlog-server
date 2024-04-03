package com.ppp.api.diary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ppp.api.diary.service.DiaryLikeService;
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
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DiaryLikeController.class)
@AutoConfigureMockMvc(addFilters = false)
class DiaryLikeControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private DiaryLikeService diaryLikeService;

    private static final String TOKEN = "Bearer token";

    @Test
    @WithMockCustomUser
    @DisplayName("일기 좋아요 성공")
    void likeDiary_success() throws Exception {
        mockMvc.perform(post("/api/v1/pets/{petId}/diaries/{diaryId}/like", 1L, 1L)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("일기 좋아요 유저 조회 성공")
    void retrieveDiaryLikeUsers_success() throws Exception {
        mockMvc.perform(get("/api/v1/pets/diaries/{diaryId}/like", 1L, 1L)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isOk());
    }
}