package com.notification.service;

import com.notification.model.Notification;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * In-memory queue for async notification processing.
 * Thread-safe implementation using ConcurrentLinkedQueue.
 * Notifications are added here when created and consumed by NotificationWorker.
 * 
 * Queue-based processing decouples API from delivery logic for better scalability
 */
@Component
public class NotificationQueue {

    private final ConcurrentLinkedQueue<Notification> queue = new ConcurrentLinkedQueue<>();

    public void add(Notification notification) {
        queue.offer(notification);
    }

    public Notification poll() {
        return queue.poll();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int size() {
        return queue.size();
    }
}
