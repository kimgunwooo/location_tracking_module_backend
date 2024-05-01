package org.changppo.account.payment.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.changppo.account.member.Member;

@Data
@AllArgsConstructor
public class PaymentCompletedEvent {
    private Member member;
}
