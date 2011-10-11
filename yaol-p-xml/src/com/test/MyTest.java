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

import com.QueryEvaluation.StackbasedEvaluation;
import com.myjdbc.JdbcImplement;
import com.mysaxParser.TokenPreprocessor;
import com.tools.PropertyReader;

public class MyTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MyTest mytest = new MyTest();
		mytest.testStack();
	}
	
	public void testStack ()
	{
		try {
			PrintWriter outStream = new PrintWriter(new BufferedWriter(
					new FileWriter(new File("./out/StackbasedEvaluation.log"))));

			String databaseName = PropertyReader.getProperty("dbname");
			JdbcImplement.ConnectToDB(databaseName);

			String ksFile = PropertyReader.getProperty("ksFile");

			BufferedReader queryRead = new BufferedReader(
					new InputStreamReader(new DataInputStream(
							new FileInputStream(ksFile))));
			String query;
			while ((query = queryRead.readLine()) != null) {
				String keywordSet[] = query.split("[,]");

				// clean the keyword query using stop words
				TokenPreprocessor thisPreprocessor = new TokenPreprocessor();
				keywordSet = thisPreprocessor.trimTokens(keywordSet);
				keywordSet = thisPreprocessor.stopWordRemoval(keywordSet);
				keywordSet = thisPreprocessor
						.removeIrrelevantTokens(keywordSet);

				List<String> refinedkeywords = new LinkedList<String>();

				for (String item : keywordSet) {
					if (item != null) {
						refinedkeywords.add(item);
						System.out.println(item);
					}
				}

				refinedkeywords.toArray();
				System.out.println(refinedkeywords.size());
				// k specifies the number of required SLCA results

				// give a refined keyword query to load
				// the corresponding keyword nodes
				StackbasedEvaluation myEstimation = new StackbasedEvaluation(
						outStream, refinedkeywords);

				// Start to estimate
				outStream.printf("-- " + "Keyword Query: %s \n", query);
				outStream.println();
				System.out.printf("-- " + "Keyword Query: %s \n", query);
				myEstimation.LoadInformation(refinedkeywords);

				long start, qtime;
				start = System.currentTimeMillis();

				myEstimation.computeSLCA();
				
				//release memory
				StackbasedEvaluation._keyword2deweylist.clear(); 
				System.gc();
				
				qtime = System.currentTimeMillis() - start;

				// record memory usage
				Runtime rt = Runtime.getRuntime();
				long freememory = rt.freeMemory();
				long totalmemory = rt.totalMemory();
				long useagememory = totalmemory - freememory;

				outStream.printf("--" + "Response Time: %d \n", qtime);
				outStream.println();
				System.out.printf("--" + "Response Time: %d \n", qtime);
				outStream.printf("--" + "Memory usage: %d \n",
						useagememory);
				outStream.println();
				System.out.printf("--" + "Memory usage: %d \n", useagememory);

				myEstimation.PrintResults();				
				

			}
			queryRead.close();
			JdbcImplement.DisconnectDB();

			outStream.close();
			System.out.println("====================>>> Stop application!");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
