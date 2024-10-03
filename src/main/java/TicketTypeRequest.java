public final class TicketTypeRequest {
    private final TicketType ticketType;
    private final int quantity;

    public TicketTypeRequest(TicketType ticketType, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
        this.ticketType = ticketType;
        this.quantity = quantity;
    }

    public TicketType getTicketType() {
        return ticketType;
    }

    public int getQuantity() {
        return quantity;
    }
}
