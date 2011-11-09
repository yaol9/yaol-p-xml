package com.QueryEvaluation;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.db.DBHelper;
import com.tools.Helper;

public class KeywordQuery {

	public HashMap<String, LinkedList<String>> keyword2deweylist;
	public List<String> keywordList;
	public List<String> shareResultList;
	private String _selectDeweySql = "select dewey from KeywordDewey where keyword=";

	public String curKeyword; // currently selected keyword

	public Map<String, Integer> pointerOfSmallNodes;

	public KeywordQuery() {
		keywordList=new LinkedList<String>();
		shareResultList=new LinkedList<String>();
		keyword2deweylist = new HashMap<String, LinkedList<String>>();
		pointerOfSmallNodes = new HashMap<String, Integer>();
	}
	public KeywordQuery(List<String> keywords) {

		keywordList = keywords;
		keyword2deweylist = new HashMap<String, LinkedList<String>>();
		pointerOfSmallNodes = new HashMap<String, Integer>();
		for (int i = 0; i < keywords.size(); i++) {
			pointerOfSmallNodes.put(keywords.get(i), 0);
		}

	}

	/*
	 * load dewey codes for keywords, load prdewey codes for keywords, load
	 * distributions to hash map, return the keyword with the minimal keyword
	 * nodes.
	 */
	public void LoadAllInformation() {
			
		for (int i = 0; i < keywordList.size(); i++) {
			// remove null from the refined keywords
			if (keywordList.get(i) != null) {
				String keyword = keywordList.get(i).trim();
				LoadKeywordNodes(keyword);			
			}
		}	
	}

	public void LoadSpecificInformation(String keyword) {
		LoadKeywordNodes(keyword.trim());	
		
	}
	
	public void LoadSpecificInformationFromList(String keyword,LinkedList<String> keywordList) {
		keyword2deweylist.put(keyword, keywordList);
		
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
	public int LoadKeywordNodes(String keyword) {

		String deweysql = _selectDeweySql + "'" + keyword
				+ "' order by XMLid ASC";

	//	 System.out.println("nodelist: keyword:"+keyword);

		ResultSet deweySet = DBHelper.performQuery(deweysql);
		int count = 0;
		if (deweySet != null) {
			try {
				LinkedList<String> mylist = new LinkedList<String>();
				while (deweySet.next()) {
					String dewey = deweySet.getString("dewey");
					dewey = dewey.trim();

					// write dewey into keyword2deweylist
					if (keyword2deweylist.containsKey(keyword)) {
						mylist = keyword2deweylist.get(keyword);

					} else {
						mylist = new LinkedList<String>();
						keyword2deweylist.put(keyword, mylist);
					}

					if (!mylist.contains(dewey)) {

						mylist.add(mylist.size(), dewey);
					}

					count++;

				}
			//	 Helper.PrintList(mylist);

			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {

			System.exit(-1);
		}

		return count;
	}

	// get the startindex of each keyword node list
	public String GetNextNode() {

		String selectkeyword = null;
		String selectnode = null;
		Set<String> keyset = pointerOfSmallNodes.keySet();
		for (String key : keyset) {

			List<String> list = keyword2deweylist.get(key);

			// how can we do when a node list has been scanned completely
			// we need to continue to scan the other lists to the end
			int index = pointerOfSmallNodes.get(key);
			String node = list.get(index);

			if (selectnode == null) {
				selectnode = node;
				selectkeyword = key;
			} else if (selectnode.compareToIgnoreCase(node) > 0) {

				selectnode = node;
				selectkeyword = key;
			}

		}

		int index = pointerOfSmallNodes.remove(selectkeyword);
		List<String> list = keyword2deweylist.get(selectkeyword);
		// check next node at the next time
		index++;
		if (list.size() > index) {

			pointerOfSmallNodes.put(selectkeyword, index);
		} else {
			pointerOfSmallNodes.remove(selectkeyword);
		}
		curKeyword = selectkeyword;

		// System.out.println("Curkeyword:" + curKeyword+" CurNode"+selectnode);

		return selectnode;
	}

	public void clearMem() {
		keyword2deweylist.clear();
	}
	public void clearKeyword(String keyword) {
		keyword2deweylist.remove(keyword);
		keywordList.remove(keyword);
	}


}
