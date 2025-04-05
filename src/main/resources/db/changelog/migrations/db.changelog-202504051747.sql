--liquibase formatted sql
-- changeset gustavo:202504060001
-- comment: Add card movement tracking tables

CREATE TABLE CARD_MOVEMENTS (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    card_id BIGINT NOT NULL,
    from_column_id BIGINT NULL,
    to_column_id BIGINT NOT NULL,
    moved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT cards__movements_fk FOREIGN KEY (card_id) REFERENCES CARDS(id) ON DELETE CASCADE,
    CONSTRAINT from_column_fk FOREIGN KEY (from_column_id) REFERENCES BOARDS_COLUMNS(id),
    CONSTRAINT to_column_fk FOREIGN KEY (to_column_id) REFERENCES BOARDS_COLUMNS(id)
) ENGINE=InnoDB;

-- rollback DROP TABLE CARD_MOVEMENTS