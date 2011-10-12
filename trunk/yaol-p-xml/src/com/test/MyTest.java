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
import java.util.LinkedList;
import java.util.List;

import com.QueryEvaluation.KeywordQuery;
import com.QueryEvaluation.StackbasedEvaluation;
import com.myjdbc.JdbcImplement;
import com.tools.Helper;
import com.tools.PropertyReader;
import com.tools.TimeRecorder;

public class MyTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MyTest mytest = new MyTest();
		mytest.SequenceAlgorithms();
	}
	
	public void SequenceAlgorithms ()
	{
		try {
			PrintWriter outStream = new PrintWriter(new BufferedWriter(
					new FileWriter(new File("./out/SequenceEvaluation.log"))));

			
			String databaseName = PropertyReader.getProperty("dbname");
			JdbcImplement.ConnectToDB(databaseName);

			String ksFile = PropertyReader.getProperty("ksFile");

			BufferedReader queryRead = new BufferedReader(
					new InputStreamReader(new DataInputStream(
							new FileInputStream(ksFile))));
			
			String query;
			TimeRecorder.startRecord();
			while ((query = queryRead.readLine()) != null) {
				
				List<String> refinedkeywords = new LinkedList<String>();
				refinedkeywords=Helper.getRefinedKeywords(query);
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
			
				//print keyword dewey list info
				for(String keyword:kquery.keywordList)
				{					
					if (kquery.keyword2deweylist.get(keyword).size() == 0) {
						System.out.println("-- Error happened: \n --Keyword Size "
								+ keyword + " -> number: " + kquery.keyword2deweylist.get(keyword).size() + "\n");
						System.exit(-1);
					}
					outStream.println("Keyword Size " + keyword
							+ " -> number: " + kquery.keyword2deweylist.get(keyword).size() + "\n");

				}						
				myEstimation.computeSLCA(kquery);			
				
				//release memory				
				kquery.clearMem();
				System.gc();				
				
				myEstimation.PrintResults();						

			}
			
			TimeRecorder.stopRecord();
			
			long qtime=TimeRecorder.getTimeRecord();
			// get memory usage
			long usagememory=Helper.getMemoryUsage();
			
			outStream.println("Sequence Algorithms:");
			System.out.println("Sequence Algorithms:");
			outStream.printf("--" + "Response Time: %d \n", qtime);
			outStream.println();
			System.out.printf("--" + "Response Time: %d \n", qtime);
			outStream.printf("--" + "Memory usage: %d \n",
					usagememory);
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

	public void BasicAlgorithms ()
	{
		try {
			PrintWriter outStream = new PrintWriter(new BufferedWriter(
					new FileWriter(new File("./out/BasicEvaluation.log"))));

			
			String databaseName = PropertyReader.getProperty("dbname");
			JdbcImplement.ConnectToDB(databaseName);

			String ksFile = PropertyReader.getProperty("ksFile");

			BufferedReader queryRead = new BufferedReader(
					new InputStreamReader(new DataInputStream(
							new FileInputStream(ksFile))));
			
			String query;
			TimeRecorder.startRecord();
			while ((query = queryRead.readLine()) != null) {
				
				List<String> refinedkeywords = new LinkedList<String>();
				refinedkeywords=Helper.getRefinedKeywords(query);
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
			
				//print keyword dewey list info
				for(String keyword:kquery.keywordList)
				{					
					if (kquery.keyword2deweylist.get(keyword).size() == 0) {
						System.out.println("-- Error happened: \n --Keyword Size "
								+ keyword + " -> number: " + kquery.keyword2deweylist.get(keyword).size() + "\n");
						System.exit(-1);
					}
					outStream.println("Keyword Size " + keyword
							+ " -> number: " + kquery.keyword2deweylist.get(keyword).size() + "\n");

				}						
				myEstimation.computeSLCA(kquery);			
				
				//release memory				
				kquery.clearMem();
				System.gc();				
				
				myEstimation.PrintResults();						

			}
			
			TimeRecorder.stopRecord();
			
			long qtime=TimeRecorder.getTimeRecord();
			// get memory usage
			long usagememory=Helper.getMemoryUsage();
			
			outStream.println("Sequence Algorithms:");
			System.out.println("Sequence Algorithms:");
			outStream.printf("--" + "Response Time: %d \n", qtime);
			outStream.println();
			System.out.printf("--" + "Response Time: %d \n", qtime);
			outStream.printf("--" + "Memory usage: %d \n",
					usagememory);
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
}
