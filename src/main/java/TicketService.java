import java.util.List;

public interface TicketService {
    void purchaseTickets(Long accountId, List<TicketTypeRequest> ticketRequests);
}
