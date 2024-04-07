package org.changppo.cost_management_service.repository.member;

import org.changppo.cost_management_service.entity.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByName(String name);

    @Query(value = "select * from member where name = :name", nativeQuery = true)
    Optional<Member> findByNameIgnoringDeleted(@Param("name") String name);

    @Query("select a from Member a join fetch a.memberRoles where a.id = :id")
    Optional<Member> findByIdWithRoles(@Param("id") Long id);
}