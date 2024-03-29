package com.ppp.api.subscription.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ppp.api.subscription.dto.request.SubscriberBlockRequest;
import com.ppp.api.subscription.service.SubscriptionService;
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

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SubscriptionController.class)
@AutoConfigureMockMvc(addFilters = false)
class SubscriptionControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UserDetailsServiceImpl userDetailsService;
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean
    private SubscriptionService subscriptionService;
    private static final String TOKEN = "Bearer token";

    @Test
    @WithMockCustomUser
    @DisplayName("구독 및 구독 취소 성공")
    void subscribe_success() throws Exception {
        //given
        //when
        mockMvc.perform(post("/api/v1/pets/{petId}/subscriptions", 1L)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                ).andDo(print())
                .andExpect(status().isOk());
        //then
    }

    @Test
    @WithMockCustomUser
    @DisplayName("구독중인 펫 계정 조회 성공")
    void displayMySubscribingPets_success() throws Exception {
        //given
        //when
        mockMvc.perform(get("/api/v1/pets/subscriptions")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                ).andDo(print())
                .andExpect(status().isOk());
        //then
    }

    @Test
    @WithMockCustomUser
    @DisplayName("구독자 리스트 조회")
    void displayMyPetsSubscribers_success() throws Exception {
        //given
        //when
        mockMvc.perform(get("/api/v1/pets/{petId}/subscriptions", 1L)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                ).andDo(print())
                .andExpect(status().isOk());
        //then
    }

    @Test
    @WithMockCustomUser
    @DisplayName("구독자 차단 성공")
    void updateComment_success() throws Exception {
        //given
        SubscriberBlockRequest request = new SubscriberBlockRequest("abcde1234");
        //when
        mockMvc.perform(put("/api/v1/pets/{petId}/subscriptions", 1L)
                        .content(objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8))
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                ).andDo(print())
                .andExpect(status().isOk());
        //then
    }
}