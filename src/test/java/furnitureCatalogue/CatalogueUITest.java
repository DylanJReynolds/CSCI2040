/*
 * This file contains JUnit test cases for the CatalogueUI class.
 * The CatalogueUI class is responsible for displaying the catalogue data and allowing the user to interact with it.
 * 
 * DJ: I have removed all mentions of the previous console-based login system and replaced it with the correct UI-based login system.
 */

 package furnitureCatalogue;

 import org.junit.jupiter.api.*;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import static org.junit.jupiter.api.Assertions.*;
 
 public class CatalogueUITest {
     private CatalogueUI ui;
     private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
     private final PrintStream originalOut = System.out;
 
     @BeforeEach
     void setUp() {
         // Create a test UI that:
         // (a) doesn't pop login
         // (b) doesn't open the Swing window
         ui = new CatalogueUI() {
             @Override
             protected boolean inputLogin() {
                 this.role = "admin"; 
                 return false; 
             }
 
             @Override
             protected void initSwingUI() {
                 // do nothing to prevent the real GUI from appearing
             }
         };
 
         // Provide some sample data
         ArrayList<String> entry = new ArrayList<>();
         entry.add("Blue Wooden Chair"); // Name
         entry.add("250");               // Price
         entry.add("Chair");             // Furniture Type
         entry.add("Blue");              // Colour
         entry.add("Wooden");            // Materials
         entry.add("Large");             // Size
         entry.add("75");                // Quantity
         entry.add("Leon's");            // Company
         entry.add("Modern");            // Style
         entry.add("122");               // Weight
         ui.catalogue.put(193, entry);
 
         // Redirect System.out
         System.setOut(new PrintStream(outContent));
     }
 
     @AfterEach
     void tearDown() {
         System.setOut(originalOut);
     }
 
     @Test
     void testDisplayEntries() {
         // Now that displayEntriesSwing() is public, we can call it:
         ui.displayEntriesSwing();
 
         String output = outContent.toString();
         // Expect the output to show the ID + name
         assertTrue(output.contains("193\tBlue Wooden Chair"), 
             "Output should display the entry with ID 193 and name 'Blue Wooden Chair'.");
     }
 
     @Test
     void testDataStructureAdd() {
         // Directly add an entry to the map, confirm it appears
         ArrayList<String> newItem = new ArrayList<>(
             Arrays.asList("New Table", "100", "Table", "Green", "Steel", "Small", "5", "IKEA", "Vintage", "45")
         );
         ui.catalogue.put(200, newItem);
 
         assertTrue(ui.catalogue.containsKey(200), "Catalogue should have the new item with ID 200.");
         assertEquals("New Table", ui.catalogue.get(200).get(0), "First field should be 'New Table'.");
     }
 }
 