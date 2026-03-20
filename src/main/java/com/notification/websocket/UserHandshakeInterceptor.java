package com.notification.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;

public class UserHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(UserHandshakeInterceptor.class);

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        URI uri = request.getURI();
        String query = uri.getQuery();
        
        if (query != null && query.contains("userId=")) {
            String userId = extractUserId(query);
            if (userId != null) {
                attributes.put("userId", userId);
                logger.info("Handshake: User {} connecting", userId);
                return true;
            }
        }
        
        logger.warn("Handshake failed: No userId provided");
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                WebSocketHandler wsHandler, Exception exception) {
    }

    private String extractUserId(String query) {
        String[] params = query.split("&");
        for (String param : params) {
            if (param.startsWith("userId=")) {
                return param.substring(7);
            }
        }
        return null;
    }
}
