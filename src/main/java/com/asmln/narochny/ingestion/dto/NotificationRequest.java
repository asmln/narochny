package com.asmln.narochny.ingestion.dto;

import com.asmln.narochny.domain.Channel;
import com.asmln.narochny.domain.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NotificationRequest(
        @NotBlank @Size(max = 100) String userId,
        @NotNull Channel channel,
        @NotNull Priority priority,
        @NotBlank @Size(max = 10_000) String body
) {}
