package com.ppp.domain.subscription;


import com.ppp.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(indexes = {
        @Index(name = "idx_petId_userId", columnList = "petId, userId")
})
public class SubscriptionLog extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long petId;

    @Column(nullable = false, length = 20)
    private String userId;

    public SubscriptionLog(Long petId, String userId) {
        this.petId = petId;
        this.userId = userId;
    }
}
