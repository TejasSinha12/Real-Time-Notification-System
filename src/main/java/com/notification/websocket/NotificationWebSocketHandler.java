package com.notification.websocket;

import com.notification.dto.NotificationResponse;
import com.notification.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles real-time notification delivery via WebSocket.
 * Maps userId to session and sends notifications to connected users.
 * If user is offline, notification remains in queue for retry.
 */
@Component
public class NotificationWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(NotificationWebSocketHandler.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, String> connectedUsers = new ConcurrentHashMap<>();

    public NotificationWebSocketHandler(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public boolean sendNotification(Notification notification) {
        String sessionId = connectedUsers.get(notification.getRecipientId());
        
        if (sessionId != null) {
            try {
                NotificationResponse response = NotificationResponse.fromEntity(notification);
                messagingTemplate.convertAndSendToUser(
                    notification.getRecipientId(),
                    "/queue/notifications",
                    response
                );
                logger.info("Notification {} sent via WebSocket to user {}", notification.getId(), notification.getRecipientId());
                return true;
            } catch (Exception e) {
                logger.error("Failed to send notification {} via WebSocket", notification.getId(), e);
                return false;
            }
        }
        
        logger.info("User {} not connected, notification {} will be delivered on reconnect", 
            notification.getRecipientId(), notification.getId());
        return false;
    }

    public void registerUser(String userId, String sessionId) {
        connectedUsers.put(userId, sessionId);
        logger.info("User {} connected with session {}", userId, sessionId);
    }

    public void unregisterUser(String userId) {
        connectedUsers.remove(userId);
        logger.info("User {} disconnected", userId);
    }

    public boolean isUserConnected(String userId) {
        return connectedUsers.containsKey(userId);
    }
}
