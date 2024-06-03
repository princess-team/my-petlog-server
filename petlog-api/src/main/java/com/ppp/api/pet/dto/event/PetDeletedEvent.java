package com.ppp.api.pet.dto.event;

import lombok.Getter;

@Getter
public class PetDeletedEvent {
    private final Long petId;

    public PetDeletedEvent(Long petId) {
        this.petId = petId;
    }
}
