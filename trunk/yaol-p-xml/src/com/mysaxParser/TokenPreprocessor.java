

package com.mysaxParser;


import java.io.*;
import java.util.*;
//import java.lang.*;
//import java.util.regex.*;


public class TokenPreprocessor{

	// const
    final static String STOPWORD_FILE = "stop_words.txt";
	final static int MIN_TOKEN_LENGTH = 3; 


    //private static PrintWriter pw;
    private static BufferedReader br;
    private Set<String> stopwordset=null;

    public TokenPreprocessor(){
    	
    	this.stopwordset = new HashSet<String>();
    	this.stopwordset = loadStopwordList(STOPWORD_FILE);
    }
    
    public static String[] stemming(String[] tokens)
	{
		Stemmer s = new Stemmer();
		//char[] tokenChars; // array of characters in a token

		for (int i = 0; i < tokens.length; i++) {
			String token = tokens[i];

			if (token != null) {
				s.add(token.toCharArray(), token.length()); // add to Stemmer class
				s.stem(); // perform stemming
				tokens[i] = s.toString();
			}
		}
		return tokens;
    }
	
    public Set<String> loadStopwordList(String stopwordFile) 
    {
		
		String input = null;
		
		try {
			br = new BufferedReader(new FileReader(stopwordFile));//load the stopwordliat file
			while ((input = br.readLine()) != null) {
				stopwordset.add(input);
			}
			br.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return stopwordset;
    }

    public String[] stopWordRemoval(String[] tokens)
    {
		for(int i = 0; i < tokens.length; i++) {
			if (tokens[i] != null) {
				//test
			//	Iterator myIte = stopwordset.iterator();
			//	while(myIte.hasNext()){
			//		String str = myIte.next().toString();
			//		System.out.println(str);
			//	}
				//System.out.println(tokens[i].trim());
				if (stopwordset.contains(tokens[i].trim())) {
					tokens[i] = null;
				}
			}
		}

		return tokens;	    
    }

        
    public String[] trimTokens(String[] tokens)
    {
		for(int i = 0; i < tokens.length; i++) {						
			if (tokens[i].trim().equals("")) {
				tokens[i] = null;				
			}			
		}
		return tokens;	    
    }
    
    public static boolean containsOnlyNumbers(String str)
    {
    	// It cannot contain only numbers if it's null or empty ...
    	if (str == null || str.length() == 0){
    		return false;
    	}
    	
    	for (int i = 0; i < str.length(); i++)
    	{
    		//If we find a non-digit character we return false.
    		if (!Character.isDigit(str.charAt(i))){
    			return false;
    		}
    	}
    	return true;
    }
    
    public String[] removeIrrelevantTokens(String[] tokens)
	{
		//Pattern reDigits = Pattern.compile("\\d+");
    	//String[] newTokens = new String[5];
    	//int realsize = 0;
		for(int i = 0; i < tokens.length; i++) {
			if (tokens[i] != null) {
				// CASE 1: char of length < MIN_TOKEN_LENGTH
				if (tokens[i].length() < MIN_TOKEN_LENGTH) {
					tokens[i] = null;
				} else {
					//newTokens[realsize] = tokens[i];
					//realsize++;
					// CASE 2: numbers
					//Matcher m = reDigits.matcher(tokens[i]);
					//if (m.matches()) {
					//	tokens[i] = null;
					//}
					//if (containsOnlyNumbers(tokens[i])){
					//	int valInteger = Integer.parseInt(tokens[i]);
						
						//We only keep the number between 1000 to 3000.
					//	if (valInteger < 1000 || valInteger > 3000){
					//		tokens[i] = null;
					//	}
					}
					
				}
			}
		
		return tokens;	    
	}

    public static void main(String[] args)throws IOException
    {
		String[] strings ={"mangoes","mangoes","asked","women","gone","fishes","in","are","of","am","on"};

        TokenPreprocessor.stemming(strings);
		// for(int i=0;i<strings.length;i++) {
		// 	    System.out.println(strings[i]);
		// 	}
        TokenPreprocessor tp = new TokenPreprocessor();
        tp.loadStopwordList(STOPWORD_FILE);
        tp.stopWordRemoval(strings);
    }
}

