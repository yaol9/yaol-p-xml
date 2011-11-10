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
import com.QueryEvaluation.IndexbasedEvaluation;
import com.QueryEvaluation.KeywordQuery;
import com.QueryEvaluation.SLCAEvaluation;
import com.QueryEvaluation.StackbasedEvaluation;
import com.tools.Helper;
import com.tools.PropertyReader;
import com.tools.TimeRecorder;

public class TestBasicAlgorithm implements TestCase {

	@Override
	public void run() {
		try {
			PrintWriter outStream = new PrintWriter(new BufferedWriter(
					new FileWriter(new File(PropertyReader.getProperty("BasicAlgorithmResult")))));

			String databaseName = PropertyReader.getProperty("dbname");
			DBHelper.ConnectToDB(databaseName);

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
				int min=Integer.MAX_VALUE;
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

				myEstimation.printResults();

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
			DBHelper.DisconnectDB();

			outStream.close();
			System.out.println("====================>>> Stop application!");

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
