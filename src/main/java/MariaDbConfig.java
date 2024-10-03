import javax.sql.DataSource;
import org.mariadb.jdbc.MariaDbDataSource;
import java.sql.SQLException;

public class MariaDbConfig {
    public static DataSource createDataSource() throws SQLException {
        MariaDbDataSource dataSource = new MariaDbDataSource();
        dataSource.setUrl("jdbc:mariadb://localhost:3306/tickets");
        dataSource.setUser("tickets");
        dataSource.setPassword("tickets");
        return dataSource;
    }
}
