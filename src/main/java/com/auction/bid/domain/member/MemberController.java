package com.auction.bid.domain.member;

import com.auction.bid.domain.auction.AuctionStatus;
import com.auction.bid.domain.member.dto.ChargeDto;
import com.auction.bid.domain.member.dto.EmailDto;
import com.auction.bid.domain.member.dto.SignUpDto;
import com.auction.bid.domain.member.dto.TokenVerificationDto;
import com.auction.bid.domain.sale.SaleStatus;
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

    @PreAuthorize(ConstSecurity.HAS_ROLE_MEMBER)
    @GetMapping("/auction")
    public ResponseEntity<?> getAuctionHistory(
            @RequestHeader(ConstSecurity.AUTHORIZATION) String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) AuctionStatus auctionStatus
    ) {
        return ResponseEntity.ok(memberService.getAuctionHistory(token, page, size, auctionStatus));
    }

    @PreAuthorize(ConstSecurity.HAS_ROLE_MEMBER)
    @GetMapping("/auction/{auctionId}")
    public ResponseEntity<?> getAuctionDetail(
            @RequestHeader(ConstSecurity.AUTHORIZATION) String token,
            @PathVariable(name = "auctionId") Long auctionId
    ) {
        return ResponseEntity.ok(memberService.getAuctionDetail(token, auctionId));
    }

    @PreAuthorize(ConstSecurity.HAS_ROLE_MEMBER)
    @GetMapping("/sale")
    public ResponseEntity<?> getSaleHistory(
            @RequestHeader(ConstSecurity.AUTHORIZATION) String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) SaleStatus saleStatus
    ) {
        return ResponseEntity.ok(memberService.getSaleHistory(token, page, size, saleStatus));
    }

    @PreAuthorize(ConstSecurity.HAS_ROLE_MEMBER)
    @GetMapping("/sale/{saleId}")
    public ResponseEntity<?> getSaleDetail(
            @PathVariable(name = "saleId") Long saleId
    ) {
        return ResponseEntity.ok(memberService.getSaleDetail(saleId));
    }

}