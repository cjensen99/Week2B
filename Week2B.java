package week2B;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Week2B {
	public static void main(String[] args) {
        String connectionString = "jdbc:mysql://127.0.0.1:3306/practice";
        String dbLogin = "javauser1";
        String dbPassword = "D62azbjw!";
        Connection conn = null;
        
        Scanner scnr = new Scanner(System.in);
        int input = 0;
        boolean isDecember = true;
        
        do {
        	System.out.println("Please select 1 to view the report from November or 2 to view the report from December: ");
        	input = scnr.nextInt();
        	if(input == 1) isDecember = false;
        } while(input != 1 && input != 2);
        String sql;
        
        if(!isDecember) sql = "SELECT month, day, year, hi, lo FROM temperatures WHERE month = 11 AND year = 2020 ORDER BY month, day, year";
        else sql = "SELECT month, day, year, hi, lo FROM temperatures WHERE month = 12 AND year = 2020 ORDER BY month, day, year";   
        
        try 
        {
            conn = DriverManager.getConnection(connectionString, dbLogin, dbPassword);
            if (conn != null) 
            {
                System.out.println("Database connection successful.");
                try (Statement stmt = conn.createStatement(
                        ResultSet.TYPE_SCROLL_INSENSITIVE, 
                        ResultSet.CONCUR_UPDATABLE);
                    ResultSet rs = stmt.executeQuery(sql)) 
               {
                   int numRows;
                   int numCols = 5; // Number of attributes in SELECT statement
                   rs.last();
                   numRows = rs.getRow();
                   rs.first();
                   int[][] temperatures = new int[numRows][numCols];
                   for(int i = 0; i < numRows; i++) {
                	   temperatures[i][0] = Integer.parseInt(rs.getString("month"));
                	   temperatures[i][1] = Integer.parseInt(rs.getString("day"));
                	   temperatures[i][2] = Integer.parseInt(rs.getString("year"));
                	   temperatures[i][3] = Integer.parseInt(rs.getString("hi"));
                	   temperatures[i][4] = Integer.parseInt(rs.getString("lo"));
                	   rs.next();
                   }
                   
                   System.out.println(returnReportHead(isDecember));
	           	   BufferedWriter bw = new BufferedWriter(new FileWriter(new File("src/week2B/TemperaturesReportFromDB.txt")));
	           	   bw.write(returnReportHead(isDecember) + "\n");
                   int highestDay = temperatures[0][0];
       		       int lowestDay = temperatures[0][0];
       		   	   int averageHi = 0;
       		       int averageLow = 0;
       		       String dayAndYear;
       		       int counter = 0;
       		       
       		       //if selected month is December, has 31 days vs November has 30
       		       if(isDecember) counter = 31;
       		       else counter = 30;
       		       
	           	   for(int i = 0; i < counter; i++) {
	           		   int variation = temperatures[i][3] - temperatures[i][4];
	           		   if(temperatures[i][3] > temperatures[highestDay - 1][3]) highestDay = temperatures[i][1];
	           		   if(temperatures[i][4] < temperatures[lowestDay - 1][4]) lowestDay = temperatures[i][1];
	           		   averageHi += temperatures[i][3];
	           		   averageLow += temperatures[i][4];
	           		   
	           		   dayAndYear = Integer.toString(temperatures[i][1]) + "/" + Integer.toString(temperatures[i][2]);
	           		   System.out.printf("%d/%-9s %-4d %-4d %d%n", temperatures[i][0], dayAndYear,
	           				   temperatures[i][3], temperatures[i][4], variation);
	           		   
	           		   bw.append(String.format("%d/%-9s %-4d %-4d %d%n", temperatures[i][0], dayAndYear,
	           				   temperatures[i][3], temperatures[i][4], variation));
	           	   }
	           	   System.out.println(returnMonthlyStats(temperatures, highestDay, lowestDay, averageHi, averageLow, isDecember));
	           	   bw.append(returnMonthlyStats(temperatures, highestDay, lowestDay, averageHi, averageLow, isDecember));
	           	   System.out.println(returnGraphHead());
	           	   System.out.println(returnGraph(temperatures, isDecember));
	           	   System.out.println(returnGraphBottom());
	           	   bw.append("\n" + returnGraphHead());
	           	   bw.append("\n" + returnGraph(temperatures, isDecember));
	           	   bw.append("\n" + returnGraphBottom());
	           	   
	           	   bw.close();
                   
               } 
               catch (SQLException ex) 
               {
                   System.out.println(ex.getMessage());
               }
            }
        }
        catch (Exception e) 
        {
            System.out.println("Database connection failed.");
            e.printStackTrace();
        }
        
    }
	
	private static String returnReportHead(boolean isDecember) {
		String month;
		
		if(isDecember) month = "December";
		else month = "November";
		
		String head = "--------------------------------------------------------------\n"
				+ month + " 2020: Temperatures in Utah\n"
				+ "--------------------------------------------------------------\n"
				+ "Date         High Low  Variance\n"
				+ "--------------------------------------------------------------";
		return head;
	}
	
	private static String returnMonthlyStats(int[][] temperatures, int highestDay, int lowestDay, 
			int averageHi, int averageLow, boolean isDecember) {
		double averageH = (double) averageHi / 31.0;
		double averageL = (double) averageLow / 31.0;
		String month;
		int monthNumber;
		if(isDecember) {
			monthNumber = 12;
			month = "December";
		}
		else {
			monthNumber = 11;
			month = "November";
		}
		String head = "--------------------------------------------------------------\n"
				+ month + " Highest Temperature: " + monthNumber + "/" + highestDay + ": " + temperatures[highestDay - 1][3]
				+ " Average Hi: " + String.format("%.1f", averageH) + "\n"
				+ month + " Lowest Temperature:  " + monthNumber + "/" + lowestDay + ": " + temperatures[lowestDay - 1][4]
				+ " Average Lo: " + String.format("%.1f", averageL);
		return head;
	}
	
	private static String returnGraphHead() {
		String head = "--------------------------------------------------------------\n"
				+ "Graph\n"
				+ "--------------------------------------------------------------\n"
				+ "      1   5    10   15   20   25   30   35   40   45   50\n"
				+ "      |   |    |    |    |    |    |    |    |    |    |\n"
				+ "--------------------------------------------------------------";
		return head;
	}
	
	private static String returnGraphBottom() {
		String head = "--------------------------------------------------------------\n"
				+ "      1   5    10   15   20   25   30   35   40   45   50\n"
				+ "      |   |    |    |    |    |    |    |    |    |    |\n"
				+ "--------------------------------------------------------------";
		return head;
	}
	
	private static String returnGraph(int[][] temperatures, boolean isDecember) {
		String head = "";
		int counter;
		
		if(!isDecember) counter = 30;
		else counter = 31;
		
		for(int i = 0; i < counter; i++) {
			head += String.format("%-2d %-2s ", i + 1, "Hi");
			for(int j = 0; j < temperatures[i][3]; j++) {
				head += "+";
			}
			head += "\n   Lo ";
			for(int j = 0; j < temperatures[i][4]; j++) {
				head += "-";
			}
			if(isDecember) {
				if(i < 30) head += "\n";
			}
			else {
				if(i < 29) head += "\n";
			}
		}
		return head;
	}

}


