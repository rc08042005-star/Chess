
package service;

import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(
                new MemoryUserDAO(),
                new MemoryAuthDAO()
        );
    }

    @Test
    void registerSuccess() throws Exception {
        var result = service.register(
                "rahul",
                "password123",
                "rahul@example.com"
        );

        assertEquals("rahul", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    void registerDuplicateUsername() throws Exception {
        service.register(
                "rahul",
                "password123",
                "rahul@example.com"
        );

        assertThrows(IllegalStateException.class, () ->
                service.register(
                        "rahul",
                        "differentPassword",
                        "another@example.com"
                )
        );
    }

    @Test
    void registerNullUsername() throws Exception {
        assertThrows(IllegalArgumentException.class, () ->
                service.register(
                        null,
                        "password123",
                        "rahul@example.com"
                )
        );
    }

    @Test
    void registerNullPassword() throws Exception {
        assertThrows(IllegalArgumentException.class, () ->
                service.register(
                        "rahul",
                        null,
                        "rahul@example.com"
                )
        );
    }

    @Test
    void registerNullEmail() throws Exception {
        assertThrows(IllegalArgumentException.class, () ->
                service.register(
                        "rahul",
                        "password123",
                        null
                )
        );
    }

    @Test
    void loginSuccess() throws Exception {
        service.register(
                "rahul",
                "password123",
                "rahul@example.com"
        );

        var result = service.login("rahul", "password123");

        assertEquals("rahul", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    void loginWrongPassword() throws Exception {
        service.register(
                "rahul",
                "password123",
                "rahul@example.com"
        );

        assertThrows(SecurityException.class, () ->
                service.login("rahul", "wrongPassword")
        );
    }

    @Test
    void loginUnknownUser() throws Exception {
        assertThrows(SecurityException.class, () ->
                service.login("unknown", "password123")
        );
    }

    @Test
    void logoutSuccess() throws Exception {
        String token = service.register(
                "rahul",
                "password123",
                "rahul@example.com"
        ).authToken();

        assertDoesNotThrow(() -> service.logout(token));
    }

    @Test
    void logoutInvalidToken() throws Exception {
        assertThrows(SecurityException.class, () ->
                service.logout("invalid-token")
        );
    }
}
