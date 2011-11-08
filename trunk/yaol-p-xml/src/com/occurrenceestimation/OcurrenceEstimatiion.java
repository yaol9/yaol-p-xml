package com.occurrenceestimation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.myjdbc.JdbcImplement;
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
		JdbcImplement.ConnectToDB(databaseName);
		
		String deweysql = "SELECT DISTINCT Dewey,XMLid FROM keyworddewey order by 'XMLid' limit 10000;";
		ResultSet deweySet = JdbcImplement.performQuery(deweysql);

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

		int start=0;
		int end=0;
		int threshhold=5;
		while(end<maxSize)
		{
			while(!deweyList.containsKey(start))
			{
				start++;
			}
			String startDewey = deweyList.get(start);
			end=start+1;
			while(!deweyList.containsKey(end))
			{
				end++;
			}
			String endDewey = deweyList.get(end);
			//whether valid
			System.out.println(startDewey+" + "+endDewey);
			
			while(!endDewey.startsWith(startDewey+"."))
			{
				end++;
				while(!deweyList.containsKey(end))
				{
					end++;
					if(end>maxSize)
					{
						
					}
				}
				endDewey = deweyList.get(end);
				if(end-start>threshhold*10)
				{
					start++;
					break;
				}
			}
			
			if(end-start>threshhold)
			{					
				//valid
				HashMap<String,Integer> sample = new HashMap<String,Integer>();
				sample.put("start", start);
				sample.put("end", end);
				sampleList.add(sample);
				start=end+1;
				end++;
			}
			else
			{
				end++;
			}
			
		}
			
		//Helper.printHashMap(deweyList);
		Helper.printList(sampleList);
		//create samples
		
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
