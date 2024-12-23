package com.auction.bid.global.websocket;

import com.auction.bid.domain.bid.BidDto;
import com.auction.bid.domain.member.Member;
import com.auction.bid.domain.member.MemberService;
import com.auction.bid.domain.product.Product;
import com.auction.bid.domain.product.ProductBidPhase;
import com.auction.bid.domain.product.ProductRepository;
import com.auction.bid.global.exception.ErrorCode;
import com.auction.bid.global.exception.exceptions.MoneyException;
import com.auction.bid.global.exception.exceptions.ProductException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static com.auction.bid.global.scheduler.ConstAuction.AUCTION;
import static com.auction.bid.global.websocket.ConstWebsocket.MEMBER;
import static com.auction.bid.global.websocket.ConstWebsocket.PRODUCT_ID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketBidHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final ProductRepository productRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MemberService memberService;

    private final Set<WebSocketSession> sessions = new HashSet<>();
    private final Map<Long, Set<WebSocketSession>> roomSessionMap = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Map<String, Object> attributes = session.getAttributes();
        Member findMember = (Member) attributes.get(MEMBER);
        Long productId = (Long) attributes.get(PRODUCT_ID);

        sendViewerCountToBidRoom(getSocketSetWhenEntered(productId, session));
        sessions.add(session);

        List<BidDto> bidDtoList = BidDto.convertToBidDtoList(
                bidListFromRedis(productId)
        );

        BidDto bidDto = bidDtoList.isEmpty() ? BidDto.emptyDtoList(productId) :
                bidDtoList.get(bidDtoList.size() - 1);

        sendMessageToEntrant(bidDto, session);

        if (findMember == null) {
            log.info("[sessionId={}] 연결됨", session.getId());
        } else {
            log.info("[memberId={}] 연결됨", findMember.getId());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String, Object> attributes = session.getAttributes();
        Member member = (Member) attributes.get(MEMBER);

        if (member == null) {
            sendMessage(session, "로그인해야 입찰이 가능합니다.");
            return;
        }

        Long productId = (Long) attributes.get(PRODUCT_ID);
        Product findProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ErrorCode.NOT_EXISTS_PRODUCT));

        if (findProduct.getProductBidPhase() == ProductBidPhase.ENDED) {
            sendMessage(session, "경매가 종료되었습니다.");
            return;
        }

        if (findProduct.getProductBidPhase() == ProductBidPhase.BEFORE) {
            sendMessage(session, "경매가 시작되야 입찰을 할 수 있습니다..");
            return;
        }

        Set<WebSocketSession> bidRoomSessions = getWebSocketSessions(productId);
        if (Objects.equals(findProduct.getMember().getId(), member.getId())) {
            sendMessage(session, "상품 판매자는 입찰할 수 없습니다.");
            return;
        }

        String payload = message.getPayload();
        MessageDto.Request dtoRequest = objectMapper.readValue(payload, MessageDto.Request.class);

        List<BidDto> bidList;
        bidList = bidListFromRedis(productId);

        Long currMaxBidAmount = getMaxBidAmount(bidList, findProduct.getStartBid());
        if (currMaxBidAmount >= dtoRequest.getBidAmount()) {
            sendMessage(session, "경매 시작가나 현재 경매가보다 입찰 금액이 높아야합니다.");
            return;
        }

        if (dtoRequest.getMaxBidLimit() < dtoRequest.getBidAmount()) {
            sendMessage(session, "입찰한계금액보다 입찰금액은 작아야합니다.");
            return;
        }

        // 세션을 기준으로 돈 작성 시에 새로운 세션에의 같은 멤버는 제대로 된 입찰이 불가능 해짐
        // 추후에 레디스를 이용해 별도로 입찰 금액을 꺼내와야 됨

        try {
            Long lastMoney = (Long) attributes.get("lastMoney");
            if (lastMoney == null) {
                lastMoney = 0L;
            }

            memberService.bidToAuction(member, dtoRequest.getBidAmount(), lastMoney);
            attributes.put("lastMoney", dtoRequest.getBidAmount());
        } catch (MoneyException | NullPointerException e) {
            log.info("MoneyEx={}", e.getMessage());
            sendMessage(session, "잔액이 부족합니다.");
            return;
        }

        MessageDto.Response response = MessageDto.Response
                .fromRequest(member, productId, dtoRequest.getBidAmount());

        putInRedis(productId, member, dtoRequest, bidList);
        sendMessageToBidRoom(response, bidRoomSessions);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Map<String, Object> attributes = session.getAttributes();
        Member member = (Member) attributes.get(MEMBER);
        Long productId = (Long) attributes.get(PRODUCT_ID);

        if (member == null) {
            log.info("{} 연결 끊김", session.getId());
            return;
        }

        roomSessionMap.get(productId).remove(session);
        sessions.remove(session);
        sendViewerCountToBidRoom(getWebSocketSessions(productId));
        log.info("{} 연결 끊김", member.getId());
    }

    public void phaseChange(Long productId, ProductBidPhase phase) {
        Set<WebSocketSession> webSocketSessions = getWebSocketSessions(productId);
        webSocketSessions.parallelStream().forEach(session -> sendMessage(session, phase));
        if (phase == ProductBidPhase.ENDED){
            closeAllSessions(webSocketSessions);
        }

    }

    private void sendMessageToBidRoom(MessageDto.Response res, Set<WebSocketSession> bidRoomSession) {
        bidRoomSession.parallelStream()
                .filter(WebSocketSession::isOpen)
                .forEach(sess -> sendMessage(sess, res));
    }

    private void sendViewerCountToBidRoom(Set<WebSocketSession> bidRoomSession) {
        String viewerCnt = "viewerCount : " + bidRoomSession.size();
        bidRoomSession.parallelStream()
                .filter(WebSocketSession::isOpen)
                .forEach(sess -> sendMessage(sess, viewerCnt));
    }


    private <T> void sendMessage(WebSocketSession session, T message) {
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void sendMessageToEntrant(BidDto bidDto, WebSocketSession session) {
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(bidDto)));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private Set<WebSocketSession> getWebSocketSessions(Long productId) {
        if (!roomSessionMap.containsKey(productId)) {
            roomSessionMap.put(productId, new HashSet<>());
        }

        Set<WebSocketSession> webSocketSessions = roomSessionMap.get(productId);
        if (webSocketSessions.size() >= 5) {
            removeClosedSession(webSocketSessions);
        }

        return webSocketSessions;
    }

    private Set<WebSocketSession> getSocketSetWhenEntered(Long productId, WebSocketSession webSocketSession) {
        if (!roomSessionMap.containsKey(productId)) {
            roomSessionMap.put(productId, new HashSet<>());
        }

        Set<WebSocketSession> webSocketSessions = roomSessionMap.get(productId);
        if (webSocketSessions.size() >= 5) {
            removeClosedSession(webSocketSessions);
        }

        webSocketSessions.add(webSocketSession);
        return webSocketSessions;
    }

    private void removeClosedSession(Set<WebSocketSession> bidRoomSession) {
        bidRoomSession.removeIf(sess -> !sessions.contains(sess));
    }

    private List<BidDto> bidListFromRedis(Long productId) {
        HashOperations<String, Long, List<BidDto>> auctionRedis = redisTemplate.opsForHash();
        if (!auctionRedis.hasKey(AUCTION, productId)) return new ArrayList<>();

        return auctionRedis.get(AUCTION, productId);
    }

    private void closeAllSessions(Set<WebSocketSession> bidRoomSession) {
        Iterator<WebSocketSession> iterator = bidRoomSession.iterator();

        while (iterator.hasNext()) {
            WebSocketSession session = iterator.next();
            if (session.isOpen()) {
                iterator.remove();
                sessions.remove(session);
                try {
                    session.close();
                } catch (IOException e) {
                    log.error("웹소켓 세션 종료 실패: sessionId={}", session.getId(), e);
                }
            }
        }
    }

    private void closeOneSession(WebSocketSession session) {
        Map<String, Object> attributes = session.getAttributes();
        Long productId = (Long) attributes.get(PRODUCT_ID);
        roomSessionMap.get(productId).remove(session);

        try {
            sendMessage(session, "경매를 진행할 수 없습니다.");
            sessions.remove(session);
            session.close();
        } catch (IOException e) {
            log.error("웹소켓 세션 종료 실패: sessionId={}", session.getId(), e);
        }
    }

    private void putInRedis(Long productId, Member member, MessageDto.Request dtoRequest, List<BidDto> bidList) {
        bidList.add(BidDto.builder()
                .productId(productId)
                .memberId(member.getId())
                .nickname(member.getNickname())
                .bidAmount(dtoRequest.getBidAmount())
                .bidTime(LocalDateTime.now())
                .build());
        HashOperations<String, Long, List<BidDto>> auctionRedis = redisTemplate.opsForHash();
        auctionRedis.put(AUCTION, productId, bidList);
    }

    private Long getMaxBidAmount(List<BidDto> bidList, long startBid) {
        if (bidList.isEmpty()) {
            return startBid - 1;
        }

        return BidDto.convertToBidDtoList(bidList)
                .get(bidList.size() - 1)
                .getBidAmount();
    }

}