package com.mysax2parser2;

/*
 Note: 
 1) processings done in this program:
 - filter out non-ascii chars
 - tokenization 
 - all tokens converted to lower cases

 Known Bugs:
 - tokenization will decompose URLs. Similarly, filenames (with extensions) will be decomposed.
 - probably need to add [, ] to the TOKEN_SEPARATORS

 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

import com.tools.PropertyReader;

class DeweyStats {

	private int _maxDepth;
	private int _maxDepthSeen;


	public DeweyStats(int maxDepth) {
		_maxDepth = maxDepth;
		_maxDepthSeen = 0;
	//	_maxNumbers = new int[_maxDepth];
	}

	public void updateStats(String[] dewey, int depth) {
		if (depth > _maxDepthSeen) {
			_maxDepthSeen = depth;
		}	
		
	}

	public void reportStats() {
		// System.out.printf("\nDewey Code Stats:\n\tMax Depths Seen = %d\n\t[",
		// new Integer(_maxDepthSeen)); // in fact, should be $+1 ?
		//for (int i = 0; i <= _maxDepthSeen; i++) {

			// System.out.printf("%d ", _maxNumbers[i]);
		//}
		// System.out.printf("]\n");

	}
}

public class mySAXParser extends DefaultHandler {
	private static final char OUTPUT_FIELD_SEPARATOR = '|';
	private static final int XML_MAX_DEPTH = 60;
	private static final char DEWEY_SEPARATOR = '.';
	private static final char Pr_DEWEY_SEPARATOR = '-';
	// private static final char PATH_SEPARATOR = '/';

	// private static final String PATH_SEPARATOR = "'\'/";
	private static final String PATH_SEPARATOR = "/";
	private static final String REVERSE_PATH_SEPARATOR = "";// "'\'";

	private static final String TOKEN_SEPARATORS = "[| \t\n\r()!.,:;\"?/]+"; // MUST
																				// include
																				// OUTPUT_FIELD_SEPARATOR
	private static final char NON_ASCII_CHAR_CHAR = '_'; // ? is no good.
	//private static final String UNI_LEAF_LABEL = "ADD_LEAF";

	// static variables shared by all instances (probably not a good idea)
	
	private static PrintWriter outTexts; // output parsed text info

	// instance-specific variables
	public int _docID;
	public int depth = 0;
	
	public String[] deweyCode = new String[XML_MAX_DEPTH];
	
	public String[] levelIndicator = new String[XML_MAX_DEPTH];
	public String MUX = "M";
	public String IND = "I";

	public String[] pathAsElemName = new String[XML_MAX_DEPTH];
	public DeweyStats _stats;
	public String tempLeafContent = "";
	public String proceedingElemLabel = "";

	public static int collectionOfAllElements = 0;
	public static int collectionOfLeafNodes = 0;

	// debug function
	public void d(String str) {
		System.out.printf(".. %s \n", str);
	}

	public void panic(String str) {
		System.err.printf("!! %s ", str);
		System.exit(-1);
	}

	// Helper functions
	public static String getFileNameWithoutExtension(String fileName) {
		int whereDot = fileName.lastIndexOf('.');
		if (0 < whereDot && whereDot <= fileName.length() - 2) {
			return fileName.substring(0, whereDot);
		}
		return fileName;
	}

	public String arrayToDeweyString(String[] code, int localdepth) {
		StringBuilder b = new StringBuilder();
		b.append(code[0]);
		for (int i = 1; i <= localdepth; i++) {;
			b.append(DEWEY_SEPARATOR).append(code[i]);
		}
		return b.toString();
	}

	public String arrayToPrDeweyString(String[] code, int localdepth) {
		StringBuilder b = new StringBuilder();
		b.append(code[1]);
		for (int i = 2; i <= localdepth; i++) {
			b.append(Pr_DEWEY_SEPARATOR).append(code[i]);
		}
		return b.toString();
	}

	public String arrayToString(String[] pathName, int localdepth) {
		// starts from pathName[0]
		StringBuilder b = new StringBuilder();
		b.append(pathName[0]);
		for (int i = 1; i <= localdepth; i++) {
			b.append(PATH_SEPARATOR).append(pathName[i]);
		}
		return b.toString();
	}

	public String reverseArrayToString(String[] pathName, int localdepth) {
		StringBuilder b = new StringBuilder();
		b.append(pathName[localdepth]);
		for (int i = localdepth - 1; i >= 0; i--) {
			b.append("/").append(REVERSE_PATH_SEPARATOR).append(pathName[i]);
		}
		return b.toString();
	}

	public boolean isAscii(char ch) {
		// return (Character.UnicodeBlock.BASIC_LATIN.of(ch) != null);
		return ((int) ch < 128); // we use the ascii integer of ch to compare
									// with 128
	}

	// Replace all punctuations by ' ' and replace non-ASCII characters by '_'
	// c.f., http://mindprod.com/jgloss/encoding.html#CONVERSION (does not work
	// well?)
	// We also convert tokens into lowercases.
	public String sanitizeString(String input) {
		try {
			String[] tokens = input.toLowerCase().split(TOKEN_SEPARATORS);
			StringBuilder b = new StringBuilder();
			for (int i = 0; i < tokens.length; i++) {

				// ! does not work well ..., will just swith to the brute force
				// method
				// byte []bytes = tokens[i].getBytes("8859_1");
				// d(" converted to " + new String(bytes, "8859_1"));
				// b.append(new String(bytes, "8859_1")).append(" "); // extra
				// trailing space does not really matter

				for (int j = 0; j < tokens[i].length(); j++) {
					char c = tokens[i].charAt(j);
					if (!isAscii(c)) {
						c = NON_ASCII_CHAR_CHAR;
						// int ascii = (int) c;
						// System.out.println("ASCII of " + c + ": " + ascii);
						// need to convert c to ascii code??
					} else {
						// currently we only consider the char within the range
						// of ascii
						b.append(c);
					}

				}

				if (b.length() != 0) {
					b.append(' ');
				}

			}
			return b.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/*
	 * Because we need to process non-ascii char, we have to process char by
	 * char
	 */
	/*
	 * public String myStringTokenizer(String input){
	 * 
	 * try {
	 * 
	 * StringTokenizer tokens = new StringTokenizer(input,TOKEN_SEPARATORS);
	 * String b = ""; while(tokens.hasMoreTokens()){
	 * 
	 * b = b + " " + tokens.nextToken();
	 * 
	 * } System.out.println(b); return b; } catch (Exception e) {
	 * e.printStackTrace(); } return ""; }
	 */
	// parsing the XML file
	public mySAXParser(int docID, DeweyStats stats) {
		_docID = docID;
		depth = 0;
		_stats = stats;
		// by default, all variables were initialized to 0, so we do
		// not need to initialize deweyCode and pathName arrays. (also
		// because Java does not have memset or zeromem functions)
	}

	private void parseDocument(String _fileName) {

		String fileName = _fileName;
		// get a factory
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try {
			// get a new instance of parser
			SAXParser sp = spf.newSAXParser();
			// parse the file and also register this class for call backs
			sp.parse(fileName, this);

		} catch (SAXException se) {
			se.printStackTrace();
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}

	/**
	 * Iterate through the list and print the contents
	 */

	// ===========================================================
	// Methods in SAX DocumentHandler
	// ===========================================================

	public void startDocument() throws SAXException {
		// initiate the first Dewey code to the doc ID a trick is used
		// that save us an useless ID components for the root: we
		// decrease the depth and set deweyCode[0] as _docID - 1.
		deweyCode[0] = Integer.toString(_docID - 1);
		depth = -1;
		// set the position number for each level, which starts from 0
		// In parsing, one new level comes, it should retrieve the position
		// number.
		for (int i = 0; i < XML_MAX_DEPTH; i++) {
			levelIndicator[i] = "0";
		}

	}

	public void endDocument() throws SAXException {
		d("The parsing and encoding work have been done!");

	}

	// Event Handlers
	public void startElement(String uri, String localName, String qName,
			Attributes attrs) throws SAXException {
		
		depth += 1;

		String position = levelIndicator[depth];
	
		long realpos = 0;
		realpos = Long.valueOf(position.trim());
		realpos += 1;

		levelIndicator[depth] = Long.toString(realpos);

		deweyCode[depth] = levelIndicator[depth];


		// check if our Dewey code is not enough
		if (depth > XML_MAX_DEPTH) {
			panic("XML file too deep. ");
		}

		pathAsElemName[depth] = qName;
		proceedingElemLabel = qName;

		// summary the number of elements where elements are not leaf nodes
		collectionOfAllElements++;
		d(Integer.toString(collectionOfAllElements));
	//we don't need element.txt now
		/*
		// !OUT_ELEM
		outElements.write(reverseArrayToString(pathAsElemName, depth)
				+ OUTPUT_FIELD_SEPARATOR + arrayToDeweyString(deweyCode, depth)
				+ "\n");

		d(reverseArrayToString(pathAsElemName, depth) + OUTPUT_FIELD_SEPARATOR
				+ arrayToDeweyString(deweyCode, depth) );
*/ 
		
		_stats.updateStats(deweyCode, depth);

		// Process the current text data that is taken by tempLeafContent

	}

	/*
	 * endElement() : If preceding label (start element) and succeeding label
	 * (end element) have the same label name, we do not assign a new leaf node
	 * to the current text data. Otherwise, we create a new leaf for text data.
	 */

	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		// simulate pop()
		if (!tempLeafContent.isEmpty()) {
			if (proceedingElemLabel.equalsIgnoreCase(qName)) { //the text content which is not tightly contained by a node will not be processed 

					// !OUT_TEXT
					outTexts.write(reverseArrayToString(pathAsElemName, depth)
							+ OUTPUT_FIELD_SEPARATOR
							+ arrayToDeweyString(deweyCode, depth)
							+ OUTPUT_FIELD_SEPARATOR
							+ sanitizeString(tempLeafContent)
							+ OUTPUT_FIELD_SEPARATOR
							+ Integer.toString(depth+1)+ "\n");

					// summary the number of elements where elements are leaf
					// nodes
					// collectionOfAllElements++; // they have been added at the
					// point of startElement
					collectionOfLeafNodes++;

					d(reverseArrayToString(pathAsElemName, depth)
							+ OUTPUT_FIELD_SEPARATOR
							+ arrayToDeweyString(deweyCode, depth)
							+ OUTPUT_FIELD_SEPARATOR
							+ sanitizeString(tempLeafContent)
							+ OUTPUT_FIELD_SEPARATOR
							+ Integer.toString(depth+1));

					tempLeafContent = "";
					_stats.updateStats(deweyCode, depth);		
			} 
		}
		else
		{
			//if remove this line, dewey number will be continually increased in a level
			levelIndicator[depth+1]="0";
		}

		--depth;

	}

	/*
	 * Because text data can share the same dewey with their parents, we should
	 * not specify the dewey code to text data. Otherwise,
	 * reverseArrayToString(pathAsElemName) will output null at the 'depth'
	 * position
	 */
	/*
	 * public void characters (char buf [], int start, int len) throws
	 * SAXException { if (!new String(buf, start, len).trim().equals("")) {
	 * super.characters(buf, start, len); String s = new String(buf, start,
	 * len);
	 * 
	 * // depth += 1; // deweyCode[depth] += 1; // deweyCode[depth+1] = 0;
	 * 
	 * // System.out.println(reverseArrayToString(pathAsElemName) // +
	 * OUTPUT_FIELD_SEPARATOR + arrayToDeweyString(deweyCode) // +
	 * OUTPUT_FIELD_SEPARATOR + sanitizeString(s));
	 * 
	 * //!OUT_TEXT outTexts.write(arrayToDeweyString(deweyCode) +
	 * OUTPUT_FIELD_SEPARATOR + sanitizeString(s) + "\n");
	 * 
	 * //outTexts.write(reverseArrayToString(pathAsElemName) // +
	 * OUTPUT_FIELD_SEPARATOR + arrayToDeweyString(deweyCode) // +
	 * OUTPUT_FIELD_SEPARATOR + sanitizeString(s) + "\n");
	 * d(reverseArrayToString(pathAsElemName) + OUTPUT_FIELD_SEPARATOR +
	 * arrayToDeweyString(deweyCode) + OUTPUT_FIELD_SEPARATOR +
	 * sanitizeString(s)); _stats.updateStats(deweyCode, depth);
	 * 
	 * // clean up // -- depth; } }
	 */

	/*
	 * We let text data have their own dewey code when the text data are
	 * separated by an internal node.
	 */
	public void characters(char buf[], int start, int len) throws SAXException {
		if (!new String(buf, start, len).trim().equals("")) {
			super.characters(buf, start, len);
			String s = new String(buf, start, len);

			//temp store content of node
			tempLeafContent = s;			
		}
	}

	public static int getCollectionOfAllElements() {
		System.out.println(collectionOfAllElements);
		return collectionOfAllElements;
	}

	public static int getCollectionOfLeafNodes() {
		System.out.println(collectionOfLeafNodes);
		return collectionOfLeafNodes;
	}

	public static void StartSaxProgram() {

		
		try {		

			System.setProperty("entityExpansionLimit", PropertyReader.getProperty("entityExpansionLimit"));
	
			// stats
			DeweyStats stats = new DeweyStats(XML_MAX_DEPTH);

			// open output files
			PrintWriter outStream = null;

		
			outStream = new PrintWriter(new BufferedWriter(new FileWriter(
					new File(PropertyReader.getProperty("textsFileUrl")))));
			outTexts = outStream;

			outStream = new PrintWriter(new BufferedWriter(new FileWriter(
					new File(PropertyReader.getProperty("connectionsFileUrl")))));
			PrintWriter outCollections = outStream;

			// input files
			File folder = new File("./data/");
			String _dir = "./data/";

			if (folder.canRead()) {
				File[] listOfFiles = folder.listFiles();
				// mySAXParser spe = new mySAXParser();
				for (int i = 0; i < listOfFiles.length; i++) {
					String _fileName = listOfFiles[i].getName();
					if (listOfFiles[i].isFile()) {
						// Use the id in the filename as the document ID

						try {
							int docID = Integer
									.parseInt(getFileNameWithoutExtension(_fileName));

							mySAXParser spe = new mySAXParser(docID, stats);

							String dirFilename = _dir + _fileName;
							spe.parseDocument(dirFilename);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} 
				}

				// close the files
				
				outTexts.close();

				outCollections.printf("Collection of all elements"
						+ OUTPUT_FIELD_SEPARATOR + "%d\n",
						getCollectionOfAllElements());
				outCollections.printf("Collection of all leaf nodes"
						+ OUTPUT_FIELD_SEPARATOR + "%d\n",
						getCollectionOfLeafNodes());
				outCollections.close();
				// report the status
				stats.reportStats();

				System.out
						.println("..The xml files in "
								+ _dir
								+ "have been parsed and encoded correctly! \n"
								+ "..the results are saved as: ./out/elements.log and ./out/texts.log.\n"
								+ "..******* Please implement DeweyToSQL.java at next step.******");

			} else {
				System.err.println("The specified folder cannot be open.\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		StartSaxProgram();
		 System.exit(0);
	}
}
