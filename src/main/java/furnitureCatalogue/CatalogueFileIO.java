package furnitureCatalogue;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class CatalogueFileIO {
    private String fileName;
    public CatalogueUI UI;
    private File csvFile;
    private final String mode;

    public CatalogueFileIO(String fileName, CatalogueUI catalogueUI, String mode) {
        this.UI = catalogueUI;
        this.fileName = fileName;
        this.mode = mode;
        this.loadFile();
    }

    /**
     * Loads the CSV file into a Hash Map
     */
    public void loadFile() {
        try {
            // Use class loader to get file from resources
            if (!mode.equals("1")){
                URL resource = getClass().getClassLoader().getResource(fileName);

                if (resource == null) {
                    throw new FileNotFoundException("File not found: " + fileName);
                }
                csvFile = new File(resource.toURI());  // Convert URL to File using URI
            } else{
                csvFile = new File(fileName);
            }


            // Proceed as usual
            Scanner fileScanner = new Scanner(csvFile);
            this.UI.headers = fileScanner.nextLine().split(","); // Get headers from first line of CSV
            this.UI.catalogue = new HashMap<>(); // Initialize blank hash map
            // load each entry from CSV file into hash map
            while (fileScanner.hasNextLine()) {
                String[] line = fileScanner.nextLine().split(",");
                ArrayList<String> temp = new ArrayList<>(Arrays.asList(line).subList(1, line.length));
                UI.catalogue.put(Integer.parseInt(line[0]), temp);
            }
            fileScanner.close();
            for (int i = 1; i < UI.headers.length; i++) {
                int maxLength = 0;
                for (ArrayList<String> entry : UI.catalogue.values()) {
                    if (entry.get(i - 1).length() > maxLength) {
                        maxLength = entry.get(i - 1).length();
                    }
                }
                UI.maxLengths[i - 1] = maxLength;
            }
        } catch (FileNotFoundException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds a new line to the bottom of the CSV file
     *
     * @param newLine String to add to file
     */
    public void addCSVLine(String newLine) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile, true));
            writer.write(newLine);
            writer.newLine();
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.err.println("Error writing to CSV file: " + e.getMessage());
        }
    }

    /**
     * Finds a line in the CSV and changes its text
     *
     * @param lineID  the ID of the line to change
     * @param newLine the new text to use
     */
    public void editCSVLine(String lineID, String newLine) {
        try {
            ArrayList<String> lines = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new FileReader(csvFile));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(lineID)) {
                    lines.add(newLine);
                } else {
                    lines.add(line);
                }
            }
            reader.close();

            BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile));
            for (String l : lines) {
                writer.write(l);
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            System.err.println("Error editing CSV file: " + e.getMessage());
        }
    }

    /**
     * Deletes a specified line from the CSV
     *
     * @param lineID ID of the line to remove
     */
    public void deleteCSVLine(String lineID) {
        try {
            ArrayList<String> lines = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new FileReader(csvFile));
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (!parts[0].equals(lineID)) {
                    lines.add(line);
                }
            }
            reader.close();

            BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile));
            for (String l : lines) {
                writer.write(l);
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            System.err.println("Error deleting from CSV file: " + e.getMessage());
        }
    }

    public ArrayList<String> getRandomEntry() {
        if (UI.catalogue.isEmpty()) {
            return null;  // Null if empty
        }
        Random rand = new Random(); //Calls randomness and pulls a random item from the catalogue
        Integer randomId = (Integer) UI.catalogue.keySet().toArray()[rand.nextInt(UI.catalogue.size())];
        return UI.catalogue.get(randomId);
    }

    public List<String> relevancySearch(String search) {
        List<String> results = new ArrayList<>();
        return results;
    }
}
