package com.auction.bid.domain.member;

import com.auction.bid.domain.member.dto.SignUpDto;

import java.util.UUID;

public interface MemberService {

    SignUpDto.Response signUp(SignUpDto.Request request);

    String sendEmail(String email);

    boolean verifyEmail(String email, String token);

    String logout(String token);

    Member findByMemberUUID(UUID memberUUID);
}
