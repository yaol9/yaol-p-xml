package com.test;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class ShareFactor {

	public List<String>items;
	
	//public int maximumShareCount=0;
	
	public HashSet<Integer> involvedQuery;
	
	
	ShareFactor()
	{
		items=new LinkedList<String> ();
		involvedQuery = new HashSet<Integer>();
	}
	
	ShareFactor(List<String> itemList)
	{
		items=new LinkedList<String> ();
		involvedQuery = new HashSet<Integer>();
		for(String item:itemList)
		{
			items.add(item);
		}
	}
	
	public int getMaxShareCount()
	{
		return involvedQuery.size();
	}
	public boolean isEqual(ShareFactor sf)
	{
		boolean isEqual=false;
		
		if(sf.items.size() == items.size())
		{
			int count=0;
			for(String s: items)
			{
				if(sf.items.contains(s))
				{
					count++;
				}
			}
			if(count==items.size())
			{
				isEqual=true;
			}
		}
		
		return isEqual;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
