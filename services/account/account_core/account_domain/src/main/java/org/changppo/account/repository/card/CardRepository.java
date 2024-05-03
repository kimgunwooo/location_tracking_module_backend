package org.changppo.account.repository.card;

import org.changppo.account.entity.card.Card;
import org.changppo.account.service.dto.card.CardDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    @Query("select c from Card c join fetch c.member where c.id = :id")
    Optional<Card> findByIdWithMember(@Param("id") Long id);

    @Query("select c from Card c join fetch c.paymentGateway where c.id = :id")
    Optional<Card> findByIdWithPaymentGateway(@Param("id") Long id);

    @Query("select new org.changppo.account.service.dto.card.CardDto(c.id, c.type, c.issuerCorporation, c.bin, c.paymentGateway.paymentGatewayType, c.createdAt) " +
            "from Card c where c.member.id = :memberId " +
            "order by c.id asc")
    List<CardDto> findAllByMemberId(@Param("memberId") Long memberId);

    long countByMemberId(Long memberId);

    void deleteAllByMemberId(Long memberId);

    Optional<Card> findByKey(String key);

    @Query("select c from Card c where c.member.id = :memberId order by c.id asc")
    List<Card> findAllCardByMemberId(@Param("memberId") Long memberId);
}