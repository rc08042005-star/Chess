
package dataaccess.sql;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.UserDAO;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

public class SQLUserDAO implements UserDAO {

    public SQLUserDAO() throws DataAccessException {
        SQLDatabase.initialize();
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        String sql = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {

            String hashedPassword = BCrypt.hashpw(
                    user.password(), BCrypt.gensalt());

            statement.setString(1, user.username());
            statement.setString(2, hashedPassword);
            statement.setString(3, user.email());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Failed to create user", e);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String sql = "SELECT username, password, email FROM user WHERE username = ?";

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (var result = statement.executeQuery()) {
                if (result.next()) {
                    return new UserData(
                            result.getString("username"),
                            result.getString("password"),
                            result.getString("email")
                    );
                }
            }

            return null;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to get user", e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM user";

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Failed to clear users", e);
        }
    }
}
