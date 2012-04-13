package com.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

import com.QueryEvaluation.IndexbasedEvaluation;
import com.QueryEvaluation.KeywordQuery;
import com.QueryEvaluation.SLCAEvaluation;
import com.db.DBHelper;
import com.tools.Helper;
import com.tools.PropertyReader;

public class GenerateLog {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			
			String databaseName = PropertyReader.getProperty("dbname");
			DBHelper.ConnectToDB(databaseName);
			
			
			PrintWriter outStream = new PrintWriter(new BufferedWriter(
					new FileWriter(new File(PropertyReader
							.getProperty("resultLog")))));

			
					
			String ksFile = PropertyReader.getProperty("ksFile");

			BufferedReader queryRead = new BufferedReader(
					new InputStreamReader(new DataInputStream(
							new FileInputStream(ksFile))));

			String query = "";
			 // user
					
			while ((query = queryRead.readLine()) != null) {
				
				if(!query.startsWith("#"))
				{
					List<String> refinedkeywords = Helper.getRefinedKeywords(query);
					
					KeywordQuery kquery = new KeywordQuery(refinedkeywords);
					kquery.LoadAllInformation();
					SLCAEvaluation myEstimation =new IndexbasedEvaluation(outStream,
							refinedkeywords, refinedkeywords.get(1));
					myEstimation.computeSLCA(kquery);
					String combineS=refinedkeywords.get(0);
					
					for(int i =1;i< refinedkeywords.size();i++)
					{
						combineS+="|"+refinedkeywords.get(i);
					}
					outStream.println(combineS+","+myEstimation.getResult().size());
				}
			}
			
			queryRead.close();
			outStream.close();

			System.out.println("finished");
	}
	catch(Exception e)
	{
	
	}
		
	}

}
