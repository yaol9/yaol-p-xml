package com.test;

import java.util.LinkedList;
import java.util.List;

public class ShareFactorManager {

	public List<ShareFactor> sfList;
	
	ShareFactorManager()
	{
		sfList=new LinkedList<ShareFactor> ();		
	}
	
	public void addShareFactor(ShareFactor sf)
	{
		if(!isShareFactorExist(sf))
		{
			sfList.add(sf);
		}
		else
		{
		}
	}
	
	public void addShareFactor(List<String> itemList)
	{
		ShareFactor sf = new ShareFactor(itemList);
		if(!isShareFactorExist(sf))
		{
			sfList.add(sf);
		}
		else
		{
		
		}
	}
	
	public ShareFactor findShareFactor(ShareFactor sf)
	{
		
		for(ShareFactor sf_temp: sfList)
		{
			if(sf_temp.isEqual(sf))
			{
				return sf_temp;
			}
			
		}
		System.out.println("error in find share factor");
		return null;
	}
	
	public boolean isShareFactorExist(ShareFactor sf)
	{
		boolean isExist=false;
		for(ShareFactor sf_temp: sfList)
		{
			if(sf_temp.isEqual(sf))
			{
				isExist=true;
			}
			
		}
		return isExist;
	}
	
	public boolean isShareFactorExist(List<String> itemList)
	{
		ShareFactor sf = new ShareFactor(itemList);
		
		
		boolean isExist=false;
		for(ShareFactor sf_temp: sfList)
		{
			if(sf_temp.isEqual(sf))
			{
				isExist=true;
			}
			
		}
		return isExist;
		
	}
	
	public ShareFactor getExistFromFullOrPartList(List<String> itemList)
	{
		ShareFactor sf = new ShareFactor(itemList);
		
		
		//boolean isOrPartOfShareFactorExist=false;
		for(ShareFactor sf_temp: sfList)
		{
			if(sf_temp.isEqual(sf))
			{
				return sf_temp;
			}
			else
			{
				if(sf_temp.items.size()>sf.items.size())
				{
					int count=0;
					for(String s : sf.items)
					{
						if(sf_temp.items.contains(s))
						{
							count++;
						}
					}
					if(count==sf.items.size())
					{
						return sf_temp;					
					}
				}
			}
			
		}
		return null;
	}
	public boolean isOrPartOfShareFactorExist(List<String> itemList)
	{
		ShareFactor sf = new ShareFactor(itemList);
				
		boolean isOrPartOfShareFactorExist=false;
		for(ShareFactor sf_temp: sfList)
		{
			if(sf_temp.isEqual(sf))
			{
				isOrPartOfShareFactorExist=true;
			}
			else
			{
				if(sf_temp.items.size()>sf.items.size())
				{
					int count=0;
					for(String s : sf.items)
					{
						if(sf_temp.items.contains(s))
						{
							count++;
						}
					}
					if(count==sf.items.size())
					{
						isOrPartOfShareFactorExist=true;						
					}
				}
			}
			
		}
		return isOrPartOfShareFactorExist;
		
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
