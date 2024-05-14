package com.ppp.api.auth.service;

import com.ppp.api.auth.dto.request.RegisterRequest;
import com.ppp.api.auth.dto.request.SigninRequest;
import com.ppp.api.auth.dto.response.AuthenticationResponse;
import com.ppp.api.auth.exception.AuthException;
import com.ppp.api.email.service.EmailService;
import com.ppp.common.client.RedisClient;
import com.ppp.common.security.jwt.JwtTokenProvider;
import com.ppp.domain.email.EmailVerification;
import com.ppp.domain.email.repository.EmailVerificationRepository;
import com.ppp.domain.user.User;
import com.ppp.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    JwtTokenProvider jwtTokenProvider;
    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    RedisClient redisClient;
    @Mock
    EmailService emailService;
    @Mock
    EmailVerificationRepository emailVerificationRepository;
    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("회원가입")
    void signup() {
        //given
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("j2@gmail.com")
                .password("password")
                .build();

        //when
        authService.signup(registerRequest);
    }

    @Test
    @DisplayName("로그인")
    void signin() {
        //given
        SigninRequest signinRequest = SigninRequest.builder()
                .email("j2@gmail.com")
                .password("password")
                .build();

        User mockUser = User.builder()
                .email(signinRequest.getEmail())
                .password(passwordEncoder.encode(signinRequest.getPassword()))
                .build();

        //when
        when(userRepository.findByEmail(signinRequest.getEmail())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(signinRequest.getPassword(), mockUser.getPassword())).thenReturn(true);

        String mockAccessToken = "mockAccessToken";
        String mockRefreshToken = "mockRefreshToken";

        when(jwtTokenProvider.generateAccessToken(mockUser)).thenReturn(mockAccessToken);
        when(jwtTokenProvider.generateRefreshToken(mockUser)).thenReturn(mockRefreshToken);


        AuthenticationResponse authenticationResponse = authService.signin(signinRequest);

        assertEquals(mockAccessToken, authenticationResponse.getAccessToken());
        assertEquals(mockRefreshToken, authenticationResponse.getRefreshToken());
    }

    @Test
    @DisplayName("로그아웃")
    void logout() {
        //given
        String mockAccessToken = "mockAccessToken";
        String mockUserEmail = "test@example.com";
        Long mockAccessTokenExpiration = System.currentTimeMillis() + Duration.ofMinutes(30).toMillis();

        when(jwtTokenProvider.getAccessExpiration(mockAccessToken)).thenReturn(mockAccessTokenExpiration);
        when(jwtTokenProvider.getUserEmailFromAccessToken(mockAccessToken)).thenReturn(mockUserEmail);

        when(redisClient.getValues(mockUserEmail)).thenReturn("mockRefreshToken");

        //when
        authService.logout(mockAccessToken);

        //then
        verify(redisClient, times(1)).deleteValues(mockUserEmail);
        verify(redisClient, times(1)).setValues(mockAccessToken, "logout", Duration.ofMillis(mockAccessTokenExpiration));
    }

    @Test
    @DisplayName("인증코드 전송 - 최초 전송")
    void sendMessage_first() {
        //given
        String email = "test@test.com";

        //when
        when(userRepository.existsByEmail(email)).thenReturn(false);

        //then
        assertDoesNotThrow(() -> authService.sendEmailCodeForm(email));
    }

    @Test
    @DisplayName("인증코드 검증")
    void verify_Authentication_EmailCode() {
        //given
        String email = "test@test.com";
        EmailVerification emailVerification = EmailVerification.createVerification(email, 123456, 60000);

        //when
        when(emailVerificationRepository.findByEmailAndVerificationCode(email, 123456)).thenReturn(Optional.ofNullable(emailVerification));
        LocalDateTime verificationTime = emailVerification.getExpiredAt();
        LocalDateTime now = LocalDateTime.now().plusMinutes(5);
        long minutesElapsed = Duration.between(verificationTime, now).toMinutes();

        //then
        assertTrue(minutesElapsed < 10);
        assertDoesNotThrow(() -> authService.verifiedCode(email, 123456));
    }

    @Test
    @DisplayName("인증코드 검증 -  만료시간 초과시 예외 발생")
    void verification_Email_AuthException() {
        //given
        String email = "test@test.com";
        int verificationCode = 123456;
        EmailVerification emailVerification = new EmailVerification();
        emailVerification.setExpirationDate(LocalDateTime.now().minusMinutes(11));

        //when
        when(emailVerificationRepository.findByEmailAndVerificationCode(email, verificationCode)).thenReturn(Optional.ofNullable(emailVerification));

        //then
        assertThrows(AuthException.class, () -> authService.verifiedCode(email, verificationCode), "만료시간이 지났으므로 CODE_EXPIRATION 예외가 발생해야 합니다.");
    }
}