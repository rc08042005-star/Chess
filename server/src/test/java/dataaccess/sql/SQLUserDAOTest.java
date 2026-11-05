
package dataaccess.sql;

import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;

public class SQLUserDAOTest {

    private UserDAO userDAO;

    @BeforeEach
    public void setup() throws Exception {
        SQLDatabase.initialize();
        userDAO = new SQLUserDAO();
        userDAO.clear();
    }

    @Test
    public void createUserSuccess() throws Exception {
        UserData user = new UserData(
                "testuser",
                "password123",
                "test@example.com"
        );

        userDAO.createUser(user);

        UserData saved = userDAO.getUser("testuser");

        assertNotNull(saved);
        assertEquals("testuser", saved.username());
        assertEquals("test@example.com", saved.email());

        // Verify the password was hashed correctly
        assertNotEquals("password123", saved.password());
        assertTrue(BCrypt.checkpw(
                "password123",
                saved.password()
        ));
    }

    @Test
    public void createDuplicateUserFails() throws Exception {
        UserData user = new UserData(
                "duplicate",
                "password123",
                "duplicate@example.com"
        );

        userDAO.createUser(user);

        assertThrows(
                DataAccessException.class,
                () -> userDAO.createUser(user)
        );
    }

    @Test
    public void getMissingUserReturnsNull() throws Exception {
        UserData user = userDAO.getUser("missing");
        assertNull(user);
    }

    @Test
    public void clearUsersSuccess() throws Exception {
        userDAO.createUser(new UserData(
                "user1",
                "password1",
                "user1@example.com"
        ));

        assertNotNull(userDAO.getUser("user1"));

        userDAO.clear();

        assertNull(userDAO.getUser("user1"));
    }
}
