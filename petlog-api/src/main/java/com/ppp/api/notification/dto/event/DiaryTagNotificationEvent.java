package com.ppp.api.notification.dto.event;

import com.ppp.domain.notification.constant.MessageCode;
import com.ppp.domain.pet.Pet;
import com.ppp.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class DiaryTagNotificationEvent {
    private MessageCode messageCode;
    private User actor;
    private Pet pet;
    private List<String> taggedIds;
}