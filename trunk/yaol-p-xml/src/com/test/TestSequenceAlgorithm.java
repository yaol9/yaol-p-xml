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
import java.util.List;

import com.myjdbc.JdbcImplement;
import com.QueryEvaluation.IndexbasedEvaluation;
import com.QueryEvaluation.KeywordQuery;
import com.QueryEvaluation.SLCAEvaluation;
import com.QueryEvaluation.StackbasedEvaluation;
import com.tools.Helper;
import com.tools.PropertyReader;
import com.tools.TimeRecorder;

public class TestSequenceAlgorithm implements TestCase {

	@Override
	public void run() {
		
		try {
			PrintWriter outStream = new PrintWriter(new BufferedWriter(
					new FileWriter(new File(PropertyReader.getProperty("SequenceAlgorithmResult")))));

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

				myEstimation.printResults();

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

}
