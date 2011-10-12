package com.QueryEvaluation;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import com.tools.Helper;

public class StackbasedEvaluation {
	
	private static PrintWriter StackSLCAResults;

	private List<String> keywordList;

	private List<String> _resultheap;

	private static int _totalnumberofresults;

	private static int _numberofchecked;

	public StackbasedEvaluation(PrintWriter outStream, List<String> keywords) {
		StackSLCAResults = outStream;

		_totalnumberofresults = 0;
		_numberofchecked = 0;
		
		_resultheap = new ArrayList<String>();
	
		keywordList = keywords;

	}
	
	public LinkedList<String> insertsortedlist(LinkedList<String> list,
			String insertstr) {

		if (list.size() == 0) {
			list.add(insertstr);

		} else {
			int index = -1;
			for (int i = 0; i < list.size(); i++) {
				String node = list.get(i);

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

	public void computeSLCA(KeywordQuery kquery) {

		// scan keyword nodes and compute dist of v

		// the nodes that are the descendants of v are needed to be explored
		// once the nodes are explored, they will be removed from the keyword
		// node lists

		// get a smallest node v

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

							// first pop vstack.get(i) from vstack, then
							// promote its dist
							// to its parent and then write it into _hashMap
							while (vstack.size() > i) {

								// record the checked node number
								_numberofchecked++;

								// get the current dewey from stack
								String currdewey = Helper.GetDewey(vstack);
								vstack.pop();
								HashMap<String, Integer> topKeywordStack = keyStack
										.pop();

								if (Helper.isSLCA(topKeywordStack,keywordList)) {
									// output SLCA
									// System.out.println("Result:" +
									// currdewey);
									_resultheap.add(currdewey);
									for (int j = 0; j < keyStack.size(); j++) {
										for (String key : keywordList) {
											if (topKeywordStack
													.containsKey(key)) {
												keyStack.get(j).remove(
														kquery.curKeyword);
											}
										}
									}
								} else {
									for (String key : keywordList) {
										if (topKeywordStack.containsKey(key)) {
											keyStack.get(keyStack.size() - 1)
													.put(key,
															topKeywordStack
																	.get(key));
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

				if (Helper.isSLCA(topKeywordStack,keywordList)) {
					// output SLCA
					// System.out.println("Result:" + currdewey);
					_resultheap.add(currdewey);
					for (int j = 0; j < keyStack.size(); j++) {
						for (String key : keywordList) {
							if (topKeywordStack.containsKey(key)) {
								keyStack.get(j).remove(kquery.curKeyword);
							}
						}
					}

				} else {
					for (String key : keywordList) {
						if (topKeywordStack.containsKey(key)) {
							keyStack.get(keyStack.size() - 1).put(key,
									topKeywordStack.get(key));
						}
					}
				}
			}

		}

		// stop search

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

			StackSLCAResults.println("SLCA result: " + result);
			System.out.println("SLCA result: " + result);
		}

		StackSLCAResults.println();
		StackSLCAResults.println();
		StackSLCAResults.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	}	

}
