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

public class TestBasic implements TestCase {

	// for basic algorithm: every time the server choose 2 shortest keyword to
	// join until the query is answered.

	
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

			int counter = 0;

			while ((query = queryRead.readLine()) != null) {
				outStream.printf("-- " + "Keyword Query: %s \n", query);
				outStream.println();
		//		System.out.printf("-- " + "Keyword Query: %s \n", query);
				
				
				if(!query.startsWith("#"))
				{
					List<String> refinedkeywords = Helper.getRefinedKeywords(query);
					userQuery.put(counter, refinedkeywords);
					counter++;
				}

				

				
				
			}
			
			int answerSeq = 0; // start answer from the first query
		
			while (userQuery.size() > 0) {

				List<String> refinedkeywords = userQuery.remove(answerSeq++);

				int keywordSize = refinedkeywords.size();
		//		System.out.println(keywordSize);

				//get keyword count
				HashMap<String,Integer> keywordCount=new HashMap<String,Integer>();
				// Start to estimate
				outStream.printf("-- " + "Keyword Query: %s \n",
						refinedkeywords);
				outStream.println();
	//			System.out.printf("-- " + "Keyword Query: %s \n",refinedkeywords);

				SLCAEvaluation myEstimation = null;
				
				// answer 2 keyword per run
				List<String> curKeywords = new ArrayList<String>();
				
				//2 shortest keyword
				
				curKeywords.add(refinedkeywords.get(0));
			    refinedkeywords.remove(0);
				curKeywords.add(refinedkeywords.get(0));
				refinedkeywords.remove(0);
				
				/*
				curKeywords.add(shortestKeyword);
				refinedkeywords.remove(shortestKeyword);
				shortestKeyword=Helper.getShortestKeyword(keywordCount, refinedkeywords);
				curKeywords.add(shortestKeyword);
				refinedkeywords.remove(shortestKeyword);
				*/
				
				HashMap<String, LinkedList<String>> shareFactor=new HashMap<String, LinkedList<String>> ();
				
				KeywordQuery kquery = new KeywordQuery(curKeywords);
				kquery.LoadAllInformationDisc(shareFactor); //empty share factor
				while (curKeywords.size() == 2) {

					
					// choose stack or index
					int sizeA = kquery.keyword2deweylist.get(curKeywords.get(0)).size();
					int sizeB = kquery.keyword2deweylist.get(curKeywords.get(1)).size();
				//	Helper.printList(kquery.keyword2deweylist.get(curKeywords.get(0)));
				//	System.out.println("aaaa");
				//	Helper.printList(kquery.keyword2deweylist.get(curKeywords.get(1)));
					
					// go index
					if ((sizeA* 5) < sizeB  ) {
						outStream.println("index based");
	//					System.out.println("index based");
						myEstimation = new IndexbasedEvaluation(outStream,
								curKeywords, curKeywords.get(0));
					} 
					else if ((sizeB* 5) < sizeA  ) {
						outStream.println("index based");
		//				System.out.println("index based");
						myEstimation = new IndexbasedEvaluation(outStream,
								curKeywords, curKeywords.get(1));
					} 
					else // go stack
					{
						outStream.println("stack based");
		//				System.out.println("stack based");
						myEstimation = new StackbasedEvaluation(outStream,
								curKeywords);
					}

					// print keyword dewey list info
					for (String keyword : kquery.keywordList) {
						if (kquery.keyword2deweylist.get(keyword).size() == 0) {
							System.out
									.println("-- Error happened: \n --Keyword Size "
											+ keyword
											+ " -> number: "
											+ kquery.keyword2deweylist.get(
													keyword).size() + "\n");
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
					//System.gc();
					
					if(refinedkeywords.size()>0)
					{
						String joinK = curKeywords.get(0)+"|"+ curKeywords.get(1);
						curKeywords.clear();
						curKeywords.add(joinK);
						
						//String secondK=Helper.getShortestKeyword(keywordCount, refinedkeywords);
						//curKeywords.add(secondK);
						//refinedkeywords.remove(secondK);
												
						String secondK=refinedkeywords.get(0);
						curKeywords.add(secondK);
					    refinedkeywords.remove(0);
						
						kquery=new KeywordQuery(curKeywords);
						kquery.LoadKeywordNodesfromDisc(secondK);
						kquery.LoadSpecificInformationFromList(joinK,myEstimation.getResult());
						
					}
					else
					{
						curKeywords.clear();
					}
					
				}

				myEstimation.printResults();
				queryRead.close();
				
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public long run() {
		try {
			
			String databaseName = PropertyReader.getProperty("dbname");
			DBHelper.ConnectToDB(databaseName);
			
			
			PrintWriter outStream = new PrintWriter(new BufferedWriter(
					new FileWriter(new File(PropertyReader
							.getProperty("BasicAlgorithmResult")))));

			//warm up
			runSingle(outStream);
			
			TimeRecorder.startRecord();
			// run 5 times
			for (int i = 0; i < 1; i++) {
				runSingle(outStream);
			}
						
			TimeRecorder.stopRecord();
			//System.gc();
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

			
			DBHelper.DisconnectDB();

			outStream.close();
			System.out.println("====================>>> Stop application!");
			return qtime;
			

		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestCase test = new TestBasic();
		test.run();
	}

}
