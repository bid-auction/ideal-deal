package com.auction.bid.domain.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByProviderId(String providerId);

    boolean existsByLoginId(String loginId);

    Optional<Member> findByLoginId(String loginId);

    Optional<Member> findByMemberUUID(UUID memberId);
}

