package com.asmln.narochny.persistence;

import com.asmln.narochny.domain.OutboxEntry;
import com.asmln.narochny.domain.OutboxStore;
import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.Optional;

@Component
public class JdbcOutboxStore implements OutboxStore {

    private final JdbcClient jdbc;

    JdbcOutboxStore(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<OutboxStore> findByKey(String key) {
        return jdbc.sql("""
                SELECT key, notification_id, request_body_hash, response_status,
                       response_body::text AS response_body, created_at, expires_at
                FROM idempotency_keys
                WHERE key = :key
                """)
                .param("key", key)
                .query(OutboxStore.class)
                .optional();
    }

    @Override
    public void save(OutboxEntry entry) {
        var id = entry.id() == null ? UuidCreator.getTimeOrderedEpoch() : entry.id();
        jdbc.sql("""
                INSERT INTO outbox
                    (id, notification_id, payload, status, attempts, created_at)
                VALUES (:id, :notificationId, :payload, :status, :attempts, :createdAt)
                """)
                .param("id", id)
                .param("notificationId", entry.notificationId())
                .param("payload", entry.payload())
                .param("status", entry.status())
                .param("attempts", entry.attempts())
                .param("createdAt", entry.createdAt() == null ? null : entry.createdAt().atOffset(ZoneOffset.UTC))
                .update();
    }
}
