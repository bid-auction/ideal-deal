package com.auction.bid.domain.sale;

import com.auction.bid.domain.member.Member;
import com.auction.bid.domain.member.MemberRepository;
import com.auction.bid.domain.product.Product;
import com.auction.bid.domain.product.ProductRepository;
import com.auction.bid.global.exception.ErrorCode;
import com.auction.bid.global.exception.exceptions.MemberException;
import com.auction.bid.global.exception.exceptions.ProductException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService{

    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;

//    @Override
//    public void saveSale(Long sellerId, Long buyerId, Long productId, Long finalAmount) {
//        if (buyerId == null) {
//            saleRepository.save(Sale.fromAuction(null, SaleStatus.SALE_FAILURE, null, null));
//            return;
//        }
//
//        Member findBuyer = memberRepository.findById(buyerId)
//                .orElseThrow(() -> new MemberException(ErrorCode.NOT_EXIST_MEMBER));
//
//        Product findProduct = productRepository.findById(productId)
//                .orElseThrow(() -> new ProductException(ErrorCode.NOT_EXISTS_PRODUCT));
//
//        saleRepository.save(Sale.fromAuction(finalAmount, SaleStatus.SALE_SUCCESS, findBuyer, findProduct));
//    }

}
