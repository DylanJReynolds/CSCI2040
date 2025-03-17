/*
 * This is the main class for the Furniture Catalogue application.
 * It is responsible for displaying the main menu and handling user input.
 * It also contains the main method for running the application.
 * 
 * DJ: I have removed all mentions of the previous console-based login system and replaced it with the correct UI-based login system.
 */

package furnitureCatalogue;

import furnitureCatalogue.SearchPackage.SearchController;
import furnitureCatalogue.SearchPackage.SearchView;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.*;

public class CatalogueUI extends JFrame {

    public HashMap<Integer, ArrayList<String>> catalogue;
    public int[] maxLengths = new int[10];
    public CatalogueFileIO fileIO;
    public String[] headers;

    private SearchController c;
    private SearchView v;
    protected Login login;
    protected String role;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CatalogueUI catalogueUI = new CatalogueUI();
            catalogueUI.setVisible(true);
        });
    }

    /** 
     * Default constructor: always Swing-based for this version. 
     */
    public CatalogueUI() {
        login = new Login();

        boolean shouldExit = inputLogin();
        if (shouldExit) {
            dispose(); 
            return;
        }

        fileIO = new CatalogueFileIO("Sample.csv", this);
        c = SearchController.getInstance();
        v = SearchView.getInstance();

        initSwingUI();
    }

    /**
     * We make this protected so the test’s subclass can override it
     * without causing “must override supertype method” errors.
     */
    protected void initSwingUI() {
        setTitle("Furniture Catalogue");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        getContentPane().add(mainPanel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        JTextArea outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        if ("admin".equals(role)) {
            // admin
            addButton(buttonPanel, "Display all Entries", 
                e -> captureConsoleOutput(outputArea, this::displayEntriesSwing));
            addButton(buttonPanel, "Edit an Entry", 
                e -> captureConsoleOutput(outputArea, this::editEntrySwing));
            addButton(buttonPanel, "Add an Entry", 
                e -> captureConsoleOutput(outputArea, this::addEntrySwing));
            addButton(buttonPanel, "Remove an Entry", 
                e -> captureConsoleOutput(outputArea, this::removeEntrySwing));
            addButton(buttonPanel, "View Specific Entry", 
                e -> captureConsoleOutput(outputArea, this::viewEntrySwing));
            addButton(buttonPanel, "Search", 
                e -> captureConsoleOutput(outputArea, this::specificSearchSwing));
            addButton(buttonPanel, "Sort", 
                e -> captureConsoleOutput(outputArea, this::sortEntriesSwing));
            addButton(buttonPanel, "Filter", 
                e -> captureConsoleOutput(outputArea, this::filterEntriesSwing));
            addButton(buttonPanel, "Advanced Search", 
                e -> captureConsoleOutput(outputArea, this::advancedSearchSwing));
            addButton(buttonPanel, "Random Entry", 
                e -> captureConsoleOutput(outputArea, this::randomEntrySwing));
                addButton(buttonPanel, "Add a User", e -> {
                    // We can still capture console output if you want, 
                    // but now the actual user input is via JOptionPane:
                    PrintStream originalOut = System.out;
                    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                         PrintStream ps = new PrintStream(bos)) {
                        System.setOut(ps);
                
                        // The new method with Swing-based user creation:
                        login.makeUserSwing(this);
                
                        ps.flush();
                        outputArea.append(bos.toString());
                        outputArea.append("\n");
                        outputArea.setCaretPosition(outputArea.getDocument().getLength());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        System.setOut(originalOut);
                    }
                });
        } else {
            // user
            addButton(buttonPanel, "Display all Entries", 
                e -> captureConsoleOutput(outputArea, this::displayEntriesSwing));
            addButton(buttonPanel, "View Specific Entry", 
                e -> captureConsoleOutput(outputArea, this::viewEntrySwing));
            addButton(buttonPanel, "Search", 
                e -> captureConsoleOutput(outputArea, this::specificSearchSwing));
        }

        addButton(buttonPanel, "Exit", e -> System.exit(0));

        mainPanel.add(buttonPanel, BorderLayout.WEST);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private void addButton(JPanel panel, String text, ActionListener al) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.addActionListener(al);
        panel.add(btn);
        panel.add(Box.createVerticalStrut(10));
    }

    // Capture output and append it to the JTextArea
    private void captureConsoleOutput(JTextArea outputArea, Runnable action) {
        PrintStream originalOut = System.out;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             PrintStream ps = new PrintStream(bos)) 
        {
            System.setOut(ps);
            action.run();
            ps.flush();
            outputArea.append(bos.toString());
            outputArea.append("\n");
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.setOut(originalOut);
        }
    }

    // Swing-based login
    protected boolean inputLogin() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JTextField userField = new JTextField(15);
        JPasswordField passField = new JPasswordField(15);

        panel.add(new JLabel("Username:"));
        panel.add(userField);
        panel.add(new JLabel("Password:"));
        panel.add(passField);

        int result = JOptionPane.showConfirmDialog(
            this,
            panel,
            "Please Log In",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) {
            System.out.println("User cancelled login");
            return true; 
        }

        String username = userField.getText();
        String password = new String(passField.getPassword());

        // hash the password
        String hashed = login.hashString(password);

        // check user map
        if (!login.users.containsKey(username) ||
            !login.users.get(username).equals(hashed)) {
            JOptionPane.showMessageDialog(
                this,
                "Invalid credentials.",
                "Login Failed",
                JOptionPane.ERROR_MESSAGE
            );
            return true;
        }

        // figure out role
        String userRole = login.roles.get(username);
        if (userRole == null) {
            JOptionPane.showMessageDialog(
                this,
                "Could not determine role. Exiting...",
                "Login Error",
                JOptionPane.ERROR_MESSAGE
            );
            return true;
        }
        this.role = userRole;
        return false;
    }

    // Swing-based methods
    /**
     * Expose this method as public so the test can call it.
     */
    public void displayEntriesSwing() {
        System.out.println();
        printTableHeader();
        catalogue.entrySet().forEach(this::printTableRow);
        System.out.println();
    }

    /**
     * Prompt user for ID and then view that entry. 
     */
    private void viewEntrySwing() {
        String inp = JOptionPane.showInputDialog(this, "Enter ID to view:");
        if (inp == null || inp.isEmpty()) {
            System.out.println("Cancelled or blank.");
            return;
        }
        int id;
        try {
            id = Integer.parseInt(inp);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number. Aborting.");
            return;
        }
        if (!catalogue.containsKey(id)) {
            System.out.println("ID not found: " + id);
            return;
        }
        // Print in console
        System.out.println("ID: " + id);
        ArrayList<String> value = catalogue.get(id);
        for (int i = 0; i < value.size(); i++) {
            System.out.println("\t" + headers[i + 1] + ": " + value.get(i));
        }
    }

    /** 
     * Edit an entry by ID, using JOptionPane for prompts. 
     */
    private void editEntrySwing() {
        String inp = JOptionPane.showInputDialog(this, "Enter ID to edit:");
        if (inp == null || inp.isEmpty()) {
            System.out.println("Cancelled or blank.");
            return;
        }
        int id;
        try {
            id = Integer.parseInt(inp);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number. Aborting.");
            return;
        }
        if (!catalogue.containsKey(id)) {
            System.out.println("ID not found: " + id);
            return;
        }
        ArrayList<String> row = catalogue.get(id);
        for (int i = 0; i < row.size(); i++) {
            String field = headers[i + 1];
            String oldVal = row.get(i);
            String newVal = JOptionPane.showInputDialog(
                this,
                "Current " + field + " = '" + oldVal + "'\n" +
                "Enter new (or blank to keep):"
            );
            if (newVal != null && !newVal.isEmpty()) {
                if (isNumericField(field)) {
                    try {
                        Double.parseDouble(newVal);
                        row.set(i, newVal);
                    } catch (NumberFormatException ex) {
                        System.out.println("Invalid numeric input, skipping.");
                    }
                } else {
                    row.set(i, newVal);
                }
            }
        }
        catalogue.put(id, row);
        fileIO.editCSVLine(String.valueOf(id), id + "," + String.join(",", row));
        System.out.println("Updated entry for ID " + id);
    }

    /** 
     * Add new entry with a user-supplied ID and field data. 
     */
    private void addEntrySwing() {
        String inp = JOptionPane.showInputDialog(this, "Enter new ID:");
        if (inp == null || inp.isEmpty()) {
            System.out.println("Cancelled or blank.");
            return;
        }
        int id;
        try {
            id = Integer.parseInt(inp);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number. Aborting.");
            return;
        }
        if (catalogue.containsKey(id)) {
            System.out.println("ID already exists: " + id);
            return;
        }
        ArrayList<String> row = new ArrayList<>();
        // Fill each column
        for (int i = 1; i < headers.length; i++) {
            String field = headers[i];
            String userVal = JOptionPane.showInputDialog(
                this,
                "Enter " + field + " (blank if none):"
            );
            if (userVal == null) {
                System.out.println("Cancelled. Aborting add.");
                return;
            }
            if (isNumericField(field)) {
                if (!userVal.isEmpty()) {
                    try {
                        Double.parseDouble(userVal);
                        row.add(userVal);
                    } catch (NumberFormatException ex) {
                        System.out.println("Invalid numeric input, storing empty.");
                        row.add("");
                    }
                } else {
                    row.add("");
                }
            } else {
                row.add(userVal);
            }
        }
        catalogue.put(id, row);
        fileIO.addCSVLine(id + "," + String.join(",", row));
        System.out.println("Added entry ID=" + id);
    }

    /** 
     * Remove an entry by ID. 
     */
    private void removeEntrySwing() {
        String inp = JOptionPane.showInputDialog(this, "Enter ID to remove:");
        if (inp == null || inp.isEmpty()) {
            System.out.println("Cancelled or blank.");
            return;
        }
        int id;
        try {
            id = Integer.parseInt(inp);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number. Aborting.");
            return;
        }
        if (!catalogue.containsKey(id)) {
            System.out.println("ID not found: " + id);
            return;
        }
        catalogue.remove(id);
        fileIO.deleteCSVLine(String.valueOf(id));
        System.out.println("Removed entry ID=" + id);
    }

    /** 
     * Let user specify name to search. 
     */
    private void specificSearchSwing() {
        String name = JOptionPane.showInputDialog(this, "Enter exact name to search:");
        if (name == null || name.isEmpty()) {
            System.out.println("Cancelled or blank.");
            return;
        }
        boolean found = false;
        for (Map.Entry<Integer, ArrayList<String>> entry : catalogue.entrySet()) {
            if (entry.getValue().get(0).equals(name)) {
                found = true;
                int id = entry.getKey();
                System.out.println("Found ID=" + id);
                ArrayList<String> row = entry.getValue();
                for (int i = 0; i < row.size(); i++) {
                    System.out.println("   " + headers[i + 1] + ": " + row.get(i));
                }
            }
        }
        if (!found) {
            System.out.println("No item found with that name.");
        }
    }

    /** 
     * Sort entries by a chosen field. 
     */
    private void sortEntriesSwing() {
        String field = JOptionPane.showInputDialog(
            this, 
            "Which field to sort by?\n" + String.join(", ", headers)
        );
        if (field == null || field.isEmpty()) {
            System.out.println("Cancelled or blank.");
            return;
        }
        int index = Arrays.asList(headers).indexOf(field);
        if (index == -1) {
            System.out.println("Invalid field: " + field);
            return;
        }

        String mode = JOptionPane.showInputDialog(
            this, 
            "Ascending or Descending? (A/D)"
        );
        if (mode == null) {
            System.out.println("Cancelled. Aborting sort.");
            return;
        }
        boolean ascending = mode.equalsIgnoreCase("A");

        List<Map.Entry<Integer, ArrayList<String>>> myList = getEntries(index);
        if (!ascending) {
            Collections.reverse(myList);
        }
        printTableHeader();
        for (Map.Entry<Integer, ArrayList<String>> entry : myList) {
            printTableRow(entry);
        }
        System.out.println("Sorted by: " + field + " (" + (ascending ? "A" : "D") + ")");
    }

    /** 
     * Filter entries by a chosen field. (Simplistic approach—like old console logic) 
     */
    private void filterEntriesSwing() {
        String field = JOptionPane.showInputDialog(
            this, 
            "Which field to filter?\n" + String.join(", ", headers)
        );
        if (field == null || field.isEmpty()) {
            System.out.println("Cancelled or blank.");
            return;
        }
        int index = Arrays.asList(headers).indexOf(field);
        if (index == -1) {
            System.out.println("Invalid field: " + field);
            return;
        }

        if (isNumericField(headers[index]) || index == 0) {
            String minInput = JOptionPane.showInputDialog(this, "Min value for " + field + ":");
            String maxInput = JOptionPane.showInputDialog(this, "Max value for " + field + ":");
            if (minInput == null || maxInput == null) {
                System.out.println("Cancelled. Aborting filter.");
                return;
            }
            try {
                int minVal = Integer.parseInt(minInput);
                int maxVal = Integer.parseInt(maxInput);
                if (minVal > maxVal) {
                    int temp = minVal;
                    minVal = maxVal;
                    maxVal = temp;
                }
                printTableHeader();
                for (Map.Entry<Integer, ArrayList<String>> e : catalogue.entrySet()) {
                    int val = (index == 0)
                        ? e.getKey()
                        : Integer.parseInt(e.getValue().get(index - 1));
                    if (val >= minVal && val <= maxVal) {
                        printTableRow(e);
                    }
                }
            } catch (NumberFormatException ex) {
                System.out.println("Invalid numeric input. Aborting.");
            }
        } else {
            String match = JOptionPane.showInputDialog(this, "Enter the string to match:");
            if (match == null) {
                System.out.println("Cancelled. Aborting filter.");
                return;
            }
            printTableHeader();
            for (Map.Entry<Integer, ArrayList<String>> e : catalogue.entrySet()) {
                String val = e.getValue().get(index - 1);
                if (val.equalsIgnoreCase(match)) {
                    printTableRow(e);
                }
            }
        }
    }

    /** 
     * Advanced search: example that modifies the search controller's filters, then calls searchQuery. 
     */
    private void advancedSearchSwing() {
        v.filters.clear();
        v.ranges.clear();

        while (true) {
            String field = JOptionPane.showInputDialog(
                this, 
                "Enter field to filter (blank to end):"
            );
            if (field == null || field.isEmpty()) {
                break;
            }
            int index = Arrays.asList(headers).indexOf(field);
            if (index == -1) {
                System.out.println("Invalid field: " + field);
                continue;
            }
            if (isNumericField(headers[index]) || index == 0) {
                String min = JOptionPane.showInputDialog(this, "Min for " + field + ":");
                String max = JOptionPane.showInputDialog(this, "Max for " + field + ":");
                if (min != null && max != null) {
                    ArrayList<String> r = new ArrayList<>();
                    r.add(min);
                    r.add(max);
                    v.ranges.put(field, r);
                }
            } else {
                String val = JOptionPane.showInputDialog(this, "Filter value for " + field + ":");
                if (val != null) {
                    v.filters.put(field, val);
                }
            }
        }

        String sortField = JOptionPane.showInputDialog(
            this, 
            "Sort by what category?\n" + String.join(", ", headers)
        );
        if (sortField == null) {
            System.out.println("Cancelled. No advanced search done.");
            return;
        }
        v.sortCategory = sortField;

        String mode = JOptionPane.showInputDialog(this, "Ascending or Descending? (A/D)");
        if (mode == null) {
            System.out.println("Cancelled. No advanced search done.");
            return;
        }
        v.sortMode = mode.equalsIgnoreCase("A");

        printTableHeader();
        c.searchQuery();
    }

    /** 
     * Print a random entry. 
     */
    private void randomEntrySwing() {
        if (catalogue.isEmpty()) {
            System.out.println("No items in the catalogue.");
            return;
        }
        Random rand = new Random();
        Integer id = (Integer) catalogue.keySet().toArray()[rand.nextInt(catalogue.size())];
        ArrayList<String> row = catalogue.get(id);

        System.out.println("Random item: ID #" + id + " => " + row.get(0));
        for (int i = 0; i < row.size(); i++) {
            System.out.println("   " + headers[i + 1] + ": " + row.get(i));
        }
    }

    // HELPER LOGIC
    private boolean isNumericField(String fieldName) {
        String lower = fieldName.toLowerCase();
        return lower.contains("price") 
            || lower.contains("quantity") 
            || lower.contains("weight");
    }

    /** 
     * Prints a table header line to System.out. 
     */
    private void printTableHeader() {
        StringBuilder sb = new StringBuilder("id\t");
        for (int i = 1; i < headers.length; i++) {
            sb.append(headers[i]);
            for (int j = 0; j <= Math.ceil((double) (maxLengths[i - 1]) / 4.0)
                             - Math.floor((double) (headers[i].length()) / 4.0); j++) {
                sb.append("\t");
            }
        }
        System.out.println(sb);
    }

    /** 
     * Prints a single row. 
     */
    private void printTableRow(Map.Entry<Integer, ArrayList<String>> entry) {
        Integer key = entry.getKey();
        ArrayList<String> row = entry.getValue();
        StringBuilder sb = new StringBuilder(key + "\t");
        for (int i = 0; i < row.size(); i++) {
            sb.append(row.get(i));
            for (int j = 0; j <= Math.ceil((double) (maxLengths[i]) / 4.0)
                             - Math.floor((double) (row.get(i).length()) / 4.0); j++) {
                sb.append("\t");
            }
        }
        System.out.println(sb);
    }

    /** 
     * Returns a sorted list of entries based on the given header index. 
     */
    private List<Map.Entry<Integer, ArrayList<String>>> getEntries(int index) {
        List<Map.Entry<Integer, ArrayList<String>>> myList = new ArrayList<>(catalogue.entrySet());
        Comparator<Map.Entry<Integer, ArrayList<String>>> comp;

        if (index == 2 || index == 7 || index == 10) {
            comp = (o1, o2) -> {
                int n = Integer.parseInt(o1.getValue().get(index - 1));
                int m = Integer.parseInt(o2.getValue().get(index - 1));
                return Integer.compare(n, m);
            };
        } else if (index == 0) {
            comp = (o1, o2) -> Integer.compare(o1.getKey(), o2.getKey());
        } else {
            comp = (o1, o2) -> o1.getValue().get(index - 1).compareTo(o2.getValue().get(index - 1));
        }
        myList.sort(comp);
        return myList;
    }
}
