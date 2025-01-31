import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class main {
  public static Scanner s;
  public static String[] headers = { "Indicator ID", "Name", "Measure", "Measure Info", "Geo Type Name", "Geo Join ID",
      "Geo Place Name", "Time Period", "Start_Date", "Data Value", "Message" };

  public static void main(String[] args) {
    HashMap<Integer, ArrayList<String>> map = new HashMap<>();

    try {
      // Initialize File scanner
      File dataSet = new File("Air_Quality.csv");
      Scanner fileScanner = new Scanner(dataSet);
      fileScanner.nextLine(); // Skips over first line with column labels, should be changed later

      while (fileScanner.hasNext()) { // For each line in the csv
        String line = fileScanner.nextLine();
        String splitLine[] = line.split(","); // Columns split into string array

        // Then placed into list
        ArrayList<String> temp = new ArrayList<>();
        for (int i = 1; i < splitLine.length; i++) {
          temp.add(splitLine[i]);
        }

        // And finally added to the hashmap
        map.put(Integer.parseInt(splitLine[0]), temp);
      }

      fileScanner.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    s = new Scanner(System.in);
    Boolean running = true;
    while (running) {

      System.out.println("1. Display all Entries");
      System.out.println("2. Edit an entry");
      System.out.println("3. Add an entry");
      System.out.println("4. View Specific Entry");
      System.out.println("5. Exit");
      System.out.print("Input: ");

      String inp = s.nextLine();
      switch (inp) {
        case "1":
          displayEntries(map);
          break;
        case "2":
          editEntry(map);
          break;
        case "3":
          addEntry(map);
          break;
        case "4":
          viewEntry(map);
          break;
        case "5":
          running = false;
          break;
        default:
          break;
      }
    }
    s.close();
  }

  public static void displayEntries(Map<Integer, ArrayList<String>> map) {
    System.out.println();
    for (Map.Entry<Integer, ArrayList<String>> entry : map.entrySet()) {
      Integer key = entry.getKey();
      ArrayList<String> value = entry.getValue();
      System.out.println(key + ": " + value);
    }
    System.out.println();
  }

  public static void viewEntry(Map<Integer, ArrayList<String>> map) {
    System.out.print("Choose Entry by ID: ");
    String inp = s.nextLine();
    if (map.containsKey(Integer.parseInt(inp))) {
      ArrayList<String> value = map.get(Integer.parseInt(inp));
      System.out.println(inp);
      for (int i = 0; i < value.size(); i++) {
        System.out.println("\t" + headers[i] + ": " + value.get(i));
      }
    }
  }

  public static void editEntry(Map<Integer, ArrayList<String>> map) {
    System.out.print("Choose Entry by ID: ");
    String inp = s.nextLine();
    if (map.containsKey(Integer.parseInt(inp))) {
      ArrayList<String> value = map.get(Integer.parseInt(inp));
      for (int i = 0; i < value.size(); i++) {
        System.out.print("Input new " + headers[i] + " to replace " + value.get(i) + ": ");
        String input = s.nextLine();
        value.set(i, input);
      }
      map.put(Integer.parseInt(inp), value);
      editCSVLine("Air_Quality.csv", inp, inp + "," + String.join(",", value));
    }
  }

  public static void addEntry(Map<Integer, ArrayList<String>> map) {
    System.out.print("Choose ID for Entry: ");
    String inp = s.nextLine();
    ArrayList<String> value = new ArrayList<>();
    for (int i = 0; i < headers.length; i++) {
      System.out.print("Input " + headers[i] + ": ");
      String input = s.nextLine();
      value.add(input);
    }
    map.put(Integer.parseInt(inp), value);
    addCSVLine("Air_Quality.csv", inp + "," + String.join(",", value));
  }

  public static boolean addCSVLine(String filename, String newLine) {
    BufferedWriter writer = null;
    try {
      writer = new BufferedWriter(new FileWriter(filename, true));
      writer.write(newLine);
      writer.newLine();
      writer.flush();
      writer.close();
      return true;
    } catch (IOException e) {
      System.err.println("Error writing to CSV file: " + e.getMessage());
      return false;
    }
  }

  public static boolean editCSVLine(String filename, String lineID, String newLine) {
    try {
      ArrayList<String> lines = new ArrayList<>();
      BufferedReader reader = new BufferedReader(new FileReader(filename));
      String line;
      while ((line = reader.readLine()) != null) {
        String currentLine = line.substring(0, 6);
        if (currentLine.equals(lineID)) {
          lines.add(newLine);
        } else {
          lines.add(line);
        }
      }
      reader.close();

      BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
      for (String l : lines) {
        writer.write(l);
        writer.newLine();
      }
      writer.close();
      return true;
    } catch (IOException e) {
      System.err.println("Error editing CSV file: " + e.getMessage());
      return false;
    }
  }

  public static boolean deleteCSVLine(String filename, String lineID) {
    try {
      ArrayList<String> lines = new ArrayList<>();
      BufferedReader reader = new BufferedReader(new FileReader(filename));
      String line;
      while ((line = reader.readLine()) != null) {
        String currentLine = line.substring(0, 6);
        if (!currentLine.equals(lineID)) {
          lines.add(line);
        }
      }
      reader.close();

      BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
      for (String l : lines) {
        writer.write(l);
        writer.newLine();
      }
      writer.close();
      return true;
    } catch (IOException e) {
      System.err.println("Error deleting from CSV file: " + e.getMessage());
      return false;
    }
  }

}