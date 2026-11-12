package com.asmln.narochny.domain;

import java.time.Instant;
import java.util.UUID;

public record OutboxEntry(
        UUID id,
        UUID notificationId,
        String channel,
        String payload,
        String status,
        int attempts,
        Instant createdAt,
        Instant sentAt
) {
    public static OutboxEntry forNotification(Notification notification, String payload) {
        return new OutboxEntry(null, notification.id(), notification.channel().toString(), payload, notification.status().toString(),
                0, Instant.now(), null);
    }
}
