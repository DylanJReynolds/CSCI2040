package furnitureCatalogue;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;

/*
 * This class is responsible for authenticating users based on a hardcoded list of users and roles to interact with the catalogue.
 * It reads the username and password from the console and checks if they match any of the users in the list, then is sent
 * to the CatalogueUI class to determine what actions the user can take.
 */
public class Login {
    protected HashMap<String, String> users;
    protected final HashMap<String, String> roles;
    protected Scanner scanner;

    public Login(String config) {
        users = new HashMap<>();
        roles = new HashMap<>();
        scanner = new Scanner(System.in);

        //Default Passwords:
        /*
        * User: user123
        * Admin: admin123
        * */
        if (config.equals("1")){
            readCSV("classes/Users.csv");
        } else{
            readCSV("src/main/resources/Users.csv");
        }

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
    private String hashString(String input) {
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

    public void makeUser() {
        writeCSV("src/main/resources/Users.csv");
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
        String password = scanner.nextLine();
        password = hashString(password);
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
