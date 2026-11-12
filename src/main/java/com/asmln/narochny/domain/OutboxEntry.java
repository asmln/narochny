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
    public static OutboxEntry withIdForNotification(UUID id, Notification notification, String payload) {
        return new OutboxEntry(id, notification.id(), notification.channel().toString(), payload, notification.status().toString(),
                0, Instant.now(), null);
    }

    public static OutboxEntry forNotification(Notification notification, String payload) {
        return new OutboxEntry(null, notification.id(), notification.channel().toString(), payload, notification.status().toString(),
                0, Instant.now(), null);
    }

    public OutboxEntry withId(UUID id) {
        return new OutboxEntry(id, notificationId, channel, payload, status,
                attempts, createdAt, sentAt);
    }
}
