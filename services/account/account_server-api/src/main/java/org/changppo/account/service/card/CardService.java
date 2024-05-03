package org.changppo.account.service.card;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.changppo.account.dto.card.CardCreateRequest;
import org.changppo.account.dto.card.CardListDto;
import org.changppo.account.entity.card.Card;
import org.changppo.account.entity.card.PaymentGateway;
import org.changppo.account.entity.member.Member;
import org.changppo.account.entity.member.Role;
import org.changppo.account.paymentgateway.PaymentGatewayClient;
import org.changppo.account.repository.apikey.ApiKeyRepository;
import org.changppo.account.repository.card.CardRepository;
import org.changppo.account.repository.card.PaymentGatewayRepository;
import org.changppo.account.repository.member.MemberRepository;
import org.changppo.account.repository.member.RoleRepository;
import org.changppo.account.response.exception.card.CardNotFoundException;
import org.changppo.account.response.exception.card.PaymentGatewayNotFoundException;
import org.changppo.account.response.exception.card.UnsupportedPaymentGatewayException;
import org.changppo.account.response.exception.member.MemberNotFoundException;
import org.changppo.account.response.exception.member.RoleNotFoundException;
import org.changppo.account.response.exception.member.UpdateAuthenticationFailureException;
import org.changppo.account.security.oauth2.CustomOAuth2User;
import org.changppo.account.service.dto.card.CardDto;
import org.changppo.account.type.PaymentGatewayType;
import org.changppo.account.type.RoleType;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Validated
@Transactional(readOnly = true)
public class CardService {

    private final CardRepository cardRepository;
    private final PaymentGatewayRepository paymentGatewayRepository;
    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final List<PaymentGatewayClient> paymentGatewayClients;

    @Transactional
    public CardDto create(@Valid CardCreateRequest req) { // PaymentGateway에서 호출 되므로 @Validated 사용
        PaymentGateway paymentGateway = paymentGatewayRepository.findByPaymentGatewayType(req.getPaymentGatewayType()).orElseThrow(PaymentGatewayNotFoundException::new);
        Member member = memberRepository.findByIdWithRoles(req.getMemberId()).orElseThrow(MemberNotFoundException::new);
        Card card = Card.builder()
                .key(req.getKey())
                .type(req.getType())
                .acquirerCorporation(req.getAcquirerCorporation())
                .issuerCorporation(req.getIssuerCorporation())
                .bin(req.getBin())
                .paymentGateway(paymentGateway)
                .member(member)
                .build();
        card = cardRepository.save(card);

        if (hasFreeRole(member)) {
            upgradeRole(member);
            unbanForCardDeletionApiKeys(member);
        }
        return new CardDto(card.getId(), card.getType(), card.getIssuerCorporation(), card.getBin(),
                card.getPaymentGateway().getPaymentGatewayType(), card.getCreatedAt());
    }

    private boolean hasFreeRole(Member member) {
        return member.getMemberRoles().stream()
                .anyMatch(memberRole -> memberRole.getRole().getRoleType() == RoleType.ROLE_FREE);
    }

    private void upgradeRole(Member member) {
        Role normalRole = roleRepository.findByRoleType(RoleType.ROLE_NORMAL).orElseThrow(RoleNotFoundException::new);
        member.changeRole(RoleType.ROLE_FREE, normalRole);
        updateAuthentication(member);
    }

    private void unbanForCardDeletionApiKeys(Member member) {
        apiKeyRepository.unbanForCardDeletionByMemberId(member.getId());
    }

    @PreAuthorize("@cardAccessEvaluator.check(#id)")
    public CardDto read(@Param("id")Long id) {
        Card card = cardRepository.findById(id).orElseThrow(CardNotFoundException::new);
        return new CardDto(card.getId(), card.getType(), card.getIssuerCorporation(), card.getBin(),
                card.getPaymentGateway().getPaymentGatewayType(), card.getCreatedAt());
    }

    @PreAuthorize("@memberAccessEvaluator.check(#memberId)")
    public CardListDto readAll(@Param("memberId")Long memberId){
        List<CardDto> cards = cardRepository.findAllByMemberId(memberId);
        return new CardListDto(cards);
    }

    @Transactional
    @PreAuthorize("@cardAccessEvaluator.check(#id)")
    public void delete(@Param("id")Long id) {
        Card card = cardRepository.findById(id).orElseThrow(CardNotFoundException::new);
        cardRepository.delete(card);
        inactivePaymentGatewayCard(card);

        if (isLastCard(card)) {
            downgradeRole(card.getMember());
            banForCardDeletionApiKeys(card.getMember());
        }
    }

    private boolean isLastCard(Card card) {
        return cardRepository.countByMemberId(card.getMember().getId()) == 0;
    }

    private void downgradeRole(Member member) {
        Role freeRole = roleRepository.findByRoleType(RoleType.ROLE_FREE).orElseThrow(RoleNotFoundException::new);
        member.changeRole(RoleType.ROLE_NORMAL, freeRole);
        updateAuthentication(member);
    }

    private void banForCardDeletionApiKeys(Member member) {
        apiKeyRepository.banForCardDeletionByMemberId(member.getId(), LocalDateTime.now());
    }

    private void inactivePaymentGatewayCard(Card card) {
        PaymentGatewayType paymentGatewayType = card.getPaymentGateway().getPaymentGatewayType();
        paymentGatewayClients.stream()
                .filter(client -> client.supports(paymentGatewayType))
                .findFirst()
                .orElseThrow(UnsupportedPaymentGatewayException::new)
                .inactive(card.getKey());
    }

    private void updateAuthentication(Member member) {
        Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(this::isOAuth2AuthenticationToken)
                .map(OAuth2AuthenticationToken.class::cast)
                .map(oauth2AuthenticationToken -> getOAuth2AuthenticationToken(oauth2AuthenticationToken, member))
                .ifPresentOrElse(
                        SecurityContextHolder.getContext()::setAuthentication,
                        () -> {
                            throw new UpdateAuthenticationFailureException();
                        }
                );
    }

    private boolean isOAuth2AuthenticationToken(Authentication authentication) {
        return authentication instanceof OAuth2AuthenticationToken;
    }

    private OAuth2AuthenticationToken getOAuth2AuthenticationToken(OAuth2AuthenticationToken oauth2AuthenticationToken, Member member) {
        return new OAuth2AuthenticationToken(
                getPrincipal(member),
                getAuthorities(member),
                oauth2AuthenticationToken.getAuthorizedClientRegistrationId()
        );
    }

    private CustomOAuth2User getPrincipal(Member member) {
        return new CustomOAuth2User(
                member.getId(),
                member.getName(),
                getAuthorities(member)
        );
    }

    private Set<GrantedAuthority> getAuthorities(Member member) {
        return member.getMemberRoles().stream()
                .map(memberRole -> new SimpleGrantedAuthority(memberRole.getRole().getRoleType().name()))
                .collect(Collectors.toSet());
    }
}