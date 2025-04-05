package br.com.dio.dto;

import java.time.Duration;
import java.time.OffsetDateTime;

public record CardMovementDTO(
        Long id,
        String cardTitle,
        OffsetDateTime movedAt,
        String fromColumnName,
        String toColumnName,
        Duration timeInColumn
) {}