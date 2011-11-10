package com.occurrenceestimation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import com.db.DBHelper;
import com.tools.Helper;
import com.tools.PropertyReader;

public class OcurrenceEstimatiion {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
		String databaseName = PropertyReader.getProperty("dbname");
		DBHelper.ConnectToDB(databaseName);
		
		List <HashMap<String,Integer>> sampleList = new	ArrayList <HashMap<String,Integer>>();//sample	
		
		for(int times=1;times<16;times++)
		{
			String deweysql = "SELECT DISTINCT Dewey,XMLid FROM deweyid where XMLid < "+times*100000+" and XMLid > "+ (times-1)*100000+" order by XMLid;";
			ResultSet deweySet = DBHelper.performQuery(deweysql);

			HashMap<Integer,String> deweyList= new HashMap<Integer,String>();//xml id <->dewey
			int maxSize=0;
			while (deweySet.next()) {
				deweyList.put(deweySet.getInt("XMLid"), deweySet.getString("Dewey").trim());
				if(deweySet.getInt("XMLid")>maxSize)
				{
					maxSize=deweySet.getInt("XMLid");
				}
			}
			
				

			int start=1;
			int end=1;
			int threshhold=80;
					
			while(end<maxSize)
			{
				while(!deweyList.containsKey(start))
				{
					start++;
				}
				String startDewey = deweyList.get(start);
				end=start+threshhold;
				if(end>maxSize)
				{
					break;
				}
				while(!deweyList.containsKey(end))
				{
					end++;
				}
				String endDewey = deweyList.get(end);
				
				//whether valid						
				if(endDewey.startsWith(startDewey+"."))
				{					
					//valid
					HashMap<String,Integer> sample = new HashMap<String,Integer>();
					sample.put("start", start);
					sample.put("end", end);
					sampleList.add(sample);
					start=end+1;				
				}
				else
				{
					start++;
				}
				
			}
			deweyList=null;
			deweySet.close();
			deweySet=null;
			System.gc();
			System.out.println("Round: "+times+" sample size: "+sampleList.size());	
		}
		
		
		//print sample
		System.out.println("sample size: "+sampleList.size());	
		
		for( HashMap<String,Integer> s : sampleList)
		{
			System.out.println("start: "+s.get("start")+" end: "+s.get("end"));					
		}
		
		//generate sample occurrence
		
		//load top 1000 keyword
		String top1000sql = "SELECT Keyword,keywordCount FROM top1000 order by 'XMLid' desc;";
		ResultSet top1000Set = DBHelper.performQuery(top1000sql);
		List<String> frequenceKeywords = new ArrayList<String> ();

		while (top1000Set.next()) {
			frequenceKeywords.add(top1000Set.getString("Keyword"));
		}		
		//Helper.printList(frequenceKeywords);
		
		HashMap<String,Integer> occurrenceLog = new HashMap<String,Integer>(); // result
		
		//calculate result
				
		for( HashMap<String,Integer> s : sampleList)
		{
			String startPos = s.get("start").toString();
			String endPos = s.get("end").toString();
			String sql ="SELECT Keyword, Dewey,XMLid FROM KeywordDewey where XMLid <= "+endPos+" and XMLid >= "+startPos;
			ResultSet sqlResult = DBHelper.performQuery(sql);
			
			HashSet<String> keywordsSet = new HashSet<String>();
			
			while (sqlResult.next()) {
				keywordsSet.add(sqlResult.getString("Keyword").trim());									
			}
			//for single
			for(String keyword:frequenceKeywords)
			{
				if(keywordsSet.contains(keyword))
				{
					if(occurrenceLog.containsKey(keyword))
					{
						occurrenceLog.put(keyword, occurrenceLog.get(keyword)+1);
					}
					else
					{
						occurrenceLog.put(keyword, 1);
					}
				}				
			}
			
			//for combination
			for(String keyword_outer:frequenceKeywords)
			{
				for(String keyword_inner:frequenceKeywords)
				{
					if(!keyword_outer.equalsIgnoreCase(keyword_inner))
					{
						if(keywordsSet.contains(keyword_outer) && keywordsSet.contains(keyword_inner))
						{
							String comb=null;
							if(keyword_outer.compareToIgnoreCase(keyword_inner)>=0)
							{
								comb = keyword_inner + '|' + keyword_outer;							
							
								if(occurrenceLog.containsKey(comb))
								{
									occurrenceLog.put(comb, occurrenceLog.get(comb)+1);
								}
								else
								{
									occurrenceLog.put(comb, 1);
								}
							}
							else
							{
								//do nothing, only count combination once;
							}
						}
					}
				}
			}
			
		}
		
		//output
		PrintWriter outStream;
		outStream = new PrintWriter(new BufferedWriter(new FileWriter(new File(PropertyReader.getProperty("SampleOccur")))));
				
		for(String s : occurrenceLog.keySet())
		{
			
			outStream.printf("INSERT INTO occursample VALUES " +
					"(\"%s\", \"%s\");\n",s, occurrenceLog.get(s).toString().trim());
		}
		//Helper.printHashMap(occurrenceLog);
		outStream.close();
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

}
