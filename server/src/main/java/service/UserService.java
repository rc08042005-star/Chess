
package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class UserService {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    // Register a new user
    public AuthData register(String username, String password, String email)
            throws DataAccessException {

        if (username == null || username.isBlank()
                || password == null || password.isBlank()
                || email == null || email.isBlank()) {
            throw new IllegalArgumentException("bad request");
        }

        if (userDAO.getUser(username) != null) {
            throw new IllegalStateException("already taken");
        }

        userDAO.createUser(new UserData(username, password, email));

        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData(token, username);
        authDAO.createAuth(auth);

        return auth;
    }

    // Log in an existing user
    public AuthData login(String username, String password)
            throws DataAccessException {

        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            throw new IllegalArgumentException("bad request");
        }

        UserData user = userDAO.getUser(username);

        if (user == null || !passwordMatches(password, user.password())) {
            throw new SecurityException("unauthorized");
        }

        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData(token, username);
        authDAO.createAuth(auth);

        return auth;
    }

    // Verify passwords for SQL and memory DAOs
    private boolean passwordMatches(String password, String storedPassword) {
        if (storedPassword == null) {
            return false;
        }

        if (storedPassword.startsWith("$2a$")
                || storedPassword.startsWith("$2b$")
                || storedPassword.startsWith("$2y$")) {
            try {
                return BCrypt.checkpw(password, storedPassword);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        // Supports existing memory DAO tests
        return storedPassword.equals(password);
    }

    // Log out an existing user
    public void logout(String authToken) throws DataAccessException {

        if (authToken == null || authToken.isBlank()
                || authDAO.getAuth(authToken) == null) {
            throw new SecurityException("unauthorized");
        }

        authDAO.deleteAuth(authToken);
    }
}
