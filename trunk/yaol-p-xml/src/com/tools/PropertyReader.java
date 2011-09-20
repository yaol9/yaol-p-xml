package com.tools;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class PropertyReader {
	private static Properties prop;
	static
	{
		prop = new Properties();
		FileInputStream propFile;
		try {
			propFile = new FileInputStream("./config/project.properties");
			prop.load(propFile);
			propFile.close();
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
	public static String getProperty(String pName)
	{
		return prop.getProperty(pName);
	}
	
	//for test
	public static void main(String[] args) {

		System.out.println(PropertyReader.getProperty("dbname"));
		System.out.println(PropertyReader.getProperty("dbuser"));
		
	}
	
}
