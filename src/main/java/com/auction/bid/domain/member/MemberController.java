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

/**
 * MemberController
 * 멤버와 관련된 작업을 처리하는 REST API 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {

    private final MemberService memberService;

    /**
     * 회원가입
     * @param request 회원가입 요청 DTO
     * @return 회원가입 결과
     */
    @PostMapping("/auth/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpDto.Request request) {
        return ResponseEntity.ok(memberService.signUp(request));
    }

    /**
     * 이메일 전송
     * @param emailDto 이메일 전송 요청 DTO
     * @return 이메일 전송 결과
     */
    @PostMapping("/auth/send-email")
    public ResponseEntity<?> sendEmail(@Valid @RequestBody EmailDto emailDto) {
        return ResponseEntity.ok(memberService.sendEmail(emailDto.getEmail()));
    }

    /**
     * 이메일 인증
     * @param verifyDto 이메일 인증 요청 DTO
     * @return 이메일 인증 결과
     */
    @PostMapping("/auth/verify-email")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody TokenVerificationDto verifyDto) {
        return ResponseEntity.ok(memberService.verifyEmail(verifyDto.getEmail(), verifyDto.getToken()));
    }

    /**
     * 로그아웃
     * @param token 인증 토큰
     * @return 로그아웃 결과
     */
    @PreAuthorize(ConstSecurity.HAS_ROLE_MEMBER)
    @PostMapping("/auth/logout")
    public ResponseEntity<?> logout(
            @RequestHeader(ConstSecurity.AUTHORIZATION) String token) {
        return ResponseEntity.ok(memberService.logout(token));
    }

    /**
     * 잔액 충전
     * @param token 인증 토큰
     * @param dtoRequest 충전 요청 DTO
     * @return 충전 결과
     */
    @PreAuthorize(ConstSecurity.HAS_ROLE_MEMBER)
    @PostMapping("/charge/money")
    public ResponseEntity<?> charge(
            @RequestHeader(ConstSecurity.AUTHORIZATION) String token,
            @RequestBody ChargeDto.Request dtoRequest) {
        return ResponseEntity.ok(memberService.chargeMoney(token, dtoRequest));
    }

    /**
     * 잔액 조회
     * @param token 인증 토큰
     * @return 잔액 정보
     */
    @PreAuthorize(ConstSecurity.HAS_ROLE_MEMBER)
    @GetMapping("/charge")
    public ResponseEntity<?> getMoney(
            @RequestHeader(ConstSecurity.AUTHORIZATION) String token) {
        return ResponseEntity.ok(memberService.getMoney(token));
    }

    /**
     * 경매 기록 조회
     * @param token 인증 토큰
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param auctionStatus 경매 상태 (선택적)
     * @return 경매 기록 목록
     */
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

    /**
     * 경매 상세 정보 조회
     * @param token 인증 토큰
     * @param auctionId 경매 ID
     * @return 경매 상세 정보
     */
    @PreAuthorize(ConstSecurity.HAS_ROLE_MEMBER)
    @GetMapping("/auction/{auctionId}")
    public ResponseEntity<?> getAuctionDetail(
            @RequestHeader(ConstSecurity.AUTHORIZATION) String token,
            @PathVariable(name = "auctionId") Long auctionId
    ) {
        return ResponseEntity.ok(memberService.getAuctionDetail(token, auctionId));
    }

    /**
     * 판매 기록 조회
     * @param token 인증 토큰
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param saleStatus 판매 상태 (선택적)
     * @return 판매 기록 목록
     */
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

    /**
     * 판매 상세 정보 조회
     * @param saleId 판매 ID
     * @return 판매 상세 정보
     */
    @PreAuthorize(ConstSecurity.HAS_ROLE_MEMBER)
    @GetMapping("/sale/{saleId}")
    public ResponseEntity<?> getSaleDetail(
            @PathVariable(name = "saleId") Long saleId
    ) {
        return ResponseEntity.ok(memberService.getSaleDetail(saleId));
    }

}