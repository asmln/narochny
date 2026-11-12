package com.asmln.narochny.domain;

import java.time.Instant;
import java.util.UUID;

public record IdempotencyEntry(
        String key,
        UUID notificationId,
        String requestBodyHash,
        Integer responseStatus,
        String responseBody,
        Instant createdAt,
        Instant expiresAt
) {
    public static IdempotencyEntry newEntry(String key, UUID notificationId, String bodyHash,
                                            int status, String responseBody, Instant expiresAt) {
        return new IdempotencyEntry(key, notificationId, bodyHash, status, responseBody, null, expiresAt);
    }
}
