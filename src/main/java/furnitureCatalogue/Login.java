/*
 * This class is responsible for authenticating users based on a hardcoded list of users and roles to interact with the catalogue.
 * It reads the username and password from the console and checks if they match any of the users in the list, then is sent
 * to the CatalogueUI class to determine what actions the user can take.
 */

package furnitureCatalogue;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import java.io.*;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;
import java.awt.Component;


public class Login {
    protected HashMap<String, String> users;
    protected final HashMap<String, String> roles;
    protected Scanner scanner;

    public Login() {
        users = new HashMap<>();
        roles = new HashMap<>();
        scanner = new Scanner(System.in);

        //Default Passwords:
        /*
        * User: user123
        * Admin: admin123
        * */

        readCSV("src/main/resources/Users.csv");
    }

    // Authenticates the user based on the provided username and password
    // Returns the role of the user if authenticated, otherwise returns null
    public String authenticate() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        //Hashes password to compare stored values and original password.
        String password = hashString(readPassword("Enter password: "));

        if (users.containsKey(username) && users.get(username).equals(password)) {
            return roles.get(username);
        } else {
            System.out.println("Invalid credentials.");
            return null;
        }
    }

    // Reads the password from the console securely (doesn't display the password)
    protected String readPassword(String prompt) {
        Console console = System.console();
        if (console == null) {
            System.out.print(prompt);
            return scanner.nextLine();
        } else {
            char[] passwordArray = console.readPassword(prompt);
            return new String(passwordArray);
        }
    }

    //This function encrypts the string, so that passwords remain protected.
    //The raw password is never used, instead the encrypted ones are compared.
    public String hashString(String input) {
        try {
            String password = input;
            byte[] salt = new byte[16];

            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHMacSha1");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return String.format("%x", new BigInteger(hash));

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    private void readCSV(String fileName){
        try{
            File userCSV = new File(fileName);
            Scanner userReader = new Scanner(userCSV);

            while(userReader.hasNextLine()) {
                String[] splitLine = userReader.nextLine().split(",");
                users.put(splitLine[0], splitLine[1]);
                //Checks admin/user flag to determine if the rank is an admin or user.
                if (Objects.equals(splitLine[2], "0")){
                    roles.put(splitLine[0], "user");
                } else{
                    roles.put(splitLine[0], "admin");
                }

            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void makeUserSwing(Component parent) {
    // 1) Get username via dialog
    String username = JOptionPane.showInputDialog(parent, "Enter new username:");
    if (username == null || username.isEmpty()) {
        System.out.println("Cancelled or blank. Aborting user creation.");
        return;
    }
    // Check for duplicates
    if (users.containsKey(username)) {
        JOptionPane.showMessageDialog(parent, 
            "That username already exists!", 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
        return;
    }

    // 2) Get password
    JPasswordField pwdField = new JPasswordField(10);
    int result = JOptionPane.showConfirmDialog(
        parent, pwdField, "Enter Password", JOptionPane.OK_CANCEL_OPTION);
    if (result != JOptionPane.OK_OPTION) {
        System.out.println("Cancelled. Aborting user creation.");
        return;
    }
    String rawPassword = new String(pwdField.getPassword());
    String hashed = hashString(rawPassword);

    // 3) Ask if admin or normal
    String[] options = {"Admin", "User"};
    int choice = JOptionPane.showOptionDialog(
        parent,
        "Is this user an admin?",
        "User Type",
        JOptionPane.DEFAULT_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        options,
        options[0]
    );
    // Map 0 -> admin, 1 -> user
    String adminFlag = (choice == 0) ? "1" : "0";

    // 4) Write to CSV
    try (FileWriter fw = new FileWriter("src/main/resources/Users.csv", true)) {
        fw.write("\n" + username + "," + hashed + "," + adminFlag);
    } catch (IOException e) {
        e.printStackTrace();
    }

    // 5) Update your in-memory `users` and `roles` maps
    users.put(username, hashed);
    if (adminFlag.equals("1")) {
        roles.put(username, "admin");
    } else {
        roles.put(username, "user");
    }

    JOptionPane.showMessageDialog(parent, 
        "Created user: " + username,
        "Success",
        JOptionPane.INFORMATION_MESSAGE);
}

    private void writeCSV(String fileName) {
        //Check for duplicate users:
        boolean userLoop = true;
        String username = "";
        while (userLoop){
            System.out.println("Enter the Username: ");
            username = scanner.nextLine();
            if(users.containsKey(username)){
                System.out.println("Error: Invalid input");
            } else{
                userLoop = false;
            }
        }
        System.out.println("Enter the Password: ");
        String password = hashString(scanner.nextLine());
        boolean adminLoop = true;
        String admin = "";
        while (adminLoop){
            System.out.println("Is this an admin? 1: Yes, 0: No");
            admin = scanner.nextLine();
            if(!Objects.equals(admin, "1") && !Objects.equals(admin, "0")){
                System.out.println("Error: Invalid input");
            } else{
                adminLoop = false;
            }
        }
        try {
            File userCSV = new File(fileName);
            FileWriter userWriter = new FileWriter(userCSV, true);
            userWriter.write("\n");
            userWriter.write(username + "," + password + "," + admin);
            userWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
