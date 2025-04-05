package br.com.dio.dto;

import java.time.Duration;
import java.util.List;

public record BoardTimeReportDTO(
        Long boardId,
        String boardName,
        List<CardTimeReportDTO> cards,
        Duration averageCompletionTime
) {}