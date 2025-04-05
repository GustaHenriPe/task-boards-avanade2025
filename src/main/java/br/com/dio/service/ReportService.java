package br.com.dio.service;

import br.com.dio.dto.BoardTimeReportDTO;
import br.com.dio.dto.CardBlockReportDTO;
import br.com.dio.dto.CardTimeReportDTO;
import br.com.dio.persistence.dao.BlockDAO;
import br.com.dio.persistence.dao.BoardDAO;
import br.com.dio.persistence.dao.CardDAO;
import br.com.dio.persistence.dao.CardMovementDAO;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static br.com.dio.persistence.converter.OffsetDateTimeConverter.toOffsetDateTime;

@AllArgsConstructor
public class ReportService {

    private final Connection connection;

    public BoardTimeReportDTO generateBoardTimeReport(Long boardId) throws SQLException {
        var cardDao = new CardDAO(connection);
        var movementDao = new CardMovementDAO(connection);
        var boardDao = new BoardDAO(connection);

        var board = boardDao.findById(boardId).orElseThrow();
        var cards = cardDao.findAllByBoard(boardId);

        List<CardTimeReportDTO> cardReports = new ArrayList<>();
        Duration totalBoardTime = Duration.ZERO;

        for (var card : cards) {
            var report = new CardService(connection).getCardTimeReport(card.getId());
            cardReports.add(report);
            totalBoardTime = totalBoardTime.plus(report.getTotalTime());
        }

        Duration averageTime = !cardReports.isEmpty() ?
                totalBoardTime.dividedBy(cardReports.size()) :
                Duration.ZERO;

        return new BoardTimeReportDTO(
                boardId,
                board.getName(),
                cardReports,
                averageTime
        );
    }

    public List<CardBlockReportDTO> generateBlockReport(Long boardId) throws SQLException {
        var blockDao = new BlockDAO(connection);
        var cardDao = new CardDAO(connection);

        var sql = """
            SELECT b.id, b.card_id, b.blocked_at, b.unblocked_at, 
                   b.block_reason, b.unblock_reason, c.title as card_title
            FROM BLOCKS b
            JOIN CARDS c ON b.card_id = c.id
            JOIN BOARDS_COLUMNS bc ON c.board_column_id = bc.id
            WHERE bc.board_id = ?
            ORDER BY b.blocked_at
            """;

        List<CardBlockReportDTO> reports = new ArrayList<>();

        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, boardId);
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                var blockedAt = toOffsetDateTime(resultSet.getTimestamp("blocked_at"));
                var unblockedAt = toOffsetDateTime(resultSet.getTimestamp("unblocked_at"));

                reports.add(new CardBlockReportDTO(
                        resultSet.getLong("card_id"),
                        resultSet.getString("card_title"),
                        blockedAt,
                        unblockedAt,
                        unblockedAt != null ? Duration.between(blockedAt, unblockedAt) : null,
                        resultSet.getString("block_reason"),
                        resultSet.getString("unblock_reason")
                ));
            }
        }

        return reports;
    }
}