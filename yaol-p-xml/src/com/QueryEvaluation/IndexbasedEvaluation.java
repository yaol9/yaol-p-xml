package com.QueryEvaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.myjdbc.JdbcImplement;
import com.test.TestManager;
import com.tools.Helper;
import com.tools.PropertyReader;

public class IndexbasedEvaluation implements SLCAEvaluation {

	private PrintWriter SLCAResults;

	private List<String> keywordList;

	public LinkedList<String> resultList;

	public int _totalnumberofresults;

	public int _numberofchecked;

	private String indexWord;
	
	public IndexbasedEvaluation(PrintWriter outStream, List<String> keywords,String indexWord) {
		SLCAResults = outStream;

		_totalnumberofresults = 0;
		_numberofchecked = 0;

		resultList = new LinkedList<String>();

		keywordList = keywords;

		this.indexWord=indexWord;
	}
	
	@Override
	public void computeSLCA(KeywordQuery kquery) {
		// TODO Auto-generated method stub
		//load list into []
		kquery.pointerOfSmallNodes.clear();
		
		String nodeV=null;
		List<String> resultB = new LinkedList<String>();
		
		for(String node:kquery.keyword2deweylist.get(indexWord))
		{
			resultB.add(node);
			//start lookup
			for(String keyword: kquery.keyword2deweylist.keySet())
			{
				if(!keyword.equalsIgnoreCase(indexWord))
				{
					//B=get_slca(B,Si);
					resultB = getSlca(resultB,kquery.keyword2deweylist.get(keyword));
										
				}
			}
			if(nodeV!=null)
			{
				if(nodeV.startsWith(resultB.get(0)))
				{
					resultB.remove(0);
				}
				
				else if(!resultB.get(0).startsWith(nodeV))
				{
					resultList.add(nodeV);					
				}
			}
			if(!resultB.isEmpty())
			{
				nodeV=resultB.remove(resultB.size()-1);					
			}
			
			resultB.clear();
			
		}
		
		resultList.add(nodeV);
			

	}

	@Override
	public void PrintResults() {
		// TODO Auto-generated method stub
		// record the number of checked nodes
				SLCAResults.println("The number of checked nodes is: "
						+ _numberofchecked);

				// from _resultheap and _resultmonitor
				SLCAResults.println("SLCA results as follow. ");
				System.out.println("SLCA results as follow");

				for (String result : resultList) {

					SLCAResults.println("SLCA result: " + result);
					System.out.println("SLCA result: " + result);
				}

				SLCAResults.println();
				SLCAResults.println();
			    SLCAResults.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	}

	private List<String> getSlca(List<String> resultB, List<String>  sList)
	{
		if(resultB==null)
		{
			return null;
		}
		List<String> result = new LinkedList<String>();
		String u = "";
		String x="";
		
		for(String v : resultB)
		{
			int rm=getRM(v,sList);
			String lmNode=null;
			String rmNode=null;
			if(rm>0)
			{
				rmNode = sList.get(rm);
				lmNode = sList.get(rm-1);
			}
			else if(rm==0)
			{
				rmNode = sList.get(rm);
				lmNode=rmNode;
			}
			else
			{
				lmNode=sList.get(sList.size()-1);
				rmNode=lmNode;
			}
			
			if(rmNode.equalsIgnoreCase(v))
			{
				lmNode=rmNode;
			}
			x=getDescendant(getLCA(v,lmNode),getLCA(v,rmNode));
			
			if(u.length()==0 || Helper.compareDewey(u, x)<=0)
			{
				if(!x.startsWith(u))
				{
					result.add(u);
				}
				u=x;
			}
			
		}
		result.add(u);
		return result;	
	}
	
	private String getLCA(String node1, String node2)
	{
		String lca = null;
		
		String[] deweyList1 = node1.split("[.]");
		String[] deweyList2 = node2.split("[.]");
		int min = (deweyList1.length>deweyList2.length?deweyList2.length:deweyList1.length);
		
		for (int i = 0; i < min; i++) {
			if(deweyList1[i].equalsIgnoreCase(deweyList2[i]))
			{
				if(lca==null)
				{
					lca=deweyList1[i];
				}
				else
				{
					lca=lca+"."+deweyList1[i];
				}
			}
			else
			{
				return lca;
			}
		}
		
		return lca;
	}
	
	private String getDescendant(String node1, String node2)
	{
			
		if(node1.length()>node2.length())
		{
			return node1;
		}
		else
		{
			return node2;
		}
		
	}
	// need change 
	private int getRM(String node, List<String>  sList)
	{
		
		int pos = 0;
		
		for(String s :sList)
		{
			if(Helper.compareDewey(s,node)>=0)
			{
				return pos;
						
			}
			pos++;
		}
		
		return -1;
		
		/*
		int pos = 0;
		int half=(int)sList.size()/2+1;
		if(Helper.compareDewey(sList.get(half),node)>0)
		{			
			return getRM(node,sList.subList(0,half));						
		}
		else if (Helper.compareDewey(sList.get(half),node)==0)
		{
			return half;
		}
		else
		{
			if(sList.size()>half+1)
			{
				return half+getRM(node,sList.subList(half+1,sList.size()-1));
			}
			else
			{
				return half;
			}
		}
		*/
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
		PrintWriter outStream;
		
			outStream = new PrintWriter(new BufferedWriter(
					new FileWriter(new File("./out/testIndex.log"))));
	

		String databaseName = PropertyReader.getProperty("dbname");
		JdbcImplement.ConnectToDB(databaseName);

		
		List<String> refinedkeywords = new LinkedList<String>();
		refinedkeywords.add("company");
		refinedkeywords.add("cook");
		refinedkeywords.add("australia");
		
		KeywordQuery kquery = new KeywordQuery(refinedkeywords);
		kquery.LoadAllInformation();
		SLCAEvaluation mytest = new IndexbasedEvaluation(outStream,refinedkeywords,"australia");
		mytest.computeSLCA(kquery);
	
		mytest.PrintResults();
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
