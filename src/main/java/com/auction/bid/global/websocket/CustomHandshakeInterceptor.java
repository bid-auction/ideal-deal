package com.auction.bid.global.websocket;

import com.auction.bid.domain.member.Member;
import com.auction.bid.domain.member.MemberService;
import com.auction.bid.domain.product.Product;
import com.auction.bid.domain.product.ProductBidPhase;
import com.auction.bid.domain.product.ProductService;
import com.auction.bid.global.exception.ErrorCode;
import com.auction.bid.global.exception.exceptions.ProductException;
import com.auction.bid.global.exception.exceptions.SocketException;
import com.auction.bid.global.security.jwt.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.text.ParseException;
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
        Long productId;
        try {
            productId = getProductIdFromURI(request);
        } catch (SocketException e) {
            log.info("SocketEx={}", e.getMessage());
            return false;
        }

        Product findProduct;
        try {
            findProduct = productService.findById(productId);
        } catch (ProductException e) {
            log.info("ProductEx={}", e.getMessage());
            return false;
        }

        if (findProduct.getProductBidPhase() == ProductBidPhase.ENDED) {
            return false;
        }

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

    private Long getProductIdFromURI(ServerHttpRequest request) {
        String path = request.getURI().getPath();

        String[] pathSegments = path.split("/");

        Long id;
        try {
            id = Long.parseLong(pathSegments[3]);
        } catch (NumberFormatException e) {
            throw new SocketException(ErrorCode.INVALID_ACCESS);
        }

        if (pathSegments.length > 4) {
            throw new SocketException(ErrorCode.INVALID_ACCESS);
        }

        return id;
    }

}
