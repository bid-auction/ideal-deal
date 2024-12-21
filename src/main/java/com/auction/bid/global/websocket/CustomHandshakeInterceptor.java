package com.auction.bid.global.websocket;

import com.auction.bid.domain.member.Member;
import com.auction.bid.domain.member.MemberService;
import com.auction.bid.domain.product.ProductService;
import com.auction.bid.global.exception.ErrorCode;
import com.auction.bid.global.exception.exceptions.SocketException;
import com.auction.bid.global.security.jwt.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.UUID;

import static com.auction.bid.global.websocket.ConstWebsocket.MEMBER;
import static com.auction.bid.global.websocket.ConstWebsocket.PRODUCT_ID;

@Slf4j
@RequiredArgsConstructor
public class CustomHandshakeInterceptor implements HandshakeInterceptor {

    private final JWTUtil jwtUtil;
    private final MemberService memberService;
    private final ProductService productService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        Long productId = getOngoingAuctionProductId(request);
        if (productId == null) return false;

        try {
            String token = request.getHeaders().getFirst("Cookie");
            UUID memberUUID = jwtUtil.getMemberUUIDFromToken(token);
            Member findMember = memberService.findByMemberUUID(memberUUID);
            attributes.put(MEMBER, findMember);
        } catch (Exception e) {
            log.info("인증되지 않은 접근{}", e.getMessage());
            attributes.put(MEMBER, null);
        }

        attributes.put(PRODUCT_ID, productId);
        log.info("BeforeHandshake Success");
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        log.error("AfterHandshake ex=", exception);
    }

    private Long getOngoingAuctionProductId(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        log.info(path);

        String[] pathSegments = path.split("/");
        Long id = Long.parseLong(pathSegments[3]);

        if (pathSegments.length > 4) {
            throw new SocketException(ErrorCode.INVALID_ACCESS);
        }

        if (!productService.isOnGoing(id)) return null;

        return id;
    }

}
