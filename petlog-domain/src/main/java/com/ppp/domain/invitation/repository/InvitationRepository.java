package com.ppp.domain.invitation.repository;

import com.ppp.domain.invitation.Invitation;
import com.ppp.domain.invitation.constant.InviteStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    Optional<Invitation> findFirstByInviteeIdAndPetIdOrderByCreatedAtDesc(String inviteeId, Long petId);

    List<Invitation> findByInviteeIdAndInviteStatus(String inviteeId, InviteStatus inviteStatus);

    @EntityGraph(attributePaths = {"pet"}, type = EntityGraph.EntityGraphType.FETCH)
    @Query("SELECT i FROM Invitation i JOIN i.pet p WHERE i.id = :invitationId AND i.inviteStatus = :inviteStatus AND i.inviteeId = :inviteeId")
    Optional<Invitation> findInvitaionAndPetByIdAndInviteStatusAndInviteeId(@Param("invitationId") Long invitationId, @Param("inviteStatus") InviteStatus inviteStatus, @Param("inviteeId") String inviteeId);

    Optional<Invitation> findByIdAndInviteStatusAndInviterId(Long invitationId, InviteStatus inviteStatus, String inviteeId);
}
