package com.auction.bid.domain.bid;

import com.auction.bid.domain.member.Member;
import com.auction.bid.domain.product.Product;
import com.auction.bid.domain.product.ProductBidPhase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BidDto {

    private Long productId;
    private Long memberId;
    private String nickname;
    private Long bidAmount;
    private LocalDateTime bidTime;

    public static BidDto emptyDtoList(Long productId) {
        return BidDto.builder()
                .productId(productId)
                .memberId(null)
                .nickname(null)
                .bidAmount(null)
                .bidTime(null)
                .build();
    }

    public static Bid toBidEntity(BidDto bidDto, Member member, Product product) {

        return Bid.builder()
                .member(member)
                .product(product)
                .bidAmount(bidDto.getBidAmount())
                .bidTime(bidDto.getBidTime())
                .build();
    }

    public static List<BidDto> convertToBidDtoList(List<BidDto> bidDtoList) {
        List<BidDto> resultList = new ArrayList<>();

        for (Object bidData : bidDtoList) {
            LinkedHashMap<String, Object> bidMap = (LinkedHashMap<String, Object>) bidData;

            Long productId = ((Integer) bidMap.get("productId")).longValue();
            Long memberId = ((Integer) bidMap.get("memberId")).longValue();
            String nickname = (String) bidMap.get("nickname");
            Long bidAmount = ((Integer) bidMap.get("bidAmount")).longValue();
            LocalDateTime bidTime = formatTime((ArrayList<Integer>) bidMap.get("bidTime"));

            resultList.add(bidDtoBuild(productId, memberId, nickname, bidAmount, bidTime));
        }

        return resultList;
    }

    private static LocalDateTime formatTime(ArrayList<Integer> bidTimeList) {
        String bidTimeStr = String.format("%04d-%02d-%02d %02d:%02d:%02d.%03d",
                bidTimeList.get(0),   // year
                bidTimeList.get(1),   // month
                bidTimeList.get(2),   // day
                bidTimeList.get(3),   // hour
                bidTimeList.get(4),   // minute
                bidTimeList.get(5),   // second
                bidTimeList.get(6));  // millisecond

        if (bidTimeStr.length() > 23) {
            bidTimeStr = bidTimeStr.substring(0, 23);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

        return LocalDateTime.parse(bidTimeStr, formatter);
    }

    private static BidDto bidDtoBuild(Long productId, Long memberId, String nickname, Long bidAmount, LocalDateTime bidTime) {
        return BidDto.builder()
                .productId(productId)
                .memberId(memberId)
                .nickname(nickname)
                .bidAmount(bidAmount)
                .bidTime(bidTime)
                .build();
    }

}

