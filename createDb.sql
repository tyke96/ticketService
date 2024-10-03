CREATE TABLE TicketPrices (
    ticket_type VARCHAR(10) PRIMARY KEY,
    price INT NOT NULL
);

INSERT INTO TicketPrices (ticket_type, price) VALUES ('INFANT', 0);
INSERT INTO TicketPrices (ticket_type, price) VALUES ('CHILD', 15);
INSERT INTO TicketPrices (ticket_type, price) VALUES ('ADULT', 25);
