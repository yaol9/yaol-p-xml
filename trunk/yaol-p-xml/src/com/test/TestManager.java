package com.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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

public class TestManager {

    
	public TestManager() {

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//	test2();
		//test3();
	
		TestCase testSequence = new TestSequenceAlgorithm();
		testSequence.run();
		
		TestCase testBasic = new TestBasicAlgorithm();
	//	testBasic.run();
		
		TestCase testQueryAware = new TestQueryAwareAlgorithm();
		//testQueryAware.run();
		
		TestCase testInstanceAware = new TestInstanceAwareAlgorithm();
	//	testInstanceAware.run();
		
	}	
	
	static void test2()
	{
		try {
			PrintWriter outStream;
			
				outStream = new PrintWriter(new BufferedWriter(
						new FileWriter(new File("./testresult/yaoltemp.log"))));
		

			String databaseName = PropertyReader.getProperty("dbname");
			DBHelper.ConnectToDB(databaseName);

			
			List<String> refinedkeywords = new LinkedList<String>();
			refinedkeywords.add("apple");
			refinedkeywords.add("company");
			
			KeywordQuery kquery = new KeywordQuery(refinedkeywords);
			kquery.LoadAllInformation();
			SLCAEvaluation mytest = new IndexbasedEvaluation(outStream,refinedkeywords,"apple");
			
			TimeRecorder.startRecord();
			
			mytest.computeSLCA(kquery);
		
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
			
			mytest.printResults();
			} 
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
		
	static void test3()
	{
		try {
			PrintWriter outStream;
			
				outStream = new PrintWriter(new BufferedWriter(
						new FileWriter(new File("./testresult/yaoltemp2.log"))));
		

			String databaseName = PropertyReader.getProperty("dbname");
			DBHelper.ConnectToDB(databaseName);

			
			List<String> refinedkeywords = new LinkedList<String>();
			refinedkeywords.add("apple");
			refinedkeywords.add("company");
			
			KeywordQuery kquery = new KeywordQuery(refinedkeywords);
			kquery.LoadAllInformation();
			SLCAEvaluation mytest = new StackbasedEvaluation(outStream, refinedkeywords);
			
			TimeRecorder.startRecord();
			
			mytest.computeSLCA(kquery);
		
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
			
			mytest.printResults();
			} 
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

}
