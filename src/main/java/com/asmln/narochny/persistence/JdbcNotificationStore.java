package com.asmln.narochny.persistence;

import com.asmln.narochny.domain.Notification;
import com.asmln.narochny.domain.NotificationStore;
import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class JdbcNotificationStore implements NotificationStore {

    private final JdbcClient jdbc;

    public JdbcNotificationStore(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Notification save(Notification entry) {
        var id = entry.id() == null ? UuidCreator.getTimeOrderedEpoch() : entry.id();
        return jdbc.sql("""
            INSERT INTO notifications (id, user_id, channel, priority, body, status)
            VALUES (:id, :userId, :channel, :priority, :body, :status)
            RETURNING id, user_id, channel, priority, body, status, sent_at, created_at, updated_at
            """)
                .param("id", id)
                .param("userId", entry.userId())
                .param("channel", entry.channel().name())
                .param("priority", entry.priority().name())
                .param("body", entry.body())
                .param("status", entry.status().name())
                .query(Notification.class)
                .single();
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return jdbc.sql("""
                SELECT id, user_id, channel, priority, body, status, sent_at, created_at, updated_at
                FROM notifications
                WHERE id = :id
                """)
                .param("id", id)
                .query(Notification.class)        // record-проекция по имени колонок
                .optional();
    }
}
