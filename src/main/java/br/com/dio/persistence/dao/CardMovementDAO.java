package br.com.dio.persistence.dao;

import br.com.dio.dto.CardMovementDTO;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static br.com.dio.persistence.converter.OffsetDateTimeConverter.toOffsetDateTime;
import static br.com.dio.persistence.converter.OffsetDateTimeConverter.toTimestamp;

@AllArgsConstructor
public class CardMovementDAO {

    private final Connection connection;

    public void recordMovement(Long cardId, Long fromColumnId, Long toColumnId) throws SQLException {
        var sql = "INSERT INTO CARD_MOVEMENTS (card_id, from_column_id, to_column_id, moved_at) VALUES (?, ?, ?, ?)";
        try (var statement = connection.prepareStatement(sql)) {
            var i = 1;
            statement.setLong(i++, cardId);
            if (fromColumnId != null) {
                statement.setLong(i++, fromColumnId);
            } else {
                statement.setNull(i++, java.sql.Types.BIGINT);
            }
            statement.setLong(i++, toColumnId);
            statement.setTimestamp(i, toTimestamp(OffsetDateTime.now()));
            statement.executeUpdate();
        }
    }

    public List<CardMovementDTO> getCardMovements(Long cardId) throws SQLException {
        var sql = """
                SELECT cm.id, cm.moved_at, 
                       fc.name as from_column_name, 
                       tc.name as to_column_name,
                       c.title as card_title
                FROM CARD_MOVEMENTS cm
                JOIN BOARDS_COLUMNS tc ON cm.to_column_id = tc.id
                LEFT JOIN BOARDS_COLUMNS fc ON cm.from_column_id = fc.id
                JOIN CARDS c ON cm.card_id = c.id
                WHERE cm.card_id = ?
                ORDER BY cm.moved_at
                """;

        var movements = new ArrayList<CardMovementDTO>();
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, cardId);
            var resultSet = statement.executeQuery();

            OffsetDateTime previousTime = null;
            while (resultSet.next()) {
                var movedAt = toOffsetDateTime(resultSet.getTimestamp("moved_at"));
                var movement = new CardMovementDTO(
                        resultSet.getLong("id"),
                        resultSet.getString("card_title"),
                        movedAt,
                        resultSet.getString("from_column_name"),
                        resultSet.getString("to_column_name"),
                        previousTime != null ? Duration.between(previousTime, movedAt) : null
                );
                movements.add(movement);
                previousTime = movedAt;
            }
        }
        return movements;
    }
}