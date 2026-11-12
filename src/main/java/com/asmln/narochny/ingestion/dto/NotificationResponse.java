package com.asmln.narochny.ingestion.dto;

import com.asmln.narochny.domain.NotificationStatus;

import java.util.UUID;

public record NotificationResponse(
        UUID notificationId,
        NotificationStatus status
) {}
