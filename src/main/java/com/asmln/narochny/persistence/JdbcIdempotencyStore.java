package com.asmln.narochny.persistence;

import com.asmln.narochny.domain.IdempotencyEntry;
import com.asmln.narochny.domain.IdempotencyStore;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.Optional;

@Component
public class JdbcIdempotencyStore implements IdempotencyStore {

    private final JdbcClient jdbc;

    JdbcIdempotencyStore(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<IdempotencyEntry> findByKey(String key) {
        return jdbc.sql("""
                SELECT key, notification_id, request_body_hash, response_status,
                       response_body::text AS response_body, created_at, expires_at
                FROM idempotency_keys
                WHERE key = :key
                """)
                .param("key", key)
                .query(IdempotencyEntry.class)
                .optional();
    }

    @Override
    public void save(IdempotencyEntry entry) {
        jdbc.sql("""
                INSERT INTO idempotency_keys
                    (key, notification_id, request_body_hash, response_status, response_body, expires_at)
                VALUES (:key, :notificationId, :bodyHash, :status, :responseBody::jsonb, :expiresAt)
                """)
                .param("key", entry.key())
                .param("notificationId", entry.notificationId())
                .param("bodyHash", entry.requestBodyHash())
                .param("status", entry.responseStatus())
                .param("responseBody", entry.responseBody())
                .param("expiresAt", entry.expiresAt() == null ? null : entry.expiresAt().atOffset(ZoneOffset.UTC))
                .update();
    }
}
