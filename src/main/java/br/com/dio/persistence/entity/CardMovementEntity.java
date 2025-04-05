package br.com.dio.persistence.entity;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class CardMovementEntity {
    private Long id;
    private Long cardId;
    private Long fromColumnId;
    private Long toColumnId;
    private OffsetDateTime movedAt;
}