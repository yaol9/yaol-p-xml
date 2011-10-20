package com.QueryEvaluation;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import com.tools.Helper;

public class SuperStackbasedEvaluation {

	private PrintWriter StackSLCAResults;

	public List<String> keywordList;

	public List<String> _resultheap;

	public int _totalnumberofresults;

	public int _numberofchecked;
	
	private KeywordQuery kQuery;

	public SuperStackbasedEvaluation(PrintWriter outStream, List<String> keywords,KeywordQuery k) {
		StackSLCAResults = outStream;

		_totalnumberofresults = 0;
		_numberofchecked = 0;

		_resultheap = new ArrayList<String>();

		keywordList = keywords;
		kQuery=k;
	}

	public void computeSLCA(KeywordQuery kquery) {

		String v = kquery.GetNextNode(); // get first node
		if (v.contains(".")) {
			String[] vcomponents = v.split("[.]");
			Stack<String> vstack = new Stack<String>();
			Stack<HashMap<String, Integer>> keyStack = new Stack<HashMap<String, Integer>>();
			HashMap<String, Integer> keywordMap = new HashMap<String, Integer>();

			int stacksize = vcomponents.length;
			for (int i = 0; i < stacksize; i++) {
				vstack.push(vcomponents[i]);
				keywordMap.put(kquery.curKeyword, 1);
				keyStack.push(keywordMap);
			}

			do {
				// check if the list can be reduced automatically
				String leftmostnode = kquery.GetNextNode();

				String[] nextcomponents = leftmostnode.split("[.]");

				for (int i = 0; i < nextcomponents.length; i++) {

					if (i < vstack.size()) {

						if (nextcomponents[i]
								.compareToIgnoreCase(vstack.get(i)) != 0) {

							while (vstack.size() > i) {

								// record the checked node number
								_numberofchecked++;

								// get the current dewey from stack
								String currdewey = Helper.GetDewey(vstack);
								vstack.pop();
								HashMap<String, Integer> topKeywordStack = keyStack
										.pop();
								
								if (isSLCA(topKeywordStack, keywordList)) {
									// output SLCA
									// System.out.println("Result:" +
									// currdewey);
									_resultheap.add(currdewey);
									for (int j = 0; j < keyStack.size(); j++) {
										keyStack.get(j).put("a-refuse-mark", 1);
										for (String key : keywordList) {
											if (keyStack.get(j).containsKey(key)) {
												keyStack.get(j).remove(key);
											}
										}
									}
								} else {
									for (String key : keywordList) {
										if (topKeywordStack.containsKey(key)) {
											if (keyStack.size() > 1) {
											keyStack.get(keyStack.size() - 1)
													.put(key,
															topKeywordStack
																	.get(key));
											}
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
						keywordMap2.put(kquery.curKeyword, 1);
						keyStack.push(keywordMap2);
					}
				}

			} while (!kquery.pointerOfSmallNodes.isEmpty());
			// it says we completely process all the keyword nodes

			while (vstack.size() > 0) {

				// record the checked node number
				_numberofchecked++;

				// get the current dewey from stack
				String currdewey = Helper.GetDewey(vstack);

				vstack.pop();
				HashMap<String, Integer> topKeywordStack = keyStack.pop();

				if (isSLCA(topKeywordStack, keywordList)) {
					// output SLCA
					// System.out.println("Result:" + currdewey);
					_resultheap.add(currdewey);
					for (int j = 0; j < keyStack.size(); j++) {
						keyStack.get(j).put("arefusemark", 1);
						for (String key : keywordList) {
							if (keyStack.get(j).containsKey(key)) {
								keyStack.get(j).remove(key);
							}
						}
					}

				} else {
					for (String key : keywordList) {
						if (topKeywordStack.containsKey(key)) {
							if (keyStack.size() > 1) {
								keyStack.get(keyStack.size() - 1).put(key,
										topKeywordStack.get(key));

							}
						}
					}
				}
			}

		}

	}

	public void PrintResults() {

		// record the number of checked nodes
		StackSLCAResults.println("The number of checked nodes is: "
				+ _numberofchecked);

		// from _resultheap and _resultmonitor
		StackSLCAResults.println("SLCA results as follow. ");
		System.out.println("SLCA results as follow");

		for (String result : _resultheap) {

			StackSLCAResults.println("SLCA result: " + result);
			System.out.println("SLCA result: " + result);
		}

		StackSLCAResults.println();
		StackSLCAResults.println();
		StackSLCAResults.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	}

	public Boolean isSLCA(HashMap<String, Integer> topKeywordStack,List<String> keywordList) {
		// check whether satisfy slca
		if(topKeywordStack.containsKey("a-refuse-mark"))
		{
			return false;
		}
		int containKeyCount = 0;
		int keywordCount=0;
		for (String key : keywordList) {
			if (topKeywordStack.containsKey(key)) {
				// System.out.println(keywordStack.get(key));
				if (topKeywordStack.get(key) == 1) {
					containKeyCount++;
				}
			}		
			if(!key.contains("|"))
			{
				keywordCount++;				
			}
			//get keyword count
		}
		
		
		//if(containKeyCount>1)
	//	{
	//		System.out.println(containKeyCount);
	//	}
		if (containKeyCount == keywordCount) {
			return true;
		} else {
			return false;
		}

	}
}
