package furnitureCatalogue;

import java.util.HashMap;
import java.util.Scanner;
import java.io.Console;

/*
 * This class is responsible for authenticating users based on a hardcoded list of users and roles to interact with the catalogue.
 * It reads the username and password from the console and checks if they match any of the users in the list, then is sent
 * to the CatalogueUI class to determine what actions the user can take.
 */
public class Login {
    private HashMap<String, String> users;
    private HashMap<String, String> roles;
    private Scanner scanner;

    public Login() {
        users = new HashMap<>();
        roles = new HashMap<>();
        scanner = new Scanner(System.in);
        
        // TODO: Replace hardcoded users and roles with a database or external service
        users.put("admin", "admin123");
        roles.put("admin", "admin");
        users.put("user", "user123");
        roles.put("user", "user");
    }

    // Authenticates the user based on the provided username and password
    // Returns the role of the user if authenticated, otherwise returns null
    public String authenticate() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        String password = readPassword("Enter password: ");

        if (users.containsKey(username) && users.get(username).equals(password)) {
            return roles.get(username);
        } else {
            System.out.println("Invalid credentials.");
            return null;
        }
    }

    // Reads the password from the console securely (doesn't display the password)
    private String readPassword(String prompt) {
        Console console = System.console();
        if (console == null) {
            System.out.print(prompt);
            return scanner.nextLine();
        } else {
            char[] passwordArray = console.readPassword(prompt);
            return new String(passwordArray);
        }
    }
}
