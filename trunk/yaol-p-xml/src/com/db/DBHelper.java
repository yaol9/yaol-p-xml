

/* Note:
 * We use this program to load data set from files into MySQL DB.
 * 
 */

package com.db;

import java.sql.*;
import java.io.*;
import java.util.*;

import javax.swing.JOptionPane;

import com.tools.PropertyReader;
import com.xmlparser.*;

public class DBHelper {

	
//public static class processDB{
	private static final char PATH_SEPARATOR = '/';
	
	
	/* the print function d() is used for debug
	 * 
	 */
	public static void d(String str) {
		System.out.printf(".. %s\n", str);
	}
	
	private static Connection con = null;
	
	public static boolean createNewDB(String dbName){
		
		try{						
			
			String createDB = "CREATE DATABASE " + PropertyReader.getProperty("dbname");
			
			final String driverClass       = "com.mysql.jdbc.Driver";
	
			final String connectionURLThin = PropertyReader.getProperty("dbconnectionURLThin");
			final String userID            = PropertyReader.getProperty("dbuser");
			final String userPassword      = PropertyReader.getProperty("dbpassword");
			
			  Class.forName(driverClass).newInstance();
		      Connection conn = DriverManager.getConnection(connectionURLThin, userID, userPassword);
		     
		      if(conn != null){		       
			      Statement stmt = conn.createStatement();
			      stmt.executeUpdate(createDB);		     
			      conn.close();
			      
			      System.out.println("Successfully connected to MySQL server.");
		      }
		       
	    } catch(Exception e) {
	      System.err.println("Exception: " + e.getMessage());
	      return false;
	    }		    
		return true;
	}

	public static void ConnectToDB(String databaseName){

		 try {
			/* We first connect to the MySQL database by providing the following
			 * parameters. And then we create corresponding tables tokenTable and
			 * tokenNodeTable for token and token-node respectively. The query 
			 * statements have been stored in the folder queries, which are named 
			 * as tokenTable.sql and tokenNodeTable.sql
			 */
	
				
			final String driverClass       = "com.mysql.jdbc.Driver";
			final String connectionURLThin =  PropertyReader.getProperty("dbconnectionURLThin") + databaseName;
		
			final String userID            = PropertyReader.getProperty("dbuser");
			final String userPassword      = PropertyReader.getProperty("dbpassword");
			
			  Class.forName(driverClass).newInstance();
		      con = DriverManager.getConnection(connectionURLThin, userID, userPassword);
		     
		      if(con != null){
		        System.out.println("Successfully connected to MySQL server.");
		      }else{
		    	  System.out.println("getConnection errors."); 
		      }
		      
		    } catch(Exception e) {
		      System.err.println("Exception: " + e.getMessage());
		    }		    
		 
	}
	
	
	public static boolean loadDataSet(String readFile){
		Statement stmt = null;
		String strLine;
		try{
			
			FileInputStream fstream = new FileInputStream(readFile);
			DataInputStream elemIn = new DataInputStream(fstream);
			BufferedReader elemBr = new BufferedReader(new InputStreamReader(elemIn));

			stmt = con.createStatement();
			
			
			//Read File line by line
			while ((strLine = elemBr.readLine()) != null){
				
				//test
				String[] temp = strLine.split("[ ]");
				if(temp[4].length()<=23){
				
				
				// Print the content on the console
				strLine = strLine.replace(';' , ' '); //Remove the ; since jdbc complains
				d(strLine);				
				stmt.executeUpdate(strLine);
				
				}
			}
			
			elemBr.close();
			
			return true;
		}catch(Exception e){
			System.out.println(e.getMessage());
			return false;
		}
		
	}
	
	/* now we read the file *.txt line by line and construct 
	 * SQL query statement 
	 */
	public static boolean performUpdate(String sqlScriptFile){
		Statement stmt = null;
		String thisLine, sqlQuery;
		try{
			sqlQuery = "";
			File curFile = new File(sqlScriptFile);
			
			FileInputStream fstream = new FileInputStream(curFile);
			DataInputStream sqlIn = new DataInputStream(fstream);
			BufferedReader sqlBr = new BufferedReader(new InputStreamReader(sqlIn));
			
			stmt = con.createStatement();
			
			while ((thisLine = sqlBr.readLine()) != null){
				if(thisLine.length() > 0 && thisLine.charAt(0) == '-' || thisLine.length() == 0 ) 
		            continue;
				if(sqlQuery == ""){
					sqlQuery = thisLine;
				}else{
					sqlQuery = sqlQuery + " " + thisLine;
				}
		        System.out.println(sqlQuery.length());
		        //If one command complete
		        if(sqlQuery.charAt(sqlQuery.length() - 1) == ';') {
		            sqlQuery = sqlQuery.replace(';' , ' '); //Remove the ; since jdbc complains
		            try {		            			            	
		            	d(sqlQuery);
		            	
		            	stmt.executeUpdate(sqlQuery);
		            	sqlQuery = "";
		            }
		            catch(SQLException ex) {
		                JOptionPane.showMessageDialog(null, 
		                		"Error Creating the SQL Database : " + ex.getMessage());
		            }
		            catch(Exception ex) {
		                JOptionPane.showMessageDialog(null, 
		                		"Error Creating the SQL Database : " + ex.getMessage());
		            }
		           // sqlQuery = "";
		        }   
			}
			sqlBr.close();
			return true;
		}catch(Exception e){
			return false;
		}
		
	}
	
	public static ResultSet performQuery(String sql){
		try{
			Statement stmt = null;
			stmt = con.createStatement();
		
			ResultSet rs = null;
			rs = stmt.executeQuery(sql);
		
			return rs;
			
		}catch(Exception e){
			System.out.println(e.getMessage());
			return null;
		}
	}
	
	public static void DisconnectDB(){
		try {
	        if(con != null){
	        	con.close();
	        	System.out.println("Current DB has been disconnected!");
	        }	         
	    }catch(SQLException e) {
	      System.err.println("Exception: " + e.getMessage());
	    }
	}
	
	
	/* check if the data file can be read or not
	 * 
	 */
	public boolean checkFile(String curPath, String fileName){
		
		boolean foundFlag = false;
		try{
			File curFile = new File(curPath);
			if (curFile.isDirectory()){
				String[] fileList = curFile.list();
				File temp = null;
				for (int i=0; i<fileList.length; i++){
					temp = new File(curPath+PATH_SEPARATOR+fileList[i]);
					if(temp.isFile()){
						if(temp.getName().equalsIgnoreCase(fileName)){
							foundFlag = true;
							break;
						}
					}
				}
				d("The specified " + fileName + " is not correct!");
				
			}else{
				d("The specified directory " + curPath + " is not correct!");				
			}
		}catch(Exception e){
			System.out.println("JdbcExample.checkFile() failed! " 
					+ e.getMessage());
		}
		return foundFlag;
	}
	
	
//}
	
	public static void StartJdbcImplement(String databasename){
		
		try {
			
			//		processDB myConnection = new processDB();
			
					createNewDB(databasename);
					ConnectToDB(databasename);
					
					/* create the corresponding tables: tokens and postings
					 * ? need to be extended to detect the existence of the tables, if 
					 * they do not exist, we then create them.
					 */
					
					// If the tables do not exist, please set flagTable as false, other than it should
					// be set as true.
					 
					boolean flagTable = false;
					if(!flagTable){
						
						//delete the existing tables
						
						
						if (performUpdate("./config/createTables.txt")){
							System.out.println("Tables have been created successfully.");
						} else{
							System.out.println("Tables are not created correctly.");
						}
									
					
						String TokenLevEnID = PropertyReader.getProperty("TokenLevEnID");
						
					
						if (loadDataSet(TokenLevEnID)){
							System.out.println(".. The data "+ TokenLevEnID + " have been loaded correctly.");
						}		
						
						String EncodeID = PropertyReader.getProperty("EncodeID");
												
						if (loadDataSet(EncodeID)){
							System.out.println(".. The data "+ EncodeID + " have been loaded correctly.");
						}	
						
					}
								
					DisconnectDB();			
				}catch(Exception e){// catch exception when processing file
					System.out.println("Error: " + e.getMessage());			
				}
	}

	
	public static void main(String args[]){
		try {
			Properties prop = new Properties();
			FileInputStream propFile = new FileInputStream(
					"./config/project.properties");
			prop.load(propFile);
			propFile.close();

			String dbname = prop.getProperty("dbname");
			mySAXParser.StartSaxProgram();
			DeweyToSQL.StartDeweyToSQL();

			StartJdbcImplement(dbname);

		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}
}	


