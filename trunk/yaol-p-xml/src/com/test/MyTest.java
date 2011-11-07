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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.QueryEvaluation.IndexbasedEvaluation;
import com.QueryEvaluation.KeywordQuery;
import com.QueryEvaluation.StackbasedEvaluation;
import com.QueryEvaluation.SLCAEvaluation;
import com.myjdbc.JdbcImplement;
import com.tools.Helper;
import com.tools.PropertyReader;
import com.tools.TimeRecorder;

public class MyTest {

    private int curUserQuery ; // only for query aware algorithm
	public MyTest() {

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MyTest mytest = new MyTest();
		mytest.testSequenceAlgorithm();
		mytest.testBasicAlgorithm();
		mytest.testQueryAwareAlgorithm();
		
	}

	public void testSequenceAlgorithm() {
		try {
			PrintWriter outStream = new PrintWriter(new BufferedWriter(
					new FileWriter(new File("./out/SequenceEvaluation.log"))));

			String databaseName = PropertyReader.getProperty("dbname");
			JdbcImplement.ConnectToDB(databaseName);

			String ksFile = PropertyReader.getProperty("ksFile");

			BufferedReader queryRead = new BufferedReader(
					new InputStreamReader(new DataInputStream(
							new FileInputStream(ksFile))));

			String query = "";
			TimeRecorder.startRecord();
			while ((query = queryRead.readLine()) != null) {

				List<String> refinedkeywords = Helper.getRefinedKeywords(query);
				int keywordSize = refinedkeywords.size();
				System.out.println(keywordSize);

				// give a refined keyword query to load
				// the corresponding keyword nodes
				

				KeywordQuery kquery = new KeywordQuery(refinedkeywords);

				// Start to estimate
				outStream.printf("-- " + "Keyword Query: %s \n", query);
				outStream.println();
				System.out.printf("-- " + "Keyword Query: %s \n", query);

				kquery.LoadAllInformation();

				SLCAEvaluation myEstimation=null;
				
				//choose stack or index
				int min=1000;
				int totalSize=0;
				String minKeyword=null;
				for(String s : refinedkeywords)
				{
					int tempSize=kquery.keyword2deweylist.get(s).size();
					if(tempSize<min)
					{
						min=tempSize;
						minKeyword=s;
					}
					totalSize += tempSize;
				}
				//go index
				if((min*keywordSize*5) < totalSize )
				{
					outStream.printf("index based");
					System.out.println("index based");
					myEstimation = new IndexbasedEvaluation(
							outStream, refinedkeywords,minKeyword);
				}
				else //go stack
				{
					outStream.printf("stack based");
					System.out.println("stack based");
					myEstimation = new StackbasedEvaluation(
							outStream, refinedkeywords);
				}
				 
				
				// print keyword dewey list info
				for (String keyword : kquery.keywordList) {
					if (kquery.keyword2deweylist.get(keyword).size() == 0) {
						System.out
								.println("-- Error happened: \n --Keyword Size "
										+ keyword
										+ " -> number: "
										+ kquery.keyword2deweylist.get(keyword)
												.size() + "\n");
						System.exit(-1);
					}
					outStream.println("Keyword Size " + keyword
							+ " -> number: "
							+ kquery.keyword2deweylist.get(keyword).size()
							+ "\n");

				}
				myEstimation.computeSLCA(kquery);

				// release memory
				kquery.clearMem();
				System.gc();

				myEstimation.PrintResults();

			}

			TimeRecorder.stopRecord();

			long qtime = TimeRecorder.getTimeRecord();
			// get memory usage
			long usagememory = Helper.getMemoryUsage();

			outStream.println("Sequence Algorithms:");
			System.out.println("Sequence Algorithms:");
			outStream.printf("--" + "Response Time: %d \n", qtime);
			outStream.println();
			System.out.printf("--" + "Response Time: %d \n", qtime);
			outStream.printf("--" + "Memory usage: %d \n", usagememory);
			outStream.println();
			System.out.printf("--" + "Memory usage: %d \n", usagememory);

			queryRead.close();
			JdbcImplement.DisconnectDB();

			outStream.close();
			System.out.println("====================>>> Stop application!");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void testBasicAlgorithm() {
		try {
			PrintWriter outStream = new PrintWriter(new BufferedWriter(
					new FileWriter(new File("./out/BasicEvaluation.log"))));

			String databaseName = PropertyReader.getProperty("dbname");
			JdbcImplement.ConnectToDB(databaseName);

			String ksFile = PropertyReader.getProperty("ksFile");

			BufferedReader queryRead = new BufferedReader(
					new InputStreamReader(new DataInputStream(
							new FileInputStream(ksFile))));

			String query = "";
			TimeRecorder.startRecord();

            HashMap<String, Integer> scheduler = new HashMap<String,Integer>(); 
		
			if (!scheduler.isEmpty()) {
				scheduler.clear();
			}
			HashMap<Integer, List<String>> userQuery = new HashMap<Integer, List<String>>(); // user
																								// query

			int counter = 0;
			while ((query = queryRead.readLine()) != null) {
				outStream.printf("-- " + "Keyword Query: %s \n", query);
				outStream.println();
				System.out.printf("-- " + "Keyword Query: %s \n", query);

				List<String> refinedkeywords = Helper.getRefinedKeywords(query);

				for (String key : refinedkeywords) {
					if (scheduler.containsKey(key)) {
						scheduler.put(key, scheduler.get(key) + 1);
					} else {
						scheduler.put(key, 1);
					}
				}
				userQuery.put(counter, refinedkeywords);
				counter++;
			}
			KeywordQuery kquery = new KeywordQuery();
			for (int i = 0; i < counter; i++) {
				List<String> refinedkeywords = new LinkedList<String>();
				refinedkeywords = userQuery.get(i);
				int keywordSize = refinedkeywords.size();
				System.out.println(keywordSize);

				
				for (String keyword : refinedkeywords) {
					if (!kquery.keyword2deweylist.containsKey(keyword)) {
						kquery.LoadSpecificInformation(keyword);
					}
					kquery.pointerOfSmallNodes.put(keyword, 0);
				}
			
				
				// give a refined keyword query to load
				// the corresponding keyword nodes
				SLCAEvaluation myEstimation = null;//new StackbasedEvaluation(	outStream, refinedkeywords);

				//choose stack or index
				int min=1000;
				int totalSize=0;
				String minKeyword=null;
				for(String s : refinedkeywords)
				{
					int tempSize=kquery.keyword2deweylist.get(s).size();
					if(tempSize<min)
					{
						min=tempSize;
						minKeyword=s;
					}
					totalSize += tempSize;
				}
				//go index
				if((min*keywordSize*5) < totalSize )
				{
					outStream.printf("index based");
					System.out.println("index based");
					myEstimation = new IndexbasedEvaluation(
							outStream, refinedkeywords,minKeyword);
				}
				else //go stack
				{
					outStream.printf("stack based");
					System.out.println("stack based");
					myEstimation = new StackbasedEvaluation(
							outStream, refinedkeywords);
				}
								
				// Start to estimate
								
				// print keyword dewey list info
				for (String keyword : refinedkeywords) {
					if (kquery.keyword2deweylist.get(keyword).size() == 0) {
						System.out
								.println("-- Error happened: \n --Keyword Size "
										+ keyword
										+ " -> number: "
										+ kquery.keyword2deweylist.get(keyword)
												.size() + "\n");
						System.exit(-1);
					}
					System.out.println("Keyword Size " + keyword
							+ " -> number: "
							+ kquery.keyword2deweylist.get(keyword).size()
							+ "\n");
					outStream.println("Keyword Size " + keyword
							+ " -> number: "
							+ kquery.keyword2deweylist.get(keyword).size()
							+ "\n");

				}
				
				
				myEstimation.computeSLCA(kquery);

				// release memory
				for (String keyword : refinedkeywords) {
					int curCount = scheduler.get(keyword);
					if (curCount > 1) {
						scheduler.put(keyword, curCount - 1);
					} else {
						kquery.clearKeyword(keyword);
					}
				}
				
				System.gc();

				myEstimation.PrintResults();

			}
		
			TimeRecorder.stopRecord();

			long qtime = TimeRecorder.getTimeRecord();
			// get memory usage
			long usagememory = Helper.getMemoryUsage();

			outStream.println("Basic Algorithms:");
			System.out.println("Basic Algorithms:");
			outStream.printf("--" + "Response Time: %d \n", qtime);
			outStream.println();
			System.out.printf("--" + "Response Time: %d \n", qtime);
			outStream.printf("--" + "Memory usage: %d \n", usagememory);
			outStream.println();
			System.out.printf("--" + "Memory usage: %d \n", usagememory);

			queryRead.close();
			JdbcImplement.DisconnectDB();

			outStream.close();
			System.out.println("====================>>> Stop application!");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void testQueryAwareAlgorithm()
	{
		try {
			PrintWriter outStream = new PrintWriter(new BufferedWriter(
					new FileWriter(
							new File("./out/QueryAwareEvaluation.log"))));

			String databaseName = PropertyReader.getProperty("dbname");
			JdbcImplement.ConnectToDB(databaseName);
			String ksFile = PropertyReader.getProperty("ksFile");
			BufferedReader queryRead = new BufferedReader(
					new InputStreamReader(new DataInputStream(
							new FileInputStream(ksFile))));

			String query = "";
			HashMap<Integer, List<String>> userQuery = new HashMap<Integer, List<String>>(); // user query
			
			int counter = 0;

			while ((query = queryRead.readLine()) != null) {
				outStream.printf("-- " + "Keyword Query: %s \n", query);
				outStream.println();
				System.out.printf("-- " + "Keyword Query: %s \n", query);

				List<String> refinedkeywords = Helper.getRefinedKeywords(query);

				userQuery.put(counter, refinedkeywords);
				counter++;
			}
			HashMap<String, List<String>> scheduler=new HashMap<String, List<String>> ();
			Helper.PrintHashMap(scheduler);	
			HashMap<Integer, List<String>> lattice =	generateLattice(userQuery, counter,scheduler);
			
			Helper.PrintHashMap(scheduler);	
	
			
			TimeRecorder.startRecord();
			KeywordQuery kquery = new KeywordQuery();
			List<String> curItem = getNextNodeFromLattice(lattice,kquery);
			while(!curItem.isEmpty())
			{			
				// Start to estimate
				outStream.printf("-- " + "Keyword Query: %s \n", curItem);
				outStream.println();
				System.out.printf("-- " + "Keyword Query: %s \n", curItem);
								
				for(String keyword:curItem)
				{
					if(keyword.contains("|"))
					{
						if(!kquery.keyword2deweylist.containsKey(keyword))
						{
							List<String> kList=Arrays.asList(keyword.split("[|]"));
							KeywordQuery tempQuery = new KeywordQuery(kList);
							//tempQuery.LoadAllInformation();
							for(String s:kList)
							{
								if(!kquery.keyword2deweylist.containsKey(s))
								{
									tempQuery.LoadSpecificInformation(s);	
								}
								else
								{
									tempQuery.LoadSpecificInformationFromList(s,kquery.keyword2deweylist.get(s));
								}
								tempQuery.pointerOfSmallNodes.put(s, 0);
							}
							
							StackbasedEvaluation tempEstimation = new StackbasedEvaluation(outStream,kList);
							tempEstimation.computeSLCA(tempQuery);
							kquery.LoadSpecificInformationFromList(keyword,tempEstimation.resultList);
						}						
					}
					else
					{
						if (!kquery.keyword2deweylist.containsKey(keyword)) {
							kquery.LoadSpecificInformation(keyword);							
						}
					}
					
										
					kquery.pointerOfSmallNodes.put(keyword, 0);
					
				}
				
				SLCAEvaluation myEstimation;
				//choose stack or index
				int min=1000;
				int totalSize=0;
				int keywordSize = curItem.size();
				String minKeyword=null;
				
				for(String s : curItem)
				{
					int tempSize=kquery.keyword2deweylist.get(s).size();
					if(tempSize<min)
					{
						min=tempSize;
						minKeyword=s;
					}
					totalSize += tempSize;
				}
				//go index
				if((min*keywordSize*5) < totalSize )
				{
					outStream.printf("index based");
					System.out.println("index based");
					myEstimation = new IndexbasedEvaluation(
							outStream, curItem,minKeyword);
				}
				else //go stack
				{
					outStream.printf("stack based");
					System.out.println("stack based");
					myEstimation = new StackbasedEvaluation(
							outStream, curItem);
				}
										
				
								
				//Helper.PrintList(kquery.keywordList);
				myEstimation.computeSLCA(kquery);					
				
				// release memory			
			
				for (String keyword :curItem) {
					//int curCount = 0;
					if(scheduler.containsKey(keyword))
					{
						List<String> tempList=scheduler.get(keyword);
						if(tempList.contains( Integer.toString(curUserQuery)))
						{
							tempList.remove(Integer.toString(curUserQuery));
							scheduler.put( Integer.toString(curUserQuery), tempList);
						}						
					
						else if(tempList.size()==0)
						{
							kquery.clearKeyword(keyword);
						}
					}
					else
					{
						kquery.clearKeyword(keyword);
					}
												
					
				}
			
			
			
				System.gc();

				
				myEstimation.PrintResults();
				//Helper.PrintHashMap(kquery.keyword2deweylist);
				
				curItem=getNextNodeFromLattice(lattice,kquery);
				
			}
			
		
			TimeRecorder.stopRecord();

			long qtime = TimeRecorder.getTimeRecord();
			// get memory usage
			long usagememory = Helper.getMemoryUsage();

			outStream.println("Template Aware Algorithms:");
			System.out.println("Template Aware Algorithms:");
			outStream.printf("--" + "Response Time: %d \n", qtime);
			outStream.println();
			System.out.printf("--" + "Response Time: %d \n", qtime);
			outStream.printf("--" + "Memory usage: %d \n", usagememory);
			outStream.println();
			System.out.printf("--" + "Memory usage: %d \n", usagememory);


			queryRead.close();
			JdbcImplement.DisconnectDB();

			outStream.close();
			System.out.println("====================>>> Stop application!");
			
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
						tempJointList.add(key);
						lattice.put(i, tempJointList);
					} else {
						List<String> tempJointList = new ArrayList<String>();
						tempJointList.add(key);
						lattice.put(i, tempJointList);
					}

				}
			}
		}

		//Helper.PrintHashMap(lattice);
		return lattice;

	}
	
	private List<String> getNextNodeFromLattice(HashMap<Integer, List<String>> lattice,KeywordQuery kquery)
	{
		int nextPos=0;
		List<String> nextItem;
		if(lattice.size()>0)
		{
			for(Integer pos:lattice.keySet())
			{
				nextPos=pos;
				
				int i = compute_A(lattice.get(pos),kquery);
				System.out.println("Query: "+pos+" A value: "+i);
			}
			nextItem = lattice.remove(nextPos);
			curUserQuery=nextPos;
		}
		else
		{
			nextItem=new ArrayList<String>();
			
		}		
		
		return nextItem;
	}
	
	private int compute_A(List<String> curItem,KeywordQuery kquery)
	{
		int returnVal=0;
		
		for(String keyword:curItem)
		{
			if(keyword.contains("|"))
			{
				if(!kquery.keyword2deweylist.containsKey(keyword))
				{
					List<String> kList=Arrays.asList(keyword.split("[|]"));
			
					for(String s:kList)
					{
						if(!kquery.keyword2deweylist.containsKey(s))
						{
							
							returnVal++;
						}
				
					}
					
				
					returnVal++;
				}						
			}
			else
			{
				if (!kquery.keyword2deweylist.containsKey(keyword)) {
								
					returnVal++;
				}
			}
		}
				
		return returnVal;
	}
	
	private int compute_R(List<String> curItem,KeywordQuery kquery)
	{
		int returnVal=0;
		
		return returnVal;
	}
	
	

}
