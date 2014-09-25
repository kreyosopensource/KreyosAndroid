package com.kreyos.kreyosandroid.objectdata;

import android.util.Log;
import android.util.SparseArray;
import com.kreyos.kreyosandroid.bluetooth.Protocol;

public class SportsDataRow
{
	public static class DataType 
	{
		public static final byte DATA_WORKOUT	 = 0;
		public static final byte DATA_SPEED 	 = 1;
		public static final byte DATA_HEARTRATE = 2;
		public static final byte DATA_CALS		 = 3;
		public static final byte DATA_DISTANCE	 = 4;
		public static final byte DATA_SPEED_AVG = 5;
		public static final byte DATA_ALTITUTE	 = 6;
		public static final byte DATA_TIME		 = 7;
		public static final byte DATA_SPEED_TOP = 8;
		public static final byte DATA_CADENCE   = 9;
		public static final byte DATA_PACE      = 10;

		public static final byte DATA_HEARTRATE_AVG    = 11;
		public static final byte DATA_HEARTRATE_TOTAL  = 12;
		public static final byte DATA_ELEVATION_GAIN   = 13;
		public static final byte DATA_CURRENT_LAP      = 14;
		public static final byte DATA_BEST_LAP         = 15;
		public static final byte DATA_FLOORS           = 16;
		public static final byte DATA_STEPS            = 17;
		public static final byte DATA_PACE_AVG         = 18;
		public static final byte DATA_LAP_AVG          = 19;
		
		public static final int SPORTS_MODE_NORMAL  = 0x00;
		public static final int SPORTS_MODE_RUNNING = 0x01;
		public static final int SPORTS_MODE_BIKING  = 0x02;
		public static final int SPORTS_MODE_WALK    = 0x03;
		public static final int SPORTS_MODE_PAUSING = 0x10;
	}
	
	public int sports_mode;
	public int seconds_elapse;
	public SparseArray<Double> data;

	public static SportsDataRow loadFromBuffer(byte[] buf) {
		SportsDataRow row = new SportsDataRow();

		row.data = new SparseArray<Double>();

		int cursor = 0;
		int grid_num = ((int)buf[cursor]) & 0x000000ff; cursor++;
		Log.v("SportsDataParser", String.format("GridCount=%d", grid_num));

		// + ET 041514 : Commented because it limit the showing of values
		//if (grid_num > 5)
			//return null;

		int data_start_offset = cursor + grid_num;
		for (int i = 0; i < grid_num-1; ++i) {

			//get the value
			int key = ((int)buf[cursor]) & 0x000000ff;
			int intvalue = Protocol.bytesToInt(buf, data_start_offset + i * 4);
			cursor++;

			switch (key) 
			{
			case DataType.DATA_WORKOUT:
				row.seconds_elapse = intvalue;
				break;
			case DataType.DATA_SPEED:
			case DataType.DATA_SPEED_AVG:
			case DataType.DATA_SPEED_TOP:
				double speedValue = (double)intvalue * 36 / 1000;
				row.data.append(key,  Math.round(speedValue * 100.0) / 100.0);
				break;				
			case DataType.DATA_DISTANCE:
				row.data.append(key, (double)(intvalue) / 10);
				break;
			default:
				row.data.append(key, (double)(intvalue));
				break;
			}

			Log.v("SportsDataParser", String.format("Data: %d - %d", key, intvalue));
		}

		return row;
	}
}