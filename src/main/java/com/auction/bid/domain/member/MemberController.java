package com.auction.bid.domain.member;

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

}
