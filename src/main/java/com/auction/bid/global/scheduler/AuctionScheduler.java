package com.auction.bid.global.scheduler;

import com.auction.bid.domain.bid.BidDto;
import com.auction.bid.domain.member.MemberService;
import com.auction.bid.domain.product.Product;
import com.auction.bid.domain.product.ProductBidPhase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.auction.bid.global.scheduler.ConstAuction.AUCTION;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionScheduler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SchedulerService schedulerService;
    private final MemberService memberService;

    /**
     * 경매 시작 시 호출되는 메서드입니다.
     *
     * @param product 경매가 시작된 상품
     */
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Async
    public void openAuction(Product product) {
        log.info("경매 시작={}", product.getAuctionStart());
        schedulerService.changeAuctionPhase(product, ProductBidPhase.ONGOING);

        HashOperations<String, Long, List<BidDto>> openedAuctionRedis = redisTemplate.opsForHash();
        openedAuctionRedis.put(AUCTION, product.getId(), new ArrayList<>());
        LocalDateTime auctionStart = product.getAuctionStart();
        LocalDateTime auctionEnd = product.getAuctionEnd();
        long ttl = Duration.between(auctionStart, auctionEnd).getSeconds() + 3600;
        redisTemplate.expire(AUCTION, ttl, TimeUnit.SECONDS);
    }

    /**
     * 경매 종료 시 호출되는 메서드입니다.
     *
     * @param product 경매가 종료된 상품
     */
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Async
    public void closeAuction(Product product) {
        log.info("경매 종료={}", product.getAuctionEnd());
        schedulerService.changeAuctionPhase(product, ProductBidPhase.ENDED);

        HashOperations<String, Long, List<BidDto>> openedAuctionRedis = redisTemplate.opsForHash();
        List<BidDto> bidDtoList = BidDto
                .convertToBidDtoList(openedAuctionRedis.get(AUCTION, product.getId()));

        BidDto successBidDto = bidDtoList.isEmpty() ? new BidDto() : bidDtoList.get(bidDtoList.size() - 1);
        Long finalAmount = successBidDto.getBidAmount();
        Long finalBuyerId = successBidDto.getMemberId();

        Map<Long, Long> withDrawMap = new HashMap<>();
        for (int i = bidDtoList.size() - 2; i >= 0; i--) {
            BidDto bidDto = bidDtoList.get(i);
            if (Objects.equals(bidDto.getMemberId(), finalBuyerId)) continue;
            Long memberId = bidDto.getMemberId();
            Long bidAmount = bidDto.getBidAmount();

            if (withDrawMap.containsKey(memberId)) continue;
            withDrawMap.put(memberId, bidAmount);
        }

        processWithdrawalsAndPayout(product, withDrawMap, finalBuyerId, finalAmount);

        schedulerService.saveBids(product.getId(), bidDtoList);
        log.info("입찰 저장 완료");

        schedulerService.saveAuction(finalBuyerId, product.getId(), finalAmount, bidDtoList);
        log.info("경매 저장 완료");

        schedulerService.saveSale(finalBuyerId, product.getId(), finalAmount);
        log.info("판매 저장 완료");

        openedAuctionRedis.delete(AUCTION, product.getId());
    }

    /**
     * 환불 및 지불 처리를 담당하는 메서드입니다.
     *
     * @param product 상품
     * @param withDrawMap 환불할 회원 ID 및 금액 맵
     * @param successMemberId 최종 구매자 회원 ID
     * @param finalAmount 최종 입찰 금액
     */
    private void processWithdrawalsAndPayout(Product product, Map<Long, Long> withDrawMap, Long successMemberId, Long finalAmount) {
        if (successMemberId != null) {
            memberService.addMoney(product.getMember().getId(), finalAmount);
        }

        for (Map.Entry<Long, Long> entry : withDrawMap.entrySet()) {
            Long memberId = entry.getKey();
            Long bidAmount = entry.getValue();
            memberService.withDraw(memberId, bidAmount);
        }

    }

}
