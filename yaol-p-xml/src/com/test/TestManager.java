package com.test;

public class TestManager {

    
	public TestManager() {

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		TestCase testSequence = new TestSequenceAlgorithm();
		testSequence.run();
		
		TestCase testBasic = new TestBasicAlgorithm();
		testBasic.run();
		
		TestCase testQueryAware = new TestQueryAwareAlgorithm();
		testQueryAware.run();
		
		TestCase testInstanceAware = new TestInstanceAwareAlgorithm();
		testInstanceAware.run();
	}	
	
	

}
