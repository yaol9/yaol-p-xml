package com.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		HashSet <String> processLog = new HashSet<String> ();
		
		try {
			
			PrintWriter outStream = new PrintWriter(new BufferedWriter(
					new FileWriter(new File(PropertyReader
							.getProperty("resultLog")))));

			
					
			String ksFile = PropertyReader.getProperty("ksFile");

			BufferedReader queryRead = new BufferedReader(
					new InputStreamReader(new DataInputStream(
							new FileInputStream(ksFile))));

			String query = "";
			 // user
					
			while ((query = queryRead.readLine()) != null) {
				
				if(!query.startsWith("#"))
				{
					List<String> refinedkeywords = Helper.getRefinedKeywords(query);
					
					//make combination
					int keywordCount = refinedkeywords.size();
					
					// 2 words combination
					logFor2wordCombination(processLog, outStream,
							refinedkeywords);
					
					//3 words combination
					logFor3wordCombination(processLog, outStream,
							refinedkeywords);
					
					//4 words combination
					logFor4wordCombination(processLog, outStream,
							refinedkeywords);
					
					//5 words
					if(keywordCount>5)
					{
						logFor5wordCombination(processLog, outStream,
								refinedkeywords);						
						
					}
					
					
				}
			}
			
			queryRead.close();
			outStream.close();

			System.out.println("finished");
	}
	catch(Exception e)
	{
	
	}
		
	}

	private static void logFor5wordCombination(HashSet<String> processLog,
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
							if( (!s.equalsIgnoreCase(s2)) && (!s.equalsIgnoreCase(s3)) && (!s.equalsIgnoreCase(s4)) && (!s.equalsIgnoreCase(s5)))
							{
								List<String> mixList= new LinkedList<String>();
								mixList.add(s);
								mixList.add(s2);
								mixList.add(s3);
								mixList.add(s4);
								mixList.add(s5);
								String mix = Helper.getMixString(mixList);
								if(!processLog.contains(mix))
								{
									//calculate
									
									processQuery(outStream, mixList, mix);
									
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
	
	private static void logFor4wordCombination(HashSet<String> processLog,
			PrintWriter outStream, List<String> refinedkeywords) {
		for(String s: refinedkeywords)
		{
			for(String s2:refinedkeywords)
			{
				for(String s3:refinedkeywords)
				{
					for(String s4:refinedkeywords)
					{
						if( (!s.equalsIgnoreCase(s2)) && (!s.equalsIgnoreCase(s3)) && (!s.equalsIgnoreCase(s4)) )
						{
							List<String> mixList= new LinkedList<String>();
							mixList.add(s);
							mixList.add(s2);
							mixList.add(s3);
							mixList.add(s4);
							String mix = Helper.getMixString(mixList);
							if(!processLog.contains(mix))
							{
								//calculate
								
								processQuery(outStream, mixList, mix);
								
								//log
								processLog.add(mix);
							}								
							
						}
					}
				
				}
			
			}
		}
		
	}

	
	private static void logFor3wordCombination(HashSet<String> processLog,
			PrintWriter outStream, List<String> refinedkeywords) {
		for(String s: refinedkeywords)
		{
			for(String s2:refinedkeywords)
			{
				for(String s3:refinedkeywords)
				{
					if( (!s.equalsIgnoreCase(s2)) && (!s.equalsIgnoreCase(s3)) )
					{
						List<String> mixList= new LinkedList<String>();
						mixList.add(s);
						mixList.add(s2);
						mixList.add(s3);
						String mix = Helper.getMixString(mixList);
						if(!processLog.contains(mix))
						{
							//calculate
							
							processQuery(outStream, mixList, mix);
							
							//log
							processLog.add(mix);
						}								
						
					}
				}
			
			}
		}
		
	}

	private static void logFor2wordCombination(HashSet<String> processLog,
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
						
						processQuery(outStream, mixList, mix);
						
						//log
						processLog.add(mix);
					}
					
				}
			}
		}
	}

	private static void processQuery(PrintWriter outStream,
			List<String> mixList, String mix) {
		KeywordQuery kquery = new KeywordQuery(mixList);
		for(String m:mixList)
		{
			kquery.LoadKeywordNodesfromDisc(m);
		}
		
		SLCAEvaluation myEstimation =new StackbasedEvaluation(outStream,
				mixList);
		
		myEstimation.computeSLCA(kquery);
		
		outStream.println(mix+","+myEstimation.getResult().size());
	}

}
