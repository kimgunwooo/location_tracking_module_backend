package org.changppo.cost_management_service.security.evaluator;

import lombok.RequiredArgsConstructor;
import org.changppo.cost_management_service.entity.card.Card;
import org.changppo.cost_management_service.entity.member.RoleType;
import org.changppo.cost_management_service.repository.card.CardRepository;
import org.changppo.cost_management_service.response.exception.card.CardNotFoundException;
import org.changppo.cost_management_service.security.PrincipalHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CardAccessEvaluator extends Evaluator {

    private final CardRepository cardRepository;
    private static final List<RoleType> roleTypes = List.of(RoleType.ROLE_ADMIN);

    @Override
    protected List<RoleType> getRoleTypes() {
        return roleTypes;
    }

    @Override
    protected boolean isEligible(Long id) {
        Card card = cardRepository.findById(id).orElseThrow(CardNotFoundException::new);
        return card.getMember().getId().equals(PrincipalHandler.extractId());
    }
}