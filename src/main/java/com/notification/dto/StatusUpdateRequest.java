package com.notification.dto;

import com.notification.model.NotificationStatus;
import jakarta.validation.constraints.NotNull;

public class StatusUpdateRequest {

    @NotNull(message = "Status is required")
    private NotificationStatus status;

    public StatusUpdateRequest() {
    }

    public StatusUpdateRequest(NotificationStatus status) {
        this.status = status;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }
}
