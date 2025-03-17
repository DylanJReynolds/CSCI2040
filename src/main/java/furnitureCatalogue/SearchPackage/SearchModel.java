/*
 * This class is the model for the search functionality of the application.
 * It is responsible for querying the database and returning the results to the controller.
 */

package furnitureCatalogue.SearchPackage;

import java.net.URL;
import java.sql.*;
import java.util.HashMap;
import java.util.Objects;

public class SearchModel {
    private static SearchModel m; // This class is implemented as a singleton.
    private SearchController controller;

    private String fileName;

    // Private constructor, called at first request of class.
    private SearchModel() {
        m = this;
        controller = SearchController.getInstance();
        fileName = "Sample.csv";
    }

    // Returns reference to model (model is created on first call).
    protected static SearchModel getInstance() {
        if(Objects.isNull(m)) {
            m = new SearchModel();
        }
        return m;
    }

    public void query() {
        String url = "src/main/resources/" + fileName;
        String order;
        String filter = "";
        if(controller.sortMode) {
            order = " ASC";
        }
        else {
            order = " DESC";
        }

        for(String s : controller.filters.keySet()) {
            filter += s + " = '" + controller.filters.get(s) + "' AND ";
        }
        for(String s : controller.ranges.keySet()) {
            filter += s + " BETWEEN " + controller.ranges.get(s).get(0) + " AND " + controller.ranges.get(s).get(1) + " AND ";
        }
        filter = filter.substring(0, filter.length() - 4);

        try(Connection connection = DriverManager.getConnection("jdbc:h2:mem:")) {
            PreparedStatement load = connection.prepareStatement("CREATE TABLE t AS SELECT * FROM CSVREAD('" + url + "')");
            load.execute();

            PreparedStatement reformat = connection.prepareStatement("ALTER TABLE t ALTER COLUMN id INTEGER;" +
            "ALTER TABLE t ALTER COLUMN Quantity INTEGER;" +
            "ALTER TABLE t ALTER COLUMN Weight INTEGER;");
            reformat.execute();

//            PreparedStatement search = connection.prepareStatement("SELECT * FROM t WHERE " + filter + "Name = '%" + controller.query + "%' ORDER BY " + controller.sortCategory + order);
            PreparedStatement search = connection.prepareStatement("SELECT * FROM t WHERE " +
            filter + " ORDER BY " + controller.sortCategory + order);
            ResultSet queryResult = search.executeQuery();
            while(queryResult.next()) {
                System.out.println(queryResult.getInt("id") + " - " +
                queryResult.getString("Name") + " - " +
                queryResult.getString("Price") + " - " +
                queryResult.getString("Furniture Type") + " - " +
                queryResult.getString("Colour") + " - " +
                queryResult.getString("Materials") + " - " +
                queryResult.getString("Size") + " - " +
                queryResult.getString("Quantity") + " - " +
                queryResult.getString("Company") + " - " +
                queryResult.getString("Style") + " - " +
                queryResult.getString("Weight"));
            }
            System.out.println();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
