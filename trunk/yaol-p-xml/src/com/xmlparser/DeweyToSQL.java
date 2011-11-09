/*
  Note: 
  - mysql bulk loading treat all values as strings, and binary data
  cannot be loaded (probably need to try dump command). 
	
  So the current solution is to supply multiple tuple values in one
  INSERT statement (to avoid the overheads).

  Known Bugs:
  - 
*/

package com.xmlparser;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import com.tools.PropertyReader;


public class DeweyToSQL {

    // debug
    final boolean DEBUG = true;

	// const
	private static final String INPUT_FIELD_SEPARATOR = "[|]";
	
	private static final String KEYWORD2ENCODE_TABLE_NAME = "KeywordDewey";
	private static final String DEWEY2PATH2ID_TABLE_NAME = "DeweyID";
	// static variables shared by all instances (probably not a good idea)
	//public static PrintWriter outSql;  // output insert commands info
	public static PrintWriter outEncode2Id;
	public static PrintWriter outKeyword2Encode;
		
	public TokenPreprocessor _myTokenPreprocessor = null;
	
	// debug functions
	public static void d(String str) {
		System.out.println(".. " + str);
	}
	
	public static void d(String str1, String str2, String str3, String str4){
		System.out.printf(str1, str2, str3, str4);
	}
	
	public static void d(String formatStr, String str, int i1, int i2, double d){
		System.out.printf(formatStr, str, i1, i2, d);
	}
	
	
	public void panic(String str) {
		System.err.println("!! " + str);
		System.exit(-1);
	}


	public DeweyToSQL() 
	{
	//	_sqlBuffer = new SQLInsertCmdBuffer(POSTING_TABLE_NAME);
	//	loadDeweyIDStats();
		//_tokenDict = new TokenDict();
		
		new HashMap<String, List<String>>(); 
		 new HashMap<String, Integer>();
		 
		 _myTokenPreprocessor = new TokenPreprocessor();
	}
	

	/*
	  Dewey Code Stats:
	  Max Depths Seen = 8
	  [10055 3 47 142 65 45 11 7 4 ]
	  
	 
	*/
	/*
	  Format of the text log file:
	  - each line is a record, with 3 fields separated by |. 
	  - the fields are: root-to-leaf path, leaf-node-id, sanitized text
	*/
	public void processTextLogFile(String textLogFile)
	{
		try {
			BufferedReader input = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(textLogFile))));
			String strLine;
			
			int currLine = 1; // for checking if the text is empty
			while ((strLine = input.readLine()) != null) {
				String fields[] = strLine.split(INPUT_FIELD_SEPARATOR);
							
				if (fields.length == 5){
											
					//we delete \\_ symbol to test uwc data set at 10 Sep, 2009
					parse(fields[1], fields[2],fields[3],fields[4], "[\\s\\t\\n\\x0B\\f\\r\\#\\-\\'\\*\\~\\{\\}\\(\\)\\[\\]\\''\\``\\@\\\\]"); // text The \\s+ is equivalent to [ \\t\\n\\x0B\\f\\r]
				} else{
				
					d(textLogFile + " : " + "line - " + currLine + 
							" doesn't contain data to be processed!");
				}
				
				currLine++;
			}
			
			input.close();
				
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void processElementLogFile(String elemLogFile)
	{

		try {
			BufferedReader input = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(elemLogFile))));
			String strLine;
			
			//HashMap<String, String> encode2path = null;
			//encode2path = new HashMap<String, String>();
			
			HashMap<String, String> encode2id = null;
			encode2id = new HashMap<String, String>();
			
			while ((strLine = input.readLine()) != null) {
				
				System.out.println(strLine);
				String fields[] = strLine.split(INPUT_FIELD_SEPARATOR);
				
				if ((fields.length == 2)&&(fields[0] != null)){
					
				//	String path = fields[0].trim();
				//	String dewey = fields[0].trim();
				//	String id = fields[1].trim();
										
				//	if (!encode2path.containsKey(dewey)){					
					//	encode2path.put(dewey, path);
				//	}	
				//	if (!encode2id.containsKey(dewey)){					
				//		encode2id.put(dewey, id);
				//	}	
					
					System.out.printf("INSERT INTO "+ DEWEY2PATH2ID_TABLE_NAME +" VALUES " +
							"(\"%s\", \"%s\");\n", fields[0].trim(), fields[1].trim());
					outEncode2Id.printf("INSERT INTO "+ DEWEY2PATH2ID_TABLE_NAME +" VALUES " +
							"(\"%s\", \"%s\");\n",fields[0].trim(), fields[1].trim());
					
					fields[0]=null;
					fields[1]=null;
					fields=null;
				}
				
				
				
			}
			input.close();
			
			//output encode -> path and prDewey into table PrDeweyDict
			/*
			Set<String> encodeSet = encode2id.keySet();
			Iterator<String> encodeIte = encodeSet.iterator();
			while (encodeIte.hasNext()){
				String encode = encodeIte.next().toString();
				
				System.out.printf("INSERT INTO "+ DEWEY2PATH2ID_TABLE_NAME +" VALUES " +
						"(\"%s\", \"%s\");\n", encode, encode2id.get(encode));
				outEncode2Id.printf("INSERT INTO "+ DEWEY2PATH2ID_TABLE_NAME +" VALUES " +
						"(\"%s\", \"%s\");\n", encode, encode2id.get(encode));
			}
			*/
			//_labelPathDict.dump();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//we delete the same word in the same string text
	
	public void parse(String dewey, String text,String depth,String id, String sep)
	{
		Pattern splitter = Pattern.compile(sep);
		
		//text is the content of one leaf node
		System.out.println(text);
		String[] toks = splitter.split(text);

		/* 
		   for the moment, we do not do stemming and token removal for now. 
		*/
		//toks = TokenPreprocessor.stemming(toks);
		_myTokenPreprocessor.trimTokens(toks);
		_myTokenPreprocessor.stopWordRemoval(toks);
		_myTokenPreprocessor.removeIrrelevantTokens(toks);
		
		
		Set tempterms = new HashSet();
		
		for (String token:toks){
			if (token != null){
				token = token.toLowerCase();
				
				if(!tempterms.contains(token)){
					tempterms.add(token);
					
					System.out.printf("INSERT INTO "+ KEYWORD2ENCODE_TABLE_NAME 
							+" VALUES (\"%s\", \"%s\",\"%s\",\"%s\");\n", token, dewey,depth,id);
					outKeyword2Encode.printf("INSERT INTO "+ KEYWORD2ENCODE_TABLE_NAME
							+" VALUES (\"%s\", \"%s\",\"%s\",\"%s\");\n", token, dewey,depth,id);
				}
			}	
		}		
			
	}
	
	
	public static void StartDeweyToSQL(){
		
		try{
			//load the parsed encodes
			String curDir = System.getProperty("user.dir");
			
			String _textFile = curDir + PropertyReader.getProperty("textsFileUrl");
			String _elemFile = curDir + PropertyReader.getProperty("elementsFileUrl");
			
			PrintWriter outStream = null;
			try{
				String s = PropertyReader.getProperty("EncodeID");
				outStream = new PrintWriter(new BufferedWriter(new FileWriter(new File(PropertyReader.getProperty("EncodeID")))));
				outEncode2Id = outStream;	
				
				outStream = new PrintWriter(new BufferedWriter(new FileWriter(new File(PropertyReader.getProperty("TokenLevEnID")))));
				outKeyword2Encode = outStream;	
			
			}catch(Exception e){
				System.out.printf("Error Message: %s \n", e.getMessage());
			}
			
		
			DeweyToSQL sqlgen = new DeweyToSQL();
			sqlgen.processElementLogFile(_elemFile);
			sqlgen.processTextLogFile(_textFile);	
					
			outKeyword2Encode.close();
			outEncode2Id.close();
			
			d("Keyword, dewey, depth ====> TokenLevEnID.log;\n" +
					".. have been converted into sql statements!\n"+			
					"..******* Please implement JdbcImplement at next step.******");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	// might need to use the incorporateIDF() function
	
	public static void main(String[] args)
	{
		StartDeweyToSQL();	
	}
}



