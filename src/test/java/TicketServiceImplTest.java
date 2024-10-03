import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TicketServiceImplTest {
    private TicketServiceImpl ticketService;
    private TicketPriceDAO ticketPriceDAO;
    private HttpClient httpClient;
    private final String paymentServiceUrl = "http://payment-service";
    private final String reservationServiceUrl = "http://reservation-service";

    @BeforeEach
    public void setUp() {
        ticketPriceDAO = mock(TicketPriceDAO.class);
        httpClient = mock(HttpClient.class);
        ticketService = new TicketServiceImpl(httpClient, paymentServiceUrl, reservationServiceUrl, ticketPriceDAO);
    }

    @Test
    public void testPurchaseTickets_Success() throws SQLException, IOException, InterruptedException {
        List<TicketTypeRequest> ticketRequests = Arrays.asList(
                new TicketTypeRequest(TicketType.ADULT, 2),
                new TicketTypeRequest(TicketType.CHILD, 1)
        );

        Map<TicketType, Integer> prices = new EnumMap<>(TicketType.class);
        prices.put(TicketType.ADULT, 25);
        prices.put(TicketType.CHILD, 15);

        when(ticketPriceDAO.getTicketPrices(any())).thenReturn(prices);

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);

        ticketService.purchaseTickets(1L, ticketRequests);

        verify(ticketPriceDAO).getTicketPrices(any());
        verify(httpClient, times(2)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));

        // Capture the requests sent to the HttpClient
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient, times(2)).send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class));

        // Verify the payment request
        HttpRequest paymentRequest = requestCaptor.getAllValues().get(0);
        assertEquals(URI.create(paymentServiceUrl), paymentRequest.uri());
        assertEquals("{\"accountId\": 1, \"amount\": 65}", TestUtils.extractBodyContent(paymentRequest.bodyPublisher().get()));

        // Verify the reservation request
        HttpRequest reservationRequest = requestCaptor.getAllValues().get(1);
        assertEquals(URI.create(reservationServiceUrl), reservationRequest.uri());
        assertEquals("{\"accountId\": 1, \"seats\": 3}", TestUtils.extractBodyContent(reservationRequest.bodyPublisher().get()));
    }

    @Test
    public void testPurchaseTickets_NoAdultTicket() {
        List<TicketTypeRequest> ticketRequests = List.of(
                new TicketTypeRequest(TicketType.CHILD, 1)
        );

        assertThrows(IllegalArgumentException.class, () -> ticketService.purchaseTickets(1L, ticketRequests));
    }

    @Test
    public void testPurchaseTickets_TooManyTickets() {
        List<TicketTypeRequest> ticketRequests = List.of(
                new TicketTypeRequest(TicketType.ADULT, 26)
        );

        assertThrows(IllegalArgumentException.class, () -> ticketService.purchaseTickets(1L, ticketRequests));
    }

    @Test
    public void testPurchaseTickets_InvalidAccountId() {
        List<TicketTypeRequest> ticketRequests = List.of(
                new TicketTypeRequest(TicketType.ADULT, 1)
        );

        assertThrows(IllegalArgumentException.class, () -> ticketService.purchaseTickets(0L, ticketRequests));
    }
}
