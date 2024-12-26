package com.auction.bid.domain.member;

import com.auction.bid.domain.auction.Auction;
import com.auction.bid.domain.auction.AuctionStatus;
import com.auction.bid.domain.bid.Bid;
import com.auction.bid.domain.member.dto.*;
import com.auction.bid.domain.sale.Sale;
import com.auction.bid.domain.sale.SaleStatus;
import com.auction.bid.global.exception.ErrorCode;
import com.auction.bid.global.exception.exceptions.AuthException;
import com.auction.bid.global.exception.exceptions.MailException;
import com.auction.bid.global.exception.exceptions.MemberException;
import com.auction.bid.global.exception.exceptions.MoneyException;
import com.auction.bid.global.querydsl.QueryDslRepository;
import com.auction.bid.global.security.ConstSecurity;
import com.auction.bid.global.security.RefreshTokenRepository;
import com.auction.bid.global.security.jwt.JWTUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberServiceImpl implements MemberService{

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JavaMailSender mailSender;
    private final JWTUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RefreshTokenRepository refreshTokenRepository;
    private final QueryDslRepository queryDslRepository;

    @Override
    public SignUpDto.Response signUp(SignUpDto.Request request) {
        if (request.getEmailVerified() == null || !request.getEmailVerified()) {
            throw new MailException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        if (memberRepository.existsByLoginId(request.getLoginId())) {
            throw new MemberException(ErrorCode.ALREADY_EXIST_LOGIN_ID);
        }

        String encodedPassword = bCryptPasswordEncoder.encode(request.getPassword());
        Member savedMember = memberRepository.save(SignUpDto.Request.toEntity(request, encodedPassword));
        return SignUpDto.Response.fromEntity(savedMember);
    }

    @Override
    public String sendEmail(String to) {
        MimeMessage message = mailSender.createMimeMessage();
        String token = UUID.randomUUID().toString().substring(0, 6);

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("메일 인증코드");
            String emailContent = "<br>회원가입을 위한 메일 인증코드입니다.<p>" +
                    "<p>" + token + "</p>" +
                    "<p>만료시간은 10분입니다.</p>";

            helper.setText(emailContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new MailException(ErrorCode.CAN_NOT_SEND_MAIL);
        }
        redisTemplate.opsForValue().set(to, token, 10, TimeUnit.MINUTES);

        return to;
    }

    @Override
    public boolean verifyEmail(String email, String token) {
        if (Boolean.FALSE.equals(redisTemplate.hasKey(email))) {
            throw new MailException(ErrorCode.TOKEN_NOT_FOUND);
        }

        if (!Objects.equals(redisTemplate.opsForValue().get(email), token)) {
            throw new MemberException(ErrorCode.INVALID_TOKEN);
        }

        redisTemplate.delete(email);
        return true;
    }

    @Override
    public String logout(String token) {
        if (token == null || !token.startsWith(ConstSecurity.BEARER)) {
            throw new AuthException(ErrorCode.INVALID_TOKEN);
        }

        String jwtToken = jwtUtil.getTokenFromHeader(token);
        String memberUUID = jwtUtil.getMemberStrUUIDFromToken(jwtToken);
        redisTemplate.opsForValue().set(jwtToken, ConstSecurity.BLACK_LIST, 1, TimeUnit.DAYS);
        refreshTokenRepository.deleteByMemberId(UUID.fromString(memberUUID));

        return memberUUID;
    }

    @Override
    public Member findByMemberUUID(UUID memberUUID) {
        return memberRepository.findByMemberUUID(memberUUID)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_EXIST_MEMBER));
    }

    @Override
    public void addMoney(Long memberId, Long amount) {
        Member findMember = memberRepository.lockMemberForUpdate(memberId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_EXIST_MEMBER));

        findMember.addBalance(amount);
    }

    @Override
    public ChargeDto.Response chargeMoney(String token, ChargeDto.Request dtoRequest) {
        if (token == null || !token.startsWith(ConstSecurity.BEARER)) {
            throw new AuthException(ErrorCode.INVALID_TOKEN);
        }
        Member findMember = getMemberFromToken(token);
        Long currMoney = findMember.addBalance(dtoRequest.getChargeMoney());
        return new ChargeDto.Response(currMoney);
    }

    @Override
    public Long getMoney(String token) {
        Member findMember = getMemberFromToken(token);
        return findMember.getBalance();
    }

    @Override
    public void bidToAuction(Member member, Long bidMoney, Long lastMoney) {
        Long balance = member.getBalance();
        long neededMoney = bidMoney - lastMoney;
        if (balance - neededMoney < 0) {
            throw new MoneyException(ErrorCode.NOT_ENOUGH_MONEY);
        }

        member.subBalance(neededMoney);
        memberRepository.save(member);
    }

    @Override
    public void withDraw(Long memberId, Long withDrawMoney) {
        Member findMember = memberRepository.lockMemberForUpdate(memberId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_EXIST_MEMBER));

        findMember.addBalance(withDrawMoney);
    }

    @Override
    public Page<AuctionHistoryDto> getAuctionHistory(String token, int page, int size, AuctionStatus auctionStatus) {
        Pageable pageable = PageRequest.of(page, size);
        UUID memberUUID = jwtUtil.getMemberUUIDFromToken(token);
        Page<Auction> auctionPage = queryDslRepository.getAuctionList(memberUUID, pageable, auctionStatus);
        List<AuctionHistoryDto> auctionList = auctionPage.stream()
                .map(AuctionHistoryDto::fromAuction)
                .toList();

        return PageableExecutionUtils.getPage(auctionList, pageable, auctionPage::getTotalElements);
    }

    @Override
    public DetailAuctionHistoryDto getAuctionDetail(String token, Long auctionId) {
        UUID memberUUID = jwtUtil.getMemberUUIDFromToken(token);
        Member findMember = memberRepository.findByMemberUUID(memberUUID)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_EXIST_MEMBER));
        Auction findAuction = queryDslRepository.getAuctionEagerly(auctionId);
        List<BidHistoryDto> bidDtoList = getBidDtoList(findAuction.getProduct().getId());
        return DetailAuctionHistoryDto.fromAuction(findAuction, findMember.getId(), bidDtoList);
    }

    @Override
    public List<SaleHistoryDto> getSaleHistory(String token, int page, int size, SaleStatus saleStatus) {
        Pageable pageable = PageRequest.of(page, size);
        UUID memberUUID = jwtUtil.getMemberUUIDFromToken(token);
        List<Sale> saleList = queryDslRepository.getSaleList(memberUUID, pageable, saleStatus);
        return saleList.stream()
                .map(SaleHistoryDto::fromSale)
                .toList();
    }

    @Override
    public DetailSaleHistoryDto getSaleDetail(Long saleId) {
        Sale findSale = queryDslRepository.getSaleEagerly(saleId);
        List<BidHistoryDto> bidDtoList = getBidDtoList(findSale.getProduct().getId());
        return DetailSaleHistoryDto.fromSale(findSale, bidDtoList);
    }

    private List<BidHistoryDto> getBidDtoList(Long productId) {
        List<Bid> bidList = queryDslRepository.findAllByProductId(productId);
        return bidList.stream()
                .map(BidHistoryDto::fromBidEntity)
                .toList();
    }

    private Member getMemberFromToken(String token) {
        UUID memberUUID = jwtUtil.getMemberUUIDFromToken(token);
        return memberRepository.findByMemberUUID(memberUUID)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_EXIST_MEMBER));
    }

}
