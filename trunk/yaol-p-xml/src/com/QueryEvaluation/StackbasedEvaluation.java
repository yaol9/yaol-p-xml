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

import java.util.ArrayList;
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

	//public final HashMap<String, String> _hashMap;
	
	public final String _selectDeweySql = "select dewey from KeywordDewey where keyword=";
	
//	private static int TOPK;

	private static PrintWriter StackSLCAResults;
	private String curKeyword; // currently selected keyword

	private List<String> keywordList;

	private List<String> _resultheap;
	
	private static HashMap<String, LinkedList<String>> _keyword2deweylist; 

	Map<String, Integer> _PointerOfSmallNodes;

	private static int _totalnumberofresults;

	private static int _numberofchecked;

	public StackbasedEvaluation(PrintWriter outStream, List<String> keywords) {
		StackSLCAResults = outStream;

		_totalnumberofresults = 0;
		_numberofchecked = 0;

	//	TOPK = k;
		_resultheap = new ArrayList <String>();

		_keyword2deweylist = new HashMap<String, LinkedList<String>>();


		_PointerOfSmallNodes = new HashMap<String, Integer>();
		for (int i = 0; i < keywords.size(); i++) {
			_PointerOfSmallNodes.put(keywords.get(i), 0);
		}

		curKeyword = "";
		keywordList = keywords;

	}

	/*
	 * For a keyword, we load its relevant dewey code into a ArrayList or
	 * LinkedList. At the same time, we retrieve the prdewey for each dewey and
	 * cache it into a deweytopr map. In addition, we also need to cache the
	 * local distribution of the dewey into _hashMap.
	 * 
	 * After we do the procedure for all keywords, we can make the preparation
	 * for the second algorithm.
	 */
	public int LoadKeywordNodes(String keyword, String indexOfHashmap) {

		String deweysql = _selectDeweySql + "'" + keyword + "' order by XMLid ASC";

		ResultSet deweySet = JdbcImplement.performQuery(deweysql);
		int count = 0;
		if (deweySet != null) {
			try {
				LinkedList<String> mylist = new LinkedList<String>();
				while (deweySet.next()) {
					String dewey = deweySet.getString("dewey");
					dewey = dewey.trim();

					// write dewey into keyword2deweylist
					if (_keyword2deweylist.containsKey(keyword)) {
						mylist = _keyword2deweylist.get(keyword);

					} else {
						mylist = new LinkedList<String>();
						_keyword2deweylist.put(keyword, mylist);
					}

					if (!mylist.contains(dewey)) {
					//	System.out.println(keyword);
					//	PrintList(mylist);//yaol
					//	mylist = insertsortedlist(mylist, dewey);
						mylist.add(mylist.size(), dewey);
					}

					count++;					

				}

				StackSLCAResults.println("Keyword Size " + keyword
						+ " -> number: " + mylist.size() + "\n");

				if (mylist.size() == 0) {
					System.out.println("-- Error happened: \n --Keyword Size "
							+ keyword + " -> number: " + mylist.size() + "\n");
					System.exit(-1);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {

			System.exit(-1);
		}

		return count;
	}

	public LinkedList<String> insertsortedlist(LinkedList<String> list,
			String insertstr) {

		// due the I and M, we have to scan the whole list to insert
		// because we only compare the number of the string
		if (list.size() == 0) {
			list.add(insertstr);

		} else {
		//	String pureinsertstr = insertstr.replaceAll("I", "");
		//	pureinsertstr = pureinsertstr.replaceAll("M", "");

			int index = -1;
			for (int i = 0; i < list.size(); i++) {
				String node = list.get(i);

				//String purenode = node.replaceAll("I", "");
			//	purenode = purenode.replaceAll("M", "");

				// compare the part with the same length

				if (insertstr.compareToIgnoreCase(node) > 0) {
					// continue
				} else {
					// return the current index
					index += i + 1;
					break;
				}
			}

			if (index == -1) {

				// insert it into the end of the list
				list.add(list.size(), insertstr);
			} else {
				// insert it into the index position of the list
				list.add(index, insertstr);
			}
		}

		// PrintList(list);
		return list;
	}

	/*
	 * load dewey codes for keywords, load prdewey codes for keywords, load
	 * distributions to hash map, return the keyword with the minimal keyword
	 * nodes.
	 */
	public String LoadInformation(List<String> keywords) {

		int deweysize = 0;
		String minkeyword = null;
		for (int i = 0; i < keywords.size(); i++) {
			// remove null from the refined keywords
			if (keywords.get(i) != null) {
				// construct binary index of hash map
				String indexOfHashmap = "";
				for (int myi = 0; myi < i; myi++) {
					indexOfHashmap += '0';
				}
				indexOfHashmap = '1' + indexOfHashmap;

				String keyword = keywords.get(i).trim();
				int returnsize = LoadKeywordNodes(keyword, indexOfHashmap);
				if (minkeyword == null) {
					deweysize = returnsize;
					minkeyword = keyword;
				} else if (returnsize < deweysize) {
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
	 * 
	 * @output the full distributions of v, and the current keyword node lists
	 * 
	 * @the output has been adjusted in _hashMap and _keyword2deweylist
	 */

	public void computeSLCA() {

		// scan keyword nodes and compute dist of v

		// the nodes that are the descendants of v are needed to be explored
		// once the nodes are explored, they will be removed from the keyword
		// node lists

		// get a smallest node v

		String v = GetNextNode(); // get first node
		if (v.contains(".")) {
			String[] vcomponents = v.split("[.]");
			Stack<String> vstack = new Stack<String>();
			Stack<HashMap<String, Integer>> keyStack = new Stack<HashMap<String, Integer>>();
			HashMap<String, Integer> keywordMap = new HashMap<String, Integer>();

			int stacksize = vcomponents.length;
			for (int i = 0; i < stacksize; i++) {
				vstack.push(vcomponents[i]);
				keywordMap.put(curKeyword, 1);
				keyStack.push(keywordMap);
			}

			do {
				// check if the list can be reduced automatically
				String leftmostnode = GetNextNode();

				String[] nextcomponents = leftmostnode.split("[.]");

				for (int i = 0; i < nextcomponents.length; i++) {

					if (i < vstack.size()) {

						if (nextcomponents[i]
								.compareToIgnoreCase(vstack.get(i)) != 0) {

							// first pop vstack.get(i) from vstack, then
							// promote its dist
							// to its parent and then write it into _hashMap
							while (vstack.size() > i) {

								// record the checked node number
								_numberofchecked++;

								// get the current dewey from stack
								String currdewey = GetDewey(vstack);
								vstack.pop();
								HashMap<String, Integer> topKeywordStack = keyStack
										.pop();
								
								if (isSLCA(topKeywordStack)) {
									// output SLCA
								//	System.out.println("Result:" + currdewey);
									_resultheap.add(currdewey);
									for (int j = 0; j < keyStack.size(); j++) {
										for (String key : keywordList) {
											if (topKeywordStack
													.containsKey(key)) {
												keyStack.get(j).remove(
														curKeyword);
											}
										}
									}
								} 
								else
								{
							
										for (String key : keywordList) {
											if (topKeywordStack
													.containsKey(key)) {
												keyStack.get(keyStack.size()-1).put(key,topKeywordStack.get(key));
											}
										}
								
								}
							}
							i--;							
						}
					}
					// else do nothing
					else {
						vstack.push(nextcomponents[i]);
						HashMap<String, Integer> keywordMap2 = new HashMap<String, Integer>();
						keywordMap2.put(curKeyword, 1);
						keyStack.push(keywordMap2);
					}
				}

			} while (!_PointerOfSmallNodes.isEmpty());
			// it says we completely process all the keyword nodes
			
			while (vstack.size() > 0) {

				// record the checked node number
				_numberofchecked++;

				// get the current dewey from stack
				String currdewey = GetDewey(vstack);

				vstack.pop();
				HashMap<String, Integer> topKeywordStack = keyStack
						.pop();
				
				if (isSLCA(topKeywordStack)) {
					// output SLCA
					//System.out.println("Result:" + currdewey);
					_resultheap.add(currdewey);
					for (int j = 0; j < keyStack.size(); j++) {
						for (String key : keywordList) {
							if (topKeywordStack
									.containsKey(key)) {
								keyStack.get(j).remove(
										curKeyword);
							}
						}
					}

				} 
				else
				{
					for (String key : keywordList) {
						if (topKeywordStack
								.containsKey(key)) {
							keyStack.get(keyStack.size()-1).put(key,topKeywordStack.get(key));
						}
					}
				}
			}

		}

		// stop search

	}

	private Boolean isSLCA(HashMap<String, Integer> topKeywordStack) {
		// check whether satisfy slca
		int containKeyCount = 0;
		for (String key : keywordList) {
			if (topKeywordStack.containsKey(key)) {
				// System.out.println(keywordStack.get(key));
				if (topKeywordStack.get(key) == 1) {
					containKeyCount++;
				}
			}
		}
		if (containKeyCount == keywordList.size())
				{
			return true;
				}
		else{
			return false;
		}
		
	}

	// get the leftmost keyword node for v and start to process
	// search the relevant keyword nodes in the range,
	// for v=1.2.2, we search 1.2.2.* - 1.2.3 like ELCA

	// get the startindex of each keyword node list
	public String GetNextNode() {

		String selectkeyword = null;
		String selectnode = null;
		Set<String> keyset = _PointerOfSmallNodes.keySet();
		for (String key : keyset) {

			// double check v// _keyword2deweylist.get(key).get(
			// _PointerOfSmallNodes.get(key))
			List<String> list = _keyword2deweylist.get(key);

			// how can we do when a node list has been scanned completely
			// we need to continue to scan the other lists to the end
			int index = _PointerOfSmallNodes.get(key);
			String node = list.get(index);

			if (selectnode == null) {
				selectnode = node;
				selectkeyword = key;
			} else if (selectnode.compareToIgnoreCase(node) > 0) {

				selectnode = node;
				selectkeyword = key;
			}

		}

		int index = _PointerOfSmallNodes.remove(selectkeyword);
		List<String> list = _keyword2deweylist.get(selectkeyword);
		// check next node at the next time
		index++;
		if (list.size() > index) {

			_PointerOfSmallNodes.put(selectkeyword, index);
		} else {
			_PointerOfSmallNodes.remove(selectkeyword);
		}
		curKeyword = selectkeyword;

	//	System.out.println("Curkeyword:" + curKeyword+" CurNode"+selectnode);

		return selectnode;
	}

	/*
	 * transform the components in stack into a dewey code
	 */
	public String GetDewey(Stack<String> stack) {

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
	
	public void PrintList(List mylist) {

		Iterator ite = mylist.iterator();
		while (ite.hasNext()) {

			System.out.println(ite.next().toString());
		}
	}

	public void PrintHashMap(HashMap hm) {

		Set keyset = hm.keySet();
		for (Object key : keyset) {

			System.out.println(hm.get(key).toString());
		}
	}

	public void PrintResults() {

		// record the number of checked nodes
		StackSLCAResults.println("The number of checked nodes is: "
				+ _numberofchecked);

		// record the total number of results
		StackSLCAResults.println("The total number of real results is: "
				+ _totalnumberofresults);

		// from _resultheap and _resultmonitor
		StackSLCAResults.println("SLCA results as follow. ");
		System.out.println("SLCA results as follow");
		
		for (String result : _resultheap) {

			StackSLCAResults.println("SLCA result: "+result);
			System.out.println("SLCA result: "+result);
		}

		StackSLCAResults.println();
		StackSLCAResults.println();
		StackSLCAResults
				.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	}

	public static void main(String[] args) {

		try {
			PrintWriter outStream = new PrintWriter(new BufferedWriter(
					new FileWriter(new File("./out/StackbasedEvaluation.log"))));

			String databaseName = PropertyReader.getProperty("dbname");
			JdbcImplement.ConnectToDB(databaseName);

			String ksFile = PropertyReader.getProperty("ksFile");

			BufferedReader queryRead = new BufferedReader(
					new InputStreamReader(new DataInputStream(
							new FileInputStream(ksFile))));
			String query;
			while ((query = queryRead.readLine()) != null) {
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
						System.out.println(item);
					}
				}

				refinedkeywords.toArray();
				System.out.println(refinedkeywords.size());
				// k specifies the number of required SLCA results
				

				// give a refined keyword query to load
				// the corresponding keyword nodes
				StackbasedEvaluation myEstimation = new StackbasedEvaluation(
						outStream, refinedkeywords);

				// Start to estimate
				StackSLCAResults.printf("-- " + "Keyword Query: %s \n",
						query);
				StackSLCAResults.println();
				System.out.printf("-- " + "Keyword Query: %s \n", query);
				String minkeyword = myEstimation
						.LoadInformation(refinedkeywords);

				long start, qtime;
				start = System.currentTimeMillis();

				myEstimation.computeSLCA();

				qtime = System.currentTimeMillis() - start;

				// record memory usage
				Runtime rt = Runtime.getRuntime();
				long freememory = rt.freeMemory();
				long totalmemory = rt.totalMemory();
				long useagememory = totalmemory - freememory;

				StackSLCAResults.printf("--" + "Response Time: %d \n",
						qtime);
				StackSLCAResults.println();
				System.out.printf("--" + "Response Time: %d \n", qtime);
				StackSLCAResults.printf("--" + "Memory usage: %d \n",
						useagememory);
				StackSLCAResults.println();
				System.out.printf("--" + "Memory usage: %d \n", useagememory);

				myEstimation.PrintResults();

			}
			queryRead.close();
			JdbcImplement.DisconnectDB();

			StackSLCAResults.close();
			System.out.println("====================>>> Stop application!");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
