package com.auction.bid.domain.member;

import com.auction.bid.domain.member.dto.ChargeDto;
import com.auction.bid.domain.member.dto.EmailDto;
import com.auction.bid.domain.member.dto.SignUpDto;
import com.auction.bid.domain.member.dto.TokenVerificationDto;
import com.auction.bid.global.security.ConstSecurity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/auth/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpDto.Request request) {
        return ResponseEntity.ok(memberService.signUp(request));
    }

    @PostMapping("/auth/send-email")
    public ResponseEntity<?> sendEmail(@Valid @RequestBody EmailDto emailDto) {
        return ResponseEntity.ok(memberService.sendEmail(emailDto.getEmail()));
    }

    @PostMapping("/auth/verify-email")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody TokenVerificationDto verifyDto) {
        return ResponseEntity.ok(memberService.verifyEmail(verifyDto.getEmail(), verifyDto.getToken()));
    }

    @PreAuthorize(ConstSecurity.HAS_ROLE_MEMBER)
    @PostMapping("/auth/logout")
    public ResponseEntity<?> logout(
            @RequestHeader(ConstSecurity.AUTHORIZATION) String token) {
        return ResponseEntity.ok(memberService.logout(token));
    }

    @PreAuthorize(ConstSecurity.HAS_ROLE_MEMBER)
    @PostMapping("/charge/money")
    public ResponseEntity<?> charge(
            @RequestHeader(ConstSecurity.AUTHORIZATION) String token,
            @RequestBody ChargeDto.Request dtoRequest) {
        return ResponseEntity.ok(memberService.chargeMoney(token, dtoRequest));
    }

    @PreAuthorize(ConstSecurity.HAS_ROLE_MEMBER)
    @GetMapping("/charge")
    public ResponseEntity<?> getMoney(
            @RequestHeader(ConstSecurity.AUTHORIZATION) String token) {
        return ResponseEntity.ok(memberService.getMoney(token));
    }

    // 충전 내역 전체 조회 만들지 필요(만들면 entity, repository 만들어야 됨)
    // 멤버 register할 때 balance를 0L로 등록하기(안하면 NULL이 들어가있음)
}