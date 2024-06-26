package org.changppo.account.entity.payment;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.changppo.account.entity.common.EntityDate;
import org.changppo.account.entity.member.Member;
import org.changppo.account.type.PaymentStatus;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE payment SET deleted_at = CURRENT_TIMESTAMP WHERE payment_id = ?")
@SQLRestriction("deleted_at is NULL")
public class Payment extends EntityDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @Column(name = "`key`", unique = true)
    private String key;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private LocalDate startedAt;

    @Column(nullable = false)
    private LocalDate endedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Embedded
    private PaymentCardInfo cardInfo;

    @Column
    private LocalDateTime deletedAt;

    @Builder
    public Payment(String key, BigDecimal amount, PaymentStatus status, LocalDate startedAt, LocalDate endedAt, Member member, PaymentCardInfo cardInfo) {
        this.key = key;
        this.amount = amount;
        this.status = status;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.member = member;
        this.cardInfo = cardInfo;
        this.deletedAt = null;
    }

    public void changeStatus(PaymentStatus status, String key, PaymentCardInfo cardInfo) {
        this.status = status;
        this.key = key;
        this.cardInfo = cardInfo;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
