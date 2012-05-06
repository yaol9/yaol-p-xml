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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import com.QueryEvaluation.IndexbasedEvaluation;
import com.QueryEvaluation.KeywordQuery;
import com.QueryEvaluation.SLCAEvaluation;
import com.QueryEvaluation.StackbasedEvaluation;
import com.db.DBHelper;
import com.tools.Helper;
import com.tools.PropertyReader;

public class GenerateLog {

	private HashMap<String,Integer> outputResultSize;

	private HashMap<String,Integer> resultSize;
	
	private HashMap<String,LinkedList<String>> resultLog;
	
	
	GenerateLog()
	{
		outputResultSize = new HashMap<String,Integer>();
		resultSize = new HashMap<String,Integer>();
		resultLog = new HashMap<String,LinkedList<String>>();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		
		
		GenerateLog test = new GenerateLog();
		
		
		test.run();
		
		
		
		
		
		
	}

	
	private void run() {
		try {

			loadSizeLog();
			
						
			HashSet<String> processLog = new HashSet<String>();

			PrintWriter outStream = new PrintWriter(new BufferedWriter(
					new FileWriter(new File(
							PropertyReader.getProperty("resultLog")))));

			String ksFile = PropertyReader.getProperty("ksFile");

			BufferedReader queryRead = new BufferedReader(
					new InputStreamReader(new DataInputStream(
							new FileInputStream(ksFile))));

			String query = "";
			// user

			while ((query = queryRead.readLine()) != null) {

				if (!query.startsWith("#")) {
					List<String> refinedkeywords = Helper
							.getRefinedKeywords(query);

					// make combination
					int keywordCount = refinedkeywords.size();

					// 2 words combination
					logFor2wordCombination(processLog, outStream,
							refinedkeywords);

					// 3 words combination
					logFor3wordCombination(processLog, outStream,
							refinedkeywords);

					// 4 words combination
					logFor4wordCombination(processLog, outStream,
							refinedkeywords);

					// 5 words
					if (keywordCount > 5) {
						logFor5wordCombination(processLog, outStream,
								refinedkeywords);

					}

				}
			}

			queryRead.close();
			
			//output
			
			for(String s : outputResultSize.keySet())
			{
				outStream.println(s+","+outputResultSize.get(s));
			}
			
			outStream.close();

			System.out.println("finished");
		} catch (Exception e) {

		}
	}
	private void logFor5wordCombination(HashSet<String> processLog,
			PrintWriter outStream, List<String> refinedkeywords) {
		for(String s: refinedkeywords)
		{
			for(String s2:refinedkeywords)
			{
				for(String s3:refinedkeywords)
				{
					for(String s4:refinedkeywords)
					{
						for(String s5:refinedkeywords)
						{
							if( (!s.equalsIgnoreCase(s2)) && (!s.equalsIgnoreCase(s3)) && (!s.equalsIgnoreCase(s4)) && (!s.equalsIgnoreCase(s5))  && (!s2.equalsIgnoreCase(s3)) && (!s2.equalsIgnoreCase(s4)) && (!s2.equalsIgnoreCase(s5))  && (!s3.equalsIgnoreCase(s4))  && (!s3.equalsIgnoreCase(s5)) && (!s4.equalsIgnoreCase(s5)) )
							{
								
								List<String> tempList= new LinkedList<String>();
								tempList.add(s);
								tempList.add(s2);
								tempList.add(s3);
								tempList.add(s4);
								
								String tempMix = Helper.getMixString(tempList);
								
								
								
								List<String> mixList= new LinkedList<String>();
								mixList.add(s);
								mixList.add(s2);
								mixList.add(s3);
								mixList.add(s4);
								mixList.add(s5);
								String mix = Helper.getMixString(mixList);
								
								List<String> processList= new LinkedList<String>();
								processList.add(tempMix);
								processList.add(s5);
								
								if(!processLog.contains(mix))
								{
									//calculate
									if(!outputResultSize.containsKey(mix))
									{

										processQuery(outStream, processList, mix);
										
									}
									//log
									processLog.add(mix);
								}								
								
							}

						}
						
					}
				
				}
			
			}
		}
		
	}
	
	private void logFor4wordCombination(HashSet<String> processLog,
			PrintWriter outStream, List<String> refinedkeywords) {
		for(String s: refinedkeywords)
		{
			for(String s2:refinedkeywords)
			{
				for(String s3:refinedkeywords)
				{
					for(String s4:refinedkeywords)
					{
						if( (!s.equalsIgnoreCase(s2)) && (!s.equalsIgnoreCase(s3)) && (!s.equalsIgnoreCase(s4)) && (!s2.equalsIgnoreCase(s3)) && (!s2.equalsIgnoreCase(s4)) && (!s3.equalsIgnoreCase(s4)) )
						{
							
							List<String> tempList= new LinkedList<String>();
							tempList.add(s);
							tempList.add(s2);
							tempList.add(s3);
							String tempMix = Helper.getMixString(tempList);

							
							List<String> mixList= new LinkedList<String>();
							mixList.add(s);							
							mixList.add(s2);
							mixList.add(s3);
							mixList.add(s4);
							String mix = Helper.getMixString(mixList);
							
							List<String> processList= new LinkedList<String>();
							processList.add(tempMix);
							processList.add(s4);
							
							if(!processLog.contains(mix))
							{
								//calculate
								
								if(!outputResultSize.containsKey(mix))
								{

									processQuery(outStream, processList, mix);
									
								}
								//log
								processLog.add(mix);
							}								
							
						}
					}
				
				}
			
			}
		}
		
	}

	
	private void logFor3wordCombination(HashSet<String> processLog,
			PrintWriter outStream, List<String> refinedkeywords) {
		for(String s: refinedkeywords)
		{
			for(String s2:refinedkeywords)
			{
				for(String s3:refinedkeywords)
				{
					if( (!s.equalsIgnoreCase(s2)) && (!s.equalsIgnoreCase(s3)) && (!s2.equalsIgnoreCase(s3)))
					{
						List<String> tempList= new LinkedList<String>();
						tempList.add(s);
						tempList.add(s2);
						String tempMix = Helper.getMixString(tempList);

												
						List<String> mixList= new LinkedList<String>();
						mixList.add(s);
						mixList.add(s2);
						mixList.add(s3);
						String mix = Helper.getMixString(mixList);
						
						List<String> processList= new LinkedList<String>();
						processList.add(tempMix);
						processList.add(s3);
						
						if(!processLog.contains(mix))
						{
							//calculate
							if(!outputResultSize.containsKey(mix))
							{

								processQuery(outStream, processList, mix);
								
							}
							//log
							processLog.add(mix);
						}								
						
					}
				}
			
			}
		}
		
	}

	private void logFor2wordCombination(HashSet<String> processLog,
			PrintWriter outStream, List<String> refinedkeywords) 
	{
		for(String s: refinedkeywords)
		{
			for(String s2:refinedkeywords)
			{
				if(!s.equalsIgnoreCase(s2))
				{
					List<String> mixList= new LinkedList<String>();
					mixList.add(s);
					mixList.add(s2);
					String mix = Helper.getMixString(mixList);
					if(!processLog.contains(mix))
					{
						//calculate
						if(!outputResultSize.containsKey(mix))
						{

							processQuery(outStream, mixList, mix);
							
						}
						//log
						processLog.add(mix);
					}
					
				}
			}
		}
	}

	private void processQuery(PrintWriter outStream,
			List<String> mixList, String mix) {

		
		KeywordQuery kquery = new KeywordQuery(mixList);
		for(String m:mixList)
		{
			if(resultLog.containsKey(m))
			{
				kquery.LoadSpecificInformationFromList(m, resultLog.get(m));
			}
			else
			{
				kquery.LoadKeywordNodesfromDisc(m);
			}
			
		}
		
		//only 2 keywords in this function
		int size1=resultSize.get(mixList.get(0));
		int size2=resultSize.get(mixList.get(1));
		
		SLCAEvaluation myEstimation;
		if(size1>(size2*10))
		{
			myEstimation=new IndexbasedEvaluation(outStream,
					mixList,mixList.get(1));
		}
		else if((size1*10)<size2)
		{
			myEstimation=new IndexbasedEvaluation(outStream,
					mixList,mixList.get(0));
		}
		else
		{
			myEstimation=new StackbasedEvaluation(outStream,
					mixList);
		}

		
		
		 
		
		myEstimation.computeSLCA(kquery);

		outputResultSize.put(mix, myEstimation.getResult().size());
	
		resultSize.put(mix, myEstimation.getResult().size());
		
		resultLog.put(mix, myEstimation.getResult());
	}
	
	private void loadSizeLog() {
		try{
		Helper.loadKeywordCount(resultSize);
		
		//load log
		String resultLog = PropertyReader.getProperty("resultLog");

		BufferedReader resultLogRead = new BufferedReader(
				new InputStreamReader(new DataInputStream(
						new FileInputStream(resultLog))));

		String resultLogItem = "";
		
		while ((resultLogItem = resultLogRead.readLine()) != null) {

			String keywordSet[] = resultLogItem.split("[,]");

			resultSize.put(keywordSet[0], Integer.valueOf(keywordSet[1]));
			
			
		}
		

	}
	catch (IOException e) {
		e.printStackTrace();
	}
	}

}
