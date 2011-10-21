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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.QueryEvaluation.KeywordQuery;
import com.QueryEvaluation.StackbasedEvaluation;
import com.QueryEvaluation.SuperStackbasedEvaluation;
import com.myjdbc.JdbcImplement;
import com.tools.Helper;
import com.tools.PropertyReader;
import com.tools.TimeRecorder;

public class MyTest {

	private HashMap<Integer, List<String>> lattice;
	private HashMap<String, Integer> scheduler; // whether delete keyword
    private int curUserQuery ;
	public MyTest() {
		lattice = new HashMap<Integer, List<String>>();
		scheduler = new HashMap<String, Integer>();
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
				System.out.println(refinedkeywords.size());

				// give a refined keyword query to load
				// the corresponding keyword nodes
				StackbasedEvaluation myEstimation = new StackbasedEvaluation(
						outStream, refinedkeywords);

				KeywordQuery kquery = new KeywordQuery(refinedkeywords);

				// Start to estimate
				outStream.printf("-- " + "Keyword Query: %s \n", query);
				outStream.println();
				System.out.printf("-- " + "Keyword Query: %s \n", query);

				kquery.LoadAllInformation();

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

			// HashMap<String, Integer> scheduler = new HashMap<String,
			// Integer>(); // whether
			// a
			// keyword
			// should
			// be
			// removed
			// from
			// memory
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
				System.out.println(refinedkeywords.size());

				// give a refined keyword query to load
				// the corresponding keyword nodes
				StackbasedEvaluation myEstimation = new StackbasedEvaluation(
						outStream, refinedkeywords);

				// KeywordQuery kquery = new KeywordQuery(refinedkeywords);

				// Start to estimate
				for (String keyword : refinedkeywords) {
					if (!kquery.keyword2deweylist.containsKey(keyword)) {
						kquery.LoadSpecificInformation(keyword);
					}
					kquery.pointerOfSmallNodes.put(keyword, 0);
				}

				// kquery.LoadAllInformation();

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

			// check memory
			// for(String key:kquery.keyword2deweylist.keySet())
			// {
			// System.out.println(key);
			// System.out.println(kquery.keyword2deweylist.get(key).size());
			// }

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

	public void testTemplateAwareAlgorithm() {
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
					int curCount = 0;
					if(scheduler.containsKey(keyword))
					{
						curCount=scheduler.get(keyword);
					}
												
					if (curCount > 1) {
						scheduler.put(keyword, curCount - 1);
					} else {
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
						List<String> tempJointList = new ArrayList<String>();
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
						List<String> tempJointList = new ArrayList<String>();
						if(!tempJointList.contains(tempJoint))
						{
							tempJointList.add(tempJoint);
						}
						lattice.put(j, tempJointList);
					}

					if (scheduler.containsKey(tempJoint)) {
						scheduler.put(tempJoint, scheduler.get(tempJoint) + 2);
					} else {
						scheduler.put(tempJoint, 2);
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
		// check lattice
		// for (int i = 0; i < counter; i++) {
		// System.out.println("Lattice " + i);
		// for (String keyword : lattice.get(i)) {
		// System.out.println(keyword);
		// }
		// }
		Helper.PrintHashMap(lattice);
		// check lattice finish

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
