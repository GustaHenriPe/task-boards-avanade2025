package br.com.dio.dto;

import java.time.Duration;
import java.time.OffsetDateTime;

public record CardBlockReportDTO(
        Long cardId,
        String cardTitle,
        OffsetDateTime blockedAt,
        OffsetDateTime unblockedAt,
        Duration blockedDuration,
        String blockReason,
        String unblockReason
) {}
