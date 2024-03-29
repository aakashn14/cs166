/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;
import java.sql.Timestamp;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Amazon {

   // reference to physical database connection.
   private Connection _connection = null;

   // 500 order in the table
   private int orderNum = 501;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of Amazon store
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Amazon(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Amazon

   // Method to calculate euclidean distance between two latitude, longitude pairs. 
   public double calculateDistance (double lat1, double long1, double lat2, double long2){
      double t1 = (lat1 - lat2) * (lat1 - lat2);
      double t2 = (long1 - long2) * (long1 - long2);
      return Math.sqrt(t1 + t2); 
   }
   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   public int getNextOrderNum() {
      int currentOrderNum = this.orderNum;
      this.orderNum++;
      return currentOrderNum;
  }  // for case 3

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Amazon.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      Amazon esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance();
         // instantiate the Amazon object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Amazon (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            List<String> userDetails = null;
            switch (readChoice()){
               case 1: 
                  CreateUser(esql); 
                  break;
               case 2: 
                  userDetails = LogIn(esql);
                  break;
               case 9: 
                  keepon = false; 
                  break;
               default: 
                  System.out.println("Unrecognized choice!"); 
                  break;
            }//end switch
            if (userDetails.size() != 0) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. View Stores within 30 miles");
                System.out.println("2. View Product List");
                System.out.println("3. Place a Order");
                System.out.println("4. View 5 recent orders");

                //the following functionalities basically used by managers
                System.out.println("5. Update Product");
                System.out.println("6. View 5 recent Product Updates Info");
                System.out.println("7. View 5 Popular Items");
                System.out.println("8. View 5 Popular Customers");
                System.out.println("9. Place Product Supply Request to Warehouse");

                System.out.println(".........................");
                System.out.println("20. Log out");
                switch (readChoice()){
                   case 1:
                     viewStores(esql, userDetails.get(0)); 
                     break;
                   case 2: 
                     viewProducts(esql); 
                     break;
                   case 3: 
                     placeOrder(esql, userDetails.get(0)); 
                     break;
                   case 4: 
                     viewRecentOrders(esql, userDetails.get(0)); 
                     break;
                   case 5: 
                     // System.out.println("User Details0: " + userDetails.get(0));
                     // System.out.println("User Details5: " + userDetails.get(5));
                     updateProduct(esql, userDetails.get(0), userDetails.get(5)); 
                     break;
                   case 6: 
                     // System.out.println("User Details0: " + userDetails.get(0));
                     // System.out.println("User Details5: " + userDetails.get(5));
                     viewRecentUpdates(esql, userDetails.get(0), userDetails.get(5)); 
                     break;
                   case 7: 
                     // System.out.println("User Details0: " + userDetails.get(0));
                     // System.out.println("User Details5: " + userDetails.get(5));
                     viewPopularProducts(esql, userDetails.get(0), userDetails.get(5)); 
                     break;
                   case 8: 
                     // System.out.println("User Details0: " + userDetails.get(0));
                     // System.out.println("User Details5: " + userDetails.get(5));
                     viewPopularCustomers(esql, userDetails.get(0), userDetails.get(5)); 
                     break;
                   case 9: 
                     // System.out.println("User Details0: " + userDetails.get(0));
                     // System.out.println("User Details5: " + userDetails.get(5));
                     placeProductSupplyRequests(esql, userDetails.get(0), userDetails.get(5)); 
                     break;

                   case 20: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user
    **/
   public static void CreateUser(Amazon esql){
      try{
         System.out.print("\tEnter name: ");
         String name = in.readLine();
         System.out.print("\tEnter password: ");
         String password = in.readLine();
         System.out.print("\tEnter latitude: ");   
         String latitude = in.readLine();       //enter lat value between [0.0, 100.0]
         System.out.print("\tEnter longitude: ");  //enter long value between [0.0, 100.0]
         String longitude = in.readLine();
         
         String type="Customer";

			String query = String.format("INSERT INTO USERS (name, password, latitude, longitude, type) VALUES ('%s','%s', %s, %s,'%s')", name, password, latitude, longitude, type);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static List<String> LogIn(Amazon esql){
      try{
         System.out.print("\tEnter name: ");
         String name = in.readLine();
         System.out.print("\tEnter password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM USERS WHERE name = '%s' AND password = '%s'", name, password);
         // int userNum = esql.executeQuery(query);
         List<List<String>> result = esql.executeQueryAndReturnResult(query);

         List<String> info = new ArrayList<>();
         info.add(result.get(0).get(0));
         info.add(result.get(0).get(1));
         info.add(result.get(0).get(2));
         info.add(result.get(0).get(3));
         info.add(result.get(0).get(4));
         info.add(result.get(0).get(5));

	      if (info.size() > 0)
		      return info;

         return null;
      }catch(Exception e){
         System.err.println(e.getMessage());
         return null;
      }
   }//end

// Rest of the functions definition go in here

   public static void viewStores(Amazon esql, String userID) {
      try {
         String query = String.format("SELECT latitude, longitude FROM Users WHERE userID = '%s'", userID);
         
         List<List<String>> results = esql.executeQueryAndReturnResult(query);

         double userLat = Double.parseDouble(results.get(0).get(0));
         double userLong = Double.parseDouble(results.get(0).get(1));

         String query2 = "SELECT storeID, latitude, longitude FROM Store";
         List<List<String>> results2 = esql.executeQueryAndReturnResult(query2);

         System.out.println("Stores within 30 miles of you");
         System.out.println("-----------------------------");
         for(int i=0; i<results2.size(); i++) {
            List<String> record = results2.get(i);
            String storeID = record.get(0);
            double storeLat = Double.parseDouble(record.get(1));
            double storeLong = Double.parseDouble(record.get(2));
            double distance = esql.calculateDistance(storeLat, storeLong, userLat, userLong);
            if (distance < 30) {
                  System.out.println("Store ID: " + storeID);
                  System.out.println("Distance: " + distance + " miles");
                  System.out.println("-----------------------------");
                  
            }
         }

      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void viewProducts(Amazon esql) {
      System.out.print("Enter store ID: ");
      int id;
      try {
         id = Integer.parseInt(in.readLine().trim());
      } catch (Exception e) {
         System.out.println("Your input is invalid: " + e.getMessage());
         return;
      }
   
      try {
         final String query = String.format("SELECT productName, numberOfUnits, pricePerUnit FROM Product WHERE storeID = '%s'", id);
         List<List<String>> results = esql.executeQueryAndReturnResult(query);
   
         System.out.println(String.format("Items in Store %d", id));
         System.out.println("-------------------------");
         for (List<String> record : results) {
            String name = record.get(0);
            int num = Integer.parseInt(record.get(1).trim());
            double price = Double.parseDouble(record.get(2).trim());
            System.out.println("Item: " + name);
            System.out.println("Units available: " + num);
            System.out.println("Price: $" + price);
            System.out.println("-------------------------");
         }
      } catch (Exception e) {
         System.err.println("An error occurred: " + e.getMessage());
      }
   }

   public static void placeOrder(Amazon esql, String userID) {
      try {
         final String userQuery = String.format("SELECT latitude, longitude FROM Users WHERE userID = '%s'", userID.trim());
         List<List<String>> userResults = esql.executeQueryAndReturnResult(userQuery);

         if (userResults.isEmpty()) {
            System.out.println("User " + userID + " not found.");
            return;
         }

         double userLat = Double.parseDouble(userResults.get(0).get(0));
         double userLong = Double.parseDouble(userResults.get(0).get(1));

         System.out.print("Enter Store ID: ");
         int storeID = Integer.parseInt(in.readLine().trim());

         final String storeQuery = String.format("SELECT latitude, longitude FROM Store WHERE storeID = '%s'", storeID);
         List<List<String>> storeResults = esql.executeQueryAndReturnResult(storeQuery);

         if (storeResults.isEmpty()) {
            System.out.println("Store " + storeID + " not found.");
            return;
         }

         double storeLat = Double.parseDouble(storeResults.get(0).get(0));
         double storeLong = Double.parseDouble(storeResults.get(0).get(1));
         double distance = esql.calculateDistance(storeLat, storeLong, userLat, userLong);

         if (distance > 30) {
         System.out.println("Store " + storeID + " is too far from the current location.");
         return;
         }

         System.out.print("\nEnter product name: ");
         String productName = in.readLine().trim();

         final String productQuery = String.format("SELECT numberOfUnits FROM Product WHERE storeID = '%s' AND productName = '%s'", storeID, productName);
         List<List<String>> productResults = esql.executeQueryAndReturnResult(productQuery);

         if (productResults.isEmpty()) {
         System.out.println("Product " + productName + " not found at Store " + storeID + '.');
         return;
         }

         int availableUnits = Integer.parseInt(productResults.get(0).get(0));

         if (availableUnits == 0) {
         System.out.println("Product " + productName + " out of stock at Store " + storeID + '.');
         return;
         }

         System.out.print("\n" + availableUnits + " units available. Enter amount of units to purchase: ");
         int unitsToPurchase = Integer.parseInt(in.readLine().trim());
  
         if (unitsToPurchase > availableUnits) {
         System.out.println("Not enough units available.");
         return;
         }

         Timestamp orderTime = new Timestamp(new java.util.Date().getTime());
         final String orderQuery = String.format(
         "INSERT INTO Orders (orderNumber, customerID, storeID, productName, unitsOrdered, orderTime) VALUES ('%s', '%s', '%s', '%s', '%s', '%s')",
         esql.getNextOrderNum(), userID, storeID, productName, unitsToPurchase, orderTime
         );
  
         esql.executeUpdate(orderQuery);
         System.out.println("Order placed!");
      } catch (Exception e) {
         System.err.println("An error occurred: " + e.getMessage());
      }
  }

   public static void viewRecentOrders(Amazon esql, String userID) {
      try {
         final String query = String.format("SELECT storeID, productName, unitsOrdered, orderTime FROM Orders WHERE customerID = '%s' ORDER BY orderTime DESC LIMIT 5", userID);
         List<List<String>> results = esql.executeQueryAndReturnResult(query);

         System.out.println("\nRecent Orders");
         System.out.println("---------------");
         for (List<String> record : results) {
            String storeID = record.get(0);
            String productName = record.get(1);
            int unitsOrdered = Integer.parseInt(record.get(2));
            String date = record.get(3);

            System.out.printf("Store ID: %s%nProduct Name: %s%nUnits Ordered: %d%nDate Ordered: %s%n", storeID, productName, unitsOrdered, date);
            System.out.println("---------------");
         }
      } catch (Exception e) {
         System.err.println("An error occurred while viewing recent orders: " + e.getMessage());
      }
  }

   public static void updateProduct(Amazon esql, String userID, String type) {
      if (!type.trim().equals("manager")) {
          System.out.println("Invalid permissions.\n");
          return;
      }
  
      try {
          final String query = String.format("SELECT storeID FROM Store WHERE managerID = '%s'", userID);
          List<List<String>> result = esql.executeQueryAndReturnResult(query);
  
          if (result.isEmpty()) {
              System.out.println("You don't manage any stores.");
              return;
          }
  
          System.out.println("Stores you manage: ");
          System.out.println("-------------------");
          for (List<String> record : result) {
              System.out.println("Store ID: " + record.get(0));
          }
  
          System.out.println("-------------------\n");
          System.out.print("Enter store ID to update products: ");
          String storeID = in.readLine().trim(); 
  
          final String listProductsQuery = String.format("SELECT productName FROM Product WHERE storeID = '%s'", storeID);
          List<List<String>> products = esql.executeQueryAndReturnResult(listProductsQuery);
  
          System.out.printf("Products available at Store %s:%n-------------------\n", storeID);
          for (List<String> product : products) {
              System.out.printf("%s\n-------------------\n", product.get(0));
          }
  
          System.out.print("Enter product name to update: ");
          String updateName = in.readLine().trim();
  
          System.out.print("Enter updated number of units: ");
          String updateNum = in.readLine().trim(); 
  
          System.out.print("Enter updated price per unit: ");
          String updatePrice = in.readLine().trim();
  
          final String updateQuery = String.format("UPDATE Product SET numberOfUnits = '%s', pricePerUnit = '%s' WHERE storeID = '%s' AND productName = '%s'", updateNum, updatePrice, storeID, updateName);
          esql.executeUpdate(updateQuery);
  
          Timestamp updateTime = new Timestamp(new java.util.Date().getTime());
          final String productUpdateQuery = String.format("INSERT INTO ProductUpdates (managerID, storeID, productName, updatedOn) VALUES ('%s', '%s', '%s', '%s')", userID, storeID, updateName, updateTime);
          esql.executeUpdate(productUpdateQuery);
  
          System.out.printf("Successfully updated %s in Store %s%n---------\n", updateName, storeID);
      } catch (Exception e) {
         System.err.println("An error occurred while updating product: " + e.getMessage());
      }
  }  

   public static void viewRecentUpdates(Amazon esql, String userID, String type) {
      if(!type.trim().equals("manager")){
         System.err.println("Invalid permissions.\n");
         return;
      }
      
      try{
         String query = String.format("SELECT storeID from Store WHERE managerID = '%s'", userID);
         List<List<String>> stores = esql.executeQueryAndReturnResult(query);
         List<List<String>> recentUpdates = new ArrayList<>();

         for(List<String> store: stores){
            int storeID = Integer.parseInt(store.get(0));

            String getUpdatesQuery = String.format("SELECT productName, updatedOn FROM ProductUpdates WHERE storeID = '%s' ORDER BY updatedOn", storeID);
            List<List<String>> updates = esql.executeQueryAndReturnResult(getUpdatesQuery);

            for(List<String> update: updates){
               update.add(0, String.valueOf(storeID));
            }

            recentUpdates.addAll(updates);
         }

         recentUpdates.sort((update1, update2) -> {
            Timestamp first = Timestamp.valueOf(update1.get(2));
            Timestamp second = Timestamp.valueOf(update2.get(2));
            return second.compareTo(first);
         });

         System.out.println("\nRecent Updates: ");
         System.out.println("------------------");
         for(int i=0; i<5; i++){
            System.out.println("Store ID: " + recentUpdates.get(0));
            System.out.println("Product name: " + recentUpdates.get(1));
            System.out.println("Updated On: " + recentUpdates.get(2));
            System.out.println("------------------");
         }
         System.out.println();
      }  catch(Exception e){
         System.err.println("An error occurred while viewing recent updates: " + e.getMessage());
      }
   }

   public static void viewPopularProducts(Amazon esql, String userID, String type) {
      if(!type.trim().equals("manager")){
         System.err.println("Invalid permissions.\n");
         return;
      }

      try {
         String query = String.format("SELECT storeID from Store WHERE managerID = '%s'", userID);
         List<List<String>> result = esql.executeQueryAndReturnResult(query);

         if (result.size() == 0) {
             System.out.println("You don't manage any stores.");
             return;
         }

         System.out.println("Stores you manage: ");
         System.out.println("-------------------");  
         for (List<String> record : result) {
             System.out.println("Store ID: " + record.get(0));
         }

         System.out.println("-------------------\n");  
         
         System.out.print("Enter store ID to view popular products: ");
         String storeID = in.readLine();

         String query2 = String.format("SELECT productName, SUM(unitsOrdered) AS totalOrdered FROM Orders WHERE storeID = '%s' GROUP BY productName ORDER BY totalOrdered DESC LIMIT 5", storeID);
         List<List<String>> result2 = esql.executeQueryAndReturnResult(query2);

         System.out.println("\nMost popular products at Store " + storeID);
         System.out.println("-------------------------------------------");
         for (List<String> record : result2) {
             System.out.println("Product name: " + record.get(0));
             System.out.println("Total ordered: " + record.get(1));

             System.out.println("-------------------------------------------");   
         }

         System.out.println();

     } catch (Exception e) {
         System.err.println("An error occurred while viewing popular products: " + e.getMessage());
     }
   }

   public static void viewPopularCustomers(Amazon esql, String userID, String type) {
      if(!type.trim().equals("manager")){
         System.err.println("Invalid permissions.\n");
         return;
      }

      try {
         String query = String.format("SELECT storeID from Store WHERE managerID = '%s'", userID);
         List<List<String>> result = esql.executeQueryAndReturnResult(query);

         if (result.size() == 0) {
             System.out.println("You do not manage any stores.");
             return;
         }

         System.out.println("Here are the stores you manage: ");
         System.out.println("--------------------------------");  
         for (List<String> record : result) {
             System.out.println("Store ID: " + record.get(0));
         }

         System.out.println("--------------------------------\n");  
         
         System.out.print("Enter store ID to view popular customers: ");
         String storeID = in.readLine();

         String query2 = String.format("SELECT u.userID, u.name, COUNT(o.orderNumber) AS orderCount FROM Users u JOIN Orders o ON u.userID = o.customerID WHERE o.storeID = '%s' GROUP BY u.userID, u.name ORDER BY orderCount DESC LIMIT 5", storeID);
         List<List<String>> result2 = esql.executeQueryAndReturnResult(query2);

         System.out.println("\nMost popular customers at Store " + storeID);
         System.out.println("--------------------------------------------");
         for (List<String> record : result2) {
             System.out.println("Customer ID: " + record.get(0));
             System.out.println("Name: " + record.get(1));
             System.out.println("Order count: " + record.get(2));
             System.out.println("--------------------------------------------");    
         }

         System.out.println();

     } catch (Exception e) {
         System.err.println("An error occurred while viewing popular customers: " + e.getMessage());
     }
   }

   public static void placeProductSupplyRequests(Amazon esql, String userID, String type) {
      if(!type.trim().equals("manager")){
         System.err.println("Invalid permissions.\n");
         return;
      }

      try {
         System.out.println("\nPlace Product Supply Request: ");
         System.out.print("Enter store ID: ");
         int storeID = Integer.parseInt(in.readLine());

         String listProductsQuery = String.format("SELECT productname FROM Product WHERE storeID = '%s'", storeID);
         List<List<String>> products = esql.executeQueryAndReturnResult(listProductsQuery);

         System.out.println("Products availabe at Store " + storeID + ":");
         System.out.println("-------------------------------------------");
         for (List<String> product : products) {
             System.out.println(product.get(0));
             System.out.println("-------------------------------------------");
         }

         System.out.print("Enter desired Product to supply: ");
         String product = in.readLine();
         System.out.print("Enter Number of Units needed: ");
         String numUnits = in.readLine();
         System.out.print("Enter Warehouse ID: ");
         String warehouseID = in.readLine();

         String  supplyQuery = String.format("INSERT INTO ProductSupplyRequests (managerID, warehouseID, storeID, productName, unitsRequested) VALUES ('%s', '%s', '%s', '%s', '%s')", userID, warehouseID, storeID, product, numUnits);
         esql.executeUpdate(supplyQuery);

         String updateProductQuery = String.format("UPDATE Product SET numberOfUnits = numberOfUnits + %s WHERE storeID = %s AND productName = '%s'", numUnits, storeID, product);
         esql.executeUpdate(updateProductQuery);

         System.out.println("Product Supply Request for " + product + " has been placed sucessfully.");
         System.out.println();

     } catch (Exception e) {
         System.err.println("An error occurred while placing product supply request: " + e.getMessage());
     }
   }

}//end Amazon
