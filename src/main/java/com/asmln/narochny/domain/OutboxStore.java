package com.asmln.narochny.domain;

import java.util.Optional;

public interface OutboxStore {
    Optional<OutboxStore> findByKey(String key);
    void save(OutboxEntry entry);
}
