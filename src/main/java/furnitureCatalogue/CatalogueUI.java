/*
 * Main class for the Furniture Catalogue application.
 * This class provides a Swing-based UI for the catalogue.
 * It allows users to display, edit, add, and remove entries from the catalogue.
 * It also provides search, sort, filter, and advanced search functionality.
 * The application is role-based, with different options available for admin and non-admin users.
 */

package furnitureCatalogue;

import furnitureCatalogue.SearchPackage.SearchController;
import furnitureCatalogue.SearchPackage.SearchView;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.List;

public class CatalogueUI extends JFrame {
    public HashMap<Integer, ArrayList<String>> catalogue;
    public static int[] maxLengths = new int[10];
    public CatalogueFileIO fileIO;
    public String[] headers;
    private SearchController c;
    private SearchView v;
    protected Login login;
    protected String role;

    // Field for command-line input (used by tests)
    protected Scanner s;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CatalogueUI catalogueUI = new CatalogueUI();
            catalogueUI.setVisible(true);
        });
    }

    /**
     * Default constructor uses the Swing-based UI.
     */
    public CatalogueUI() {
        login = new Login();
        if (inputLogin()) {
            dispose();
            return;
        }
        fileIO = new CatalogueFileIO("Sample.csv", this);
        c = SearchController.getInstance();
        v = SearchView.getInstance();
        initSwingUI();
    }

    /**
     * Sets up the Swing UI.
     */
    protected void initSwingUI() {
        setTitle("Furniture Catalogue");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel mainPanel = new JPanel(new BorderLayout());
        getContentPane().add(mainPanel);

        // Optionally, add the SearchView panel at the top
        mainPanel.add(v, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        JTextArea outputArea = new JTextArea();
        outputArea.setEditable(false);
        // FONT MAKES SURE THAT SPACING IS CONSISTENT, NOT NEEDED IF/WHEN A TABLE IS USED FOR OUTPUT
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(outputArea);

        if ("admin".equals(role)) {
            addButton(buttonPanel, "Display all Entries",
                    e -> captureConsoleOutput(outputArea, this::displayEntriesSwing));
            // New button for JTable display
            addButton(buttonPanel, "Display Catalogue in JTable",
                    e -> displayEntriesTable());
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
            addButton(buttonPanel, "Manage Users", e -> {
                PrintStream originalOut = System.out;
                try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                     PrintStream ps = new PrintStream(bos)) {
                    System.setOut(ps);
                    login.modUserInput(this);
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
            // For non-admin users
            addButton(buttonPanel, "Display all Entries",
                    e -> captureConsoleOutput(outputArea, this::displayEntriesSwing));
            // New button for JTable display
            addButton(buttonPanel, "Display Catalogue in JTable",
                    e -> displayEntriesTable());
            addButton(buttonPanel, "View Specific Entry",
                    e -> captureConsoleOutput(outputArea, this::viewEntrySwing));
            addButton(buttonPanel, "Search",
                    e -> captureConsoleOutput(outputArea, this::specificSearchSwing));
        }
        addButton(buttonPanel, "Exit", e -> System.exit(0));
        mainPanel.add(buttonPanel, BorderLayout.WEST);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
    }

    // Helper method to create and add a button to a panel.
    private void addButton(JPanel panel, String text, ActionListener al) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.addActionListener(al);
        panel.add(btn);
        panel.add(Box.createVerticalStrut(10));
    }

    // Capture console output and redirect it to the JTextArea.
    private void captureConsoleOutput(JTextArea outputArea, Runnable action) {
        PrintStream originalOut = System.out;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             PrintStream ps = new PrintStream(bos)) {
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

    // Swing-based login using JOptionPane.
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
                this, panel, "Please Log In",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) {
            System.out.println("User cancelled login");
            return true;
        }
        String username = userField.getText();
        String password = new String(passField.getPassword());
        String hashed = login.hashString(password);
        if (!login.users.containsKey(username) || !login.users.get(username).equals(hashed)) {
            JOptionPane.showMessageDialog(this, "Invalid credentials.",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
            return true;
        }
        String userRole = login.roles.get(username);
        if (userRole == null) {
            JOptionPane.showMessageDialog(this, "Could not determine role. Exiting...",
                    "Login Error", JOptionPane.ERROR_MESSAGE);
            return true;
        }
        this.role = userRole;
        return false;
    }

    // --- Swing-based catalogue methods (display, view, edit, add, etc.) ---

    public void displayEntriesSwing() {
        System.out.println();
        printTableHeader();
        catalogue.entrySet().forEach(this::printTableRow);
        System.out.println();
    }

    /**
     * New method to display entries using a JTable.
     */
    public void displayEntriesTable() {
        boolean hasIDColumn = headers.length > 0 && headers[0].equalsIgnoreCase("ID");
        
        String[] columnNames;
        if (hasIDColumn) {
            columnNames = headers;
        } else {
            columnNames = new String[headers.length + 1];
            columnNames[0] = "ID";
            System.arraycopy(headers, 0, columnNames, 1, headers.length);
        }

        Object[][] data = new Object[catalogue.size()][columnNames.length];
        int rowIndex = 0;
        for (Map.Entry<Integer, ArrayList<String>> entry : catalogue.entrySet()) {
            Integer key = entry.getKey();
            ArrayList<String> rowData = entry.getValue();
            int dataColumn = 0;

            data[rowIndex][dataColumn++] = key;

            for (String value : rowData) {
                if (dataColumn >= columnNames.length) break;
                data[rowIndex][dataColumn++] = value;
            }
            rowIndex++;
        }

        JTable table = new JTable(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Disable cell editing
            }
        };

        table.setFillsViewportHeight(true);
        table.setShowGrid(true);
        table.setGridColor(Color.LIGHT_GRAY);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(200);
        }

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int column = 0; column < table.getColumnCount(); column++) {
            TableColumn tableColumn = table.getColumnModel().getColumn(column);
            int preferredWidth = tableColumn.getMinWidth();
            int maxWidth = tableColumn.getMaxWidth();

            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
                Component c = table.prepareRenderer(cellRenderer, row, column);
                int width = c.getPreferredSize().width + table.getIntercellSpacing().width;
                preferredWidth = Math.max(preferredWidth, width);
            }

            preferredWidth = Math.min(preferredWidth + 50, 400); // Cap at 400px
            tableColumn.setPreferredWidth(preferredWidth);
        }

        JFrame tableFrame = new JFrame("Catalogue Entries");
        tableFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        tableFrame.add(new JScrollPane(table));
        tableFrame.setSize(1200, 600); // Wider window
        tableFrame.setLocationRelativeTo(null);
        tableFrame.setVisible(true);
    }

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
        System.out.println("ID: " + id);
        ArrayList<String> value = catalogue.get(id);
        for (int i = 0; i < value.size(); i++) {
            System.out.println("\t" + headers[i + 1] + ": " + value.get(i));
        }
    }

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
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JTextField[] fields = new JTextField[headers.length - 1];

        for (int i = 0; i < headers.length - 1; i++) {
            panel.add(new JLabel(headers[i + 1] + ":"));
            fields[i] = new JTextField(row.get(i));
            panel.add(fields[i]);
        }

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Entry", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            for (int i = 0; i < fields.length; i++) {
                String newVal = fields[i].getText();
                if (isNumericField(headers[i + 1])) {
                    try {
                        Double.parseDouble(newVal);
                        row.set(i, newVal);
                    } catch (NumberFormatException ex) {
                        System.out.println("Invalid numeric input for " + headers[i + 1] + ", keeping old value.");
                    }
                } else {
                    row.set(i, newVal);
                }
            }
            catalogue.put(id, row);
            fileIO.editCSVLine(String.valueOf(id), id + "," + String.join(",", row));
            System.out.println("Updated entry for ID " + id);
        } else {
            System.out.println("Edit cancelled.");
        }
    }

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

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JTextField[] fields = new JTextField[headers.length - 1];

        for (int i = 0; i < headers.length - 1; i++) {
            panel.add(new JLabel(headers[i + 1] + ":"));
            fields[i] = new JTextField();
            panel.add(fields[i]);
        }

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Entry", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            ArrayList<String> row = new ArrayList<>();
            for (int i = 0; i < fields.length; i++) {
                String value = fields[i].getText();
                if (isNumericField(headers[i + 1])) {
                    try {
                        Double.parseDouble(value);
                        row.add(value);
                    } catch (NumberFormatException ex) {
                        System.out.println("Invalid numeric input for " + headers[i + 1] + ", setting as blank.");
                        row.add("");
                    }
                } else {
                    row.add(value);
                }
            }
            catalogue.put(id, row);
            fileIO.addCSVLine(id + "," + String.join(",", row));
            System.out.println("Added entry ID=" + id);
        } else {
            System.out.println("Add entry cancelled.");
        }
    }

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

    private void sortEntriesSwing() {
        String field = JOptionPane.showInputDialog(
                this, "Which field to sort by?\n" + String.join(", ", headers));
        if (field == null || field.isEmpty()) {
            System.out.println("Cancelled or blank.");
            return;
        }
        int index = Arrays.asList(headers).indexOf(field);
        if (index == -1) {
            System.out.println("Invalid field: " + field);
            return;
        }
        String mode = JOptionPane.showInputDialog(this, "Ascending or Descending? (A/D)");
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

    private void filterEntriesSwing() {
        String field = JOptionPane.showInputDialog(
                this, "Which field to filter?\n" + String.join(", ", headers));
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
                    int val = (index == 0) ? e.getKey() : Integer.parseInt(e.getValue().get(index - 1));
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

    private void advancedSearchSwing() {
        v.filters.clear();
        v.ranges.clear();
        while (true) {
            v.query = JOptionPane.showInputDialog(
                    this, "Enter name of item:");
            break;
        }
        while (true) {
            String field = JOptionPane.showInputDialog(
                    this, "Enter field to filter (blank to end):");
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
                this, "Sort by what category?\n" + String.join(", ", headers));
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

    // Helper method to check if a field is numeric.
    private boolean isNumericField(String fieldName) {
        String lower = fieldName.toLowerCase();
        return lower.contains("price") || lower.contains("quantity") || lower.contains("weight");
    }

    private void printTableHeader() {
        StringBuilder sb = new StringBuilder("id\t");
        for (int i = 1; i < headers.length; i++) {
            sb.append(headers[i]);
            for (int j = 0; j <= Math.ceil((double)(maxLengths[i - 1]) / 8.0)
                    - Math.floor((double)(headers[i].length()) / 8.0); j++) {
                sb.append("\t");
            }
        }
        System.out.println(sb);
    }

    private void printTableRow(Map.Entry<Integer, ArrayList<String>> entry) {
        Integer key = entry.getKey();
        ArrayList<String> row = entry.getValue();
        StringBuilder sb = new StringBuilder(key + "\t");
    
        for (int i = 0; i < headers.length - 1; i++) {
            String value = (i < row.size()) ? row.get(i) : ""; // Use empty string if index is out of bounds
            sb.append(value);
            for (int j = 0; j <= Math.ceil((double) (maxLengths[i]) / 8.0)
                    - Math.floor((double) (value.length()) / 8.0); j++) {
                sb.append("\t");
            }
        }
        System.out.println(sb);
    }

    private List<Map.Entry<Integer, ArrayList<String>>> getEntries(int index) {
        List<Map.Entry<Integer, ArrayList<String>>> list = new ArrayList<>(catalogue.entrySet());
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
        list.sort(comp);
        return list;
    }

    // --- Command-line interface wrapper methods for testing ---

    public void commandLineMenu() {
        // For testing purposes you can leave this as a no-op:
        System.out.println("commandLineMenu() invoked.");
    }

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
                System.out.print("Input new " + field + " to replace " + value.get(i) + ": ");
                String input = s.nextLine();
                if (!input.equals("")) {
                    try {
                        Double.parseDouble(input);
                        value.set(i, input);
                    } catch (NumberFormatException ex) {
                        System.out.println("Invalid numeric input, skipping.");
                    }
                }
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
        for (int i = 1; i < headers.length; i++) {
            String field = headers[i];
            System.out.print("Input " + field + ": ");
            String input = s.nextLine();
            value.add(input);
        }
        catalogue.put(id, value);
        fileIO.addCSVLine(id + "," + String.join(",", value));
    }

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

    public void specificSearch() {
        System.out.print("Enter name to search: ");
        String inp = s.nextLine();
        if (inp.isEmpty()) {
            System.out.println("Blank input detected (Returning).");
            return;
        }
        boolean found = false;
        for (ArrayList<String> value : catalogue.values()) {
            if (value.get(0).equals(inp)) {
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
}
