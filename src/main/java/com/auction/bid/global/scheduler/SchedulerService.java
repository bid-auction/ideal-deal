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
import com.auction.bid.global.exception.exceptions.ProductException;
import com.auction.bid.global.websocket.WebSocketBidHandler;
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
    private final WebSocketBidHandler webSocketBidHandler;

    /**
     * 경매 상태를 변경하는 메서드입니다.
     *
     * @param product 경매가 진행 중인 상품
     * @param productBidPhase 변경할 경매 상태
     */
    public void changeAuctionPhase(Product product, ProductBidPhase productBidPhase) {
        product.changeAuctionPhase(productBidPhase);
        productRepository.save(product);
        webSocketBidHandler.phaseChange(product.getId(), productBidPhase);
    }

    /**
     * 입찰 기록을 저장하는 메서드입니다.
     *
     * @param productId 상품 ID
     * @param bidDtoList 입찰 데이터 리스트
     */
    public void saveBids(Long productId, List<BidDto> bidDtoList) {
        if (bidDtoList.isEmpty()) {
            return;
        }

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

    /**
     * 경매 결과를 저장하는 메서드입니다.
     *
     * @param winnerId 경매에서 승리한 회원 ID
     * @param productId 경매가 진행된 상품 ID
     * @param finalAmount 최종 입찰 금액
     * @param bidDtoList 입찰 데이터 리스트
     */
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

    /**
     * 판매 기록을 저장하는 메서드입니다.
     *
     * @param buyerId 경매에서 상품을 구매한 회원 ID
     * @param productId 경매가 진행된 상품 ID
     * @param finalAmount 최종 입찰 금액
     */
    public void saveSale(Long buyerId, Long productId, Long finalAmount) {
        Product findProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ErrorCode.NOT_EXISTS_PRODUCT));

        if (buyerId == null) {
            saleRepository.save(
                    Sale.fromAuction(null, null, SaleStatus.SALE_FAILURE, findProduct.getMember(), findProduct)
            );
            return;
        }

        saleRepository.save(
                Sale.fromAuction(buyerId, finalAmount, SaleStatus.SALE_SUCCESS, findProduct.getMember(), findProduct)
        );
    }

}
