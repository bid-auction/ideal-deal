package com.auction.bid.domain.member;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long>{

<<<<<<< HEAD
}
=======
@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByProviderId(String providerId);
    boolean existsByLoginId(String loginId);
    Optional<Member> findByLoginId(String loginId);

}
>>>>>>> develop
