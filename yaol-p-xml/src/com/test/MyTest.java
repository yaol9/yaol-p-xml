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
import com.QueryEvaluation.SuperStackbasedEvaluation;
import com.myjdbc.JdbcImplement;
import com.tools.Helper;
import com.tools.PropertyReader;
import com.tools.TimeRecorder;

public class MyTest {

	private HashMap<Integer, List<String>> lattice;
	
	private HashMap<String, List<String>> advanceScheduler; // whether delete keyword
    private int curUserQuery ;
	public MyTest() {
		lattice = new HashMap<Integer, List<String>>();
	
		advanceScheduler= new HashMap<String, List<String>>();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MyTest mytest = new MyTest();
		mytest.testSequenceAlgorithm();
		mytest.testBasicAlgorithm();
		mytest.testTemplateAwareAlgorithm();
		//mytest.testTemplateAwareAlgorithm_old();
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

	public void testTemplateAwareAlgorithm()
	{
		try {
			PrintWriter outStream = new PrintWriter(new BufferedWriter(
					new FileWriter(
							new File("./out/TemplateAwareEvaluation.log"))));

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

			generateLattice(userQuery, counter);
			//Helper.PrintHashMap(advanceScheduler);	
	
			
			TimeRecorder.startRecord();
			KeywordQuery kquery = new KeywordQuery();
			List<String> curItem = getNextNodeFromLattice();
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
							tempQuery.LoadAllInformation();
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
				
				// stack or index
				
				myEstimation= new StackbasedEvaluation(
						outStream,curItem);
				
								
				//Helper.PrintList(kquery.keywordList);
				myEstimation.computeSLCA(kquery);					
				
				// release memory			
			
				for (String keyword :curItem) {
					//int curCount = 0;
					if(advanceScheduler.containsKey(keyword))
					{
						List<String> tempList=advanceScheduler.get(keyword);
						if(tempList.contains( Integer.toString(curUserQuery)))
						{
							tempList.remove(Integer.toString(curUserQuery));
							advanceScheduler.put( Integer.toString(curUserQuery), tempList);
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
				
				curItem=getNextNodeFromLattice();
				
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
	public void testTemplateAwareAlgorithm_old() {
		try {
			PrintWriter outStream = new PrintWriter(new BufferedWriter(
					new FileWriter(
							new File("./out/TemplateAwareEvaluation.log"))));

			String databaseName = PropertyReader.getProperty("dbname");
			JdbcImplement.ConnectToDB(databaseName);
			String ksFile = PropertyReader.getProperty("ksFile");
			BufferedReader queryRead = new BufferedReader(
					new InputStreamReader(new DataInputStream(
							new FileInputStream(ksFile))));

			String query = "";
			HashMap<Integer, List<String>> userQuery = new HashMap<Integer, List<String>>(); // user
																								// query
			int counter = 0;
			int minKeywordPointer = 0; // start from shortest keyword query
			int minKeywordCount = 0;

			while ((query = queryRead.readLine()) != null) {
				outStream.printf("-- " + "Keyword Query: %s \n", query);
				outStream.println();
				System.out.printf("-- " + "Keyword Query: %s \n", query);

				List<String> refinedkeywords = Helper.getRefinedKeywords(query);
				if (minKeywordCount == 0) {
					minKeywordCount = refinedkeywords.size();
				} else if (refinedkeywords.size() < minKeywordCount) {
					minKeywordCount = refinedkeywords.size();
					minKeywordPointer = counter;
				}

				userQuery.put(counter, refinedkeywords);
				counter++;
			}

			generateLattice(userQuery, counter);
			System.out.println("Start calculation from query:"
					+ minKeywordPointer);
			
			KeywordQuery kquery = new KeywordQuery();
			List<String> curItem = lattice.remove(minKeywordPointer);
			curUserQuery=minKeywordPointer;
			TimeRecorder.startRecord();
			
			while(!curItem.isEmpty())
			{			
				// Start to estimate
				outStream.printf("-- " + "Keyword Query: %s \n", curItem);
				outStream.println();
				System.out.printf("-- " + "Keyword Query: %s \n", curItem);
				
				// give a refined keyword query to load
				// the corresponding keyword nodes
				List<String> revisedQuery = new LinkedList<String>();
				for(String s : curItem)
				{
					if(s.contains("|"))
					{
						if(kquery.keyword2deweylist.containsKey(s))
						{
							revisedQuery.add(s);
						}
						else
						{
							String[] tempList = s.split("[|]");
							for(String temp:tempList)
							{
								if(!revisedQuery.contains(temp))
								{
									revisedQuery.add(temp);
								}
							}
						}
						
					}
					else
					{
						if(!revisedQuery.contains(s))
						{
							revisedQuery.add(s);
						}
					}
				}
				SuperStackbasedEvaluation myEstimation = new SuperStackbasedEvaluation(
						outStream, revisedQuery,kquery);
					
		
				
				// Start to estimate
				List<String> tempAddedKeyword = new LinkedList<String>();
				
				for (String keyword : curItem) {
					if (!kquery.keyword2deweylist.containsKey(keyword)) {
						
						if(keyword.contains("|"))
						{
							String[] tempList = keyword.split("[|]");
							//add share result
							kquery.shareResultList.add(keyword);
							kquery.keywordList.add(keyword);
							
							
							for(String temp:tempList)
							{
								if (!kquery.keyword2deweylist.containsKey(temp)) {
									kquery.LoadSpecificInformation(temp);
									kquery.keywordList.add(temp);
									tempAddedKeyword.add(temp);
									myEstimation.keywordList.add(temp);
									
								}
								kquery.pointerOfSmallNodes.put(temp, 0);								
							}
						}
						else
						{
							kquery.LoadSpecificInformation(keyword);
							kquery.keywordList.add(keyword);
							kquery.pointerOfSmallNodes.put(keyword, 0);
						}						
					}
					else
					{
						kquery.pointerOfSmallNodes.put(keyword, 0);
					}
					
				}

				// print keyword dewey list info
				for (String keyword : curItem) {
					if(kquery.keyword2deweylist.containsKey(keyword))
					{
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
					

				}
				//Helper.PrintList(kquery.keywordList);
				myEstimation.computeSLCA(kquery);					
				
				// release memory			
			
				for (String keyword :curItem) {
					//int curCount = 0;
					if(advanceScheduler.containsKey(keyword))
					{
						List<String> tempList=advanceScheduler.get(keyword);
						if(tempList.contains( Integer.toString(curUserQuery)))
						{
							tempList.remove(Integer.toString(curUserQuery));
							advanceScheduler.put( Integer.toString(curUserQuery), tempList);
						}
						
						if(tempList.size()==0)
						{
							kquery.clearKeyword(keyword);
						}
					}
					else
					{
						kquery.clearKeyword(keyword);
					}
												
		
				}
				for(String tempKeyword : tempAddedKeyword)
				{
					kquery.clearKeyword(tempKeyword);
				}
			
			
				System.gc();

				
				myEstimation.PrintResults();
				Helper.PrintHashMap(kquery.keyword2deweylist);
				
				curItem=getNextNodeFromLattice();
				
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

	private void generateLattice(HashMap<Integer, List<String>> userQuery,
			int counter) {
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

					if (advanceScheduler.containsKey(tempJoint)) {
						List<String> scheduleList = advanceScheduler.get(tempJoint);
						if(!scheduleList.contains(Integer.toString(i)))
						{
							scheduleList.add(Integer.toString(i));
						}
						if(!scheduleList.contains(Integer.toString(j)))
						{
							scheduleList.add(Integer.toString(j));
						}
						advanceScheduler.put(tempJoint, scheduleList);
					} else {
						List<String> scheduleList = new LinkedList<String>();
						scheduleList.add(Integer.toString(i));
						scheduleList.add(Integer.toString(j));						
						advanceScheduler.put(tempJoint, scheduleList);
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
		

	}
	
	private List<String> getNextNodeFromLattice()
	{
		int nextPos=0;
		List<String> nextItem;
		if(lattice.size()>0)
		{
			for(Integer pos:lattice.keySet())
			{
				nextPos=pos;
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

}
