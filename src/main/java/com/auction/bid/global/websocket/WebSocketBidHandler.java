package com.auction.bid.global.websocket;

import com.auction.bid.domain.bid.BidDto;
import com.auction.bid.domain.member.Member;
import com.auction.bid.domain.product.Product;
import com.auction.bid.domain.product.ProductBidPhase;
import com.auction.bid.domain.product.ProductService;
import com.auction.bid.global.exception.ErrorCode;
import com.auction.bid.global.exception.exceptions.BidException;
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

import static com.auction.bid.global.scheduler.ConstAuction.AUCTION;
import static com.auction.bid.global.websocket.ConstWebsocket.MEMBER;
import static com.auction.bid.global.websocket.ConstWebsocket.PRODUCT_ID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketBidHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final ProductService productService;
    private final RedisTemplate<String, Object> redisTemplate;

    private final Set<WebSocketSession> sessions = new HashSet<>();
    private final Map<Long, Set<WebSocketSession>> roomSessionMap = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Map<String, Object> attributes = session.getAttributes();
        Member findMember = (Member) attributes.get(MEMBER);
        Long productId = (Long) attributes.get(PRODUCT_ID);

        if (findMember == null || productId == null) {
            closeOneSession(session);
        }

        sendViewerCountToBidRoom(getSocketSetWhenEntered(productId, session));
        sessions.add(session);

        List<BidDto> bidDtoList;
        try {
            bidDtoList = BidDto.convertToBidDtoList(
                    bidListFromRedis(
                            productId,
                            roomSessionMap.get(productId)
                    ));
        } catch (BidException e) {
            log.info("경매 종료={}", e.getMessage());
            return;
        }

        BidDto bidDto = bidDtoList.isEmpty() ? BidDto.emptyDtoList(productId) :
                bidDtoList.get(bidDtoList.size() - 1);

        sendMessageToEntrant(bidDto, session);

        log.info("[{}] 연결됨", findMember.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String, Object> attributes = session.getAttributes();
        Member member = (Member) attributes.get(MEMBER);
        Long productId = (Long) attributes.get(PRODUCT_ID);

        Product findProduct = productService.findById(productId);
        Set<WebSocketSession> bidRoomSessions = getWebSocketSessions(productId);

        if (findProduct.getProductBidPhase() != ProductBidPhase.ONGOING) {
            closeAllSessions(bidRoomSessions);
        }

        String payload = message.getPayload();
        MessageDto.Request dtoRequest = objectMapper.readValue(payload, MessageDto.Request.class);

        List<BidDto> bidList;
        try {
            bidList = bidListFromRedis(productId, roomSessionMap.get(productId));
        } catch (BidException e) {
            log.info("경매 종료={}", e.getMessage());
            return;
        }

        Long currMaxBidAmount = getMaxBidAmount(bidList, findProduct.getStartBid());
        if (currMaxBidAmount >= dtoRequest.getBidAmount()) {
            sendMessage(session, "경매 시작가나 현재 경매가보다 입찰 금액이 높아야합니다.");
            return;
        }

        // 현재 잔액에서의 예외와 잔액 소모시켜야됨
        // 잔액 돌려주기는 redis에서 해야할 듯??
        // 가입안한 회원이와도 되는지 확인

        MessageDto.Response response = MessageDto.Response
                        .fromRequest(member, productId, dtoRequest.getBidAmount());

        putInRedis(productId, member, dtoRequest, bidList);
        sendMessageToBidRoom(response, bidRoomSessions);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Map<String, Object> attributes = session.getAttributes();
        Member member;
        Long productId;
        try {
            member = (Member) attributes.get(MEMBER);
            productId = (Long) attributes.get(PRODUCT_ID);
        } catch (NullPointerException e) {
            log.info("NullPointerEx={}", e.getMessage());
            return;
        }

        roomSessionMap.get(productId).remove(session);
        sessions.remove(session);
        sendViewerCountToBidRoom(getWebSocketSessions(productId));
        log.info("{} 연결 끊김", member.getId());
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
        if(!roomSessionMap.containsKey(productId)){
            roomSessionMap.put(productId, new HashSet<>());
        }

        Set<WebSocketSession> webSocketSessions = roomSessionMap.get(productId);
        if (webSocketSessions.size() >= 5) {
            removeClosedSession(webSocketSessions);
        }

        return webSocketSessions;
    }

    private Set<WebSocketSession> getSocketSetWhenEntered(Long productId, WebSocketSession webSocketSession) {
        if(!roomSessionMap.containsKey(productId)){
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

    private List<BidDto> bidListFromRedis(Long productId, Set<WebSocketSession> bidRoomSession) {
        HashOperations<String, Long, List<BidDto>> auctionRedis = redisTemplate.opsForHash();

        if (!auctionRedis.hasKey(AUCTION, productId)) {
            closeAllSessions(bidRoomSession);
            throw new BidException(ErrorCode.NOT_EXIST_AUCTION);
        }

        return auctionRedis.get(AUCTION, productId);
    }

    private void closeAllSessions(Set<WebSocketSession> bidRoomSession) {
        bidRoomSession.parallelStream().forEach(session -> {
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
        });
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
        if (bidList.isEmpty()){
            return startBid - 1;
        }

        return BidDto.convertToBidDtoList(bidList)
                .get(bidList.size() - 1)
                .getBidAmount();
    }

}