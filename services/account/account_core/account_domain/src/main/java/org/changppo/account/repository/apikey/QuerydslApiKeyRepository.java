package org.changppo.account.repository.apikey;

import org.changppo.account.service.dto.apikey.ApiKeyDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;

public interface QuerydslApiKeyRepository {
    Slice<ApiKeyDto> findAllDtosByMemberIdOrderByAsc(Long memberId, Long firstApiKeyId, Pageable pageable);
    void deleteAllByMemberId(Long memberId);
    void banForCardDeletionByMemberId(Long memberId, LocalDateTime time);
    void unbanForCardDeletionByMemberId(Long memberId);
    void banApiKeysForPaymentFailureByMemberId(Long memberId, LocalDateTime time);
    void unbanApiKeysForPaymentFailureByMemberId(Long memberId);
    void requestApiKeyDeletionByMemberId(Long memberId, LocalDateTime time);
    void cancelApiKeyDeletionRequestByMemberId(Long memberId);
    void banApiKeysByAdminByMemberId(Long memberId, LocalDateTime time);
    void unbanApiKeysByAdminByMemberId(Long memberId);
    boolean isValid(Long Id);
    Page<ApiKeyDto> findAllDtos(Pageable pageable);
}
