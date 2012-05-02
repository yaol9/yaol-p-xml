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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import com.db.DBHelper;
import com.tools.Helper;
import com.tools.PropertyReader;
import com.tools.TimeRecorder;

public class TestShareEager implements TestCase {

	private HashMap<Integer, List<String>> userQuery;
	
	private int queryCount=0;
	
	private HashMap<String,Integer> resultSize;
	
	private HashMap<Integer,List<HashMap<Integer,List<String>>>> planSet;
	
	private HashMap<String,Double> planScoreRecord;
	
	private ShareFactorManager sfm;
	
	private HashMap<Integer,Integer> combinationRecorder ;
	
	private List<HashMap<Integer,Integer>> combinationPool ;
	
	
	
	TestShareEager()
	{
		userQuery = new HashMap<Integer, List<String>>();
		resultSize = new HashMap<String,Integer>();		
		planSet=new HashMap<Integer,List<HashMap<Integer,List<String>>>>();
		planScoreRecord=new HashMap<String,Double>();
		sfm=new ShareFactorManager();
		combinationRecorder= new HashMap<Integer,Integer>();
		combinationPool=new LinkedList<HashMap<Integer,Integer>>();
	}
	
	@Override
	public long run() {
		// TODO Auto-generated method stub	
		try {
			
			String databaseName = PropertyReader.getProperty("dbname");
			DBHelper.ConnectToDB(databaseName);
			
			
			PrintWriter outStream = new PrintWriter(new BufferedWriter(
					new FileWriter(new File(PropertyReader
							.getProperty("ShareEagerAlgorithmResult")))));

			//warm up
		//	runSingle(outStream);
			
			TimeRecorder.startRecord();
			// run 5 times
			for (int i = 0; i < 1; i++) {
				runSingle(outStream);
			}

			TimeRecorder.stopRecord();
			System.gc();
			long qtime = TimeRecorder.getTimeRecord();

			
			
			// get memory usage
			long usagememory = Helper.getMemoryUsage();

			outStream.println("ShareEager Algorithms:");
			System.out.println("ShareEager Algorithms:");
			outStream.printf("--" + "Response Time: %d \n", qtime);
			outStream.println();
			System.out.printf("--" + "Response Time: %d \n", qtime);
			outStream.printf("--" + "Memory usage: %d \n", usagememory);
			outStream.println();
			System.out.printf("--" + "Memory usage: %d \n", usagememory);

			
			DBHelper.DisconnectDB();

			outStream.close();
			System.out.println("====================>>> Stop application!");
			
			return qtime;

		} catch (IOException e) {
			e.printStackTrace();
		}
			return 0;
	}

	@Override
	public void runSingle(PrintWriter outStream) {
		
		//load query to userQuery
		loadQuery(outStream);
		
		//load size log to resultSize
		loadSizeLog();
		
		//generate share factor		
		
		generateShareFactor();
		
		//generate intra-query plan
		for(int i=0;i<queryCount;i++)
		{						
			HashMap<Integer,List<String>> plan = new HashMap<Integer,List<String>>();
			generatePlan(userQuery.get(i),0,userQuery.get(i).size(),plan,0,i);			
		}

		//check plan			
	//	checkPlan();
		
		
		//generate inter-plan
				
		//init combination recorder
		for(int i=0;i<queryCount;i++)
		{	
			combinationRecorder.put(i,0); 
		}
		
		//get next combination
		HashMap<Integer,Integer> nextComb =	getNextCombination();
		
		
	}

	private HashMap<Integer, Integer> getNextCombination() {
		
		HashMap<Integer, Integer> nextComb = new HashMap<Integer, Integer> ();
		if(combinationPool.size() == 0)
		{
			for(int i=0;i<queryCount;i++)
			{	
				nextComb.put(i,0); 
			}
		}
		else
		{
			//select lowest from pool
			
			//add new one to pool
			
			
		}
		
		return nextComb;
	}

	private void generateShareFactor() {
		for (int i = 0; i < queryCount; i++)
		{
			for (int j = queryCount - 1; j > i; j--)
			{
				List<String> tempJoint = Helper.getMaxJointStringList(userQuery.get(i),userQuery.get(j));
				
				if ( (!tempJoint.isEmpty()) && (tempJoint.size()>1) )
				{
					//this is a share factor
					
					ShareFactor sf = new ShareFactor(tempJoint);
										
					if(sfm.isShareFactorExist(sf))
					{
						sfm.findShareFactor(sf).involvedQuery.add(i);
						sfm.findShareFactor(sf).involvedQuery.add(j);

					}					
					else
					{
						sf.involvedQuery.add(i);
						sf.involvedQuery.add(j);						
						sfm.addShareFactor(sf);
					}
					
					
				}
			}		
		}
			
		//check sharefactor
		for(ShareFactor sf : sfm.sfList)
		{
			System.out.println("sf: " + sf.items.toString()+"  maxCount: " + sf.getMaxShareCount());
		}
		
	}

	private void checkPlan() {
		try {
			
		String fileName="./testresult/intraPlanForQuery.log";
		PrintWriter outStreamT = new PrintWriter(new BufferedWriter(
					new FileWriter(new File(fileName))));
				
		for(int i=0;i<queryCount;i++)
		{	
				
			outStreamT.println("for query "+i+", Plans:");
			
			List<HashMap<Integer,List<String>>> plans=planSet.get(i);
			
						
			for(int j=0;j<plans.size();j++)
			{				
				
				outStreamT.println(j+":");
				for(int k=0;k<userQuery.get(i).size()-1;k++)
				{
					outStreamT.println("Step "+k+": "+plans.get(j).get(k));					
				}
				
				outStreamT.println("score: "+planScoreRecord.get(plans.get(j).toString()));					
								
			}
			
		}		
		outStreamT.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void generatePlan(List<String> query,int sequenceId,int keywordsCount,HashMap<Integer,List<String>> plan,int planScore,int queryId)
	{
		if(sequenceId==keywordsCount-1)
		{
			//output plan
			if(planSet.containsKey(queryId))
			{
				//need sort
				List<HashMap<Integer,List<String>>> plans=planSet.get(queryId);
				
				
				Double curScore = planScoreRecord.get(plan.toString());
				Double firstScore = planScoreRecord.get(plans.get(0).toString());
				Double lastScore = planScoreRecord.get(plans.get(plans.size()-1).toString());
				if( curScore<=firstScore)
				{
					plans.add(0,plan);
				}
				else if(curScore>lastScore)
				{
					plans.add(plan);
				}
				else
				{
					for(int i=0;i<plans.size()-2;i++)
					{
						Double preScore=planScoreRecord.get(plans.get(i).toString());
						Double postScore=planScoreRecord.get(plans.get(i+1).toString());
						
						if( (curScore>preScore) && (curScore <=postScore) )
						{
							plans.add(i+1,plan);
							break;
						}	
						
					
					}
				}	
				
				planSet.put(queryId, plans);
		//		System.out.println("for query "+queryId+", Plan:");
				
		//		for(int i=0;i<sequenceId;i++)
		//		{
				//	System.out.println("Step "+i+": "+plan.get(i));					
		//		}
				
			//	System.out.println("score: "+planScoreRecord.get(plan.toString()));	
			}
			else
			{
				List<HashMap<Integer,List<String>>> plans=new LinkedList<HashMap<Integer,List<String>>>();
				plans.add(plan);
				planSet.put(queryId, plans);
				
				
	//			System.out.println("for query "+queryId+", Plan:");
				
		//		for(int i=0;i<sequenceId;i++)
		//		{
		//			System.out.println("Step "+i+": "+plan.get(i));					
		//		}
			//	System.out.println("score: "+planScoreRecord.get(plan.toString()));	
				
			}
		}
		else
		{
			
			//local process log
			HashSet<String> localLog=new HashSet<String>();
			
			for(String s1:query)
			{
				for(String s2:query)
				{
					if(!s1.equalsIgnoreCase(s2))
					{
						
						if( (!localLog.contains(s2+s1)) && (!localLog.contains(s1+s2)) )
						{
							localLog.add(s1+s2);
							
							List<String> itemsForCal = new LinkedList<String>();
							itemsForCal.add(s1);
							itemsForCal.add(s2);
							
							
							List<String>mixList = new LinkedList<String>();
							if(s1.contains("|"))
							{
								String[] s=s1.split("[|]");
								for(String ss:s)
								{
									mixList.add(ss);
								}
							}
							else
							{
								mixList.add(s1);
							}
							
							if(s2.contains("|"))
							{
								String[] s=s2.split("[|]");
								for(String ss:s)
								{
									mixList.add(ss);
								}
							}
							else
							{
								mixList.add(s2);
							}
							String mix = Helper.getMixString(mixList);
				
							List<String> copyQuery = new LinkedList<String>();
							for(String s:query)
							{
								copyQuery.add(s);
							}
							copyQuery.add(mix);
							copyQuery.remove(s1);
							copyQuery.remove(s2);
							
							//get pre score
							Double preScore = 0.0;
							if(planScoreRecord.containsKey(plan.toString()))
							{
								preScore=planScoreRecord.get(plan.toString());
								
							}
							
							//create new plan , add new process to plan
							HashMap<Integer,List<String>> newPlan = new HashMap<Integer,List<String>>();
							for(Integer p : plan.keySet())
							{
								newPlan.put(p, plan.get(p));
							}
							newPlan.put(sequenceId, itemsForCal);
							
							
							//calculate score						
							//check if sharefactor
							ShareFactor sf = sfm.getExistFromFullOrPartList(mixList);
							if(sf!=null)
							{
								int maxShare = sf.getMaxShareCount();
								
								preScore += ( resultSize.get(s1)+resultSize.get(s2)) / maxShare;
							}
							else
							{
								preScore += resultSize.get(s1)+resultSize.get(s2);
							}
							
							
													
							planScoreRecord.put(newPlan.toString(),preScore);
							
			//				System.out.println(copyQuery);
							generatePlan(copyQuery, sequenceId+1,keywordsCount, newPlan,planScore,queryId);
							
						}
						
					}
				}
			}
			
		}
	
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

	private void loadQuery(PrintWriter outStream) {
		try {
			
		
		String ksFile = PropertyReader.getProperty("ksFile");

		BufferedReader queryRead = new BufferedReader(
				new InputStreamReader(new DataInputStream(
						new FileInputStream(ksFile))));

		String query = "";
	
		int counter = 0;

		while ((query = queryRead.readLine()) != null) {
			outStream.printf("-- " + "Keyword Query: %s \n", query);
			outStream.println();
	//		System.out.println("-- " + "Keyword Query: "+query);
						
			if(!query.startsWith("#"))
			{
				List<String> refinedkeywords = Helper.getRefinedKeywords(query);
				userQuery.put(counter, refinedkeywords);
				counter++;
			}		
			
		}
		
		queryCount=counter;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TestCase test = new TestShareEager();
		test.run();
	}

}
