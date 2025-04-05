package br.com.dio.ui;

import br.com.dio.dto.BoardColumnInfoDTO;
import br.com.dio.persistence.entity.BoardColumnEntity;
import br.com.dio.persistence.entity.BoardEntity;
import br.com.dio.persistence.entity.CardEntity;
import br.com.dio.service.*;
import lombok.AllArgsConstructor;

import java.sql.SQLException;
import java.util.Scanner;

import static br.com.dio.persistence.config.ConnectionConfig.getConnection;

@AllArgsConstructor
public class BoardMenu {

    private final Scanner scanner = new Scanner(System.in).useDelimiter("\n");

    private final BoardEntity board;

    public void execute() {
        try {
            System.out.printf("Welcome to board %s, select the desired operation\n", board.getId());
            var option = -1;
            while (option != 12) {
                System.out.println("1 - Create a card");
                System.out.println("2 - Move a card");
                System.out.println("3 - Block a card");
                System.out.println("4 - Unblock a card");
                System.out.println("5 - Cancel a card");
                System.out.println("6 - View board");
                System.out.println("7 - View column with cards");
                System.out.println("8 - View card");
                System.out.println("9 - View board time report");
                System.out.println("10 - View block report");
                System.out.println("11 - Return to previous menu");
                System.out.println("12 - Exit");
                option = scanner.nextInt();
                switch (option) {
                    case 1 -> createCard();
                    case 2 -> moveCardToNextColumn();
                    case 3 -> blockCard();
                    case 4 -> unblockCard();
                    case 5 -> cancelCard();
                    case 6 -> showBoard();
                    case 7 -> showColumn();
                    case 8 -> showCard();
                    case 9 -> showTimeReport();
                    case 10 -> showBlockReport();
                    case 11 -> System.out.println("Returning to previous menu");
                    case 12 -> System.exit(0);
                    default -> System.out.println("Invalid option, please select a valid one");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }

    private void createCard() throws SQLException {
        var card = new CardEntity();
        System.out.println("Enter the card title");
        card.setTitle(scanner.next());
        System.out.println("Enter the card description");
        card.setDescription(scanner.next());
        card.setBoardColumn(board.getInitialColumn());
        try (var connection = getConnection()) {
            new CardService(connection).create(card);
        }
    }

    private void moveCardToNextColumn() throws SQLException {
        System.out.println("Enter the ID of the card you want to move to the next column");
        var cardId = scanner.nextLong();
        var columnsInfo = board.getBoardColumns().stream()
                .map(c -> new BoardColumnInfoDTO(c.getId(), c.getOrder(), c.getKind()))
                .toList();
        try (var connection = getConnection()) {
            new CardService(connection).moveToNextColumn(cardId, columnsInfo);
        } catch (RuntimeException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void blockCard() throws SQLException {
        System.out.println("Enter the ID of the card to be blocked");
        var cardId = scanner.nextLong();
        System.out.println("Enter the reason for blocking the card");
        var reason = scanner.next();
        var columnsInfo = board.getBoardColumns().stream()
                .map(c -> new BoardColumnInfoDTO(c.getId(), c.getOrder(), c.getKind()))
                .toList();
        try (var connection = getConnection()) {
            new CardService(connection).block(cardId, reason, columnsInfo);
        } catch (RuntimeException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void unblockCard() throws SQLException {
        System.out.println("Enter the ID of the card to be unblocked");
        var cardId = scanner.nextLong();
        System.out.println("Enter the reason for unblocking the card");
        var reason = scanner.next();
        try (var connection = getConnection()) {
            new CardService(connection).unblock(cardId, reason);
        } catch (RuntimeException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void cancelCard() throws SQLException {
        System.out.println("Enter the ID of the card to move to the cancel column");
        var cardId = scanner.nextLong();
        var cancelColumn = board.getCancelColumn();
        var columnsInfo = board.getBoardColumns().stream()
                .map(c -> new BoardColumnInfoDTO(c.getId(), c.getOrder(), c.getKind()))
                .toList();
        try (var connection = getConnection()) {
            new CardService(connection).cancel(cardId, cancelColumn.getId(), columnsInfo);
        } catch (RuntimeException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void showBoard() throws SQLException {
        try (var connection = getConnection()) {
            var optional = new BoardQueryService(connection).showBoardDetails(board.getId());
            optional.ifPresent(b -> {
                System.out.printf("Board [%s, %s]\n", b.id(), b.name());
                b.columns().forEach(c ->
                        System.out.printf("Column [%s] type: [%s] has %s cards\n", c.name(), c.kind(), c.cardsAmount())
                );
            });
        }
    }

    private void showColumn() throws SQLException {
        var columnIds = board.getBoardColumns().stream().map(BoardColumnEntity::getId).toList();
        var selectedColumnId = -1L;
        while (!columnIds.contains(selectedColumnId)) {
            System.out.printf("Select a column from board %s by ID\n", board.getName());
            board.getBoardColumns().forEach(c ->
                    System.out.printf("%s - %s [%s]\n", c.getId(), c.getName(), c.getKind()));
            selectedColumnId = scanner.nextLong();
        }
        try (var connection = getConnection()) {
            var column = new BoardColumnQueryService(connection).findById(selectedColumnId);
            column.ifPresent(c -> {
                System.out.printf("Column %s type %s\n", c.getName(), c.getKind());
                c.getCards().forEach(card ->
                        System.out.printf("Card %s - %s\nDescription: %s",
                                card.getId(), card.getTitle(), card.getDescription()));
            });
        }
    }

    private void showCard() throws SQLException {
        System.out.println("Enter the ID of the card to view");
        var selectedCardId = scanner.nextLong();
        try (var connection = getConnection()) {
            new CardQueryService(connection).findById(selectedCardId)
                    .ifPresentOrElse(
                            c -> {
                                System.out.printf("Card %s - %s.\n", c.id(), c.title());
                                System.out.printf("Description: %s\n", c.description());
                                System.out.println(c.blocked() ?
                                        "Blocked. Reason: " + c.blockReason() :
                                        "Not blocked");
                                System.out.printf("Blocked %s times\n", c.blocksAmount());
                                System.out.printf("Currently in column %s - %s\n", c.columnId(), c.columnName());
                            },
                            () -> System.out.printf("No card found with ID %s\n", selectedCardId));
        }
    }

    private void showTimeReport() throws SQLException {
        try (var connection = getConnection()) {
            var report = new ReportService(connection).generateBoardTimeReport(board.getId());

            System.out.printf("Time Report - Board: %s (%d)\n",
                    report.boardName(),
                    report.boardId()
            );

            System.out.printf("Average completion time: %d hours %d minutes\n",
                    report.averageCompletionTime().toHours(),
                    report.averageCompletionTime().toMinutesPart()
            );

            for (var card : report.cards()) {
                System.out.printf("\nCard: %s\n", card.getCardTitle());
                System.out.printf("Total time: %d hours %d minutes\n",
                        card.getTotalTime().toHours(),
                        card.getTotalTime().toMinutesPart()
                );

                card.getTimePerColumn().forEach((column, duration) -> {
                    System.out.printf("- %s: %d hours %d minutes\n",
                            column, duration.toHours(), duration.toMinutesPart());
                });
            }
        }
    }

    private void showBlockReport() throws SQLException {
        try (var connection = getConnection()) {
            var report = new ReportService(connection).generateBlockReport(board.getId());

            System.out.printf("Block Report - Board: %s (%d)\n", board.getName(), board.getId());

            for (var block : report) {
                System.out.printf("\nCard: %s (%d)\n", block.cardTitle(), block.cardId());
                System.out.printf("Block reason: %s\n", block.blockReason());
                System.out.printf("Unblock reason: %s\n", block.unblockReason());

                if (block.blockedDuration() != null) {
                    System.out.printf("Blocked time: %d hours %d minutes\n",
                            block.blockedDuration().toHours(),
                            block.blockedDuration().toMinutesPart());
                } else {
                    System.out.println("Still blocked");
                }
            }
        }
    }
}
