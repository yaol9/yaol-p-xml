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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

public class TestQueryAware implements TestCase {

	private int curUserQuery ; 
	private HashMap<String,Integer> steinerPoints ;
	private HashMap<String, List<String>> shareFactor;
	TestQueryAware()
	{
		steinerPoints = new HashMap<String,Integer>();
		shareFactor = new HashMap<String, List<String>>();
	}
	@Override
	public long run() {
			try {
			
			String databaseName = PropertyReader.getProperty("dbname");
			DBHelper.ConnectToDB(databaseName);
			
			
			PrintWriter outStream = new PrintWriter(new BufferedWriter(
					new FileWriter(new File(PropertyReader
							.getProperty("QueryAwareAlgorithmResult")))));

			//warm up
			runSingle(outStream);
			
			TimeRecorder.startRecord();
			// run 5 times
			for (int i = 0; i < 5; i++) {
				runSingle(outStream);
			}

			TimeRecorder.stopRecord();
			System.gc();
			long qtime = TimeRecorder.getTimeRecord();
			// get memory usage
			long usagememory = Helper.getMemoryUsage();

			outStream.println("QueryAware Algorithms:");
			System.out.println("QueryAware Algorithms:");
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
		// TODO Auto-generated method stub
		try {
			

			String ksFile = PropertyReader.getProperty("ksFile");

			BufferedReader queryRead = new BufferedReader(
					new InputStreamReader(new DataInputStream(
							new FileInputStream(ksFile))));

			String query = "";

			HashMap<Integer, List<String>> userQuery = new HashMap<Integer, List<String>>(); // user
																								// query

			//get keyword count
			HashMap<String,Integer> keywordCount=new HashMap<String,Integer>();
			
			int counter = 0;
			int maxSize =Integer.MIN_VALUE; //max query size

			while ((query = queryRead.readLine()) != null) {
			//	outStream.printf("-- " + "Keyword Query: %s \n", query);
			//	outStream.println();
			//	System.out.printf("-- " + "Keyword Query: %s \n", query);

				List<String> refinedkeywords = Helper.getRefinedKeywords(query);
				if(refinedkeywords.size()>maxSize)
				{
					maxSize=refinedkeywords.size();
				}

				if(!query.startsWith("#"))
				{
					userQuery.put(counter, refinedkeywords);
					counter++;
					
					/*
					for(String s:refinedkeywords)
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
					*/
				}
				
				
			}
			
			
			
			
			HashMap<String, List<String>> scheduler=new HashMap<String, List<String>> ();
		
			HashMap<Integer, List<String>> lattice = generateLattice(userQuery, counter,scheduler);
			
			
			//Helper.printHashMap(scheduler);		
			//Helper.printList(steinerPoints);
				
			
			//bottom-up calculate sharing factor
			for(int level=2;level<=maxSize;level++)
			{
				//calculate each level node
				for(String k:steinerPoints.keySet())
				{
					if(steinerPoints.get(k)==level)
					{
						//calculate this node
						List<String> kList=new LinkedList<String>(Arrays.asList(k.split("[|]")));
						
												
						List<String> result=answerQuery(keywordCount,kList,outStream);
											
						shareFactor.put(k,result );
						keywordCount.put(k, result.size());
						
					}
				}
			}
			
		//	Helper.printHashMap(tempResult);
		//	Helper.printHashMap(keywordCount);
			
		
			for(int queryNum : lattice.keySet())
			{
				
				outStream.printf("-- " + "Keyword Query:\n", userQuery.get(queryNum));
				outStream.println();
			//	System.out.printf("-- " + "Keyword Query: %s \n",  userQuery.get(queryNum));
				
				
			
				List<String> result=answerQuery(keywordCount,lattice.get(queryNum),outStream);
							

				// from _resultheap and _resultmonitor
				outStream.println("SLCA results as follow. ");
			//	System.out.println("SLCA results as follow");

			    outStream.println("SLCA result: " + result);
		//		System.out.println("SLCA result: " + result);
			

				outStream.println();
				outStream.println();
				outStream.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			    
			}
			
			//clear mem
			shareFactor.clear();
			lattice.clear();
			
		
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private HashMap<Integer, List<String>> generateLattice(HashMap<Integer, List<String>> userQuery,
			int counter,HashMap<String, List<String>> scheduler) {
		HashMap<Integer, List<String>> lattice=new HashMap<Integer, List<String>> ();
		for (int i = 0; i < counter; i++) {
			for (int j = counter - 1; j > i; j--) {
				// System.out.println("I:" + userQuery.get(i).toString());
				// System.out.println("J:" + userQuery.get(j).toString());
				String tempJoint = Helper.getMaxJointString(userQuery.get(i),
						userQuery.get(j));

				if (!tempJoint.isEmpty()) {

					if (lattice.containsKey(i)) {
						List<String> tempJointList = lattice.get(i);
						if(!tempJointList.contains(tempJoint))
						{
							tempJointList.add(tempJoint);
						}
						
						lattice.put(i, tempJointList);
					} else {
						List<String> tempJointList = new LinkedList<String>();
						if(!tempJointList.contains(tempJoint))
						{
							tempJointList.add(tempJoint);
						}
						lattice.put(i, tempJointList);
					}

					if (lattice.containsKey(j)) {
						List<String> tempJointList = lattice.get(j);
						if(!tempJointList.contains(tempJoint))
						{
							tempJointList.add(tempJoint);
						}
						lattice.put(j, tempJointList);
					} else {
						List<String> tempJointList = new LinkedList<String>();
						if(!tempJointList.contains(tempJoint))
						{
							tempJointList.add(tempJoint);
						}
						lattice.put(j, tempJointList);
					}

					//update steiner points
					if(!steinerPoints.containsKey(tempJoint))
					{
						String[] s=tempJoint.split("[|]");
						steinerPoints.put(tempJoint,s.length);
					}
					
					//update scheduler
					if (scheduler.containsKey(tempJoint)) {
						List<String> scheduleList = scheduler.get(tempJoint);
						if(!scheduleList.contains(Integer.toString(i)))
						{
							scheduleList.add(Integer.toString(i));
						}
						if(!scheduleList.contains(Integer.toString(j)))
						{
							scheduleList.add(Integer.toString(j));
						}
						scheduler.put(tempJoint, scheduleList);
					} else {
						List<String> scheduleList = new LinkedList<String>();
						scheduleList.add(Integer.toString(i));
						scheduleList.add(Integer.toString(j));						
						scheduler.put(tempJoint, scheduleList);
					}

				}
				// System.out.println("Max Joint Result:" + tempJoint);
			}
			// add other keyword into schedule

			//keep the same keyword sequence as user input
			Collections.reverse(userQuery.get(i));
			
			for (String key : userQuery.get(i)) {
				Boolean check = false;
				if (lattice.containsKey(i)) {
					for (String temp : lattice.get(i)) {
						if (temp.contains(key)) {
							check = true;
						}
					}
				}
				if (!check) {
					if (lattice.containsKey(i)) {
						List<String> tempJointList = lattice.get(i);
						tempJointList.add(0,key);
						
						lattice.put(i, tempJointList);
					} else {
						List<String> tempJointList = new ArrayList<String>();
						tempJointList.add(0,key);
						lattice.put(i, tempJointList);
					}

				}
			}
		}

				
		//Helper.printHashMap(lattice);
		return lattice;

	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TestCase test = new TestQueryAware();
		test.run();
	}

	private List<String> answerQuery(HashMap<String,Integer> keywordCount,List<String> kList,PrintWriter outStream)
	{
		// answer 2 keyword per run
		List<String> curKeywords = new ArrayList<String>();
		//2 shortest keyword
	//	String shortestKeyword=Helper.getShortestKeyword(keywordCount, kList);
	//	curKeywords.add(shortestKeyword);
	//	kList.remove(shortestKeyword);
		
		curKeywords.add(kList.get(0));
		kList.remove(0);
		curKeywords.add(kList.get(0));
		kList.remove(0);
		
		
	//	shortestKeyword=Helper.getShortestKeyword(keywordCount, kList);
	//	curKeywords.add(shortestKeyword);
	//	kList.remove(shortestKeyword);
		
		KeywordQuery tempQuery = new KeywordQuery(curKeywords);
		for(String s : curKeywords)
		{
			if(shareFactor.containsKey(s))
			{
				tempQuery.LoadSpecificInformationFromList(s,(LinkedList<String>) shareFactor.get(s));
				
			}
			else
			{
				
				tempQuery.LoadSpecificInformation(s);
			}
		}
	
	
		SLCAEvaluation myEstimation = null;
		
		while (curKeywords.size() == 2) {

			
			// choose stack or index
			int sizeA = tempQuery.keyword2deweylist.get(curKeywords.get(0)).size();
			int sizeB = tempQuery.keyword2deweylist.get(curKeywords.get(1)).size();
			// go index
			if ((sizeA* 5) < sizeB  ) {
				outStream.println("index based");
			//	System.out.println("index based");
				myEstimation = new IndexbasedEvaluation(outStream,
						curKeywords, curKeywords.get(0));
			} 
			else if ((sizeB* 5) < sizeA  ) {
				outStream.println("index based");
			//	System.out.println("index based");
				myEstimation = new IndexbasedEvaluation(outStream,
						curKeywords, curKeywords.get(1));
			} 
			else // go stack
			{
				outStream.println("stack based");
		//		System.out.println("stack based");
				myEstimation = new StackbasedEvaluation(outStream,
						curKeywords);
			}

			// print keyword dewey list info
			for (String keyword : tempQuery.keywordList) {
				if (tempQuery.keyword2deweylist.get(keyword).size() == 0) {
					System.out
							.println("-- Error happened: \n --Keyword Size "
									+ keyword
									+ " -> number: "
									+ tempQuery.keyword2deweylist.get(
											keyword).size() + "\n");
				//	System.exit(-1);
				}
				outStream.println("Keyword Size " + keyword
						+ " -> number: "
						+ tempQuery.keyword2deweylist.get(keyword).size()
						+ "\n");

			}

	//		System.out.println(curKeywords);
	//		Helper.printHashMap(tempQuery.keyword2deweylist);
			myEstimation.computeSLCA(tempQuery);

			// release memory
			tempQuery.clearMem();
			System.gc();
			
			if(kList.size()>0)
			{
				String joinK = curKeywords.get(0)+"|"+ curKeywords.get(1);
				curKeywords.clear();
				curKeywords.add(joinK);
				
			//	String secondK=Helper.getShortestKeyword(keywordCount, kList);
			//	curKeywords.add(secondK);
			//	kList.remove(secondK);
				
				String secondK=kList.get(0);
				curKeywords.add(secondK);
				kList.remove(0);
				
				tempQuery=new KeywordQuery(curKeywords);
								
				if(shareFactor.containsKey(secondK))
				{
					tempQuery.LoadSpecificInformationFromList(secondK,(LinkedList<String>) shareFactor.get(secondK));			
				}
				else
				{
					
					tempQuery.LoadSpecificInformation(secondK);
				}
				
				tempQuery.LoadSpecificInformationFromList(joinK,myEstimation.getResult());
				
			}
			else
			{
				curKeywords.clear();
			}
			
		}
		
		return myEstimation.getResult();
	}
}
