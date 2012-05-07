package com.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.QueryEvaluation.IndexbasedEvaluation;
import com.QueryEvaluation.KeywordQuery;
import com.QueryEvaluation.SLCAEvaluation;
import com.QueryEvaluation.StackbasedEvaluation;
import com.db.DBHelper;
import com.tools.Helper;
import com.tools.PropertyReader;
import com.tools.TimeRecorder;

public class GenerateDataFromDB {


	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			
			String ksFile = PropertyReader.getProperty("ksFile");

			BufferedReader queryRead = new BufferedReader(
					new InputStreamReader(new DataInputStream(
							new FileInputStream(ksFile))));
			
			String query = "";
			
			List<String> keywords = new LinkedList<String>();
			
			while ((query = queryRead.readLine()) != null) {
				List<String> refinedkeywords = Helper.getRefinedKeywords(query);
				for(String s:refinedkeywords)
				{
					if(!keywords.contains(s))
					{
						keywords.add(s);
					}
				}
			}
			
			queryRead.close();
			
			
			for(String keyword:keywords)
			{
				String databaseName = PropertyReader.getProperty("dbname");
				DBHelper.ConnectToDB(databaseName);
				
				String fileName="./keyword/"+keyword+".log";
				
				File f = new File(fileName);
				
				if(!f.exists())
				{
					PrintWriter outStream = new PrintWriter(new BufferedWriter(
							new FileWriter(new File(fileName))));

					
					String deweysql =  "select dewey from KeywordDewey where keyword=" + "'" + keyword+ "' order by XMLid ASC";
					
					ResultSet deweySet = DBHelper.performQuery(deweysql);
					
					try {
						while (deweySet.next()) {
							String dewey = deweySet.getString("dewey");
							dewey = dewey.trim();
							outStream.println(dewey);
						}
						
					} catch (SQLException e) {
						e.printStackTrace();
					}
					outStream.close();
				}
				
				
				

				System.out.println("finished");
				
			}
			
	}
	catch(Exception e)
	{
	
	}
		
	}

}
