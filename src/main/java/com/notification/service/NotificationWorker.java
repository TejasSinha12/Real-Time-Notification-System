package com.notification.service;

import com.notification.model.Notification;
import com.notification.model.NotificationStatus;
import com.notification.repository.NotificationRepository;
import com.notification.websocket.NotificationWebSocketHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Background worker that processes notifications from the queue.
 * Implements retry logic: up to 3 attempts with 2s delay between retries.
 * If all retries fail, marks notification as FAILED.
 * 
 * Retry mechanism ensures reliability in case of temporary delivery failures
 */
@Service
public class NotificationWorker {

    private static final Logger logger = LoggerFactory.getLogger(NotificationWorker.class);
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 2000;

    private final NotificationQueue queue;
    private final NotificationRepository repository;
    private final NotificationWebSocketHandler webSocketHandler;
    private final ExecutorService executor;

    public NotificationWorker(NotificationQueue queue, NotificationRepository repository,
                             NotificationWebSocketHandler webSocketHandler) {
        this.queue = queue;
        this.repository = repository;
        this.webSocketHandler = webSocketHandler;
        this.executor = Executors.newSingleThreadExecutor();
    }

    @PostConstruct
    public void start() {
        executor.submit(this::processQueue);
        logger.info("Notification worker started");
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
        logger.info("Notification worker stopped");
    }

    private void processQueue() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Notification notification = queue.poll();
                if (notification != null) {
                    processNotification(notification);
                } else {
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("Error processing notification", e);
            }
        }
    }

    private void processNotification(Notification notification) {
        logger.info("Processing notification ID: {} for recipient: {}", notification.getId(), notification.getRecipientId());

        boolean delivered = webSocketHandler.sendNotification(notification);

        if (delivered) {
            notification.setStatus(NotificationStatus.SENT);
            notification.setDeliveredAt(LocalDateTime.now());
            repository.save(notification);
            logger.info("Notification {} delivered successfully", notification.getId());
        } else {
            notification.setRetryCount(notification.getRetryCount() + 1);
            
            if (notification.getRetryCount() >= MAX_RETRIES) {
                notification.setStatus(NotificationStatus.FAILED);
                repository.save(notification);
                logger.warn("Notification {} failed after {} retries", notification.getId(), MAX_RETRIES);
            } else {
                repository.save(notification);
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                queue.add(notification);
                logger.info("Notification {} queued for retry (attempt {})", notification.getId(), notification.getRetryCount());
            }
        }
    }

    public void processPendingNotifications(String recipientId) {
        List<Notification> pendingNotifications = repository.findByRecipientIdAndStatus(recipientId, NotificationStatus.PENDING);
        
        for (Notification notification : pendingNotifications) {
            boolean delivered = webSocketHandler.sendNotification(notification);
            
            if (delivered) {
                notification.setStatus(NotificationStatus.SENT);
                notification.setDeliveredAt(LocalDateTime.now());
                repository.save(notification);
                logger.info("Pending notification {} delivered to {}", notification.getId(), recipientId);
            }
        }
    }
}
