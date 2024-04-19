package com.ppp.api.invitation.service;

import com.ppp.api.guardian.service.GuardianService;
import com.ppp.api.invitation.dto.request.InvitationRequest;
import com.ppp.api.invitation.dto.request.RegisterInvitationRequest;
import com.ppp.api.invitation.dto.response.InvitationResponse;
import com.ppp.api.invitation.dto.response.MyInvitationResponse;
import com.ppp.api.invitation.exception.ErrorCode;
import com.ppp.api.invitation.exception.InvitationException;
import com.ppp.api.notification.dto.event.InvitationNotificationEvent;
import com.ppp.api.pet.exception.PetException;
import com.ppp.common.util.TimeUtil;
import com.ppp.domain.guardian.constant.GuardianRole;
import com.ppp.domain.invitation.Invitation;
import com.ppp.domain.invitation.constant.InviteStatus;
import com.ppp.domain.invitation.dto.MyInvitationDto;
import com.ppp.domain.invitation.repository.InvitationQuerydslRepository;
import com.ppp.domain.invitation.repository.InvitationRepository;
import com.ppp.domain.notification.constant.MessageCode;
import com.ppp.domain.pet.Pet;
import com.ppp.domain.pet.PetImage;
import com.ppp.domain.pet.repository.PetImageRepository;
import com.ppp.domain.pet.repository.PetRepository;
import com.ppp.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.ppp.api.pet.exception.ErrorCode.PET_NOT_FOUND;


@Slf4j
@Service
@RequiredArgsConstructor
public class InvitationService {
    private final InvitationRepository invitationRepository;
    private final GuardianService guardianService;
    private final PetRepository petRepository;
    private final PetImageRepository petImageRepository;
    private final InvitationQuerydslRepository invitationQuerydslRepository;
    private final ApplicationEventPublisher applicationEventPublisher;


    @Transactional(readOnly = true)
    public List<InvitationResponse> displayInvitations(User user) {
        List<InvitationResponse> invitationResponses = new ArrayList<>();
        List<Invitation> invitations = invitationRepository.findByInviteeIdAndInviteStatus(user.getId(), InviteStatus.PENDING);
        for (Invitation invitation : invitations) {
            Pet pet = invitation.getPet();
            PetImage petImage = petImageRepository.findByPet(pet).orElse(new PetImage());

            InvitationResponse invitationResponse = InvitationResponse.builder()
                    .invitationId(invitation.getId())
                    .inviteStatus(invitation.getInviteStatus().name())
                    .invitedAt(TimeUtil.calculateTerm(invitation.getCreatedAt()))
                    .petId(pet.getId())
                    .petName(pet.getName())
                    .petImageUrl(petImage.getUrl())
                    .build();
            invitationResponses.add(invitationResponse);
        }
        return invitationResponses;
    }

    @Transactional
    public void acceptInvitation(InvitationRequest invitationRequest, User user) {
        Invitation invitation = updateInvitationByInvitee(invitationRequest.getInvitationId(), user, InviteStatus.PENDING, InviteStatus.ACCEPTED);
        Pet pet = invitation.getPet();
        guardianService.createGuardian(pet, user, GuardianRole.MEMBER);

        applicationEventPublisher.publishEvent(
                new InvitationNotificationEvent(MessageCode.INVITATION_ACCEPT, user, invitation.getInviterId(), pet));
    }

    @Transactional
    public void refuseInvitation(InvitationRequest invitationRequest, User user) {
        Invitation invitation = updateInvitationByInvitee(invitationRequest.getInvitationId(), user, InviteStatus.PENDING, InviteStatus.REJECTED);
        Pet pet = invitation.getPet();

        applicationEventPublisher.publishEvent(
                new InvitationNotificationEvent(MessageCode.INVITATION_REJECT, user, invitation.getInviterId(), pet));
    }

    private Invitation updateInvitationByInvitee(Long invitationId, User user, InviteStatus fromStatus, InviteStatus toStatus) {
        Invitation invitation = invitationRepository.findByIdAndInviteStatusAndInviteeId(invitationId, fromStatus, user.getId())
                .orElseThrow(() -> new InvitationException(ErrorCode.INVITATION_NOT_FOUND));
        invitation.updateInviteStatus(toStatus);
        return invitation;
    }

    private void updateInvitationByInviter(Long invitationId, String userId, InviteStatus fromStatus, InviteStatus toStatus) {
        Invitation invitation = invitationRepository.findByIdAndInviteStatusAndInviterId(invitationId, fromStatus, userId)
                .orElseThrow(() -> new InvitationException(ErrorCode.INVITATION_NOT_FOUND));
        invitation.updateInviteStatus(toStatus);
    }

    public List<MyInvitationResponse> displayMyInvitations(Long petId, User user) {
        List<MyInvitationDto> myInvitationDtoResponses = invitationQuerydslRepository.findMyInvitationByInviterId(petId, user.getId());
        List<MyInvitationResponse> myInvitationResponses = new ArrayList<>();
        myInvitationDtoResponses.forEach(myInvitationDto ->
            myInvitationResponses.add(MyInvitationResponse.from(myInvitationDto))
        );
        return myInvitationResponses;
    }

    @Transactional
    public void cancelInvitation(InvitationRequest invitationRequest, User user) {
        updateInvitationByInviter(invitationRequest.getInvitationId(), user.getId(), InviteStatus.PENDING, InviteStatus.CANCELED);
    }

    public void registerInvitation(RegisterInvitationRequest registerInvitationRequest, User user) {
        Pet pet = petRepository.findByInvitedCodeAndIsDeletedFalse(registerInvitationRequest.getInviteCode())
                .orElseThrow(() -> new PetException(PET_NOT_FOUND));
        guardianService.createGuardian(pet, user, GuardianRole.MEMBER);
    }

    @Transactional
    public void confirmRejectedInvitation(InvitationRequest invitationRequest, User user) {
        updateInvitationByInviter(invitationRequest.getInvitationId(), user.getId(), InviteStatus.REJECTED, InviteStatus.CONFIRMED);
    }
}
