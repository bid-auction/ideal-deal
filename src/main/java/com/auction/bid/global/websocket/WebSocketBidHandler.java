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

    /**
     * WebSocket 연결이 확립된 후 호출됩니다.
     * 새로운 세션을 활성 세션 목록에 추가하고, 현재 입찰 정보를 전송합니다.
     *
     * @param session 새로 연결된 WebSocket 세션
     */
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

    /**
     * 입찰 메시지를 처리하고, 입찰 조건을 확인한 후, 입찰 기록을 업데이트합니다.
     *
     * @param session WebSocket 세션
     * @param message 입찰 메시지
     * @throws Exception 메시지 처리 중 예외 발생 시
     */
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

        if (!bidList.isEmpty()) {
            List<BidDto> bidDtos = BidDto.convertToBidDtoList(bidList);
            BidDto bidDto = bidDtos.get(bidDtos.size() - 1);
            if (Objects.equals(bidDto.getMemberId(), member.getId())) {
                sendMessage(session, "현재 최고 입찰자는 본인입니다.");
                return;
            }
        }

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

    /**
     * WebSocket 연결이 종료된 후 호출됩니다. 세션을 목록에서 제거하고, 시청자 수를 업데이트합니다.
     *
     * @param session 종료된 WebSocket 세션
     * @param status 종료 상태
     */
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

    /**
     * 경매 진행 상태(입찰 단계)를 변경하고, 해당 상태를 모든 WebSocket 클라이언트에게 전송합니다.
     * 경매가 종료된 경우, 모든 WebSocket 세션을 종료합니다.
     *
     * @param productId 경매 상품 ID
     * @param phase 변경될 입찰 단계
     */
    public void phaseChange(Long productId, ProductBidPhase phase) {
        Set<WebSocketSession> webSocketSessions = getWebSocketSessions(productId);
        webSocketSessions.parallelStream()
                .filter(WebSocketSession::isOpen)
                .forEach(session -> sendMessage(session, phase));
        if (phase == ProductBidPhase.ENDED){
            closeAllSessions(webSocketSessions);
        }

    }

    /**
     * 경매 방에 있는 모든 사용자에게 입찰 정보를 전송합니다.
     *
     * @param res 입찰 정보
     * @param bidRoomSession 경매 방에 참여한 WebSocket 세션들
     */
    private void sendMessageToBidRoom(MessageDto.Response res, Set<WebSocketSession> bidRoomSession) {
        bidRoomSession.parallelStream()
                .filter(WebSocketSession::isOpen)
                .forEach(sess -> sendMessage(sess, res));
    }

    /**
     * 경매 방에 있는 모든 사용자에게 현재 시청자 수를 전송합니다.
     *
     * @param bidRoomSession 경매 방에 참여한 WebSocket 세션들
     */
    private void sendViewerCountToBidRoom(Set<WebSocketSession> bidRoomSession) {
        String viewerCnt = "viewerCount : " + bidRoomSession.size();
        bidRoomSession.parallelStream()
                .filter(WebSocketSession::isOpen)
                .forEach(sess -> sendMessage(sess, viewerCnt));
    }

    /**
     * WebSocket 세션에 메시지를 전송합니다.
     *
     * @param session 메시지를 받을 WebSocket 세션
     * @param message 전송할 메시지
     * @param <T> 메시지 타입
     */
    private <T> void sendMessage(WebSocketSession session, T message) {
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 입장할 때의 입찰 중인 정보 전달.
     *
     * @param bidDto 입찰 정보
     * @param session 입찰자 WebSocket 세션
     */
    private void sendMessageToEntrant(BidDto bidDto, WebSocketSession session) {
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(bidDto)));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 특정 경매 상품에 연결된 모든 WebSocket 세션을 반환합니다.
     *
     * @param productId 경매 상품 ID
     * @return 해당 경매 상품에 연결된 WebSocket 세션들
     */
    private Set<WebSocketSession> getWebSocketSessions(Long productId) {
        if (!roomSessionMap.containsKey(productId)) {
            roomSessionMap.put(productId, new HashSet<>());
        }

        Set<WebSocketSession> webSocketSessions = roomSessionMap.get(productId);
        return webSocketSessions;
    }

    /**
     * 경매 방에 새로 접속한 사용자 세션을 추가하고, 연결되어 있지 않은 세션들은 제거합니다.
     *
     * @param productId 경매 상품 ID
     * @param webSocketSession 새로 접속한 WebSocket 세션
     * @return 접속된 세션들
     */
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

    /**
     * 종료된 WebSocket 세션을 목록에서 제거합니다.
     *
     * @param bidRoomSession 경매 방에 있는 WebSocket 세션들
     */
    private void removeClosedSession(Set<WebSocketSession> bidRoomSession) {
        bidRoomSession.removeIf(sess -> !sessions.contains(sess));
    }

    /**
     * Redis에서 해당 경매 상품의 입찰 내역을 가져옵니다.
     *
     * @param productId 경매 상품 ID
     * @return 해당 상품의 입찰 내역
     */
    private List<BidDto> bidListFromRedis(Long productId) {
        HashOperations<String, Long, List<BidDto>> auctionRedis = redisTemplate.opsForHash();
        if (!auctionRedis.hasKey(AUCTION, productId)) return new ArrayList<>();

        return auctionRedis.get(AUCTION, productId);
    }

    /**
     * 경매 방에 있는 모든 WebSocket 세션을 종료합니다.
     *
     * @param bidRoomSession 경매 방에 있는 WebSocket 세션들
     */
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

    /**
     * 입찰 정보를 Redis에 저장합니다.
     *
     * @param productId 경매 상품 ID
     * @param member 입찰자
     * @param dtoRequest 입찰 요청 정보
     * @param bidList 기존 입찰 내역
     */
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

    /**
     * 입찰 내역에서 현재 최고 입찰 금액을 반환합니다.
     *
     * @param bidList 입찰 내역
     * @param startBid 경매 시작 금액
     * @return 현재 최고 입찰 금액
     */
    private Long getMaxBidAmount(List<BidDto> bidList, long startBid) {
        if (bidList.isEmpty()) {
            return startBid - 1;
        }

        return BidDto.convertToBidDtoList(bidList)
                .get(bidList.size() - 1)
                .getBidAmount();
    }

}