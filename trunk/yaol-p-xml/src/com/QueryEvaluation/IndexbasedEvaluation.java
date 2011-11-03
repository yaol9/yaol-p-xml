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
import com.test.MyTest;
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
	
		
		String nodeV="";
		List<String> resultB = new LinkedList<String>();
		
		for(String node:kquery.keyword2deweylist.get(indexWord))
		{
			//start lookup
			for(String keyword: kquery.keyword2deweylist.keySet())
			{
				if(!keyword.equalsIgnoreCase(indexWord));
				{
					//B=get_slca(B,Si);
					resultB = getSlca(resultB,kquery.keyword2deweylist.get(keyword));
					
				}
			}
			System.out.println(indexWord+"+"+node);
		}
		
		
		
			

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
		for(String node : resultB)
		{
			
			
			
		}
		return null;	
	}
	
	private String getLCA(String node1, String node2)
	{
		String lca="";
		
		
		
		return lca;
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
		refinedkeywords.add("orange");
		refinedkeywords.add("apple");
		
		KeywordQuery kquery = new KeywordQuery(refinedkeywords);
		kquery.LoadAllInformation();
		SLCAEvaluation mytest = new IndexbasedEvaluation(outStream,refinedkeywords,"orange");
		mytest.computeSLCA(kquery);
	
		mytest.PrintResults();
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
