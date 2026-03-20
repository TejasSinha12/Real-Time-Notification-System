package com.notification.service;

import com.notification.dto.NotificationRequest;
import com.notification.dto.NotificationResponse;
import com.notification.dto.StatusUpdateRequest;
import com.notification.model.Notification;
import com.notification.model.NotificationStatus;
import com.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private static final int RATE_LIMIT_PER_MINUTE = 10;

    private final NotificationRepository repository;
    private final NotificationQueue queue;
    private final Map<String, List<Long>> rateLimitMap = new ConcurrentHashMap<>();

    public NotificationService(NotificationRepository repository, NotificationQueue queue) {
        this.repository = repository;
        this.queue = queue;
    }

    public NotificationResponse createNotification(NotificationRequest request) {
        if (!checkRateLimit(request.getRecipientId())) {
            throw new RateLimitExceededException("Rate limit exceeded. Maximum 10 notifications per minute.");
        }

        Notification notification = new Notification(
            request.getRecipientId(),
            request.getTitle(),
            request.getMessage()
        );

        notification = repository.save(notification);
        queue.add(notification);
        
        logger.info("Notification created with ID: {} for recipient: {}", notification.getId(), notification.getRecipientId());
        
        return NotificationResponse.fromEntity(notification);
    }

    public List<NotificationResponse> getNotificationsByRecipient(String recipientId) {
        return repository.findByRecipientId(recipientId)
                .stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public NotificationResponse getNotificationById(Long id) {
        Notification notification = repository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with ID: " + id));
        return NotificationResponse.fromEntity(notification);
    }

    public NotificationResponse updateStatus(Long id, StatusUpdateRequest request) {
        Notification notification = repository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with ID: " + id));

        notification.setStatus(request.getStatus());
        if (request.getStatus() == NotificationStatus.DELIVERED) {
            notification.setDeliveredAt(LocalDateTime.now());
        }

        notification = repository.save(notification);
        
        logger.info("Notification {} status updated to {}", id, request.getStatus());
        
        return NotificationResponse.fromEntity(notification);
    }

    private boolean checkRateLimit(String recipientId) {
        LocalDateTime now = LocalDateTime.now();
        List<Long> recentTimestamps = rateLimitMap.computeIfAbsent(recipientId, k -> new java.util.ArrayList<>());
        
        recentTimestamps.removeIf(ts -> ts < now.minusMinutes(1).toEpochSecond(java.time.ZoneOffset.UTC));
        
        if (recentTimestamps.size() >= RATE_LIMIT_PER_MINUTE) {
            return false;
        }
        
        recentTimestamps.add(now.toEpochSecond(java.time.ZoneOffset.UTC));
        return true;
    }

    public static class NotificationNotFoundException extends RuntimeException {
        public NotificationNotFoundException(String message) {
            super(message);
        }
    }

    public static class RateLimitExceededException extends RuntimeException {
        public RateLimitExceededException(String message) {
            super(message);
        }
    }
}
