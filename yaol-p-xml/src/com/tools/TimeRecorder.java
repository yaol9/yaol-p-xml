package com.tools;

public class TimeRecorder {

	private static long time;
	static
	{
		time=0;
	}
	
	public static void startRecord()
	{
		time= System.currentTimeMillis();
	}
	
	public static void stopRecord()
	{
		time= System.currentTimeMillis()-time;
	}
	
	public static long getTimeRecord()
	{
		return time;
	}
}
