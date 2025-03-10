package furnitureCatalogue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CatalogueUI {
    public HashMap<Integer, ArrayList<String>> catalogue;
    public CatalogueFileIO fileIO;
    public String[] headers;
    private Scanner s;
    private String role;

    public static void main(String[] args) {
        CatalogueUI catalogueUI = new CatalogueUI();
    }

    // refer to Login.java
    public CatalogueUI() {
        Login login = new Login();
        role = login.authenticate();
        if (role == null) {
            System.out.println("Exiting...");
            return;
        }
        fileIO = new CatalogueFileIO("Sample.csv", this);
        commandLineMenu();
    }

    /**
     * Prints a simple command line interface
     * that allows for the user to choose what they want to do
     */
    private void commandLineMenu() {
        s = new Scanner(System.in);
        boolean running = true;
        // prints the menu options based on the role of the user
        while (running) {
            String[] menuOptions = role.equals("admin") ? new String[]{
                    "Display all Entries",
                    "Edit an entry",
                    "Add an entry",
                    "Remove an entry",
                    "View Specific Entry",
                    "Search"
            } : new String[]{
                    "Display all Entries",
                    "View Specific Entry",
                    "Search"
            };
            printMenu(menuOptions);
            String inp = s.nextLine();
            switch (inp) {
                case "1":
                    displayEntries();
                    break;
                case "2":
                    if (role.equals("admin")) editEntry();
                    else viewEntry();
                    break;
                case "3":
                    if (role.equals("admin")) addEntry();
                    else specificSearch();
                    break;
                case "4":
                    if (role.equals("admin")) removeEntry();
                    else running = false;
                    break;
                case "5":
                    if (role.equals("admin")) viewEntry();
                    break;
                case "6":
                    if (role.equals("admin")) specificSearch();
                    break;
                case "7":
                    if (role.equals("admin")) running = false;
                    break;
                default:
                    break;
            }
        }
        s.close();
    }

    /**
     * Prints out the entire catalogue to the command line without showing all the details of every item
     */
    public void displayEntries() {
        System.out.println();
        for (Map.Entry<Integer, ArrayList<String>> entry : catalogue.entrySet()) {
            Integer key = entry.getKey();
            ArrayList<String> value = entry.getValue();
            // prints each entry by id, type and color
            System.out.println(key + ": " + value.getFirst());
        }
        System.out.println();
    }

    /**
     * Prompts the user to select a specific entry, and then displays all its information
     */
    public void viewEntry() {
        System.out.print("Choose Entry by ID: ");
        String inp = s.nextLine();
        if (catalogue.containsKey(Integer.parseInt(inp))) {
            ArrayList<String> value = catalogue.get(Integer.parseInt(inp));
            System.out.println(inp); // prints id
            // lists all attributes of entry
            for (int i = 0; i < value.size(); i++) {
                System.out.println("\t" + headers[i + 1] + ": " + value.get(i));
            }
        }
    }

    /**
     * Prompts the user to select a specific entry, then prompts the user to provide new information for each field
     */
    public void editEntry() {
        System.out.print("Choose Entry by ID: ");
        String inp = s.nextLine();
        if (catalogue.containsKey(Integer.parseInt(inp))) {
            ArrayList<String> value = catalogue.get(Integer.parseInt(inp));
            // loops through all headers, prompting the user for data to replace previous data
            for (int i = 0; i < value.size(); i++) {
                System.out.print("Input new " + headers[i + 1] + " to replace " + value.get(i) + ": ");
                String input = s.nextLine();
                if (input.equals("")) {
                    value.set(i, value.get(i));
                } else {
                    value.set(i, input);
                }
            }
            catalogue.put(Integer.parseInt(inp), value);
            fileIO.editCSVLine(inp, inp + "," + String.join(",", value));
        }
    }

    /**
     * Prompts the user for information to create a new entry
     */
    public void addEntry() {
        System.out.print("Choose ID for Entry: ");
        String inp = s.nextLine();
        ArrayList<String> value = new ArrayList<>();
        // loops through all headers, prompting the user for data
        for (int i = 1; i < headers.length; i++) {
            System.out.print("Input " + headers[i] + ": ");
            String input = s.nextLine();
            value.add(input);
        }
        catalogue.put(Integer.parseInt(inp), value);
        fileIO.addCSVLine(inp + "," + String.join(",", value));
    }

    /**
     * Prompts the user to select a specific entry, then removes it from the Map and CSV
     */
    public void removeEntry() {
        System.out.print("Choose ID for Entry: ");
        String inp = s.nextLine();

        if (catalogue.containsKey(Integer.parseInt(inp))) {
            catalogue.remove(Integer.parseInt(inp));
            fileIO.deleteCSVLine(inp); // Pass only the ID
            System.out.println("Entry removed successfully.");
        } else {
            System.out.println("Entry not found.");
        }
    }

    /**
     * Allows the user to search based on the name of the item
     */
    public void specificSearch() {
        System.out.println("Enter Search: ");
        String inp = s.nextLine();

        for (ArrayList<String> value : catalogue.values()) {
            if (value.getFirst().equals(inp)) {
                System.out.println(inp); // prints id
                // lists all attributes of entry
                for (int i = 0; i < value.size(); i++) {
                    System.out.println("\t" + headers[i + 1] + ": " + value.get(i));
                }
            }
        }
    }

    /**
     * Prints a numbered list of options, adding an option to exit, and prompts the user to input a number
     *
     * @param menuOptions List of options to print (excluding the "exit" option)
     */
    private void printMenu(String[] menuOptions) {
        for (int i = 0; i < menuOptions.length; i++) { // loop through provided options, prepending each with a number
            System.out.println((i + 1) + ". " + menuOptions[i]);
        }
        System.out.print((menuOptions.length + 1) + ". Exit\nInput: "); // add exit and input prompts to the end of the menu
    }
}