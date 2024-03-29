package com.tools;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import com.xmlparser.TokenPreprocessor;

public class Helper {
	
	public static void printList(List mylist) {

		Iterator ite = mylist.iterator();
		while (ite.hasNext()) {

			System.out.println(ite.next().toString());
		}
	}

	public static void printHashMap(HashMap hm) {

		Set keyset = hm.keySet();
		for (Object key : keyset) {
			System.out.println(key.toString()+"  "+ hm.get(key).toString());
		}
	}
	
	/*
	 * transform the components in stack into a dewey code
	 */
	public static String getDewey(Stack<String> stack) {

		// scan the stack from bottom to up
		String dewey = null;
		for (int i = 0; i < stack.size(); i++) {

			if (dewey == null) {
				dewey = stack.get(i);
			} else {
				dewey = dewey + "." + stack.get(i);
			}

		}

		return dewey;
	}
	
	public static Boolean isSLCA(HashMap<String, Integer> topKeywordStack,List<String> keywordList) {
		// check whether satisfy slca
		if(topKeywordStack.containsKey("a-refuse-mark"))
		{
			return false;
		}
		int containKeyCount = 0;
		for (String key : keywordList) {
			if (topKeywordStack.containsKey(key)) {
				// System.out.println(keywordStack.get(key));
				if (topKeywordStack.get(key) == 1) {
					containKeyCount++;
				}
			}
		}
		
		if (containKeyCount == keywordList.size()) {
			return true;
		} else {
			return false;
		}

	}
	
	public static List<String> getRefinedKeywords(String query)
	{
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
		//		System.out.println(item);
			}
		}

		refinedkeywords.toArray();
		return refinedkeywords;
	}
	
	public static long getMemoryUsage()
	{
		// record memory usage
		Runtime rt = Runtime.getRuntime();
		//rt.gc();
		long freememory = rt.freeMemory();
		long totalmemory = rt.totalMemory();
		long usagememory = totalmemory - freememory;
		return usagememory;
	}	
	
	public static String getMaxJointString(List<String> list1,List<String> list2)
	{
	//	String tempJoint="";
		List<String> tempList = new ArrayList<String>();
		for(String keyword:list1)
		{
			for(String keyword2:list2)
			{
				if(keyword.equalsIgnoreCase(keyword2))
				{
		
					Boolean isInserted  = false;
					
					for(int i =0;i<tempList.size();i++)
					{
						if(tempList.get(i).compareToIgnoreCase(keyword)<0)
						{
							
						}
						else
						{
							String temp=tempList.get(i);
							//tempList.set(i, keyword);
							tempList.add(i,keyword);
							//keyword=temp;
							isInserted=true;
							break;
						}
						
						
					}
					
					if(!isInserted)
					{
						tempList.add(keyword);
					}
					
					/*
					if(tempJoint.isEmpty())
					{
						tempJoint += keyword;
					}
					else
					{
						tempJoint += "|"+keyword;
					}
						*/
				}
			}
		}
		
		String  returnS="";
		if(tempList.size()>1)
		{
			for(int i =0;i<tempList.size();i++)
			{
				if(returnS.isEmpty())
				{
					returnS += tempList.get(i);
				}
				else
				{
					returnS += "|"+tempList.get(i);
				}
			}
		}
	//	if(tempJoint.contains("|"))
		//{
			//return tempJoint;
		//}
		//else
		//{
		return returnS;
		//}
		
	}
	
	public static List<String> getMaxJointStringList(List<String> list1,List<String> list2)
	{
	//	String tempJoint="";
		List<String> tempList = new ArrayList<String>();
		
		for(String keyword:list1)
		{
			for(String keyword2:list2)
			{
				if(keyword.equalsIgnoreCase(keyword2))
				{
		
					Boolean isInserted  = false;
					
					for(int i =0;i<tempList.size();i++)
					{
						if(tempList.get(i).compareToIgnoreCase(keyword)<0)
						{
							
						}
						else
						{
							String temp=tempList.get(i);
							//tempList.set(i, keyword);
							tempList.add(i,keyword);
							//keyword=temp;
							isInserted=true;
							break;
						}
						
						
					}
					
					if(!isInserted)
					{
						tempList.add(keyword);
					}
					
					/*
					if(tempJoint.isEmpty())
					{
						tempJoint += keyword;
					}
					else
					{
						tempJoint += "|"+keyword;
					}
						*/
				}
			}
		}
		
		
	//	if(tempJoint.contains("|"))
		//{
			//return tempJoint;
		//}
		//else
		//{
		return tempList;
		//}		
	}
	
	//return 1 if dewey1>dewey2
	public static int compareDewey(String dewey1, String dewey2)
	{
		int returnVal =0;
		String[] deweyList1 = dewey1.split("[.]");
		String[] deweyList2 = dewey2.split("[.]");
		for (int i = 0; i < deweyList1.length; i++) {
			if(deweyList2.length>i)
			{
				if(Integer.parseInt(deweyList1[i])>Integer.parseInt(deweyList2[i]))
				{
					returnVal=1;
					return returnVal;
				}
				else if(Integer.parseInt(deweyList1[i])<Integer.parseInt(deweyList2[i]))
				{
					returnVal=-1;
					return returnVal;
				}			
			}				
		}
		if(deweyList2.length>deweyList1.length)
		{
			returnVal=-1;
			return returnVal;
		}
		
		return returnVal;
	}
	
	public static String getShortestKeyword(HashMap<String,Integer> keywordCount, List<String> refinedkeywords)
	{
		int shortestK =Integer.MAX_VALUE;
		String shortestKeyword=null;
		for(String s:refinedkeywords)
		{
			int count=0;
			if(keywordCount.containsKey(s))
			{
				count=keywordCount.get(s);
			}
			else
			{
				System.out.println(s);
			}
			
			if(count<shortestK)
			{
				shortestKeyword=s;
				shortestK=count;
			}
		
		}
		return shortestKeyword;
	}
	
	public static double  getSharingFactorSize(String s,HashMap<String,Integer> keywordCount,double r_ratio)
	{
		if(keywordCount.containsKey(s))
		{
			return keywordCount.get(s);
		}
		int shortestK = Integer.MAX_VALUE;
		
		List<String> sfList =Arrays.asList( s.split("[|]"));
		
		for(String key:sfList)
		{
			if(keywordCount.get(key)<shortestK)
			{
				shortestK=keywordCount.get(key);							
			}
		}
		
		double size_sf = shortestK*r_ratio;
		keywordCount.put(s,(int) size_sf);
		return size_sf;
	}

	public static void loadKeywordCount(HashMap<String,Integer> keywordCount)
	{
		String fileName="./keyword/keywordcount.txt";
		
		try {
			BufferedReader queryRead = new BufferedReader(
					new InputStreamReader(new DataInputStream(
							new FileInputStream(fileName))));

			String node = "";
		
			try {
				while ((node = queryRead.readLine()) != null) {

					String [] s = node.split("[,]");					
					keywordCount.put(s[0].trim(), Integer.parseInt(s[1].trim()));
					
				}
						
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	public static String getMixString(List<String> mixList) {
	
		List<String> localMixList = new LinkedList<String>();
		for(String s: mixList)
		{
			localMixList.add(s);
		}
			
		
		String mixString="";
		
		while(localMixList.size()>1)
		{
			String sTemp =localMixList.get(0);
			for(String s: localMixList)
			{
				if(!s.equalsIgnoreCase(sTemp))
				{
					if(sTemp.compareToIgnoreCase(s)>0)
					{
						sTemp=s;
					}
				}
				
			}
			mixString=mixString+sTemp+"|";
			localMixList.remove(sTemp);
		}
		mixString = mixString + localMixList.get(0);
			
		
		return mixString;
	}
}
