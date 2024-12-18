package com.auction.bid.domain.bid;

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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class BidServiceImpl implements BidService{

//    private final MemberRepository memberRepository;
//    private final ProductRepository productRepository;
//    private final BidRepository bidRepository;
//
//    public void saveBids(Long productId, List<BidDto> bidDtoList) {
//        Product findProduct = productRepository.findById(productId)
//                .orElseThrow(() -> new ProductException(ErrorCode.NOT_EXISTS_PRODUCT));
//
//        if (bidDtoList.isEmpty()) {
//            bidRepository.save(BidDto.toBidEntity(new BidDto(), null, findProduct));
//            return;
//        }
//
//        List<Long> memberIds = bidDtoList.stream()
//                .map(BidDto::getMemberId)
//                .distinct()
//                .toList();
//
//        List<Member> findMemberList = memberRepository.findAllById(memberIds);
//        Map<Long, Member> memberMap = findMemberList.stream()
//                .collect(Collectors.toMap(Member::getId, m -> m));
//
//        bidDtoList.stream()
//                .map(bidDto -> BidDto.toBidEntity(
//                        bidDto,
//                        memberMap.get(bidDto.getMemberId()),
//                        findProduct)
//                )
//                .forEach(bidRepository::save);
//    }

}
