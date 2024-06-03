package com.ppp.api.diary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ppp.api.diary.service.DiarySearchService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DiarySearchController.class)
@AutoConfigureMockMvc(addFilters = false)
class DiarySearchControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private DiarySearchService diarySearchService;

    private static final String TOKEN = "Bearer token";

    @Test
    @WithMockCustomUser
    @DisplayName("일기 리스트 조회 성공")
    void displayDiaries_success() throws Exception {
        //given
        //when
        mockMvc.perform(get("/api/v1/pets/{petId}/diaries/search", 1L)
                        .param("keyword", "강아지")
                        .param("page", "1")
                        .param("size", "10")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                ).andDo(print())
                .andExpect(status().isOk());
        //then
    }

    @Test
    @WithMockCustomUser
    @DisplayName("일기에서 자주 사용한 용어 조회")
    void findMostUsedTermsByPetId_success() throws Exception {
        //given
        //when
        mockMvc.perform(get("/api/v1/pets/{petId}/diaries/search/terms", 1L)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                ).andDo(print())
                .andExpect(status().isOk());
        //then
    }

    @Test
    @WithMockCustomUser
    @DisplayName("일기 리스트 조회 성공")
    void searchInFeed_success() throws Exception {
        //given
        //when
        mockMvc.perform(get("/api/v1/pets/diaries/search")
                        .param("keyword", "강아지")
                        .param("page", "1")
                        .param("size", "10")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                ).andDo(print())
                .andExpect(status().isOk());
        //then
    }
}