package com.asmln.narochny.domain;

import java.util.Optional;
import java.util.UUID;

public interface NotificationStore {
    Notification save(Notification n);
    Optional<Notification> findById(UUID id);
}
