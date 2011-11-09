package com.occurrenceestimation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
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
		
		String deweysql = "SELECT DISTINCT Dewey,XMLid FROM deweyid order by 'XMLid' limit 10000;";
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
		
		List <HashMap<String,Integer>> sampleList = new	ArrayList <HashMap<String,Integer>>();//sample		

		int start=1;
		int end=1;
		int threshhold=10;
				
		while(end<maxSize)
		{
			while(!deweyList.containsKey(start))
			{
				start++;
			}
			String startDewey = deweyList.get(start);
			end=start+threshhold;
			
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
		
		//print sample
		System.out.println("sample size: "+sampleList.size());	
		
		for( HashMap<String,Integer> s : sampleList)
		{
			System.out.println("start: "+deweyList.get(s.get("start"))+" end: "+deweyList.get(s.get("end")));					
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
		
		
		
		
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
