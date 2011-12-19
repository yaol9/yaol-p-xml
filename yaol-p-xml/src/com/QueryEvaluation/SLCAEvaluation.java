package com.QueryEvaluation;


import java.util.LinkedList;



public interface SLCAEvaluation
{
	public LinkedList<String> getResult();
	public void computeSLCA(KeywordQuery kquery);
	public void printResults();
	
}