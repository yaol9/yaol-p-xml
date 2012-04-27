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
import java.util.List;

import com.QueryEvaluation.IndexbasedEvaluation;
import com.QueryEvaluation.KeywordQuery;
import com.QueryEvaluation.SLCAEvaluation;
import com.QueryEvaluation.StackbasedEvaluation;
import com.db.DBHelper;
import com.tools.Helper;
import com.tools.PropertyReader;
import com.tools.TimeRecorder;

public class CompareIndexandStack {


	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			
			String databaseName = PropertyReader.getProperty("dbname");
			DBHelper.ConnectToDB(databaseName);
			
			
			PrintWriter outStream = new PrintWriter(new BufferedWriter(
					new FileWriter(new File(PropertyReader
							.getProperty("compareIndexStack")))));

			
					
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
					
					HashMap<String,Integer> keywordCount=new HashMap<String,Integer>();
					
					
					for(String s:refinedkeywords)
					{
						if(!keywordCount.containsKey(s))
						{
							String deweysql = "select sum(1) as count from KeywordDewey where keyword='"
									+ s+"'";
							ResultSet deweySet = DBHelper.performQuery(deweysql);
							
							try {
								deweySet.next();
								int count = deweySet.getInt("count");
								keywordCount.put(s, count);
														
							} catch (SQLException e) {
								
								e.printStackTrace();
							}

						}
						
					}
					
					
					KeywordQuery kquery = new KeywordQuery(refinedkeywords);
					kquery.LoadAllInformation();
					SLCAEvaluation myEstimation =new IndexbasedEvaluation(outStream,
							refinedkeywords, refinedkeywords.get(1));
					
					
					TimeRecorder.startRecord();
					
										
					myEstimation.computeSLCA(kquery);

					
					TimeRecorder.stopRecord();
					System.gc();
					long qtime_index = TimeRecorder.getTimeRecord();

			        kquery = new KeywordQuery(refinedkeywords);
					kquery.LoadAllInformation();
					myEstimation =new StackbasedEvaluation(outStream,refinedkeywords);
										
					TimeRecorder.startRecord();
															
					myEstimation.computeSLCA(kquery);
					
					TimeRecorder.stopRecord();
					System.gc();
					long qtime_stack = TimeRecorder.getTimeRecord();
					
					
					String combineS=refinedkeywords.get(0)+":"+keywordCount.get(refinedkeywords.get(0));
					
					for(int i =1;i< refinedkeywords.size();i++)
					{
						combineS+="|"+refinedkeywords.get(i)+":"+keywordCount.get(refinedkeywords.get(i));
					}
					
					outStream.println("index");
					System.out.println("index");
					outStream.println("query:"+combineS+" process time:"+qtime_index+" ms"+" result size:"+myEstimation.getResult().size());
					System.out.println("query:"+combineS+" process time:"+qtime_index+" ms"+" result size:"+myEstimation.getResult().size());
					
					outStream.println("stack");
					System.out.println("stack");
					outStream.println("query:"+combineS+" process time:"+qtime_stack+" ms"+" result size:"+myEstimation.getResult().size());
					System.out.println("query:"+combineS+" process time:"+qtime_stack+" ms"+" result size:"+myEstimation.getResult().size());
					
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
