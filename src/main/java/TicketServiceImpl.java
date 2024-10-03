import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TicketServiceImpl implements TicketService {
    private final HttpClient httpClient;
    private final String paymentServiceUrl;
    private final String reservationServiceUrl;
    private final TicketPriceDAO ticketPriceDAO;

    public TicketServiceImpl(HttpClient httpClient, String paymentServiceUrl, String reservationServiceUrl, TicketPriceDAO ticketPriceDAO) {
        this.httpClient = httpClient;
        this.paymentServiceUrl = paymentServiceUrl;
        this.reservationServiceUrl = reservationServiceUrl;
        this.ticketPriceDAO = ticketPriceDAO;
    }

    @Override
    public void purchaseTickets(Long accountId, List<TicketTypeRequest> ticketRequests) {
        validateRequest(accountId, ticketRequests);

        Map<TicketType, Integer> ticketPrices;
        try {
            ticketPrices = ticketPriceDAO.getTicketPrices(ticketRequests.stream()
                    .map(TicketTypeRequest::getTicketType)
                    .distinct()
                    .collect(Collectors.toList()));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch ticket prices.", e);
        }

        int totalCost = calculateTotalCost(ticketRequests, ticketPrices);
        int totalSeats = calculateTotalSeats(ticketRequests);

        makePaymentRequest(accountId, totalCost);
        makeReservationRequest(accountId, totalSeats);
    }

    private void validateRequest(Long accountId, List<TicketTypeRequest> ticketRequests) {
        if (accountId <= 0) {
            throw new IllegalArgumentException("Invalid account ID.");
        }

        int adultTickets = 0;
        int totalTickets = 0;

        for (TicketTypeRequest request : ticketRequests) {
            if (request.getTicketType() == TicketType.ADULT) {
                adultTickets += request.getQuantity();
            }
            totalTickets += request.getQuantity();
        }

        if (adultTickets == 0) {
            throw new IllegalArgumentException("At least one adult ticket must be purchased.");
        }

        if (totalTickets > 25) {
            throw new IllegalArgumentException("Cannot purchase more than 25 tickets at a time.");
        }
    }

    private int calculateTotalCost(List<TicketTypeRequest> ticketRequests, Map<TicketType, Integer> ticketPrices) {
        int totalCost = 0;

        for (TicketTypeRequest request : ticketRequests) {
            int price = ticketPrices.get(request.getTicketType());
            totalCost += request.getQuantity() * price;
        }

        return totalCost;
    }

    private int calculateTotalSeats(List<TicketTypeRequest> ticketRequests) {
        int totalSeats = 0;

        for (TicketTypeRequest request : ticketRequests) {
            if (request.getTicketType() != TicketType.INFANT) {
                totalSeats += request.getQuantity();
            }
        }

        return totalSeats;
    }

    private void makePaymentRequest(Long accountId, int totalCost) {
        String requestBody = String.format("{\"accountId\": %d, \"amount\": %d}", accountId, totalCost);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(paymentServiceUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Payment request failed.");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Payment request failed.", e);
        }
    }

    private void makeReservationRequest(Long accountId, int totalSeats) {
        String requestBody = String.format("{\"accountId\": %d, \"seats\": %d}", accountId, totalSeats);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(reservationServiceUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Reservation request failed.");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Reservation request failed.", e);
        }
    }
}