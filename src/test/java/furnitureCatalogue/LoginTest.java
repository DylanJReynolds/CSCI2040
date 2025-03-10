// Run: mvn -Dtest=LoginTest test

package furnitureCatalogue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LoginTest {
    private Login login;

    @BeforeEach
    public void setUp() {
        login = new Login();
    }

    @Test
    public void testAdminLogin() {
        // Simulate user input for admin login
        System.setIn(new java.io.ByteArrayInputStream("admin\nadmin123\n".getBytes()));
        String role = login.authenticate();
        assertEquals("admin", role, "Admin should be able to login successfully.");
    }

    @Test
    public void testUserLogin() {
        // Simulate user input for user login
        System.setIn(new java.io.ByteArrayInputStream("user\nuser123\n".getBytes()));
        String role = login.authenticate();
        assertEquals("user", role, "User should be able to login successfully.");
    }

    @Test
    public void testInvalidLogin() {
        // Simulate user input for invalid login
        System.setIn(new java.io.ByteArrayInputStream("invalid\ninvalid\n".getBytes()));
        String role = login.authenticate();
        assertNull(role, "Invalid credentials should return null.");
    }
}
