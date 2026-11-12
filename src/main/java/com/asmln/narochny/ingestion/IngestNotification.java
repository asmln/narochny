package com.asmln.narochny.ingestion;

import com.asmln.narochny.domain.Channel;
import com.asmln.narochny.domain.Priority;

public record IngestNotification(
        String idempotencyKey,
        String userId,
        Channel channel,
        Priority priority,
        String body
) {}
