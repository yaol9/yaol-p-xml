package com.QueryEvaluation;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

public class IndexbasedEvaluation implements SLCAEvaluation {

	private PrintWriter SLCAResults;

	private List<String> keywordList;

	public LinkedList<String> resultList;

	public int _totalnumberofresults;

	public int _numberofchecked;

	public IndexbasedEvaluation(PrintWriter outStream, List<String> keywords) {
		SLCAResults = outStream;

		_totalnumberofresults = 0;
		_numberofchecked = 0;

		resultList = new LinkedList<String>();

		keywordList = keywords;

	}
	
	@Override
	public void computeSLCA(KeywordQuery kquery) {
		// TODO Auto-generated method stub

	}

	@Override
	public void PrintResults() {
		// TODO Auto-generated method stub
		// record the number of checked nodes
				SLCAResults.println("The number of checked nodes is: "
						+ _numberofchecked);

				// from _resultheap and _resultmonitor
				SLCAResults.println("SLCA results as follow. ");
				System.out.println("SLCA results as follow");

				for (String result : resultList) {

					SLCAResults.println("SLCA result: " + result);
					System.out.println("SLCA result: " + result);
				}

				SLCAResults.println();
				SLCAResults.println();
			    SLCAResults.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	}

}
