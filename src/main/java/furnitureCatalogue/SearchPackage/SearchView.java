/*
 * This class is responsible for searching the catalogue for furniture items based on user input.
 * It provides methods to search for items based on a query, sort the results, and filter the results.
 */

package furnitureCatalogue.SearchPackage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

// Placeholder to be properly implemented in iteration 3.
public class SearchView {
    private static SearchView v;

    // All these variables should be text fields or other swing objects in final product.
    public String query = ""; // Actual search string
    public String sortCategory = "id";
    public boolean sortMode = true; // true = ascending order, false = descending order.
    public HashMap<String, String> filters = new HashMap<>();
    public HashMap<String, ArrayList<String>> ranges = new HashMap<>();

    // Private constructor, called at first request of class.
    private SearchView() {
        v = this;
    }

    // Returns reference to view (view is created on first call).
    public static SearchView getInstance() {
        if(Objects.isNull(v)) {
            v = new SearchView();
        }
        return v;
    }

    public String getQuery() {return query;}
    public String getSortCategory() {return sortCategory;}
    public boolean getSortMode() {return sortMode;}
}
