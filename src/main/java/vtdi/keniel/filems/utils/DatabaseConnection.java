package vtdi.keniel.filems.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DatabaseConnection {
    // Fulfills the "Manage and Log All Exceptions" requirement 
    private static final Logger logger = LogManager.getLogger(DatabaseConnection.class);
    
    // Create connection string to the database programmatically [cite: 23]
    private static final String URL = "jdbc:mysql://localhost:3306/MaintenanceDept_WMS";
    private static final String USER = "root"; 
    private static final String PASSWORD = "P@ssword123"; // Remember to use your actual MySQL password

    public static Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            logger.info("Database connection established successfully.");
        } catch (SQLException e) {
            logger.error("Failed to connect to the database: " + e.getMessage(), e);
        }
        return connection;
    }
}