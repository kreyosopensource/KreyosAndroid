package com.kreyos.watch.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import junit.framework.Assert;
import android.content.Context;
import android.util.Log;

public class ErrorLogger {
	
	private static String mLogFilename = "kreyosErrorLogs";
	
	public static void appendLog(Context p_context, String p_log, boolean p_triggerCrash)
	{
		//Build log
		String buildLog = "" + p_context.getClass().getName() + "::" + p_log;
		
		// Get log file
		File logFile = new File(p_context.getExternalFilesDir(null)+ "/" + mLogFilename + ".txt");
		
		if (!logFile.exists()) {
			// if doesn't exist create new
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try	{
			//BufferedWriter for performance, true to set append to file flag
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
			buf.append(buildLog);
			buf.newLine();
			buf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Log.d(""+p_context.getClass().getName(), "::" + p_log);
		
		if (p_triggerCrash) {
			Assert.fail(buildLog);
		}
		
	}
}
