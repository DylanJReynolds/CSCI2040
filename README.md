# CSCI2040

# Furniture Catalogue

## Description
The purpose of this documentation is to instruct the user how to use the furniture catalogue program, as well as how to clone and execute the program.

## Download

### Prerequisites:

You will need the following to be able to run and compile this program:
- The latest version of Java
- Maven
- Git
- Windows (optional: recommended)


1. Go to your terminal, and use the proper command to get to the folder you want to clone the repo.
2. Use `git clone https://github.com/DylanJReynolds/CSCI2040` to clone the repository into your desired folder. You should have the repo in your folder.
3. Run `Build-Script.bat` to build a jar file of the directory that can be executed.
4. Non-Windows users: Run `mvn clean install` to compile and build a jar file, then run `java -jar target/CSCI2040-1.0-SNAPSHOT.jar` to execute.




## How to use:

Before you are allowed to access the program, you are required to enter a username and password. If either of these does not match a registered user, the program will exit.

Otherwise, there are two types of users: regular users, and administrators. Regular users will only be able to view furniture items, while administrators are able to modify either both the user and furniture database.

Each piece of furniture is represented using an identification number and has various parameters. These parameters are as follows:
- Price
- Furniture Type
- Colour
- Materials
- Size
- Quantity
- Company
- Weight
- Style

The GUI, depending on if you are a regular user or administrator, will give a different menu. This is the menu for the administrator:

1. Display all entries (Displays ever entry in the database to the user).
2. Edit an entry (Given an entry ID, allows the user to change each of the Price, Furniture Type, Colour, Materials, Size, Quantity, Company, Weight, and Style of that entry.)
3. Add an entry (Adds an entry to the database, and allows the user to set the ID, Price, Furniture Type, Colour, Materials, Size, Quantity, Company, Weight, and Style of that entry.)
4. Remove an entry (Removes an entry from the database corresponding to its ID).
5. View specific entry (Given an ID, view that item).
6. Search (Given an exact name, the program will fetch the corresponding item).
7. Sort (Given one of the above parameters, this will sort items in the database in either ascending or descending order with respect to that attribute).
8. Filter (Given a parameter with a numerical quantity, this will keep the items that are within that price range).
9. Advanced Search (Given a parameter, will search for all that fit within the given input).
11. Random Entry (Picks a random item by ID and displays the info).
12. Add a user (Creates a user, and sets their password to the database).
13. Exit (Terminates the program, saving any changes).

For a regular user, the program looks like this:
1. Display all entries (Displays ever entry in the database to the user).
2. View Specific Entry (Given an ID, view that item).
3. Search (Given an exact name, the program will fetch the corresponding item).
4. Exit (Terminates the program, saving any changes).

Please note that terminating the program in other ways (i.e. shutting it down via task manager) will still keep the changes made to the database made prior.

---

## Documentation: 
See our [Hashnode](https://nanonest.hashnode.space) for detailed documentation and planning.
