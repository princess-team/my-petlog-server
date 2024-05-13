package com.ppp.api.auth.service;


import com.ppp.api.auth.dto.request.RegisterRequest;
import com.ppp.api.auth.dto.request.SigninRequest;
import com.ppp.api.auth.dto.request.SocialRequest;
import com.ppp.api.auth.dto.response.AuthenticationResponse;
import com.ppp.api.auth.exception.AuthException;
import com.ppp.api.auth.exception.ErrorCode;
import com.ppp.api.email.service.EmailService;
import com.ppp.api.user.exception.NotFoundUserException;
import com.ppp.common.client.RedisClient;
import com.ppp.common.security.jwt.JwtTokenProvider;
import com.ppp.domain.email.EmailVerification;
import com.ppp.domain.email.repository.EmailVerificationRepository;
import com.ppp.domain.user.User;
import com.ppp.domain.user.constant.Role;
import com.ppp.domain.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

import static com.ppp.api.user.exception.ErrorCode.NOT_FOUND_USER;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final RedisClient redisClient;

    private final EmailService emailService;

    private final EmailVerificationRepository emailVerificationRepository;


    public void signup(RegisterRequest registerRequest) {
        if(userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new AuthException(ErrorCode.EXISTS_EMAIL);
        }

        String rawPwd= registerRequest.getPassword();
        String encPwd = encodePassword(rawPwd);

        User newUser = User.createUserByEmail(registerRequest.getEmail(), encPwd, Role.USER);

        userRepository.save(newUser);
    }

    public AuthenticationResponse signin(SigninRequest signinRequest) {
        String email = signinRequest.getEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundUserException(NOT_FOUND_USER));

        if (checkPasswordMatches(signinRequest.getPassword(), user.getPassword())) {
            String accessToken = jwtTokenProvider.generateAccessToken(user);
            String refreshToken = jwtTokenProvider.generateRefreshToken(user);

            redisClient.setValues(email, refreshToken, Duration.ofMillis(jwtTokenProvider.getRefreshExpiration(refreshToken)));

            return AuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        }

        throw new AuthException(ErrorCode.NOTMATCH_PASSWORD);
    }

    public AuthenticationResponse socialLogin(SocialRequest socialRequest) {
        User user = userRepository.findByEmail(socialRequest.getEmail())
                .orElseGet(() -> userRepository.save(User.createUserByEmail(socialRequest.getEmail(), Role.USER))
        );

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        redisClient.setValues(user.getEmail(), refreshToken, Duration.ofMillis(jwtTokenProvider.getRefreshExpiration(refreshToken)));

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthenticationResponse regenerateToken(String refreshToken) {
        jwtTokenProvider.validateRefreshToken(refreshToken);
        return generateNewToken(refreshToken);
    }

    private AuthenticationResponse generateNewToken(String refreshToken) {
        String email = jwtTokenProvider.getUserEmailFromRefreshToken(refreshToken);

        String refreshInRedis = redisClient.getValues(email);
        // 없을 경우 -> 로그아웃된 사용자는 재발급 x
        if (Objects.isNull(refreshInRedis) || !refreshInRedis.equals(refreshToken))
            throw new AuthException(ErrorCode.NOT_FOUND_TOKEN);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(NOT_FOUND_USER.getMessage()));
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public void logout(String accessToken) {
        Long accessTokenExpiration = jwtTokenProvider.getAccessExpiration(accessToken);
        String email = jwtTokenProvider.getUserEmailFromAccessToken(accessToken);
        if (redisClient.getValues(email) != null) redisClient.deleteValues(email);

        redisClient.setValues(accessToken, "logout", Duration.ofMillis(accessTokenExpiration));
    }

    public boolean checkPasswordMatches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public String encodePassword(String rowPassword) {
        return passwordEncoder.encode(rowPassword);
    }

    public void setHeaderAccessToken(HttpServletResponse response, String accessToken) {
        Long accessExpiration = jwtTokenProvider.getAccessExpiration(accessToken);
        Cookie cookie = new Cookie("accessToken", accessToken);
        cookie.setMaxAge(accessExpiration.intValue());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setSecure(true);
        cookie.setAttribute("SameSite", "None");
        response.addCookie(cookie);
    }

    public void setHeaderRefreshToken(HttpServletResponse response, String refreshToken) {
        Long refreshExpiration = jwtTokenProvider.getRefreshExpiration(refreshToken);
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setMaxAge(refreshExpiration.intValue());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setSecure(true);
        cookie.setAttribute("SameSite", "None");
        response.addCookie(cookie);
    }

    @Transactional
    public void sendEmailCodeForm(String email) {
        checkDuplicatedEmail(email);
        emailService.sendEmailCodeForm(email);
    }

    private void checkDuplicatedEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new AuthException(ErrorCode.EXISTS_EMAIL);
        }
    }

    public void verifiedCode(String email, int verificationCode) {
        EmailVerification emailVerification = emailVerificationRepository.findByEmailAndVerificationCode(email, verificationCode).orElseThrow(() -> new AuthException(ErrorCode.VERIFICATION_CODE_NOT_MATCHED));
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiredAt = emailVerification.getExpiredAt();
        long minutesElapsed = Duration.between(expiredAt, now).toMinutes();

        if (minutesElapsed > 0) {
            throw new AuthException(ErrorCode.CODE_EXPIRATION);
        }
    }
}
