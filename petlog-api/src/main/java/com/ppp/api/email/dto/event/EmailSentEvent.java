package com.ppp.api.email.dto.event;

import jakarta.mail.internet.MimeMessage;
import lombok.Getter;

@Getter
public class EmailSentEvent {
    private MimeMessage emailCodeForm;

    public EmailSentEvent(MimeMessage emailCodeForm) {
        this.emailCodeForm = emailCodeForm;
    }
}