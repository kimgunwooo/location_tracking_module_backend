package org.changppo.account.integration;

import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.changppo.account.entity.apikey.ApiKey;
import org.changppo.account.entity.apikey.Grade;
import org.changppo.account.entity.card.Card;
import org.changppo.account.entity.card.PaymentGateway;
import org.changppo.account.entity.member.Member;
import org.changppo.account.entity.member.Role;
import org.changppo.account.entity.payment.Payment;
import org.changppo.account.entity.payment.PaymentCardInfo;
import org.changppo.account.repository.apikey.ApiKeyRepository;
import org.changppo.account.repository.apikey.GradeRepository;
import org.changppo.account.repository.card.CardRepository;
import org.changppo.account.repository.card.PaymentGatewayRepository;
import org.changppo.account.repository.member.MemberRepository;
import org.changppo.account.repository.member.RoleRepository;
import org.changppo.account.repository.payment.PaymentRepository;
import org.changppo.account.response.exception.apikey.GradeNotFoundException;
import org.changppo.account.response.exception.card.CardNotFoundException;
import org.changppo.account.response.exception.card.PaymentGatewayNotFoundException;
import org.changppo.account.response.exception.member.MemberNotFoundException;
import org.changppo.account.response.exception.member.RoleNotFoundException;
import org.changppo.account.type.GradeType;
import org.changppo.account.type.PaymentGatewayType;
import org.changppo.account.type.PaymentStatus;
import org.changppo.account.type.RoleType;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Profile("test")
@Component
@RequiredArgsConstructor
@Slf4j
public class TestInitDB {

    private final RoleRepository roleRepository;
    private final MemberRepository memberRepository;
    private final GradeRepository gradeRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final PaymentGatewayRepository paymentGatewayRepository;
    private final CardRepository cardRepository;
    private final PaymentRepository paymentRepository;

    @Getter
    private final String adminMemberName = "kakao_0000";
    @Getter
    private final String freeMemberName = "kakao_1234";
    @Getter
    private final String normalMemberName = "kakao_2345";
    @Getter
    private final String banForPaymentFailureMemberName = "kakao_3456";
    @Getter
    private final String requestDeletionMemberName = "kakao_4567";
    @Getter
    private final String adminBannedMemberName = "kakao_5678";
    @Getter
    private final String freeApiKeyValue = "free-api-key";
    @Getter
    private final String classicApiKeyValue = "classic-api-key";
    @Getter
    private final String banForPaymentFailureApiKeyValue = "ban-payment-fail-api-key";
    @Getter
    private final String banForCardDeletionApiKeyValue = "ban-card-delete-api-key";
    @Getter
    private final String requestDeletionApiKeyValue = "request-delete-api-key";
    @Getter
    private final String adminBannedApiKeyValue = "admin-banned-api-key";
    @Getter
    private final String kakaopayCardKey = "kakaopay-card-key";
    @Getter
    private final String successfulFreePaymentKey = "successful-free-payment-key";
    @Getter
    private final String successfulPaidPaymentKey = "successful-paid-payment-key";
    @Getter
    private final String failedPaymentKey = "failed-payment-key";

    @Transactional
    public void initMember() {
        initRole();
        initTestAdmin();
        initTestMember();
    }

    @Transactional
    public void initApiKey() {
        initGrade();
        initTestApiKey();
    }

    @Transactional
    public void initCard() {
        initPaymentGateway();
        initTestCard();
    }

    @Transactional
    public void initPayment() {
        initTestPayment();
    }

    private void initRole() {
        roleRepository.saveAll(
                Stream.of(RoleType.values()).map(Role::new).collect(Collectors.toList())
        );
    }

    private void initTestAdmin() {
        Role adminRole = roleRepository.findByRoleType(RoleType.ROLE_ADMIN).orElseThrow(RoleNotFoundException::new);
        Member adminMember = Member.builder()
                .name(adminMemberName)
                .username("admin")
                .profileImage("adminMemberProfileImage")
                .role(adminRole)
                .build();
        memberRepository.save(adminMember);
    }

    private void initTestMember() {
        Role freeRole = roleRepository.findByRoleType(RoleType.ROLE_FREE).orElseThrow(RoleNotFoundException::new);
        Role normalRole = roleRepository.findByRoleType(RoleType.ROLE_NORMAL).orElseThrow(RoleNotFoundException::new);

        Member freeMember = Member.builder()
                .name(freeMemberName)
                .username("free")
                .profileImage("freeMemberProfileImage")
                .role(freeRole)
                .build();
        Member normalMember = Member.builder()
                .name(normalMemberName)
                .username("normal")
                .profileImage("normalMemberProfileImage")
                .role(normalRole)
                .build();
        Member banForPaymentFailureMember = Member.builder()
                .name(banForPaymentFailureMemberName)
                .username("banForPaymentFailureMember")
                .profileImage("banForPaymentFailureMemberProfileImage")
                .role(normalRole)
                .build();
        banForPaymentFailureMember.banForPaymentFailure(LocalDateTime.now());
        Member requestDeletionMember = Member.builder()
                .name(requestDeletionMemberName)
                .username("requestDeletionMember")
                .profileImage("requestDeletionMemberProfileImage")
                .role(normalRole)
                .build();
        requestDeletionMember.requestDeletion(LocalDateTime.now());
        Member adminBannedMember = Member.builder()
                .name(adminBannedMemberName)
                .username("adminBannedMember")
                .profileImage("adminBannedMemberProfileImage")
                .role(normalRole)
                .build();
        adminBannedMember.banByAdmin(LocalDateTime.now());
        memberRepository.saveAll(List.of(freeMember, normalMember, banForPaymentFailureMember, requestDeletionMember, adminBannedMember));
    }

    private void initGrade() {
        gradeRepository.saveAll(
                Stream.of(GradeType.values()).map(Grade::new).collect(Collectors.toList())
        );
    }

    private void initTestApiKey() {
        Member freeMember = memberRepository.findByName(freeMemberName).orElseThrow(MemberNotFoundException::new);
        Member normalMember = memberRepository.findByName(normalMemberName).orElseThrow(MemberNotFoundException::new);
        Member adminBannedMember = memberRepository.findByName(adminBannedMemberName).orElseThrow(MemberNotFoundException::new);
        Member banForPaymentFailureMember = memberRepository.findByName(banForPaymentFailureMemberName).orElseThrow(MemberNotFoundException::new);
        Member requestDeletionMember = memberRepository.findByName(requestDeletionMemberName).orElseThrow(MemberNotFoundException::new);
        Grade freeGrade = gradeRepository.findByGradeType(GradeType.GRADE_FREE).orElseThrow(GradeNotFoundException::new);
        Grade classicGrade = gradeRepository.findByGradeType(GradeType.GRADE_CLASSIC).orElseThrow(GradeNotFoundException::new);

        ApiKey freeApiKey = ApiKey.builder()
                .value(freeApiKeyValue)
                .grade(freeGrade)
                .member(freeMember)
                .build();
        ApiKey classicApiKey = ApiKey.builder()
                .value(classicApiKeyValue)
                .grade(classicGrade)
                .member(normalMember)
                .build();
        ApiKey banForPaymentFailureApiKey = ApiKey.builder()
                .value(banForPaymentFailureApiKeyValue)
                .grade(classicGrade)
                .member(banForPaymentFailureMember)
                .build();
        banForPaymentFailureApiKey.banForPaymentFailure(LocalDateTime.now());
        ApiKey banForCardDeletionApiKey = ApiKey.builder()
                .value(banForCardDeletionApiKeyValue)
                .grade(classicGrade)
                .member(banForPaymentFailureMember)
                .build();
        banForCardDeletionApiKey.banForCardDeletion(LocalDateTime.now());
        ApiKey requestDeletionApiKey = ApiKey.builder()
                .value(requestDeletionApiKeyValue)
                .grade(classicGrade)
                .member(requestDeletionMember)
                .build();
        requestDeletionApiKey.requestDeletion(LocalDateTime.now());
        ApiKey adminBannedApiKey = ApiKey.builder()
                .value(adminBannedApiKeyValue)
                .grade(classicGrade)
                .member(adminBannedMember)
                .build();
        adminBannedApiKey.banByAdmin(LocalDateTime.now());
        apiKeyRepository.saveAll(List.of(freeApiKey, classicApiKey, banForPaymentFailureApiKey, banForCardDeletionApiKey, requestDeletionApiKey, adminBannedApiKey));
    }

    private void initPaymentGateway() {
        paymentGatewayRepository.saveAll(
                Stream.of(PaymentGatewayType.values()).map(PaymentGateway::new).collect(Collectors.toList())
        );
    }

    private void initTestCard() {
        Member normalMember = memberRepository.findByName(normalMemberName).orElseThrow(MemberNotFoundException::new);
        PaymentGateway kakaopayPaymentGateway = paymentGatewayRepository.findByPaymentGatewayType(PaymentGatewayType.PG_KAKAOPAY).orElseThrow(PaymentGatewayNotFoundException::new);

        Card kakaopayCard = Card.builder()
                .key(kakaopayCardKey)
                .member(normalMember)
                .paymentGateway(kakaopayPaymentGateway)
                .type("신용")
                .issuerCorporation("Test Bank")
                .acquirerCorporation("Test Acquirer")
                .bin("123456")
                .build();

        cardRepository.save(kakaopayCard);
    }

    public void initTestPayment() {
        Member banForPaymentFailureMember = memberRepository.findByName(banForPaymentFailureMemberName).orElseThrow(MemberNotFoundException::new);
        Card kakaopayCard = cardRepository.findByKey(kakaopayCardKey).orElseThrow(CardNotFoundException::new);

        Payment successfulfreePayment = Payment.builder()
                .key(successfulFreePaymentKey)
                .amount(new BigDecimal("0"))
                .status(PaymentStatus.COMPLETED_FREE)
                .startedAt(LocalDate.now())
                .endedAt(LocalDate.now().plusDays(1))
                .member(kakaopayCard.getMember())
                .cardInfo(new PaymentCardInfo(kakaopayCard.getType(), kakaopayCard.getIssuerCorporation(), kakaopayCard.getBin()))
                .build();

        Payment successfulPaidPayment = Payment.builder()
                .key(successfulPaidPaymentKey)
                .amount(new BigDecimal("200.00"))
                .status(PaymentStatus.COMPLETED_PAID)
                .startedAt(LocalDate.now())
                .endedAt(LocalDate.now().plusDays(1))
                .member(kakaopayCard.getMember())
                .cardInfo(new PaymentCardInfo(kakaopayCard.getType(), kakaopayCard.getIssuerCorporation(), kakaopayCard.getBin()))
                .build();

        Payment failedPayment = Payment.builder()
                .key(failedPaymentKey)
                .amount(new BigDecimal("300.00"))
                .status(PaymentStatus.FAILED)
                .startedAt(LocalDate.now())
                .endedAt(LocalDate.now().plusDays(1))
                .member(banForPaymentFailureMember)
                .cardInfo(null)
                .build();

        paymentRepository.saveAll(List.of(successfulfreePayment, successfulPaidPayment, failedPayment));
    }
}
