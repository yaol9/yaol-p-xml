package com.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import com.QueryEvaluation.IndexbasedEvaluation;
import com.QueryEvaluation.KeywordQuery;
import com.QueryEvaluation.SLCAEvaluation;
import com.QueryEvaluation.StackbasedEvaluation;
import com.db.DBHelper;
import com.tools.Helper;
import com.tools.PropertyReader;
import com.tools.TimeRecorder;

public class TestManager {

    
	public TestManager() {

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		TestCase test_ba = new TestBasic();
		long ba=test_ba.run();
		
		TestCase test_qa = new TestQueryAware();
		long qa=test_qa.run();
		
		TestCase test_sa = new TestShortEager();
		long sa=test_sa.run();
		
		TestCase test_sa_i = new TestShareEagerI();
	//	long sa_i=test_sa_i.run();
		
		TestCase test_sa_ii = new TestShareEagerII();
		long sa_ii=test_sa_ii.run();
		
		System.out.println("Test Result:");
		System.out.println("Algorithm BA : " + ba + " ms");
		System.out.println("Algorithm QA : " + qa + " ms");
		System.out.println("Algorithm Short Eager : " + sa + " ms");
	//	System.out.println("Algorithm Share Eager I : " + sa_i + " ms");
		System.out.println("Algorithm Share Eager II: " + sa_ii + " ms");
		
	}	
	

}
