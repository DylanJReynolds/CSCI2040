/*
 * This file contains JUnit test cases for the CatalogueFileIO class.
 * The CatalogueFileIO class is responsible for reading and writing to a CSV file that stores the catalogue data.
 * 
 * DJ: I have removed all mentions of the previous console-based login system and replaced it with the correct UI-based login system.
 */

package furnitureCatalogue;

import org.junit.jupiter.api.*;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CatalogueFileIOTest {
    private CatalogueFileIO fileIO;
    private CatalogueUI ui;
    private File tempCsvFile;

    @BeforeEach
    void setUp() throws IOException, URISyntaxException {
        // Copy the original CSV resource to a temporary file
        URL resource = getClass().getClassLoader().getResource("Sample.csv");
        assertNotNull(resource, "Sample.csv resource not found");
        File originalFile = new File(resource.toURI());
        tempCsvFile = File.createTempFile("Sample", ".csv");
        Files.copy(originalFile.toPath(), tempCsvFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // Create a test UI that bypasses the interactive menu and login
        ui = new CatalogueUI() {
            @Override
            protected boolean inputLogin() {
                this.role = "admin";
                return false;
            }
        };

        fileIO = new CatalogueFileIO("Sample.csv", ui);
        // Use reflection to replace the csvFile field with the temporary file
        try {
            java.lang.reflect.Field csvFileField = CatalogueFileIO.class.getDeclaredField("csvFile");
            csvFileField.setAccessible(true);
            csvFileField.set(fileIO, tempCsvFile);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Reflection error: " + e.getMessage());
        }
    }

    @AfterEach
    void tearDown() {
        if (tempCsvFile.exists()) {
            tempCsvFile.delete();
        }
    }

    @Test
    void testLoadFile() {
        assertNotNull(fileIO);
        // Check that headers and catalogue are loaded into the UI
        assertNotNull(ui.headers, "Headers should be loaded");
        assertNotNull(ui.catalogue, "Catalogue should be initialized");
    }

    @Test
    void testAddCSVLine() throws IOException {
        String newLine = "500,Blue Cotton Sofa,319,Sofa,Blue,Cotton,Small,41,Leon's,Gothic,205";
        fileIO.addCSVLine(newLine);

        String foundLine = searchCSV("500");
        assertNotNull(foundLine, "The added line should exist in the CSV file.");

        String[] expectedValues = newLine.split(",");
        String[] actualValues = foundLine.split(",");
        assertArrayEquals(expectedValues, actualValues, "Each column should match exactly.");
    }

    @Test
    void testEditCSVLine() throws IOException {
        String originalLine = "500,Blue Cotton Sofa,319,Sofa,Blue,Cotton,Small,41,Leon's,Gothic,205";
        String newLine = "500,Edited Sofa,319,Sofa,Red,Cotton,Small,41,Leon's,Gothic,205";

        fileIO.addCSVLine(originalLine);
        fileIO.editCSVLine("500", newLine);

        String foundLine = searchCSV("500");
        assertNotNull(foundLine, "The edited line should exist in the CSV file.");
        String[] expectedValues = newLine.split(",");
        String[] actualValues = foundLine.split(",");
        assertArrayEquals(expectedValues, actualValues, "Each column should match exactly after editing.");
    }

    @Test
    void testDeleteCSVLine() throws IOException {
        String lineToDelete = "500,Blue Cotton Sofa,319,Sofa,Blue,Cotton,Small,41,Leon's,Gothic,205";
        fileIO.addCSVLine(lineToDelete);

        fileIO.deleteCSVLine("500");

        String foundLine = searchCSV("500");
        assertNull(foundLine, "The line should have been deleted and not exist in the CSV file.");
    }

    @Test
    void testRelevancySearch() throws IOException {
        fileIO.addCSVLine("100,Black Leather Sofa,500,Sofa,Black,Leather,Large,50,IKEA,Modern,300");
        fileIO.addCSVLine("101,Blue Fabric Sofa,300,Sofa,Blue,Fabric,Medium,40,Leon's,Contemporary,200");
        fileIO.addCSVLine("102,Red Leather Chair,200,Chair,Red,Leather,Small,20,Wayfair,Classic,150");
        fileIO.addCSVLine("103,White Wooden Table,150,Table,White,Wood,Medium,30,Home Depot,Rustic,100");

        // Perform relevancy search for "Leather Sofa"
        List<String> results = fileIO.relevancySearch("Leather Sofa");

        // Verify that the results are not null or empty
        assertNotNull(results, "Search results should not be null.");
        assertFalse(results.isEmpty(), "Search results should not be empty.");

        // Ensure the most relevant item is ranked first
        assertEquals("100,Black Leather Sofa,500,Sofa,Black,Leather,Large,50,IKEA,Modern,300", results.get(0),
                "The most relevant item should be first in the results.");

        // Ensure another leather-related result appears
        assertTrue(results.contains("102,Red Leather Chair,200,Chair,Red,Leather,Small,20,Wayfair,Classic,150"),
                "A relevant leather-related item should appear in the results.");
    }

    // Helper method: search for a line by the given ID in the temporary CSV file.
    private String searchCSV(String searchId) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(tempCsvFile));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] values = line.split(",");
            if (values.length > 0 && values[0].trim().equals(searchId)) {
                reader.close();
                return line;
            }
        }
        reader.close();
        return null;
    }
}
