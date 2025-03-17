/**
 * This class can read and write to two different CSV files, depending on the constructor parameter.
 * If the file is "Sample.csv", we load furniture data into UI.catalogue.
 * If the file is "Users.csv", we skip that furniture loading – but we still set up csvFile so you can edit/write user lines.
 */

package furnitureCatalogue;

import java.io.*;
import java.util.*;

public class CatalogueFileIO {
    private String fileName;
    public CatalogueUI UI;
    private File csvFile;

    public CatalogueFileIO(String fileName, CatalogueUI catalogueUI) {
        this.fileName = fileName;
        this.UI = catalogueUI;

        if ("Sample.csv".equalsIgnoreCase(fileName)) {
            loadFurnitureData();
        } else {
            System.out.println("Skipping furniture load because file is '" + fileName + "'.");
        }

        initCsvFile();
    }

    /**
     * If the file is "Sample.csv", we read from the JAR resource and load it into UI.catalogue.
     */
    private void loadFurnitureData() {
        try {
            // Attempt to read from resources
            InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(fileName);
            if (resourceStream == null) {
                System.out.println("Warning: Resource " + fileName + " not found in JAR. " +
                                   "We will create a blank furniture file if needed.");
                return;
            }
            Scanner fileScanner = new Scanner(resourceStream);

            if (!fileScanner.hasNextLine()) {
                System.out.println("Warning: " + fileName + " is empty in resources.");
                if (UI != null) {
                    UI.catalogue = new HashMap<>();
                    UI.headers = new String[0];
                }
                fileScanner.close();
                return;
            }

            // First line: headers
            String firstLine = fileScanner.nextLine();
            if (UI != null) {
                UI.headers = firstLine.split(",");
                UI.catalogue = new HashMap<>();
            }

            // Additional lines: add to UI.catalogue
            while (fileScanner.hasNextLine()) {
                String[] line = fileScanner.nextLine().split(",");
                if (line.length > 1 && UI != null) {
                    // line[0] = ID, rest are fields
                    ArrayList<String> temp = new ArrayList<>(Arrays.asList(line).subList(1, line.length));
                    UI.catalogue.put(Integer.parseInt(line[0]), temp);
                }
            }
            fileScanner.close();

            // compute maxLengths for console spacing if UI != null
            if (UI != null && UI.headers != null && UI.headers.length > 0) {
                for (int i = 1; i < UI.headers.length; i++) {
                    int maxLength = 0;
                    for (ArrayList<String> entry : UI.catalogue.values()) {
                        if (entry.get(i - 1).length() > maxLength) {
                            maxLength = entry.get(i - 1).length();
                        }
                    }
                    UI.maxLengths[i - 1] = maxLength;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error loading furniture from " + fileName + ": " + e.getMessage());
        }
    }

    /**
     * Ensure we have a real local file named fileName, so we can add/edit/delete lines for either furniture or user data.
     */
    private void initCsvFile() {
        csvFile = new File(fileName);
    
        if (!csvFile.exists()) {
            System.out.println("Local file '" + fileName 
                               + "' not found on disk. Not creating it.");
            // Optionally set csvFile = null so that
            // add/edit/delete calls become no-ops.
            // csvFile = null;
        }
    }

    /**
     * Append a line to the bottom of csvFile. 
     * For furniture, you might pass "55,Chair,..." 
     * For users, you might pass "bob,hashedpw,1" etc.
     */
    public void addCSVLine(String newLine) {
        if (csvFile == null) {
            System.err.println("csvFile is null - cannot add line: " + newLine);
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile, true))) {
            writer.write(newLine);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error writing to " + fileName + ": " + e.getMessage());
        }
    }

    /**
     * Replaces any line whose first comma-separated field equals lineID.
     * E.g. for furniture, lineID = "55". For user, lineID might be "bob" (if you store user as first column).
     */
    public void editCSVLine(String lineID, String newLine) {
        if (csvFile == null) {
            System.err.println("csvFile is null - cannot edit lineID: " + lineID);
            return;
        }
        try {
            ArrayList<String> allLines = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new FileReader(csvFile));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length > 0 && parts[0].equals(lineID)) {
                    allLines.add(newLine);
                } else {
                    allLines.add(line);
                }
            }
            reader.close();

            BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile));
            for (String l : allLines) {
                writer.write(l);
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            System.err.println("Error editing " + fileName + ": " + e.getMessage());
        }
    }

    /**
     * Delete any line whose first field equals lineID.
     */
    public void deleteCSVLine(String lineID) {
        if (csvFile == null) {
            System.err.println("csvFile is null - cannot delete lineID: " + lineID);
            return;
        }
        try {
            ArrayList<String> allLines = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new FileReader(csvFile));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                // Keep lines if the ID doesn't match
                if (parts.length > 0 && !parts[0].equals(lineID)) {
                    allLines.add(line);
                }
            }
            reader.close();

            BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile));
            for (String l : allLines) {
                writer.write(l);
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            System.err.println("Error deleting lineID=" + lineID + " from " + fileName + ": " + e.getMessage());
        }
    }

    /**
     * Returns a random entry from the in-memory UI.catalogue (only relevant if fileName == "Sample.csv").
     * For "Users.csv", UI.catalogue might not be used at all. 
     */
    public ArrayList<String> getRandomEntry() {
        if (UI == null || UI.catalogue == null || UI.catalogue.isEmpty()) {
            return null;
        }
        Random rand = new Random();
        Integer randomId = (Integer) UI.catalogue.keySet().toArray()[rand.nextInt(UI.catalogue.size())];
        return UI.catalogue.get(randomId);
    }

    /**
     * Stub. If you want a text-based search. 
     */
    public List<String> relevancySearch(String search) {
        // Implementation omitted
        return new ArrayList<>();
    }
}
