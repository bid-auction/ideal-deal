package com.auction.bid.global.scheduler;

import com.auction.bid.domain.bid.BidDto;
import com.auction.bid.domain.member.MemberService;
import com.auction.bid.domain.product.ProductBidPhase;
import com.auction.bid.domain.product.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
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

    @TransactionalEventListener
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

    @Async
    public void closeAuction(Product product) {
        // 트랜잭션 관리 제대로 해야됨 추후에 생각해볼것
        log.info("경매 종료={}", product.getAuctionEnd());
        schedulerService.changeAuctionPhase(product, ProductBidPhase.ENDED);

        HashOperations<String, Long, List<BidDto>> openedAuctionRedis = redisTemplate.opsForHash();
        List<BidDto> bidDtoList = BidDto
                .convertToBidDtoList(openedAuctionRedis.get(AUCTION, product.getId()));

        BidDto successBidDto = bidDtoList.isEmpty() ? new BidDto() : bidDtoList.get(bidDtoList.size() - 1);
        Long successMemberId = successBidDto.getMemberId();
        Long finalAmount = successBidDto.getBidAmount();
        Long finalBuyerId = successBidDto.getMemberId();
        
        Map<Long, Long> withDrawMap = new HashMap<>();
        for (int i = bidDtoList.size() - 2; i >= 0; i--) {
            BidDto bidDto = bidDtoList.get(i);
            if (Objects.equals(bidDto.getMemberId(), successMemberId)) continue;
            Long memberId = bidDto.getMemberId();
            Long bidAmount = bidDto.getBidAmount();
            
            if (withDrawMap.containsKey(memberId)) continue;
            withDrawMap.put(memberId, bidAmount);
        }

        for (Map.Entry<Long, Long> entry : withDrawMap.entrySet()) {
            Long memberId = entry.getKey();
            Long bidAmount = entry.getValue();
            memberService.withDraw(memberId, bidAmount);
        }

        if (successMemberId != null) {
            memberService.addMoney(product.getMember().getId(), finalAmount);
        }

        schedulerService.saveBids(product.getId(), bidDtoList);
        log.info("입찰 저장 완료");

        schedulerService.saveAuction(finalBuyerId, product.getId(), finalAmount, bidDtoList);
        log.info("경매 저장 완료");

        schedulerService.saveSale(finalBuyerId, product.getId(), finalAmount);
        log.info("판매 저장 완료");

        // 서버가 종료되었을 때, 대기 중인 레디스 다 삭제됨. repository하나 만들어서 서버 시작에 재등록하고 openAuction때 repository에 관리해야 됨
        // 상품이 Integer.MaxValue를 넘어도 잘 작동할까?(url로 지정해서..)
        // 레디스를 사용한 이유는 서버의 부하를 막기 위해서였다.
        // 하지만 웹소켓만으로 대용량을 처리할 수 있을까?(카프카란 걸 같이 사용해야 할 거 같다..)
        openedAuctionRedis.delete(AUCTION, product.getId());
    }

}
