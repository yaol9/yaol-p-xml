package com.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.db.DBHelper;
import com.tools.Helper;
import com.tools.PropertyReader;
import com.tools.TimeRecorder;

public class TestShareEager implements TestCase {

	private HashMap<Integer, List<String>> userQuery;
	
	private int queryCount=0;
	
	private HashMap<String,Integer> resultSize;
	
	private HashMap<Integer,List<HashMap<Integer,List<String>>>> planSet;
	
	TestShareEager()
	{
		userQuery = new HashMap<Integer, List<String>>();
		resultSize = new HashMap<String,Integer>();		
		planSet=new HashMap<Integer,List<HashMap<Integer,List<String>>>>();
	}
	
	@Override
	public long run() {
		// TODO Auto-generated method stub	
		try {
			
			String databaseName = PropertyReader.getProperty("dbname");
			DBHelper.ConnectToDB(databaseName);
			
			
			PrintWriter outStream = new PrintWriter(new BufferedWriter(
					new FileWriter(new File(PropertyReader
							.getProperty("ShareEagerAlgorithmResult")))));

			//warm up
			runSingle(outStream);
			
			TimeRecorder.startRecord();
			// run 5 times
			for (int i = 0; i < 1; i++) {
				runSingle(outStream);
			}

			TimeRecorder.stopRecord();
			System.gc();
			long qtime = TimeRecorder.getTimeRecord();

			
			
			// get memory usage
			long usagememory = Helper.getMemoryUsage();

			outStream.println("ShareEager Algorithms:");
			System.out.println("ShareEager Algorithms:");
			outStream.printf("--" + "Response Time: %d \n", qtime);
			outStream.println();
			System.out.printf("--" + "Response Time: %d \n", qtime);
			outStream.printf("--" + "Memory usage: %d \n", usagememory);
			outStream.println();
			System.out.printf("--" + "Memory usage: %d \n", usagememory);

			
			DBHelper.DisconnectDB();

			outStream.close();
			System.out.println("====================>>> Stop application!");
			
			return qtime;

		} catch (IOException e) {
			e.printStackTrace();
		}
			return 0;
	}

	@Override
	public void runSingle(PrintWriter outStream) {
		
		//load query to userQuery
		loadQuery(outStream);
		
		//load size log to resultSize
		loadSizeLog();
		
		//generate plan for individual query
		for(int i=0;i<queryCount;i++)
		{
			
			
			//(keyword.size - 1) calculation 
			
			
			HashMap<Integer,List<String>> plan = new HashMap<Integer,List<String>>();
					
			
			generatePlan(userQuery.get(i),0,userQuery.get(i).size(),plan,0,i);
			
		}

	}

	private void generatePlan(List<String> query,int sequenceId,int keywordsCount,HashMap<Integer,List<String>> plan,int planScore,int queryId)
	{
		if(sequenceId==keywordsCount-1)
		{
			//output plan
			if(planSet.containsKey(queryId))
			{
				//need sort
				List<HashMap<Integer,List<String>>> plans=planSet.get(queryId);
				plans.add(plan);
				
				System.out.println("for query "+queryId+", Plan:");
				
				for(int i=0;i<sequenceId;i++)
				{
					System.out.println("Step "+i+": "+plan.get(i));					
				}
				
			}
			else
			{
				List<HashMap<Integer,List<String>>> plans=new LinkedList<HashMap<Integer,List<String>>>();
				plans.add(plan);
				planSet.put(queryId, plans);
				
				System.out.println("for query "+queryId+", Plan:");
				
				for(int i=0;i<sequenceId;i++)
				{
					System.out.println("Step "+i+": "+plan.get(i));					
				}
			}
		}
		else
		{
			
			
			for(String s1:query)
			{
				for(String s2:query)
				{
					if(!s1.equalsIgnoreCase(s2))
					{
						
						List<String> itemsForCal = new LinkedList<String>();
						itemsForCal.add(s1);
						itemsForCal.add(s2);
						
						
						List<String>mixList = new LinkedList<String>();
						if(s1.contains("|"))
						{
							String[] s=s1.split("[|]");
							for(String ss:s)
							{
								mixList.add(ss);
							}
						}
						else
						{
							mixList.add(s1);
						}
						
						if(s2.contains("|"))
						{
							String[] s=s2.split("[|]");
							for(String ss:s)
							{
								mixList.add(ss);
							}
						}
						else
						{
							mixList.add(s2);
						}
						String mix = Helper.getMixString(mixList);
			
						List<String> copyQuery = new LinkedList<String>();
						for(String s:query)
						{
							copyQuery.add(s);
						}
						copyQuery.add(mix);
						copyQuery.remove(s1);
						copyQuery.remove(s2);
						
						plan.put(sequenceId, itemsForCal);
						generatePlan(copyQuery, sequenceId+1,keywordsCount, plan,planScore,queryId);
						
					}
				}
			}
			
		}
	
	}

	private void loadSizeLog() {
		try{
		Helper.loadKeywordCount(resultSize);
		
		//load log
		String resultLog = PropertyReader.getProperty("resultLog");

		BufferedReader resultLogRead = new BufferedReader(
				new InputStreamReader(new DataInputStream(
						new FileInputStream(resultLog))));

		String resultLogItem = "";
		
		while ((resultLogItem = resultLogRead.readLine()) != null) {

			String keywordSet[] = resultLogItem.split("[,]");

			resultSize.put(keywordSet[0], Integer.valueOf(keywordSet[1]));
			
			
		}
		

	}
	catch (IOException e) {
		e.printStackTrace();
	}
	}

	private void loadQuery(PrintWriter outStream) {
		try {
			
		
		String ksFile = PropertyReader.getProperty("ksFile");

		BufferedReader queryRead = new BufferedReader(
				new InputStreamReader(new DataInputStream(
						new FileInputStream(ksFile))));

		String query = "";
	
		int counter = 0;

		while ((query = queryRead.readLine()) != null) {
			outStream.printf("-- " + "Keyword Query: %s \n", query);
			outStream.println();
			System.out.println("-- " + "Keyword Query: "+query);
						
			if(!query.startsWith("#"))
			{
				List<String> refinedkeywords = Helper.getRefinedKeywords(query);
				userQuery.put(counter, refinedkeywords);
				counter++;
			}		
			
		}
		
		queryCount=counter;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TestCase test = new TestShareEager();
		test.run();
	}

}
