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

		TestCase test = new TestBasic();
		test.run();
		
		TestCase test2 = new TestQueryAware();
		test2.run();
		
		TestCase test3 = new TestDataAware();
		test3.run();
		
		TestCase test4 = new TestDataAwareSimple();
		test4.run();
	}	
	

}
