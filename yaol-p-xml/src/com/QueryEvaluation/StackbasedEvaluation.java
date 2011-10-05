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
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Arrays;

import java.util.HashMap;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import java.util.Set;
import java.util.Stack;

import com.myjdbc.JdbcImplement;
import com.mysax2parser2.TokenPreprocessor;
import com.tools.PropertyReader;

public class StackbasedEvaluation {


	public final HashMap<String, HashMap<String, Double>> _hashMap; 
	//public final Map<Integer, Hashtable<Integer, String>> _arrayList;
	public final String _selectDeweySql = "select dewey from KeywordDewey where keyword=";
	//public final String _selectPrDeweySql = "select prdewey from PrDeweyDict where dewey=";
	private static int TOPK;
	//private static final String[] EMPTY_STRING_ARRAY = new String[0];
	private static PrintWriter outEagerBUSJoinResults; 
	
	private static String LABEL_FULL_KEYWORDS;
	private static double[] _resultmonitor;
	private static HashMap<String, Double> _resultheap;
	//private static Arrays _resultdeweyheap;
	
	private static HashMap<String, LinkedList<String>> _keyword2deweylist; //keyword - IL 
	private static HashMap<String, String> _dewey2conprobmap;

	
	
	Map<String, Integer> _PointerOfSmallNodes;
	
	private static int _totalnumberofresults;
	
	private static int _numberofchecked;
	
	public StackbasedEvaluation(PrintWriter outStream, List<String> keywords, int k){
		outEagerBUSJoinResults = outStream;
		
		_totalnumberofresults = 0;
		_numberofchecked = 0;
		
		//create a hash map to maintain distribution rates of keywords for
		//each encoded node
		
		_hashMap = new HashMap<String, HashMap<String, Double>>();
	
		//create a type of array list to store the level2hash index
		//_arrayList = new HashMap<Integer, Hashtable<Integer, String>>();
		
		TOPK = k;
		_resultheap = new HashMap<String, Double>();
		_resultmonitor = new double[TOPK];

		
		_keyword2deweylist = new HashMap<String, LinkedList<String>>();
		_dewey2conprobmap = new HashMap<String, String>();
		
		_PointerOfSmallNodes = new HashMap<String, Integer>();
		for(int i=0; i<keywords.size(); i++){
			_PointerOfSmallNodes.put(keywords.get(i), 0);
		}
		
	}
	
	/*
	 * For a keyword, we load its relevant dewey code into
	 *  a ArrayList or LinkedList. At the same time, we 
	 *  retrieve the prdewey for each dewey and cache it into
	 *  a deweytopr map. In addition, we also need to cache the
	 *  local distribution of the dewey into _hashMap.
	 *  
	 *  After we do the procedure for all keywords, we can make the 
	 *  preparation for the second algorithm.
	 */
	public int LoadKeywordNodes(String keyword, String indexOfHashmap){
		
			String deweysql = _selectDeweySql + "'" + keyword + "'";
				
			ResultSet deweySet = JdbcImplement.performQuery(deweysql);
			int count = 0;
			if (deweySet != null){
				try {
					LinkedList<String> mylist = new LinkedList<String>();
					while (deweySet.next()){
						String dewey = deweySet.getString("dewey");
						dewey = dewey.trim();
						
						//write dewey into keyword2deweylist						
						if(_keyword2deweylist.containsKey(keyword)){
							mylist = _keyword2deweylist.get(keyword);
							
						}else{							
							mylist = new LinkedList<String>();
							_keyword2deweylist.put(keyword, mylist);
						}				
					
												
						if(!mylist.contains(dewey)){
							mylist = insertsortedlist(mylist, dewey);
						}
						
						count++;					
						
					
						//write dewey and its distribution into _hashMap
						if(!_hashMap.containsKey(dewey)){
							
							HashMap<String, Double> myDist = new HashMap<String, Double>();
							
							myDist.put(indexOfHashmap, 1.0);
							
							_hashMap.put(dewey, myDist);
						}else{
							HashMap<String, Double> myDist = 
								_hashMap.remove(dewey);
							
							//scan myDist to merge with indexOfHashmap
							//if a leaf node v contains k1, k2, then its dist only contains
							//11 for a query {k1, k2}
							Set<String> keyDist = myDist.keySet();
													
							HashMap<String, Double> newDist = new HashMap<String, Double>();
							int myindex = Integer.parseInt(indexOfHashmap,2);
							for(String mykey:keyDist){
								
								//newDist.put(mykey, myDist.get(mykey));
								myindex = myindex + Integer.parseInt(mykey,2);
								String newkey = Integer.toBinaryString(myindex);
								double newvalue = myDist.get(mykey) * 1.0;
								//we can allow the distribution as lower than 1.0 in the future.
								
								newDist.put(newkey, newvalue);
							}
							//newDist.put(indexOfHashmap, 1.0);
							//_hashMap.remove(dewey);
							_hashMap.put(dewey,newDist);
							
						//	PrintHashMap(newDist);
						}
						
						
						
					}
					//_keyword2deweylist.put(keyword, mylist);
					
					//test the order of mydewey before sorting
		/*			LinkedList<String> mylist1 = _keyword2deweylist.get(keyword);
					
					outEagerBUSJoinResults.println("No sorting of mylist:\n");
					outEagerBUSJoinResults.println(keyword + " nodes: " + mylist1.size());
					for(int j=0; j<mylist1.size(); j++){
						
						outEagerBUSJoinResults.println(mylist1.get(j));
						System.out.println(mylist1.get(j));
					}
					outEagerBUSJoinResults.println("\n");
				*/		
					
					outEagerBUSJoinResults.println("Keyword Size " + keyword +" -> number: " +
							mylist.size() + "\n");
					
					if(mylist.size()==0){
						System.out.println("-- Error happened: \n --Keyword Size " + keyword +" -> number: " +
							mylist.size() + "\n");
						System.exit(-1);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}			
			}else{
				
				System.exit(-1);
			}			
			
			return count;
	}
	
	
public LinkedList<String> insertsortedlist(LinkedList<String> list, String insertstr){
		
		
		
		//due the I and M, we have to scan the whole list to insert
		//because we only compare the number of the string
		if(list.size()==0){
			list.add(insertstr);
			
		}else{
			String pureinsertstr = insertstr.replaceAll("I", "");
			pureinsertstr = pureinsertstr.replaceAll("M", "");
			
			int index = -1;
			for(int i=0; i<list.size(); i++){
				String node = list.get(i);
				
				String purenode = node.replaceAll("I", "");
				purenode = purenode.replaceAll("M", "");
				
				//compare the part with the same length
				
				
				
				
				if(pureinsertstr.compareToIgnoreCase(purenode)>0){
					//continue
				}else{
					//return the current index
					index += i + 1;
					break;
				}
			}
			
			if(index == -1){
				
				//insert it into the end of the list
				list.add(list.size(), insertstr);
			}else{
				//insert it into the index position of the list
				list.add(index, insertstr);
			}
		}
		
	//	PrintList(list);
		return list;
	}

	/* 
	 * load dewey codes for keywords, load prdewey codes for keywords,
	 * load distributions to hash map, return the keyword with the 
	 * minimal keyword nodes.
	 */
	public String LoadInformation(List<String> keywords){
		
		//generate a set of binary code based on keywords.size()
		int numOfKeywords = keywords.size();
		int maxNum = (int) Math.pow(2, numOfKeywords)-1;
		
		//System.out.println(maxNum);
		String binaryex = Integer.toBinaryString(maxNum);
		LABEL_FULL_KEYWORDS = binaryex;
		//System.out.println(binaryex);
		
		int deweysize = 0;
		String minkeyword = null;
		for (int i=0; i<keywords.size(); i++){
			//remove null from the refined keywords	
			if(keywords.get(i) != null){
				//construct binary index of hash map
				String indexOfHashmap = "";
				for(int myi=0; myi<i; myi++){
					indexOfHashmap += '0';
				}
				indexOfHashmap = '1' + indexOfHashmap;
				
				String keyword = keywords.get(i).trim();
				int returnsize = LoadKeywordNodes(keyword, indexOfHashmap);
				if(minkeyword==null){
					deweysize = returnsize;
					minkeyword = keyword;
				}else if(returnsize < deweysize){
					deweysize = returnsize;
					minkeyword = keyword;
				}
			}
		}
		
		return minkeyword;
	}
	
	
	
	/*
	 * Given a set of slca candidates, we calculate their probabilities based on
	 * their relevant keyword nodes.
	 */
	/*
	 * @input a slca candidate v
	 * @output the full distributions of v, and the current keyword node lists
	 * @the output has been adjusted in _hashMap and _keyword2deweylist
	 */
	
	public void ComputeDist(){
		
	
		
		//scan keyword nodes and compute dist of v
		
		//the nodes that are the descendants of v are needed to be explored
		//once the nodes are explored, they will be removed from the keyword node lists
	
		//get a smallest node v
		String v = GetNextNode();
		if(v.contains(".")){
			String[] vcomponents = v.split("[.]");
			Stack<String> vstack = new Stack<String>();
		
			int stacksize = vcomponents.length;
			for(int i=0; i<stacksize; i++){
				vstack.push(vcomponents[i]);
			}
		
			do{
			
				//check if the list can be reduced automatically
				String leftmostnode = GetNextNode();
				
				if(_hashMap.containsKey(leftmostnode)){
				String[] nextcomponents = leftmostnode.split("[.]");
				
				for(int i=0; i<nextcomponents.length; i++){
					

					if(i < vstack.size()){
						
						if(nextcomponents[i].compareToIgnoreCase(vstack.get(i))!=0){
							
							
							//first pop vstack.get(i) from vstack, then promote its dist
							//to its parent and then write it into _hashMap
							while(vstack.size()>i){
								
								//record the checked node number
								_numberofchecked++;
								
															
								//get the current dewey from stack
								String currdewey = GetDewey(vstack);
								
								//test
								//System.out.println(currdewey);
								
								String conproblink = _dewey2conprobmap.remove(currdewey);
								
								
								//test
								//System.out.println(conproblink);
								double conditionalprob=0; 
								String restproblink = null;
								/*
								
								
								if(conproblink.contains("-")){
									int lastdash = conproblink.lastIndexOf("-");
									
									restproblink = conproblink.substring(0, lastdash);
									String tail = conproblink.substring(lastdash+1);
									
									
									conditionalprob = Double.parseDouble(tail);
									
								}else{
									
									conditionalprob = Double.parseDouble(conproblink);
									
								}
								*/
								
								String currtail = vstack.pop();
								
								if((!currtail.contains("M")&&(!currtail.contains("I")))){
									
									GenerateResults(currdewey, conproblink);
									
								}
								
								//get top of vstack
								String tail = vstack.peek();
								String type ="";
								
								//analyze the type of upper dewey
								if(tail.contains("M")){
									type = "M";
								}else if(tail.contains("I")){
									type = "I";
								}else{
									type = "";
								}
								
								
								//get upper dewey from stack
								int lastdotindex = currdewey.lastIndexOf(".");
								//String upperdewey = GetDewey(vstack);
								String upperdewey = currdewey.substring(0, lastdotindex);
								
								//promote dist from current dewey to the upper dewey
								
								if(_hashMap.containsKey(upperdewey)){
									
									//combine prob
									CombineProb(currdewey, type, conditionalprob, upperdewey);
								}else{
									
									//direct promotion
									DirectPromotion(currdewey, conditionalprob, restproblink, upperdewey);
								}
								
							}
							
							//push the rest components into stack
							//vstack.push(nextcomponents[i]);
							i--;
						}
						//else do nothing
					}else{
						vstack.push(nextcomponents[i]);
					}	
				}
				
				}
			}while(!_PointerOfSmallNodes.isEmpty());
			//it says we completely process all the keyword nodes
			
			String currdewey = null;
			if(!vstack.isEmpty()){
				//get the current dewey from stack
				currdewey = GetDewey(vstack);
			}
			
			while(!vstack.isEmpty()){			
				if(currdewey.contains(".")){
					
					//record the number of checked nodes
					_numberofchecked++;
					
					
					
					//currdewey = GetDewey(vstack);			
				
					String conproblink = _dewey2conprobmap.remove(currdewey);
					
					double conditionalprob; 
					String restproblink = null;
					
					if(conproblink.contains("-")){
						int lastdash = conproblink.lastIndexOf("-");
						
						restproblink = conproblink.substring(0, lastdash);
						String tail = conproblink.substring(lastdash+1);
						
						
						conditionalprob = Double.parseDouble(tail);
						
					}else{
						
						conditionalprob = Double.parseDouble(conproblink);
						
					}
					
					
					String currtail = vstack.pop();
					
					if((!currtail.contains("M")&&(!currtail.contains("I")))){
						
						GenerateResults(currdewey, conproblink);
						
					}
					
					//get top of vstack
					String tail = vstack.peek();
					String type ="";
					
					//analyze the type of upper dewey
					if(tail.contains("M")){
						type = "M";
					}else if(tail.contains("I")){
						type = "I";
					}else{
						type = "";
					}
					
					
					//get upper dewey from stack
					int lastdotindex = currdewey.lastIndexOf(".");
					//String upperdewey = GetDewey(vstack);
					String upperdewey = currdewey.substring(0, lastdotindex);
					
					//promote dist from current dewey to the upper dewey
					
					if(_hashMap.containsKey(upperdewey)){
						
						//combine prob
						CombineProb(currdewey, type, conditionalprob, upperdewey);
					}else{
						
						//direct promotion
						DirectPromotion(currdewey, conditionalprob, restproblink, upperdewey);
					}
					
					//promote currdewey to upper level
					
					lastdotindex = currdewey.lastIndexOf(".");
					currdewey = currdewey.substring(0, lastdotindex);
				}else{
					break;
				}	
				
					
			}
			
		}
			
		//stop search	
	
	}
	
	
	
	//get the leftmost keyword node for v and start to process
	//search the relevant keyword nodes in the range, 
	//for v=1.2.2, we search 1.2.2.* - 1.2.3 like ELCA
	
	//get the startindex of each keyword node list
	public String GetNextNode(){
		
		String selectkeyword = null;
		String selectnode = null;
		Set<String> keyset = _PointerOfSmallNodes.keySet();
		for(String key:keyset){
			
			//double check v// _keyword2deweylist.get(key).get(
			//_PointerOfSmallNodes.get(key))
			List<String> list =  _keyword2deweylist.get(key);
			
			
			//how can we do when a node list has been scanned completely
			//we need to continue to scan the other lists to the end
			int index = _PointerOfSmallNodes.get(key);
			String node = list.get(index);
			
			
			if(selectnode == null){
				selectnode = node;
				selectkeyword = key;
			}else if(selectnode.compareToIgnoreCase(node)>0){
					selectnode = node;
					selectkeyword = key;					
			}
			
		}
		
		
		int index = _PointerOfSmallNodes.remove(selectkeyword);
		List<String> list =  _keyword2deweylist.get(selectkeyword);
		//check next node at the next time
		index++;
		if(list.size()> index){
		
			_PointerOfSmallNodes.put(selectkeyword, index);	
		}else{
			_PointerOfSmallNodes.remove(selectkeyword);
		}
		
		return selectnode;
	}
	
	/*
	 * transform the components in stack into a dewey code
	 */
	public String GetDewey(Stack<String> stack){
		
		//scan the stack from bottom to up
		String dewey = null;
		for(int i=0; i<stack.size(); i++){
			
			if(dewey == null){
				dewey = stack.get(i);
			}else{
				dewey = dewey + "." + stack.get(i);
			}
			
		}
		
		return dewey;
	}
	
	
	
	//we may generate result here if the dewey contains full keyword distribution
	public void  GenerateResults(String dewey, String problink){
		
		
		HashMap<String, Double> dist = _hashMap.remove(dewey); 
		
		
		if(dist.containsKey(LABEL_FULL_KEYWORDS)){
			
			
			//record the total number
			_totalnumberofresults++;
			
			
			double localprob = dist.get(LABEL_FULL_KEYWORDS);
			//if dist contains LABEL_FULL_KEYWORDS, localprob should not be zero, 
			//otherwise, we don't cache it into dist. Therefore, we don't need to
			//check the if sentence.
			//if(localprob != 0){
			//	//...
			//}
			
			//compute the probability of path from root to the dewey, i.e.,
			//multiply the conditional probabilities in prob
			//double conprobresult = 1;
			String[] conprobset = problink.split("[-]");
			for(int i=0; i<conprobset.length; i++){
				String conprobstr = conprobset[i].trim();
				double conprob = Double.parseDouble(conprobstr);
				localprob = localprob * conprob;
			}
			
			//from this point, localprob has become the global probability
			
					
			//write it into result heap if conprobresult is larger than that of k-th
			//probability
			
			if(_resultheap.size() < TOPK){
				
				_resultheap.put(dewey, localprob);
				
				//add the new localprob and sort
				int index = _resultheap.size()-1;
				
				_resultmonitor[index] = localprob;
				
				//@@@@@@@@@@@@@@@@@@@ error happened here
				
				if(_resultheap.size() == TOPK){
					Arrays.sort(_resultmonitor);
				}
				
			}else if(localprob > _resultmonitor[0]){
					
				//scan _resultheap and
				//remove one candidate with _resultmonitor[0] from _resultheap
				
				Set<String> heapkeys = _resultheap.keySet();
				for(String heapkey:heapkeys){
					if(_resultheap.get(heapkey)==_resultmonitor[0]){
						_resultheap.remove(heapkey);
						
						//we only delete one candidate at each time
						break;
					}
				}
				_resultheap.put(dewey, localprob);
				
				//update the monitor
				_resultmonitor[0] = localprob;
				Arrays.sort(_resultmonitor);
			}
			
					
			dist.remove(LABEL_FULL_KEYWORDS);
			
		}
		
		_hashMap.put(dewey, dist);
	}
	
	//directly promote dist, localprob to dewey
	public void DirectPromotion
	(String dewey, double conditionalprob, String upperproblink, String upperdewey){
		
		HashMap<String, Double> dist = _hashMap.remove(dewey); 
	
		//let the "0" to be applicable.
		if(!dist.containsKey("0")){				
			dist.put("0",0.0);
		}
			
		Set<String> keys = dist.keySet();
		for(String key:keys){
				
			double value = dist.get(key)*conditionalprob;	
			if(key.compareToIgnoreCase("0")==0){
				value += 1-conditionalprob;
			}
			if(value != 0.0){
				dist.put(key, value);
			}
		}
			
		if(dist.get("0") == 0.0){
			dist.remove("0");
		}
			
		_hashMap.put(upperdewey, dist);	
		_dewey2conprobmap.put(upperdewey, upperproblink);
	}
	
	
	
	
	// we adopt different strategies for types M and I
	public void CombineProb
	(String currdewey, String type, double conditionalprob, String upperdewey){
	
		HashMap<String, Double> dist = _hashMap.remove(currdewey);
		
		HashMap<String, Double> targetDist =  _hashMap.remove(upperdewey);
		
		
		//type == "M" can not make the correct judgement.
		
		if(type.compareToIgnoreCase("M")==0){
				
			
			//put all distributions of dist to targetDist
			Set<String> keys = dist.keySet();
			for(String key:keys){
				
				double value = dist.get(key)*conditionalprob;				
				
				if(targetDist.containsKey(key)){
					value += targetDist.remove(key); 
					//targetDist.remove(key);
				}
				targetDist.put(key, value);					
			}
			
			//check zero value
			//if newTargetDist.get("0") is null, it is error
			//copy targetDist.get("0") to newTargetDist.get("0")
			if(targetDist.containsKey("0")){
				double value = targetDist.remove("0")-conditionalprob;
				//targetDist.remove("0");
				targetDist.put("0", value);
			}else{
				
				System.out.println("Error happened in CombineProb for M type of nodes.");
			}
						
			_hashMap.put(upperdewey, targetDist);
			
			// ????check the duplicate elements in _hashMap
			
			//test
			//testkeys = targetDist.keySet();
			//for(String key:testkeys){
				
			//	System.out.println("-- key: " + key + " -- prob: " + targetDist.get(key).toString());	
				
			//}
			
		}else {//if(type == "I"){
			
			HashMap<String, Double> newTargetDist = new HashMap<String, Double>();
			
			//let the "0" to be applicable.
			if(!dist.containsKey("0")){				
				dist.put("0",0.0);
			}
			
			
			Set<String> keys = dist.keySet();
						
			for(String key:keys){
				
				double value = dist.get(key)*conditionalprob;	
				if(key.compareToIgnoreCase("0")==0){
					value += 1-conditionalprob;
				}
				
				if(value != 0.0){
					Set<String> targetkeys = targetDist.keySet();
					for(String targetkey:targetkeys){
						
						double targetvalue = targetDist.get(targetkey);	
						double newtargetvalue = targetvalue*value;
						
						//bitwise or between key and targetkey
						int newindex = Integer.parseInt(key,2) | Integer.parseInt(targetkey,2);
						
						String newkey = Integer.toBinaryString(newindex);
						
						//write it into newTargetDist
						if(newTargetDist.containsKey(newkey)){
							newtargetvalue += newTargetDist.remove(newkey); 						
						}
						
						if(newtargetvalue != 0.0){
							//newTargetDist.remove(newkey);
							newTargetDist.put(newkey, newtargetvalue);		
						}
					}
				}
				
			}
			
			
			_hashMap.put(upperdewey, newTargetDist);
			// check the duplicate elements in _hashMap
			
			//test
			//System.out.println("-------- " + upperdewey + " ---------\n");
			//Set<String> mykeys = newTargetDist.keySet();
			
			//for(String key:mykeys){
			//	System.out.println("-- " + " " + key + ": " + newTargetDist.get(key).toString());
			//}
			//System.out.println();
			//System.out.println();
			
		//}else{
			
		}
	}
	
	
	
	
	
	public void PrintList(List mylist){
		
		Iterator ite = mylist.iterator();
		while(ite.hasNext()){
			
			System.out.println(ite.next().toString());
		}
	}
	
	public void PrintHashMap(HashMap hm){
		
		Set keyset = hm.keySet();
		for(Object key:keyset){
			
			System.out.println(hm.get(key).toString());
		}
	}
	

public void PrintResults(int k){
		
	//record the number of checked nodes
	outEagerBUSJoinResults.println("The number of checked nodes is: " +
			_numberofchecked);
	
	//record the total number of results
	outEagerBUSJoinResults.println("The total number of real results is: " + 
			_totalnumberofresults);
	
		// from _resultheap and _resultmonitor
	outEagerBUSJoinResults.printf("Top %d number of results as follow. \n", k); // _resultheap.size()
		System.out.printf("Top %d number of results as follow. \n", k);
		Set<String> heapkeys = _resultheap.keySet();
		for(String heapkey:heapkeys){
			
			outEagerBUSJoinResults.printf("-- Dewey code: %s	probability of SLCA: %f. \n", 
					heapkey, _resultheap.get(heapkey));
			System.out.printf("-- Dewey code: %s	probability of SLCA: %f. \n", 
					heapkey, _resultheap.get(heapkey));
		}
		
		outEagerBUSJoinResults.println();
		outEagerBUSJoinResults.println();
		outEagerBUSJoinResults.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	}

public static void main(String[] args){
		
		try {
			PrintWriter outStream = new PrintWriter(new BufferedWriter(new FileWriter(new File("./out/StackbasedEvaluation.log"))));
			//outEagerBUSJoinResults = outStream;
			
			String databaseName = PropertyReader.getProperty("dbname");
			JdbcImplement.ConnectToDB(databaseName);
						
			//read each keyword query from ks.txt
			//String curDir = System.getProperty("user.dir");
			//String ksFile = curDir + "/queries/ks.txt";			
			String ksFile = PropertyReader.getProperty("ksFile");
					
			BufferedReader queryRead = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(ksFile))));
			String query;			
			while ((query = queryRead.readLine()) != null) {
				String keywordSet[] = query.split("[,]");
				
				//clean the keyword query using stop words
				TokenPreprocessor thisPreprocessor = new TokenPreprocessor();
				keywordSet = thisPreprocessor.trimTokens(keywordSet);
				keywordSet = thisPreprocessor.stopWordRemoval(keywordSet);
				keywordSet = thisPreprocessor.removeIrrelevantTokens(keywordSet);				
				
				List<String> refinedkeywords = new LinkedList<String>();
				
				for(String item:keywordSet){
					if(item != null){
						refinedkeywords.add(item);
						System.out.println(item);
					}
				}
								
				refinedkeywords.toArray();
				System.out.println(refinedkeywords.size());
				//k specifies the number of required SLCA results
				int k = 6;
				
				//give a refined keyword query to load
				//the corresponding keyword nodes
				StackbasedEvaluation myEstimation = new StackbasedEvaluation
				(outStream, refinedkeywords, k);
				
				//load keyword node lists and do the required preparations
				//myEstimation.LoadInformation(refinedkeywords);
				//myEstimation.prepareBUSJ();
				
				//Start to estimate
				outEagerBUSJoinResults.printf("-- " + "Keyword Query: %s \n", query);
				outEagerBUSJoinResults.println();
				System.out.printf("-- " + "Keyword Query: %s \n", query);
				String minkeyword = myEstimation.LoadInformation(refinedkeywords);
				
				
				long start, qtime;
				start = System.currentTimeMillis();		
				
				//int[] indexOfLevels = myEstimation.prepareBUSJ();
				
				myEstimation.ComputeDist();
				
				qtime = System.currentTimeMillis() - start;
				
				
				//record memory usage
				Runtime rt = Runtime.getRuntime();
				long freememory = rt.freeMemory();
				long totalmemory = rt.totalMemory();
				long useagememory = totalmemory - freememory;
				
				
				
				outEagerBUSJoinResults.printf("--"+ "Response Time: %d \n", qtime);
				outEagerBUSJoinResults.println();
				System.out.printf("--"+ "Response Time: %d \n", qtime);
				outEagerBUSJoinResults.printf("--"+ "Memory usage: %d \n", useagememory);
				outEagerBUSJoinResults.println();
				System.out.printf("--"+ "Memory usage: %d \n", useagememory);
				
				myEstimation.PrintResults(k);
								
			}
			queryRead.close();
			JdbcImplement.DisconnectDB();
			
			outEagerBUSJoinResults.close();
			System.out.println("====================>>> Stop application!");
	
		}catch (IOException e) {		
			e.printStackTrace();
		}
	}

}
