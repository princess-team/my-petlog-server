package com.ppp.domain.subscription;

import com.ppp.domain.common.BaseTimeEntity;
import com.ppp.domain.pet.Pet;
import com.ppp.domain.subscription.constant.Status;
import com.ppp.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.LAZY;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(indexes = {
        @Index(name = "idx_subscriber_id", columnList = "subscriber_id")
})
public class Subscription extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "subscriber_id", nullable = false)
    private User subscriber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    public boolean isBlocked() {
        return Status.BLOCK.equals(status);
    }

    public void switchBlockStatus() {
        if (isBlocked())
            status = Status.ACTIVE;
        else status = Status.BLOCK;
    }

    @Builder
    public Subscription(Pet pet, User subscriber, Status status) {
        this.pet = pet;
        this.subscriber = subscriber;
        this.status = status;
    }
}
