package com.asmln.narochny.ingestion;

import com.asmln.narochny.domain.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class NotificationIngestionService {

    //// Варианты ответа
    public sealed interface Result permits Created, Duplicate, Conflict {}
    /** Создали новое уведомление: */
    public record Created(UUID notificationId) implements Result {}
    /** Такое уведомление уже приходило: */
    public record Duplicate(UUID notificationId) implements Result {}
    /** Получен тот же ключ идемпотентности, но hash запроса не совпал: */
    public record Conflict(String expectedHash, String actualHash) implements Result {}
    ////

    private final NotificationStore notificationStore;
    private final IdempotencyStore idempotencyStore;
    private final OutboxStore outboxStore;
    private final ObjectMapper objectMapper;

    public NotificationIngestionService(NotificationStore notificationStore, IdempotencyStore idempotencyStore, OutboxStore outboxStore, ObjectMapper objectMapper) {
        this.notificationStore = notificationStore;
        this.idempotencyStore = idempotencyStore;
        this.outboxStore = outboxStore;
        this.objectMapper = objectMapper;
    }

    public Result process(IngestNotification notification) throws Exception {
        String bodyHash = JsonHasher.calculateSha256(notification.body());
        var existing = idempotencyStore.findByKey(notification.idempotencyKey());
        return existing.map(entry -> matchOrConflict(entry, bodyHash))
                .orElseGet(() -> insertNew(notification, bodyHash));
    }

    private Result insertNew(IngestNotification ingestNotification, String bodyHash) {
        try {
            var notification = notificationStore.save(Notification.newIncoming(
                    ingestNotification.userId(), ingestNotification.channel(), ingestNotification.priority(), ingestNotification.body()));
            var notificationJson = objectMapper.writeValueAsString(notification);
            outboxStore.save(OutboxEntry.forNotification(notification, notificationJson));
            idempotencyStore.save(IdempotencyEntry.newEntry(
                    ingestNotification.idempotencyKey(), notification.id(), bodyHash, 202,
                    notificationJson, Instant.now().plus(7, ChronoUnit.DAYS)));
            return new Created(notification.id());
        } catch (DuplicateKeyException e) {
            return idempotencyStore.findByKey(ingestNotification.idempotencyKey())
                    .map(entry -> matchOrConflict(entry, bodyHash))
                    .orElseThrow(() -> new IllegalStateException(
                            "idempotency key disappeared after duplicate insert", e));
        }
    }

    private Result matchOrConflict(IdempotencyEntry entry, String bodyHash) {
        return entry.requestBodyHash().equals(bodyHash)
                ? new Duplicate(entry.notificationId())
                : new Conflict(entry.requestBodyHash(), bodyHash);
    }
}
