package com.auction.bid.domain.member;

import com.auction.bid.domain.auction.AuctionStatus;
import com.auction.bid.domain.member.dto.*;
import com.auction.bid.domain.sale.SaleStatus;
import org.springframework.data.domain.Page;

import java.util.List;
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

    Page<AuctionHistoryDto> getAuctionHistory(String token, int page, int size, AuctionStatus auctionStatus);

    DetailAuctionHistoryDto getAuctionDetail(String token, Long auctionId);

    List<SaleHistoryDto> getSaleHistory(String token, int page, int size, SaleStatus saleStatus);

    DetailSaleHistoryDto getSaleDetail(Long saleId);

}
