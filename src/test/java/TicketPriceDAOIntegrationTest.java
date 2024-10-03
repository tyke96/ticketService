import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TicketPriceDAOIntegrationTest {
    private TicketPriceDAO ticketPriceDAO;

    @BeforeEach
    public void setUp() throws SQLException {
        DataSource dataSource = MariaDbConfig.createDataSource();
        ticketPriceDAO = new TicketPriceDAO(dataSource);
    }

    @Test
    public void testGetTicketPrices() throws SQLException {
        List<TicketType> ticketTypes = Arrays.asList(TicketType.ADULT, TicketType.CHILD);
        Map<TicketType, Integer> prices = ticketPriceDAO.getTicketPrices(ticketTypes);

        assertEquals(2, prices.size());
        assertEquals(25, prices.get(TicketType.ADULT));
        assertEquals(15, prices.get(TicketType.CHILD));
    }

    @Test
    public void testGetTicketPrices_Infant() throws SQLException {
        List<TicketType> ticketTypes = List.of(TicketType.INFANT);
        Map<TicketType, Integer> prices = ticketPriceDAO.getTicketPrices(ticketTypes);

        assertEquals(1, prices.size());
        assertEquals(0, prices.get(TicketType.INFANT));
    }
}
