package furnitureCatalogue;


import furnitureCatalogue.SearchPackage.SearchController;
import furnitureCatalogue.SearchPackage.SearchView;

import java.util.*;

public class CatalogueUI {
    public static int[] maxLengths = new int[10];
    public HashMap<Integer, ArrayList<String>> catalogue;
    public CatalogueFileIO fileIO;
    public String[] headers;
    protected String role;
    private Scanner s;
    private SearchController c; // Pointer to SearchController object.
    private SearchView v; // Pointer to SearchView object.
    private Login login;

    public CatalogueUI(String config) {
        if (inputLogin(config)) return;

        if (config.equals("1")) {
            fileIO = new CatalogueFileIO("classes/Sample.csv", this, "1");
        } else{
            fileIO = new CatalogueFileIO("Sample.csv", this, "");
        }

        c = SearchController.getInstance();
        v = SearchView.getInstance();
//        c.searchQuery();
        commandLineMenu();
    }

    public static void main(String[] args) {
        if (args.length > 0){
            CatalogueUI catalogueUI = new CatalogueUI(args[0]);
        } else{
            CatalogueUI catalogueUI = new CatalogueUI("");
        }


    }

    protected boolean inputLogin(String config) {
        login = new Login(config);
        role = login.authenticate();
        if (role == null) {
            System.out.println("Exiting...");
            return true;
        }
        return false;
    }

    /**
     * Prints a simple command line interface
     * that allows for the user to choose what they want to do
     */
    protected void commandLineMenu() {
        s = new Scanner(System.in);
        boolean running = true;
        // prints the menu options based on the role of the user
        while (running) {
            String[] menuOptions = role.equals("admin") ? new String[]{
                    "Display all Entries", // 1
                    "Edit an entry", // 2
                    "Add an entry", // 3
                    "Remove an entry", // 4
                    "View Specific Entry", // 5
                    "Search", // 6
                    "Sort", // 7
                    "Filter", // 8
                    "Advanced Search (Through Filtering And Sorting)", // 9
                    "Display Random Entry",  // My additon - Parish // 10
                    "Add a user" // 11
            } : new String[]{
                    "Display all Entries", // 1
                    "View Specific Entry", // 2
                    "Search" // 3
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
                    if (role.equals("admin")) sortEntries();
                    break;
                case "8":
                    if (role.equals("admin")) filterEntries();
                    break;
                case "9":
                    if (role.equals("admin")) advancedSearch();
                    break;
                case "10":
                    if (role.equals("admin")) randomEntry();
                    break;
                case "11":
                    if (role.equals("admin")) login.makeUser();
                    break;
                case "12":
                    if (role.equals("admin")) running = false;
                    break;
                default:
                    break;
            }
        }
        s.close();
    }

    // Helper method for PotatoFix, making it so that inputs for the fields that should use numbers can ONLY be numbers
    private String getValidatedNumericInput(String fieldName, String currentValue) {
        while (true) {
            if (currentValue != null) {
                System.out.print("Input new " + fieldName + " to replace " + currentValue + ": ");
            } else {
                System.out.print("Input " + fieldName + ": ");
            }
            String input = s.nextLine();
            if (input.isEmpty()) {
                if (currentValue != null) {
                    return currentValue;
                } else {
                    // For adding an items only, allowing for blank fields
                    return "";
                }
            }
            try {
                Double.parseDouble(input);
                return input;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. " + fieldName + " must be a number.");
            }
        }
    }


    /**
     * Filters catalogue list based on input provided by the user.
     */
    private void filterEntries() {
        System.out.println("Fields: " + String.join(", ", headers));
        System.out.print("Which field would you like to filter?: ");
        String field = s.nextLine();
        int index = Arrays.asList(headers).indexOf(field);
        if (index != -1 && index != 1) { // if field is valid for filtering
            if (index == 3 || index == 4 || index == 5 || index == 6 || index == 8 || index == 9) {
                // This field has a small set of possible values e.g. Small, Medium, Large
                Set<String> mySet = new HashSet<>();
                for (Map.Entry<Integer, ArrayList<String>> entry : catalogue.entrySet()) {
                    // use set to remove duplicates in order to show list of possible values
                    mySet.add(entry.getValue().get(index - 1));
                }
                System.out.println("Possible values of " + field + ": " + String.join(", ", mySet));
                System.out.println("Enter values to add them to the whitelist, prepend with '!' to add to the blacklist.");
                String filterInput;
                ArrayList<String> whitelist = new ArrayList<>();
                ArrayList<String> blacklist = new ArrayList<>();
                while (true) {
                    // get list of inputs from user to filter catalogue
                    filterInput = s.nextLine();
                    if (filterInput.isEmpty()) break;
                    if (filterInput.charAt(0) == '!') {
                        // add to blacklist instead if '!'
                        filterInput = filterInput.substring(1);
                        if (mySet.contains(filterInput))
                            blacklist.add(filterInput); // make sure input is one of the possible values
                    } else if (mySet.contains(filterInput)) whitelist.add(filterInput);
                }
                printTableHeader();
                for (Map.Entry<Integer, ArrayList<String>> entry : catalogue.entrySet()) {
                    // print entry only if it matches the criteria
                    if ((!whitelist.isEmpty() && whitelist.contains(entry.getValue().get(index - 1)))) {
                        printTableRow(entry);
                    } else if (!blacklist.contains(entry.getValue().get(index - 1)) && !blacklist.isEmpty()) {
                        printTableRow(entry);
                    }
                }
            } else if (index == 2 || index == 7 || index == 10) {
                // This field contains an integer from a range
                int min = Integer.MAX_VALUE;
                int max = Integer.MIN_VALUE;
                for (ArrayList<String> entry : catalogue.values()) {
                    // find the max and min possible values in this field
                    if (Integer.parseInt(entry.get(index - 1)) > max)
                        max = Integer.parseInt(entry.get(index - 1));
                    if (Integer.parseInt(entry.get(index - 1)) < min)
                        min = Integer.parseInt(entry.get(index - 1));
                }
                System.out.println(field + " ranges from " + min + " to " + max);
                System.out.println("Enter minimum value for " + field + ": ");
                String minInput = s.nextLine();
                System.out.println("Enter maximum value for " + field + ": ");
                String maxInput = s.nextLine();
                if (minInput.matches("-?\\d+") && maxInput.matches("-?\\d+")) {
                    // check that the inputs are integers (negative numbers allowed)
                    int minValue = Integer.parseInt(minInput);
                    int maxValue = Integer.parseInt(maxInput);
                    if (minValue >= min && minValue <= max && maxValue <= max && maxValue >= min) { // make sure filter range is within the catalogue's range
                        if (minValue > maxValue) {
                            // swap values if min is more than max
                            int temp = minValue;
                            minValue = maxValue;
                            maxValue = temp;
                        }
                        printTableHeader();
                        for (Map.Entry<Integer, ArrayList<String>> entry : catalogue.entrySet())
                            // print only if value within specified range
                            if (Integer.parseInt(entry.getValue().get(index - 1)) <= maxValue && Integer.parseInt(entry.getValue().get(index - 1)) >= minValue)
                                printTableRow(entry);
                    } else System.out.println("Inputs must be between " + min + " and " + max);
                } else System.out.println("Invalid input (must be integers)");
            } else if (index == 0) {
                // ID is a special case of integers as it is the hash key
                int min = Integer.MAX_VALUE;
                int max = Integer.MIN_VALUE;
                for (Integer entry : catalogue.keySet()) {
                    if (entry > max) max = entry;
                    if (entry < min) min = entry;
                }
                System.out.println(field + " ranges from " + min + " to " + max);
                System.out.println("Enter minimum value for " + field + ": ");
                String minInput = s.nextLine();
                System.out.println("Enter maximum value for " + field + ": ");
                String maxInput = s.nextLine();
                if (minInput.matches("-?\\d+") && maxInput.matches("-?\\d+")) {
                    int minValue = Integer.parseInt(minInput);
                    int maxValue = Integer.parseInt(maxInput);
                    if (minValue >= min && minValue <= max && maxValue <= max && maxValue >= min) {
                        if (minValue > maxValue) {
                            int temp = minValue;
                            minValue = maxValue;
                            maxValue = temp;
                        }
                        printTableHeader();
                        for (Map.Entry<Integer, ArrayList<String>> entry : catalogue.entrySet())
                            if (entry.getKey() <= maxValue && entry.getKey() >= minValue) printTableRow(entry);
                    } else System.out.println("Inputs must be between " + min + " and " + max);
                } else System.out.println("Invalid input (must be integers)");
            }
        } else System.out.println(field + " is not a valid field to filter with.");
    }


    /**
     * Sorts catalogue based on user input, and output the resulting list as a table
     */
    private void sortEntries() {
        System.out.println("Fields: " + String.join(", ", headers));
        System.out.print("Which field would you like to sort by?: ");
        String field = s.nextLine();
        int index = Arrays.asList(headers).indexOf(field);
        if (index != -1) { // if field is valid (included in the headers array)
            System.out.print("Ascending or Descending? (A/D): ");
            String inp = s.nextLine();
            if (Objects.equals(inp, "A") || Objects.equals(inp, "D")) {
                boolean ascending = inp.equals("A");

                List<Map.Entry<Integer, ArrayList<String>>> myList = getEntries(index); // convert Hash map into a sorted list
                if (!ascending) myList = myList.reversed(); // reverse list if descending
                printTableHeader();
                for (Map.Entry<Integer, ArrayList<String>> entry : myList) {
                    printTableRow(entry);
                }
                System.out.println("Sorted by: " + field + " (" + ((ascending) ? ("Ascending") : ("Descending")) + ")");
            } else {
                System.out.println(inp + " is not a valid input.");
            }
        } else {
            System.out.println(field + " is not a valid field.");
        }
    }

    /**
     * Prints one line to the console with all the headers, each with enough space to accommodate the longest entry.
     */
    private void printTableHeader() {
        StringBuilder headerString = new StringBuilder("id\t");
        for (int i = 1; i < headers.length; i++) {
            headerString.append(headers[i]);
            // calculate how much space each column needs, and insert that amount of space
            for (int j = 0; j <= Math.ceil((double) (maxLengths[i - 1]) / 4.0) - Math.floor((double) (headers[i].length()) / 4.0); j++) {
                headerString.append("\t");
            }
        }
        System.out.println(headerString);
    }

    /**
     * Prints one row of the output table to the console
     *
     * @param entry the entry to print a row for
     */
    private void printTableRow(Map.Entry<Integer, ArrayList<String>> entry) {
        Integer key = entry.getKey();
        ArrayList<String> value = entry.getValue();
        // prints each entry into a table
        StringBuilder itemString = new StringBuilder(key + "\t");
        for (int i = 0; i < value.size(); i++) {
            itemString.append(value.get(i));
            // calculate how much space each column needs, and insert that amount of space
            for (int j = 0; j <= Math.ceil((double) (maxLengths[i]) / 4.0) - Math.floor((double) (value.get(i).length()) / 4.0); j++) {
                itemString.append("\t");
            }
        }
        System.out.println(itemString);
    }

    /**
     * Creates a sorted list of entries based on a provided field to sort by
     *
     * @param index the index of the field in the 'headers' array
     * @return the sorted list of map entries
     */
    private List<Map.Entry<Integer, ArrayList<String>>> getEntries(int index) {
        List<Map.Entry<Integer, ArrayList<String>>> myList = new ArrayList<>(catalogue.entrySet()); // convert hash map to list
        Comparator<Map.Entry<Integer, ArrayList<String>>> myComparator;
        // Different fields have different sorting methods, (i.e. the number fields need to be sorted as numbers instead of strings)
        if (index == 2 || index == 7 || index == 10) {
            myComparator = (o1, o2) -> {
                // numbers should be converted to integers before comparing
                int n = Integer.parseInt(o1.getValue().get(index - 1));
                int m = Integer.parseInt(o2.getValue().get(index - 1));
                return (int) Math.signum(n - m);
            };
        } else if (index == 0) {
            myComparator = (o1, o2) -> {
                // id needs to specifically grab the hash code
                int n = (o1.getKey());
                int m = (o2.getKey());
                return (int) Math.signum(n - m);
            };
        } else {
            myComparator = (o1, o2) -> {
                // all remaining fields are strings and can be sorted as strings
                String s = o1.getValue().get(index - 1);
                String t = o2.getValue().get(index - 1);
                return (int) Math.signum(s.compareTo(t));
            };
        }
        myList.sort(myComparator);
        return myList;
    }

    /**
     * Prints out the entire catalogue to the command line as a table, showing all the details of every item
     */
    public void displayEntries() {
        System.out.println();
        printTableHeader();
        catalogue.entrySet().forEach(this::printTableRow);
        System.out.println();
    }

    /**
     * Prompts the user to select a specific entry, and then displays all its information
     */
    public void viewEntry() {
        int id;
        while (true) {
            System.out.print("Choose Entry by ID: ");
            String inp = s.nextLine();
            if (inp.isEmpty()) {
                System.out.println("Blank input detected (Returning).");
                return;
            }
            try {
                id = Integer.parseInt(inp);
                if (!catalogue.containsKey(id)) {
                    System.out.println("ID not found. Please try again.");
                } else {
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. ID must be a number. Please try again.");
            }
        }
        ArrayList<String> value = catalogue.get(id);
        System.out.println("ID: " + id);
        for (int i = 0; i < value.size(); i++) {
            System.out.println("\t" + headers[i + 1] + ": " + value.get(i));
        }
    }

    /**
     * Prompts the user to select a specific entry, then prompts the user to provide new information for each field
     */
    public void editEntry() {
        int id;
        while (true) {
            System.out.print("Choose Entry by ID: ");
            String inp = s.nextLine();
            if (inp.isEmpty()) {
                System.out.println("Blank input detected (Returning).");
                return;
            }
            try {
                id = Integer.parseInt(inp);
                if (!catalogue.containsKey(id)) {
                    System.out.println("ID not found. Please try again.");
                } else {
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. ID must be a number. Please try again.");
            }
        }
        ArrayList<String> value = catalogue.get(id);
        for (int i = 0; i < value.size(); i++) {
            String field = headers[i + 1];
            if (field.equalsIgnoreCase("Price") ||
                    field.equalsIgnoreCase("Quantity") ||
                    field.equalsIgnoreCase("Weight")) {
                String input = getValidatedNumericInput(field, value.get(i));
                value.set(i, input);
            } else {
                System.out.print("Input new " + field + " to replace " + value.get(i) + ": ");
                String input = s.nextLine();
                if (!input.equals("")) {
                    value.set(i, input);
                }
            }
        }
        catalogue.put(id, value);
        fileIO.editCSVLine(String.valueOf(id), id + "," + String.join(",", value));
    }

    /**
     * Prompts the user for information to create a new entry
     */
    public void addEntry() {
        int id;
        while (true) {
            System.out.print("Choose ID for Entry: ");
            String inp = s.nextLine();
            if (inp.isEmpty()) {
                System.out.println("Blank input detected (Returning).");
                return;
            }
            try {
                id = Integer.parseInt(inp);
                if (catalogue.containsKey(id)) {
                    System.out.println("ID already exists. Please enter a new ID.");
                } else {
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. ID must be a number. Please try again.");
            }
        }
        ArrayList<String> value = new ArrayList<>();
        // loops through all headers, prompting the user for data
        for (int i = 1; i < headers.length; i++) {
            String field = headers[i];
            if (field.equalsIgnoreCase("Price") ||
                    field.equalsIgnoreCase("Quantity") ||
                    field.equalsIgnoreCase("Weight")) {
                String input = getValidatedNumericInput(field, null);
                value.add(input);
            } else {
                System.out.print("Input " + field + ": ");
                String input = s.nextLine();
                value.add(input);
            }
        }
        catalogue.put(id, value);
        fileIO.addCSVLine(id + "," + String.join(",", value));
    }

    /**
     * Prompts the user to select a specific entry, then removes it from the Map and CSV
     */
    public void removeEntry() {
        int id;
        while (true) {
            System.out.print("Choose ID for Entry to remove: ");
            String inp = s.nextLine();
            if (inp.isEmpty()) {
                System.out.println("Blank input detected (Returning).");
                return;
            }
            try {
                id = Integer.parseInt(inp);
                if (!catalogue.containsKey(id)) {
                    System.out.println("ID not found. Please try again.");
                } else {
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. ID must be a number. Please try again.");
            }
        }
        catalogue.remove(id);
        fileIO.deleteCSVLine(String.valueOf(id));
        System.out.println("Entry removed successfully.");
    }

    /**
     * Allows the user to search based on the name of the item
     */
    public void specificSearch() {
        System.out.print("Enter name to search: ");
        String inp = s.nextLine();
        if (inp.isEmpty()) {
            System.out.println("Blank input detected (Returning).");
            return;
        }
        boolean found = false;
        for (ArrayList<String> value : catalogue.values()) {
            if (value.getFirst().equals(inp)) {
                found = true;
                System.out.println(inp);
                for (int i = 0; i < value.size(); i++) {
                    System.out.println("\t" + headers[i + 1] + ": " + value.get(i));
                }
            }
        }
        if (!found) {
            System.out.println("No item found with that name");
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

    // Sorts and Filters csv by any number of categories.
    public void advancedSearch() {
        v.filters.clear();
        v.ranges.clear();

        while (true) {
            System.out.println("Fields: " + String.join(", ", headers));
            System.out.print("Which field would you like to filter? (Or ENTER to proceed): ");
            String field = s.nextLine();
            if (field.isEmpty()) {
                break;
            }

            int index = Arrays.asList(headers).indexOf(field);
            if (index != -1 && index != 1) { // if field is valid for filtering

                if (index == 3 || index == 4 || index == 5 || index == 6 || index == 8 || index == 9) {
                    System.out.println("Enter value:");
                    String filterInput;

                    // get list of inputs from user to filter catalogue
                    filterInput = s.nextLine();
                    v.filters.put(field, filterInput);
                } else if (index == 0 || index == 2 || index == 7 || index == 10) {
                    System.out.println("Enter minimum value for " + field + ": ");
                    String minInput = s.nextLine();
                    System.out.println("Enter maximum value for " + field + ": ");
                    String maxInput = s.nextLine();
                    if (minInput.matches("-?\\d+") && maxInput.matches("-?\\d+")) {
                        // check that the inputs are integers (negative numbers allowed)
                        int minValue = Integer.parseInt(minInput);
                        int maxValue = Integer.parseInt(maxInput);

                        if (minValue > maxValue) {
                            // swap values if min is more than max
                            int temp = minValue;
                            minValue = maxValue;
                            maxValue = temp;
                        }
                        ArrayList<String> input = new ArrayList<>();
                        input.add(String.valueOf(minValue));
                        input.add(String.valueOf(maxValue));
                        v.ranges.put(field, input);
                    } else System.out.println("Invalid input (must be integers)");
                }
            } else {
                System.out.println(field + " is not a valid field to filter with.");
            }
        }

        // Feel free to change this however you like when adding prompts for filters
        System.out.println("Sort by what category?");
        v.sortCategory = s.nextLine();
        System.out.println("Ascending or Descending? (A/D)");
        String temp = s.nextLine();
        if (temp.equals("A")) {
            v.sortMode = true;
        } else if (temp.equals("D")) {
            v.sortMode = false;
        } else {
            System.out.println("Invalid Input.");
            return;
        }
        printTableHeader();
        c.searchQuery();
    }

    /**
     * Selects a random entry from the catalogue and displays it
     */
    public void randomEntry() {
        if (catalogue.isEmpty()) {
            System.out.println("No items in the catalogue to select from");
            return;
        }
        Random rand = new Random(); // Selects a random item from the csv
        Integer randomId = (Integer) catalogue.keySet().toArray()[rand.nextInt(catalogue.size())];
        ArrayList<String> randomEntry = catalogue.get(randomId);

        System.out.println("Here is a random item from the furniture catalogue: ID #" + randomId + ", The item is a " + randomEntry.get(0)); //Print statement
        for (int i = 0; i < randomEntry.size(); i++) {
            System.out.println("\t" + headers[i + 1] + ": " + randomEntry.get(i));
        }
        System.out.println();
    }
}