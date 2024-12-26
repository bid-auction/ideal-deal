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

}