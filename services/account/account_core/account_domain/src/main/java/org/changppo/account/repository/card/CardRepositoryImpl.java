package org.changppo.account.repository.card;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.changppo.account.entity.card.Card;
import org.changppo.account.service.dto.card.CardDto;
import org.changppo.account.service.dto.card.QCardDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static org.changppo.account.entity.card.QCard.card;
import static org.changppo.account.entity.card.QPaymentGateway.paymentGateway;

@RequiredArgsConstructor
public class CardRepositoryImpl implements QuerydslCardRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Card> findAllCardByMemberIdOrderByAsc(Long memberId) {
        return queryFactory.selectFrom(card)
                .where(memberIdEquals(memberId))
                .orderBy(card.id.asc())
                .fetch();
    }

    @Override
    public long countByMemberId(Long memberId) {
        Long count = queryFactory.select(card.count())
                .from(card)
                .where(memberIdEquals(memberId))
                .fetchOne();

        return count != null ? count : 0;
    }

    @Override
    public void deleteAllByMemberId(Long memberId) {
        queryFactory.delete(card)
                .where(memberIdEquals(memberId))
                .execute();
    }

    @Override
    public List<CardDto> findAllDtosByMemberIdOrderByAsc(Long memberId) {
        return queryFactory.select(
                new QCardDto(
                        card.id,
                        card.type,
                        card.issuerCorporation,
                        card.bin,
                        card.paymentGateway.paymentGatewayType,
                        card.createdAt))
                .from(card)
                .join(card.paymentGateway, paymentGateway)
                .where(memberIdEquals(memberId))
                .orderBy(card.id.asc())
                .fetch();
    }

    @Override
    public Page<CardDto> findAllDtos(Pageable pageable) {
        List<CardDto> content = queryFactory
                .select(
                    new QCardDto(
                            card.id,
                            card.type,
                            card.issuerCorporation,
                            card.bin,
                            card.paymentGateway.paymentGatewayType,
                            card.createdAt))
                .from(card)
                .join(card.paymentGateway, paymentGateway)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(card.count())
                .from(card)
                .leftJoin(card.paymentGateway, paymentGateway);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression memberIdEquals(Long memberId) {
        return card.member.id.eq(memberId);
    }
}
