package org.changppo.account.entity.apikey;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.changppo.account.entity.common.EntityDate;
import org.changppo.account.entity.member.Member;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE api_key SET deleted_at = CURRENT_TIMESTAMP WHERE api_key_id = ?")
@SQLRestriction("deleted_at is NULL")
public class ApiKey extends EntityDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "api_key_id")
    private Long id;

    @Column(name = "`value`", unique = true, nullable = false)
    private String value;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grade_id", nullable = false)
    private Grade grade;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column
    private LocalDateTime deletedAt;

    @Column
    private LocalDateTime paymentFailureBannedAt;

    @Column
    private LocalDateTime cardDeletionBannedAt;

    @Column
    private LocalDateTime deletionRequestedAt;

    @Column
    private LocalDateTime adminBannedAt;

    @Builder
    public ApiKey(String value, Grade grade, Member member) {
        this.value = value;
        this.grade = grade;
        this.member = member;
        this.deletedAt = null;
        this.paymentFailureBannedAt = null;
        this.cardDeletionBannedAt = null;
        this.adminBannedAt = null;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public boolean isPaymentFailureBanned() {
        return this.paymentFailureBannedAt != null;
    }

    public boolean isCardDeletionBanned() {
        return this.cardDeletionBannedAt != null;
    }

    public boolean isDeletionRequested() {
        return this.deletionRequestedAt != null;
    }

    public boolean isAdminBanned() {
        return this.adminBannedAt != null;
    }

    public void updateValue(String value){
        this.value = value;
    }

    public void banForPaymentFailure(LocalDateTime time) {
        this.paymentFailureBannedAt = time;
    }

    public void unbanForPaymentFailure() {
        this.paymentFailureBannedAt = null;
    }

    public void banForCardDeletion(LocalDateTime time) {
        this.cardDeletionBannedAt = time;
    }

    public void unbanForCardDeletion() {
        this.cardDeletionBannedAt = null;
    }

    public void requestDeletion(LocalDateTime time) {
        this.deletionRequestedAt =time;
    }

    public void cancelDeletionRequest() {
        this.deletionRequestedAt = null;
    }

    public void banByAdmin(LocalDateTime time) {
        this.adminBannedAt = time;
    }

    public void unbanByAdmin() {
        this.adminBannedAt = null;
    }
}
