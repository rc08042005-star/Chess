
package dataaccess.sql;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;

import java.sql.SQLException;

public class SQLDatabase {

    public static void initialize() throws DataAccessException {
        DatabaseManager.createDatabase();

        String[] statements = {
                """
                CREATE TABLE IF NOT EXISTS user (
                    username VARCHAR(255) NOT NULL PRIMARY KEY,
                    password VARCHAR(255) NOT NULL,
                    email VARCHAR(255) NOT NULL
                )
                """,

                """
                CREATE TABLE IF NOT EXISTS auth (
                    authToken VARCHAR(255) NOT NULL PRIMARY KEY,
                    username VARCHAR(255) NOT NULL
                )
                """,

                """
                CREATE TABLE IF NOT EXISTS game (
                    gameID INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    whiteUsername VARCHAR(255),
                    blackUsername VARCHAR(255),
                    gameName VARCHAR(255) NOT NULL,
                    game TEXT NOT NULL
                )
                """
        };

        try (var connection = DatabaseManager.getConnection()) {
            for (String sql : statements) {
                try (var statement = connection.prepareStatement(sql)) {
                    statement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(
                    "Failed to initialize database tables", e);
        }
    }
}
