package com.tools;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import com.mysaxParser.TokenPreprocessor;

public class Helper {
	
	public static void PrintList(List mylist) {

		Iterator ite = mylist.iterator();
		while (ite.hasNext()) {

			System.out.println(ite.next().toString());
		}
	}

	public static void PrintHashMap(HashMap hm) {

		Set keyset = hm.keySet();
		for (Object key : keyset) {

			System.out.println(hm.get(key).toString());
		}
	}
	
	/*
	 * transform the components in stack into a dewey code
	 */
	public static String GetDewey(Stack<String> stack) {

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
				System.out.println(item);
			}
		}

		refinedkeywords.toArray();
		return refinedkeywords;
	}
	
	public static long getMemoryUsage()
	{
		// record memory usage
		Runtime rt = Runtime.getRuntime();
		long freememory = rt.freeMemory();
		long totalmemory = rt.totalMemory();
		long usagememory = totalmemory - freememory;
		return usagememory;
	}	
}