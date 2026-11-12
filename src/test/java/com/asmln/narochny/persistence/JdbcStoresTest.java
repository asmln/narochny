package com.asmln.narochny.persistence;

import com.asmln.narochny.TestcontainersConfig;
import com.asmln.narochny.domain.*;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = TestcontainersConfig.class)
@Import({JdbcNotificationStore.class, JdbcIdempotencyStore.class})
public class JdbcStoresTest {
    @Autowired
    NotificationStore notificationStore;
    @Autowired
    IdempotencyStore keyStore;
    @Autowired
    TransactionTemplate tx;

    @Test
    void save() {
        var saved = notificationStore.save(
                Notification.newIncoming("u-42", Channel.EMAIL, Priority.P1, "hello"));

        assertThat(saved.id()).isNotNull();
        assertThat(saved.createdAt()).isNotNull();
        assertThat(saved.updatedAt()).isNotNull();

        var reloaded = notificationStore.findById(saved.id()).orElseThrow();
        assertThat(reloaded).usingRecursiveComparison().ignoringFields("createdAt", "updatedAt")
                .isEqualTo(saved);
        assertThat(reloaded.createdAt()).isCloseTo(saved.createdAt(), within(1, ChronoUnit.SECONDS)); // timestamps из БД может отличаться на доли мс.
    }

    @Test
    void idempotencyWithJsonb() throws JSONException {
        var n = notificationStore.save(Notification.newIncoming("u-7", Channel.PUSH, Priority.P1, "ping"));
        var expiresAt = Instant.now().plus(7, ChronoUnit.DAYS);

        keyStore.save(IdempotencyEntry.newEntry(
                "req-1", n.id(), "sha256:abc", 202,
                "{\"status\":\"ACCEPTED\"}", expiresAt));

        var found = tx.execute(s ->
                keyStore.findByKey("req-1").orElseThrow());

        assertThat(found.notificationId()).isEqualTo(n.id());
        JSONAssert.assertEquals(
                "{\"status\":\"ACCEPTED\"}",
                found.responseBody(),
                JSONCompareMode.STRICT
        );
        assertThat(found.expiresAt()).isCloseTo(expiresAt, within(1, ChronoUnit.SECONDS)); // timestamps из БД может отличаться от рассчитанного значения в коде.
    }
}
