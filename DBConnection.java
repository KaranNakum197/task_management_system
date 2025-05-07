import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/task_management_system";
    private static final String USER = "root";        // MySQL username
    private static final String PASSWORD = "19K@ran2001";        // MySQL password

    private static Connection connection;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Database connected successfully!");
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
                System.out.println("❌ Failed to connect to the database.");
            }
        }
        return connection;
    }
}
