package furnitureCatalogue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class CatalogueTestSuite {

    @BeforeAll
    public static void setup() {
        System.out.println("Starting test suite...");
    }

    @Test
    public void testCatalogueFileIO() throws IOException {
        CatalogueFileIOTest catalogueFileIOTest = new CatalogueFileIOTest();
        catalogueFileIOTest.testLoadFile();
        catalogueFileIOTest.testAddCSVLine();
        catalogueFileIOTest.testEditCSVLine();
        catalogueFileIOTest.testDeleteCSVLine();
    }

    @Test
    public void testCatalogueUI() {
        //CatalogueUITest catalogueUITest = new CatalogueUITest();
        //catalogueUITest.testDisplayEntries();
        //catalogueUITest.testViewEntry();
        //catalogueUITest.testEditEntry();
        //catalogueUITest.testAddEntry();
        //catalogueUITest.testRemoveEntry();
        //catalogueUITest.testSpecificSearch();
    }

    @Test
    public void testLogin() {
        LoginTest loginTest = new LoginTest();
        loginTest.setUp();
        loginTest.testAdminLogin();
        loginTest.testUserLogin();
        loginTest.testInvalidLogin();
    }
}
