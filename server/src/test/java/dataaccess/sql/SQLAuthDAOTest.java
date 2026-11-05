
package dataaccess.sql;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SQLAuthDAOTest {

    private AuthDAO authDAO;

    @BeforeEach
    public void setup() throws Exception {
        SQLDatabase.initialize();
        authDAO = new SQLAuthDAO();
        authDAO.clear();
    }

    @Test
    public void createAuthSuccess() throws Exception {
        AuthData auth = new AuthData("token123", "testuser");

        authDAO.createAuth(auth);

        AuthData saved = authDAO.getAuth("token123");

        assertNotNull(saved);
        assertEquals("token123", saved.authToken());
        assertEquals("testuser", saved.username());
    }

    @Test
    public void createDuplicateAuthFails() throws Exception {
        AuthData auth = new AuthData("duplicateToken", "testuser");

        authDAO.createAuth(auth);

        assertThrows(
                DataAccessException.class,
                () -> authDAO.createAuth(auth)
        );
    }

    @Test
    public void getMissingAuthReturnsNull() throws Exception {
        assertNull(authDAO.getAuth("missingToken"));
    }

    @Test
    public void deleteAuthSuccess() throws Exception {
        AuthData auth = new AuthData("deleteToken", "testuser");

        authDAO.createAuth(auth);
        assertNotNull(authDAO.getAuth("deleteToken"));

        authDAO.deleteAuth("deleteToken");

        assertNull(authDAO.getAuth("deleteToken"));
    }

    @Test
    public void clearAuthSuccess() throws Exception {
        authDAO.createAuth(new AuthData("token1", "user1"));
        authDAO.createAuth(new AuthData("token2", "user2"));

        authDAO.clear();

        assertNull(authDAO.getAuth("token1"));
        assertNull(authDAO.getAuth("token2"));
    }
}
