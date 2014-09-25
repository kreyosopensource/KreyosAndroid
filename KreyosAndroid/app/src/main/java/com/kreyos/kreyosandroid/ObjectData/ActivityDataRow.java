package com.kreyos.kreyosandroid.objectdata;
 
 import android.util.SparseArray;
 
 public class ActivityDataRow {
 	
 	public static class ActivityMode {
 		public final int DATA_MODE_NORMAL    = 0x00;
 		public final int DATA_MODE_RUNNING   = 0x01;
 		public final int DATA_MODE_BIKING    = 0x02;
 		public final int DATA_MODE_WALKING   = 0x03;
 		public final int DATA_MODE_PAUSED    = 0x10;
 	}
 	
 	public static class DataType {
 		public static final int  DATA_COL_INVALID = 0x00;
 		public static final int  DATA_COL_STEP    = 0x01;
 		public static final int  DATA_COL_DIST    = 0x02;
 		public static final int  DATA_COL_CALS    = 0x03;
 		public static final int  DATA_COL_CADN    = 0x04;
 		public static final int  DATA_COL_HR      = 0x05;
 	}
 
 	public int mode;
 	public int hour;
 	public int minute;
 	public SparseArray<Double> data;
 	
 	// +AS:05142014 General Values
 	// When you're going to convert epoch to Date object,
 	//  You must multiply epoch to 1000L
  	public long epoch; 
  	public String dateString = null;
  	
  	// +AS:05132014 Total/Title Values
  	public static final int TYPE_INVALID = -1;
  	public static final int TYPE_TITLE = 0;
  	public static final int TYPE_ACTIVITY = 1;
  	public int displayType = TYPE_INVALID;
  	
  	public int totalSteps = 0;
  	public float totalDistance = 0f;
  	public float totalCalories = 0f;
 }