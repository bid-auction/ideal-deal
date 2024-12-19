package com.auction.bid.global.scheduler;

import com.auction.bid.domain.auction.AuctionService;
import com.auction.bid.domain.bid.BidDto;
import com.auction.bid.domain.bid.BidService;
import com.auction.bid.domain.product.ProductBidPhase;
import com.auction.bid.domain.product.Product;
import com.auction.bid.domain.product.ProductService;
import com.auction.bid.domain.product.ProductServiceImpl;
import com.auction.bid.domain.sale.SaleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.*;

import static com.auction.bid.global.scheduler.ConstAuction.AUCTION;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionScheduler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SchedulerService schedulerService;

    @TransactionalEventListener
    @Async
    public void openAuction(Product product) {
        log.info("경매 시작={}", product.getAuctionStart());
        schedulerService.changeAuctionPhase(product, ProductBidPhase.ONGOING);

        HashOperations<String, Long, List<BidDto>> openedAuctionRedis = redisTemplate.opsForHash();
        openedAuctionRedis.put(AUCTION, product.getId(), new ArrayList<>());

        List<BidDto> bidDtos = openedAuctionRedis.get(AUCTION, product.getId());
        bidDtos.add(new BidDto(1L, 2L, "test", 100L, LocalDateTime.now()));
        bidDtos.add(new BidDto(1L, 3L, "test1", 200L, LocalDateTime.now()));
        bidDtos.add(new BidDto(1L, 2L, "test", 300L, LocalDateTime.now()));
        bidDtos.add(new BidDto(1L, 4L, "test2", 400L, LocalDateTime.now()));
        openedAuctionRedis.put(AUCTION, product.getId(), bidDtos);
    }

    @Async
    public void closeAuction(Product product) {
        log.info("경매 종료={}", product.getAuctionEnd());
        schedulerService.changeAuctionPhase(product, ProductBidPhase.ENDED);

        HashOperations<String, Long, List<BidDto>> openedAuctionRedis = redisTemplate.opsForHash();
        List<BidDto> bidDtoList = BidDto
                .convertToBidDtoList(openedAuctionRedis.get(AUCTION, product.getId()));

        BidDto bidDto = bidDtoList.isEmpty() ? new BidDto() : bidDtoList.get(bidDtoList.size() - 1);
        Long finalAmount = bidDto.getBidAmount();
        Long finalBuyerId = bidDto.getMemberId();

        schedulerService.saveBids(product.getId(), bidDtoList);
        log.info("입찰 저장 완료");

        schedulerService.saveAuction(finalBuyerId, product.getId(), finalAmount, bidDtoList);
        log.info("경매 저장 완료");

        schedulerService.saveSale(finalBuyerId, product.getId(), finalAmount);
        log.info("판매 저장 완료");

        openedAuctionRedis.delete(AUCTION, product.getId());
    }

}
