package com.ppp.api.email.service;

import com.ppp.api.auth.exception.AuthException;
import com.ppp.api.auth.exception.ErrorCode;
import com.ppp.api.email.dto.event.EmailSentEvent;
import com.ppp.domain.email.EmailVerification;
import com.ppp.domain.email.repository.EmailVerificationRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSender {
    private final EmailVerificationRepository emailVerificationRepository;

    private final JavaMailSender javaMailSender;

    private final ApplicationEventPublisher applicationEventPublisher;

    private static final String senderEmail = "mypetlog.auth@gmail.com";
    private static final String emailAuthTitle = "[마이펫로그] 이메일 인증 코드를 확인해 주세요";
    private static final long codeExpirationMillis = 90000;

    private MimeMessage createEmailCodeForm(String mail, int number) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            message.setFrom(senderEmail);
            message.setSubject(emailAuthTitle);
            message.setRecipients(MimeMessage.RecipientType.TO, mail);
            String body =
                    "<div style=\"letter-spacing: -0.025em; padding: 28px; max-width: 374px; margin: 16px auto; border: 1px solid rgba(0,0,0,.06); border-radius: 8px;\">\n" +
                            "\n" +
                            "    <div style=\"letter-spacing: -0.025em; padding: 13px 0; height: 38px;\">\n" +
                            "    </div>\n" +
                            "<div style=\"letter-spacing: -0.025em; box-sizing: border-box; display: block; Margin: 0 auto;\">\n" +
                            "    <h1 style=\"letter-spacing: -0.025em; margin-bottom: 30px; width: 100%; font-size: 28px; line-height: 38px; color: #1D1D1D; font-weight: bold; margin: 16px 20px 0 0; white-space: pre-line; word-break: break-word; padding-bottom: 24px;\">이메일 인증 코드를\n확인해 주세요</h1>\n" +
                            "    <div style=\"letter-spacing: -0.025em; color: #545454; font-size: 15px; line-height: 24px;\">\n" +
                            "        <p style=\"letter-spacing: -0.025em; font-size: 15px; font-weight: 400; font-style: normal; margin: 0; padding-bottom: 8px; color: #555555; white-space: pre-line;\">아래 인증 코드를 회원가입 페이지에 입력해 주세요</p>\n" +
                            "        <div style=\"letter-spacing: -0.025em; padding: 40px 0 20px 0;\">\n" +
                            "            <section style=\"letter-spacing: -0.025em; background: rgba(153, 153, 153, 0.15); border-radius: 8px; font-weight: 500; font-size: 24px; line-height: 30px; text-align: center; padding: 16px 32px;\">"
                            +number+
                            "</section>\n" +
                            "        </div>\n" +
                            "    </div>\n" +
                            "    <div style=\"letter-spacing: -0.025em; margin: 0; padding: 0; word-break: break-word;\">\n" +
                            "        <ul style=\"letter-spacing: -0.025em; font-size: 15px; font-weight: 400; font-style: normal; padding: 8px 0 4px 20px; margin: 0;\"><li style=\"letter-spacing: -0.025em; margin-left: 5px; line-height: 20px; margin: 0 0 4px 0; list-style-position: outside; color: #999999; font-weight: 400; font-size: 13px;\">인증 코드는 10분 동안 유효합니다</li><li style=\"letter-spacing: -0.025em; margin-left: 5px; line-height: 20px; margin: 0 0 4px 0; list-style-position: outside; color: #999999; font-weight: 400; font-size: 13px;\">본 메일은 발신 전용입니다</li></ul>\n" +
                            "    </div>\n" +
                            "</div>\n" +
                            "</div>";

            message.setText(body,"UTF-8", "html");
        } catch (Exception e) {
            log.error("MailService.sendEmail exception occur toEmail: {}", mail);
            throw new AuthException(ErrorCode.SEND_EMAIL_FAILURE);
        }
        return message;
    }

    private int createAuthNumber() {
        return (int)(Math.random() * (900000)) + 100000;
    }

    @Transactional
    public void sendEmailCodeForm(String email) {
        int code = createAuthNumber();
        emailVerificationRepository.findByEmail(email)
                .ifPresentOrElse(verification -> {
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime expiredAt = verification.getExpiredAt();
                    long minutesElapsed = Duration.between(expiredAt, now).toMinutes();

                    if (minutesElapsed < 0) {
                        throw new AuthException(ErrorCode.UNABLE_TO_SEND_EMAIL);
                    } else {
                        verification.update(code ,LocalDateTime.now(), codeExpirationMillis);
                        applicationEventPublisher.publishEvent(new EmailSentEvent(createEmailCodeForm(email, code)));
                    }
                }, () -> {
                    emailVerificationRepository.save(EmailVerification.createVerification(email, code, codeExpirationMillis));
                    applicationEventPublisher.publishEvent(new EmailSentEvent(createEmailCodeForm(email, code)));
                });
    }
}