package com.auction.bid.domain.member;

import com.auction.bid.domain.member.dto.ChargeDto;
import com.auction.bid.domain.member.dto.SignUpDto;

import java.util.UUID;

public interface MemberService {

    SignUpDto.Response signUp(SignUpDto.Request request);

    String sendEmail(String email);

    boolean verifyEmail(String email, String token);

    String logout(String token);

    Member findByMemberUUID(UUID memberUUID);

    ChargeDto.Response chargeMoney(String token, ChargeDto.Request dtoRequest);

    Long getMoney(String token);

    void bidToAuction(Member member, Long money, Long lastMoney);

    void withDraw(Long memberId, Long withDrawMoney);

    void addMoney(Long memberId, Long amount);
}
