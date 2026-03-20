package com.notification.service;

import com.notification.model.Notification;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentLinkedQueue;

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
