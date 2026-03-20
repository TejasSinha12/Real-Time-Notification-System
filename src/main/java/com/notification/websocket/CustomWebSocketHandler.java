package com.notification.websocket;

import com.notification.service.NotificationWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class CustomWebSocketHandler extends TextWebSocketHandler implements WebSocketConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(CustomWebSocketHandler.class);

    private final NotificationWebSocketHandler notificationWebSocketHandler;
    private final NotificationWorker notificationWorker;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public CustomWebSocketHandler(NotificationWebSocketHandler notificationWebSocketHandler,
                                  NotificationWorker notificationWorker) {
        this.notificationWebSocketHandler = notificationWebSocketHandler;
        this.notificationWorker = notificationWorker;
    }

    @Override
    public void registerWebSocketHandlers(org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry registry) {
        registry.addHandler(this, "/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new UserHandshakeInterceptor());
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = getUserId(session);
        if (userId != null) {
            sessions.put(session.getId(), session);
            notificationWebSocketHandler.registerUser(userId, session.getId());
            notificationWorker.processPendingNotifications(userId);
            logger.info("WebSocket connection established for user: {}", userId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        logger.debug("Received message: {}", message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = getUserId(session);
        if (userId != null) {
            sessions.remove(session.getId());
            notificationWebSocketHandler.unregisterUser(userId);
            logger.info("WebSocket connection closed for user: {}", userId);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("WebSocket transport error for session: {}", session.getId(), exception);
        afterConnectionClosed(session, CloseStatus.SERVER_ERROR);
    }

    private String getUserId(WebSocketSession session) {
        Map<String, Object> attributes = session.getAttributes();
        return (String) attributes.get("userId");
    }
}
