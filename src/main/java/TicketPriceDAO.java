import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class TicketPriceDAO {
    private final DataSource dataSource;

    public TicketPriceDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Map<TicketType, Integer> getTicketPrices(List<TicketType> ticketTypes) throws SQLException {
        Map<TicketType, Integer> prices = new EnumMap<>(TicketType.class);
        String query = "SELECT ticket_type, price FROM TicketPrices WHERE ticket_type IN ("
                + String.join(",", ticketTypes.stream().map(type -> "?").toArray(String[]::new)) + ")";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            for (int i = 0; i < ticketTypes.size(); i++) {
                statement.setString(i + 1, ticketTypes.get(i).name());
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String type = resultSet.getString("ticket_type");
                    int price = resultSet.getInt("price");
                    prices.put(TicketType.valueOf(type), price);
                }
            }
        }

        return prices;
    }
}
