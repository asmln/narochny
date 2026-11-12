package com.asmln.narochny.domain;

import java.time.Instant;
import java.util.UUID;

public record Notification(
        UUID id,
        String userId,
        Channel channel,
        Priority priority,
        String body,
        NotificationStatus status,
        Instant sentAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static Notification newIncoming(String userId, Channel channel, Priority priority, String body) {
        return new Notification(null, userId, channel, priority, body, NotificationStatus.ACCEPTED, null, null, null);
    }
}
