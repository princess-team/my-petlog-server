package com.ppp.api.auth.controller;

import com.ppp.api.auth.dto.request.RegisterRequest;
import com.ppp.api.auth.dto.request.SigninRequest;
import com.ppp.api.auth.dto.request.SocialRequest;
import com.ppp.api.auth.dto.response.AuthenticationResponse;
import com.ppp.api.auth.service.AuthService;
import com.ppp.api.exception.ExceptionResponse;
import com.ppp.api.user.dto.request.EmailRequest;
import com.ppp.api.user.dto.request.EmailVerificationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "Auth APIs")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "409", description = "이메일 중복", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Invalid password", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))}),
    })
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.signup(registerRequest);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "로그인")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationResponse.class)) }),
            @ApiResponse(responseCode = "404", description = "User not found", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Invalid password", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))}),
    })
    @PostMapping("/signin")
    public ResponseEntity<AuthenticationResponse> signin(@Valid @RequestBody SigninRequest signinRequest, HttpServletResponse response) {
        AuthenticationResponse authenticationResponse = authService.signin(signinRequest);
        authService.setHeaderAccessToken(response, authenticationResponse.getAccessToken());
        authService.setHeaderRefreshToken(response, authenticationResponse.getRefreshToken());

        response.addHeader("Authorization", "Bearer " + authenticationResponse.getAccessToken());
        response.addHeader("refreshToken", authenticationResponse.getRefreshToken());
        return ResponseEntity.ok(authenticationResponse);
    }

    @Operation(summary = "소셜로그인", description = "소셜 로그인 성공시 해당 유저를 회원가입/로그인 시켜 토큰을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationResponse.class))}),
            @ApiResponse(responseCode = "409", description = "이미 사용 중인 이메일 주소입니다.", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))})
    })
    @PostMapping("/login/social")
    public ResponseEntity<AuthenticationResponse> socialLogin(@RequestBody SocialRequest socialRequest) {
        return ResponseEntity.ok(authService.socialLogin(socialRequest));
    }

    @Operation(summary = "로그아웃")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "401", description = "Invalid token", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))})
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("accessToken") String accessToken) {
        authService.logout(accessToken);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Renew Access Token", description = "Refresh token을 통해 Access Token을 갱신합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationResponse.class)) }),
            @ApiResponse(responseCode = "401", description = "Invalid token", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "404", description = "User not found", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))}),
    })
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponse> regenerateToken(@RequestHeader("refreshToken") String refreshToken) {
        return ResponseEntity.ok(authService.regenerateToken(refreshToken));
    }

    @Operation(summary = "인증코드 전송", description = "해당 이메일에 인증코드 6자리를 전송합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "400", description = "10분이 지나지 않았습니다.", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "409", description = "이미 사용 중인 이메일 주소입니다.", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "500", description = "이메일 전송에 실패하였습니다.", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))}),
    })
    @PostMapping("/emails/verification-requests")
    public ResponseEntity<Void> sendMessage(@Valid @RequestBody EmailRequest emailRequest) {
        authService.sendEmailCodeForm(emailRequest.getEmail());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "인증코드 확인", description = "10분 내에 이메일과 인증코드를 비교하여 검증합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "400", description = "인증번호의 유효기간이 만료되었습니다.", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))}),
            @ApiResponse(responseCode = "404", description = "인증번호가 일치하지 않습니다.", content = {@Content(schema = @Schema(implementation = ExceptionResponse.class))}),
    })
    @PostMapping("/emails/verifications")
    public ResponseEntity<Void> verificationEmail(@Valid @RequestBody EmailVerificationRequest emailVerificationRequest) {
        authService.verifiedCode(emailVerificationRequest.getEmail(), emailVerificationRequest.getCode());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
