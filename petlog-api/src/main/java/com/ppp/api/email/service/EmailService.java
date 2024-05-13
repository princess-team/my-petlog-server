package com.ppp.api.email.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final EmailSender emailSender;

    public void sendEmailCodeForm(String email) {
        emailSender.sendEmailCodeForm(email);
    }
}