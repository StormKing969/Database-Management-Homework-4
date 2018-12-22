// As discussed in class: This program is supposed to find the duration
// of flights, where the departure and arrival times are of type timestamp
// in the database.
// If you compute the timestamp difference in the database, the value
// is of type interval, not timestamp, and the interval datatype is
// not mapped to JDBC.  It can be accessed by getString as a text string.
// But to stay with numbers, we need to read in the two timestamps into
// type java.sql.Timestamp, and then use its getTime() method to provide
// the number of milliseconds since 1970, and so on.

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

class FindFlights {
	public static void main(String args[]) {
		String dbSys = null;
		Scanner in = null;
		try {
			in = new Scanner(System.in);
			System.out
					.println("Please enter information to test connection to the database");
			dbSys = readEntry(in, "Using Oracle (o), MySql (m) or HSQLDB (h)? ");

		} catch (IOException e) {
			System.out.println("Problem with user input, please try again\n");
			System.exit(1);
		}
		// Prompt the user for connect information
		String user = null;
		String password = null;
		String connStr = null;
		String yesNo;
		try {
			if (dbSys.equals("o")) {
				user = readEntry(in, "user: ");
				password = readEntry(in, "password: ");
				yesNo = readEntry(in,
						"use canned Oracle connection string (y/n): ");
				if (yesNo.equals("y")) {
					String host = readEntry(in, "host: ");
					String port = readEntry(in, "port (often 1521): ");
					String sid = readEntry(in, "sid (site id): ");
					connStr = "jdbc:oracle:thin:@" + host + ":" + port + ":"
							+ sid;
				} else {
					connStr = readEntry(in, "connection string: ");
				}
			} else if (dbSys.equals("m")) {// MySQL--
				user = readEntry(in, "user: ");
				password = readEntry(in, "password: ");
				yesNo = readEntry(in,
						"use canned MySql connection string (y/n): ");
				if (yesNo.equals("y")) {
					String host = readEntry(in, "host: ");
					String port = readEntry(in, "port (often 3306): ");
					String db = user + "db";
					connStr = "jdbc:mysql://" + host + ":" + port + "/" + db;
				} else {
					connStr = readEntry(in, "connection string: ");
				}
			} else if (dbSys.equals("h")) { // HSQLDB (Hypersonic) db
				yesNo = readEntry(in,
						"use canned HSQLDB connection string (y/n): ");
				if (yesNo.equals("y")) {
					String db = readEntry(in, "db or <CR>: ");
					connStr = "jdbc:hsqldb:hsql://localhost/" + db;
				} else {
					connStr = readEntry(in, "connection string: ");
				}
				user = "sa";
				password = "";
			} else {
				user = readEntry(in, "user: ");
				password = readEntry(in, "password: ");
				connStr = readEntry(in, "connection string: ");
			}
		} catch (IOException e) {
			System.out.println("Problem with user input, please try again\n");
			System.exit(3);
		}
		System.out.println("using connection string: " + connStr);
		System.out.print("Connecting to the database...");
		System.out.flush();
		Connection conn = null;
		// Connect to the database
		// Use finally clause to close connection
		try {
			conn = DriverManager.getConnection(connStr, user, password);
			System.out.println("connected.");
			findFlights(conn);
		} catch (SQLException e) {
			System.out.println("Problem with JDBC Connection\n");
			printSQLException(e);
			System.exit(4);
		} finally {
			// Close the connection, if it was obtained, no matter what happens
			// above or within called methods
			if (conn != null) {
				try {
					conn.close(); // this also closes the Statement and
									// ResultSet, if any
				} catch (SQLException e) {
					System.out
							.println("Problem with closing JDBC Connection\n");
					printSQLException(e);
					System.exit(5);
				}
			}
		}
	}
	
	// try to create and use a "welcome" table, or throw if this fails
	static void findFlights(Connection conn) throws SQLException 
	{
		Scanner in = null;
		String origin = null;
		String destination = null;
		try {
			in = new Scanner(System.in);
			origin = readEntry(in, "origin city: ");
			destination = readEntry(in, "destination city: ");

		} catch (IOException e) {
			System.out.println("Problem with user input, please try again\n");
			System.exit(1);
		}
		// Create a statement		
		String prepared = "select f.*, f.arrives-f.departs as delta from flights f where f.origin = ? and f.destination = ?";
		PreparedStatement ps = conn.prepareStatement(prepared);
		ResultSet rset = null;
		try 
		{
			ps.setString(1,  origin);
			ps.setString(2,  destination);
			rset = ps.executeQuery();
			while(rset.next())
			{
				String fnum = rset.getString("FLNO");
				String distance = rset.getString("DISTANCE");
				Timestamp departs = rset.getTimestamp("DEPARTS");
				Timestamp arrives = rset.getTimestamp("ARRIVES");
				String delta = rset.getString("delta");
				long duration = (arrives.getTime() - departs.getTime());
				// Alternatively to the following, use division by various constants, etc.
				String durationS = String.format("%02d:%02d", 
					    TimeUnit.MILLISECONDS.toHours(duration),
					    TimeUnit.MILLISECONDS.toMinutes(duration) - 
					    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)));
				System.out.println("Flight: " + fnum + " Distance: " + distance + " Duration: " + durationS);
				System.out.println("delta = " + delta);
			}
			
		} finally {   // Note: try without catch: let the caller handle
					  // any exceptions of the "normal" db actions. 
			ps.close(); // clean up statement resources, incl. rset
		}
	}

	// print out all exceptions connected to e by nextException or getCause
	static void printSQLException(SQLException e) {
		// SQLExceptions can be delivered in lists (e.getNextException)
		// Each such exception can have a cause (e.getCause, from Throwable)
		while (e != null) {
			System.out.println("SQLException Message:" + e.getMessage());
			Throwable t = e.getCause();
			while (t != null) {
				System.out.println("SQLException Cause:" + t);
				t = t.getCause();
			}
			e = e.getNextException();
		}
	}

	// super-simple prompted input from user
	public static String readEntry(Scanner in, String prompt)
			throws IOException {
		System.out.print(prompt);
		return in.nextLine().trim();
	}
}
