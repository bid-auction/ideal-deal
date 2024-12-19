package com.auction.bid.domain.auction;

import com.auction.bid.domain.bid.BidDto;
import com.auction.bid.domain.member.Member;
import com.auction.bid.domain.member.MemberRepository;
import com.auction.bid.domain.product.Product;
import com.auction.bid.domain.product.ProductRepository;
import com.auction.bid.global.exception.ErrorCode;
import com.auction.bid.global.exception.exceptions.ProductException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class AuctionServiceImpl implements AuctionService {

//    private final ProductRepository productRepository;
//    private final MemberRepository memberRepository;
//    private final AuctionRepository auctionRepository;
//
//    @Override
//    public void saveAuction(Long winnerId, Long productId, Long finalAmount, List<BidDto> bidDtoList) {
//        if (bidDtoList.isEmpty()) return;
//
//        Product findProduct = productRepository.findById(productId)
//                .orElseThrow(() -> new ProductException(ErrorCode.NOT_EXISTS_PRODUCT));
//
//        List<Long> memberIds = bidDtoList.stream()
//                .map(BidDto::getMemberId)
//                .distinct()
//                .toList();
//
//        List<Member> findMemberList = memberRepository.findAllById(memberIds);
//
//        findMemberList.forEach(member -> {
//            if (Objects.equals(member.getId(), winnerId)) {
//                auctionRepository.save(Auction.fromBid(member, findProduct, finalAmount, AuctionStatus.BID_SUCCESS));
//            } else {
//                auctionRepository.save(Auction.fromBid(member, findProduct, finalAmount, AuctionStatus.BID_FAILURE));
//            }
//        });
//    }

}