package com.auction.bid.global.scheduler;

import com.auction.bid.domain.auction.Auction;
import com.auction.bid.domain.auction.AuctionRepository;
import com.auction.bid.domain.auction.AuctionStatus;
import com.auction.bid.domain.bid.BidDto;
import com.auction.bid.domain.bid.BidRepository;
import com.auction.bid.domain.member.Member;
import com.auction.bid.domain.member.MemberRepository;
import com.auction.bid.domain.product.Product;
import com.auction.bid.domain.product.ProductBidPhase;
import com.auction.bid.domain.product.ProductRepository;
import com.auction.bid.domain.sale.Sale;
import com.auction.bid.domain.sale.SaleRepository;
import com.auction.bid.domain.sale.SaleStatus;
import com.auction.bid.global.exception.ErrorCode;
import com.auction.bid.global.exception.exceptions.MemberException;
import com.auction.bid.global.exception.exceptions.ProductException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SchedulerService {

    private final ProductRepository productRepository;
    private final BidRepository bidRepository;
    private final MemberRepository memberRepository;
    private final AuctionRepository auctionRepository;
    private final SaleRepository saleRepository;


    public void changeAuctionPhase(Product product, ProductBidPhase productBidPhase) {
        product.changeAuctionPhase(productBidPhase);
        productRepository.save(product);
    }

    public void saveBids(Long productId, List<BidDto> bidDtoList) {
        Product findProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ErrorCode.NOT_EXISTS_PRODUCT));

        if (bidDtoList.isEmpty()) {
            bidRepository.save(BidDto.toBidEntity(new BidDto(), null, findProduct));
            return;
        }

        List<Long> memberIds = bidDtoList.stream()
                .map(BidDto::getMemberId)
                .distinct()
                .toList();

        List<Member> findMemberList = memberRepository.findAllById(memberIds);
        Map<Long, Member> memberMap = findMemberList.stream()
                .collect(Collectors.toMap(Member::getId, m -> m));

        bidDtoList.stream()
                .map(bidDto -> BidDto.toBidEntity(
                        bidDto,
                        memberMap.get(bidDto.getMemberId()),
                        findProduct)
                )
                .forEach(bidRepository::save);
    }

    public void saveAuction(Long winnerId, Long productId, Long finalAmount, List<BidDto> bidDtoList) {
        if (bidDtoList.isEmpty()) return;

        Product findProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ErrorCode.NOT_EXISTS_PRODUCT));

        List<Long> memberIds = bidDtoList.stream()
                .map(BidDto::getMemberId)
                .distinct()
                .toList();

        List<Member> findMemberList = memberRepository.findAllById(memberIds);

        findMemberList.forEach(member -> {
            if (Objects.equals(member.getId(), winnerId)) {
                auctionRepository.save(Auction.fromBid(member, findProduct, finalAmount, AuctionStatus.BID_SUCCESS));
            } else {
                auctionRepository.save(Auction.fromBid(member, findProduct, finalAmount, AuctionStatus.BID_FAILURE));
            }
        });
}

    public void saveSale(Long buyerId, Long productId, Long finalAmount) {
        if (buyerId == null) {
            saleRepository.save(Sale.fromAuction(null, SaleStatus.SALE_FAILURE, null, null));
            return;
        }

        Member findBuyer = memberRepository.findById(buyerId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_EXIST_MEMBER));

        Product findProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ErrorCode.NOT_EXISTS_PRODUCT));

        saleRepository.save(Sale.fromAuction(finalAmount, SaleStatus.SALE_SUCCESS, findBuyer, findProduct));
    }

}
