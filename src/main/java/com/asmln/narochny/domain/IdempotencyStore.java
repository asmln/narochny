package com.asmln.narochny.domain;

import java.util.Optional;

public interface IdempotencyStore {
    Optional<IdempotencyEntry> findByKey(String key);
    void save(IdempotencyEntry entry);
}
