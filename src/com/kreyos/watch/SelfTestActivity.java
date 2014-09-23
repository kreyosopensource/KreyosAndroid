package com.kreyos.watch;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import com.google.android.gms.internal.gi;
import com.kreyos.watch.R;
import com.kreyos.watch.bluetooth.BluetoothAgent;
import com.kreyos.watch.bluetooth.Protocol;
import com.kreyos.watch.objectdata.ActivityDataDoc;
import com.kreyos.watch.objectdata.ActivityDataRow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class SelfTestActivity extends KreyosActivity {

	private static final String defaultFileUrl = "http://freebsd.cloudapp.net/~howardsu/upgrade.bin";

	private BTDataHandler btMsgHandler = null;

	public SelfTestActivity() {
		btMsgHandler = new BTDataHandler(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.more_selfttest_activity);
		
//		setSubActivityEntry(R.id.btnBackToSettings, "More", HomeActivity.class);
		View btnBack = findViewById(R.id.btnBackToSettings);
		btnBack.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent i1 = new Intent(SelfTestActivity.this, HomeActivity.class);
				startActivity(i1);
				finish();
			}
		});
		
		
		BluetoothAgent.getInstance(btMsgHandler).bindMessageHandler(
				btMsgHandler);

		EditText editBox = (EditText) findViewById(R.id.editTextFileURL);
		editBox.setText(getPrefs().getString("fileUlr", defaultFileUrl));

		View btnUpload = findViewById(R.id.btnUploadFile);
		btnUpload.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				final EditText urlText = (EditText) findViewById(R.id.editTextFileURL);
				SharedPreferences.Editor editor = getPrefs().edit();
				editor.putString("fileUrl", urlText.getText().toString());
				editor.commit();

				AsyncTask<Void, Void, InputStream> t = new AsyncTask<Void, Void, InputStream>() {
					protected InputStream doInBackground(Void... p) {
						try {

							URL aURL = new URL(urlText.getText().toString());
							URLConnection conn = aURL.openConnection();
							conn.setUseCaches(true);
							conn.connect();
							InputStream is = conn.getInputStream();
							Protocol btp = new Protocol(BluetoothAgent
									.getInstance(null), SelfTestActivity.this);
							btp.sendStream("firmware", is);
							is.close();

						} catch (IOException e) {
							e.printStackTrace();
						}
						return null;
					}

					protected void onPostExecute(InputStream is) {
					}
				};
				t.execute();
			}
		});

		View btnSync = findViewById(R.id.buttonSyncSportsData);
		btnSync.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Protocol p = new Protocol(BluetoothAgent.getInstance(btMsgHandler), SelfTestActivity.this);
				p.getActivityData();
			}
		});

		TextView fwVerText = (TextView) findViewById(R.id.textViewWatchFWVer);
		String fwVer = getPrefs().getString("watch.version", "unknown");
		String value = String.format("Watch FW Ver:%s", fwVer);
		fwVerText.setText(value);

		//TextView btStatusText = (TextView) findViewById(R.id.textViewBTStatus);
		//String btStatusVal = String.format("BTAgent:%s", BluetoothAgent.getInstance(btMsgHandler));
		//btStatusText.setText(btStatusVal);
	

		TextView btStatusText = (TextView) findViewById(R.id.textViewBTStatus);
		String btStatusVal = String.format("BTAgent:%s", BluetoothAgent.getInstance(btMsgHandler).getStatus());
		btStatusText.setText(btStatusVal);
	}

	private static class BTDataHandler extends Handler {
		private final WeakReference<SelfTestActivity> mService;

		public BTDataHandler(SelfTestActivity service) {
			mService = new WeakReference<SelfTestActivity>(service);
		}

		@Override
		public void handleMessage(Message msg) {
			SelfTestActivity service = mService.get();
			if (service != null) {
				service.handleBTData(msg);
			}
		}
	}

	private void handleBTData(Message msg) {

		Log.e("MSG_FROM_WATCH", ":"+msg.obj.toString());
		
		switch (msg.what) {
		case Protocol.MessageID.MSG_BLUETOOTH_STATUS: {
			TextView text = (TextView) findViewById(R.id.textViewBTStatus);
			String value = String.format("BTAgent:%s", msg.obj.toString());
			text.setText(value);

			Log.e("MSG_BLUETOOTH_STATUS", ":"+msg.obj.toString());
		}
			break;
		case Protocol.MessageID.MSG_FIRMWARE_VERSION: {
			TextView text = (TextView) findViewById(R.id.textViewWatchFWVer);
			String version = (String) msg.obj;
			String value = String.format("Watch FW Ver:%s", version);
			text.setText(value);
			Log.e("MSG_FIRMWARE_VERSION", ":"+version);
		}
			break;
		case Protocol.MessageID.MSG_FILE_RECEIVED: {
			TextView text = (TextView) findViewById(R.id.textViewSportsData);
			ActivityDataDoc dataDoc = (ActivityDataDoc) msg.obj;
			
			double steps = 0;
			double distance = 0;
			double calories = 0;
			double heartRate = 0;
			
			for (ActivityDataRow row: dataDoc.data) {
				try{
					steps += row.data.get(ActivityDataRow.DataType.DATA_COL_STEP);
				}catch(NullPointerException e){
					steps = 0;
				}
				
				try{
					distance += row.data.get(ActivityDataRow.DataType.DATA_COL_DIST);
				}catch(NullPointerException e){
					distance = 0;
				}
				
				try{
					heartRate += row.data.get(ActivityDataRow.DataType.DATA_COL_HR);
				}catch(NullPointerException e){
					heartRate = 0;
				}				
				
				try{
					calories += row.data.get(ActivityDataRow.DataType.DATA_COL_CALS);
				}catch(NullPointerException e){
					calories = 0;
				}
	        }  
			
			Log.v("steps", String.format("steps is %f", steps));
			Log.v("Distance", String.format("distance is %f", distance));
			Log.v("Heart Rate", String.format("Heart Rate is %f", heartRate));
			Log.v("calories", String.format("calories is %f", calories));
			
			
			ArrayList<ActivityDataRow> agregatedData = new ArrayList<ActivityDataRow>();
			
			int periodStartHour = 0; 
			int periodStartMin = 0;
			int periodMode = 0;
			
			double periodSteps = 0;
			double periodDistance = 0;

			for (ActivityDataRow row: dataDoc.data) {
				if (periodMode != row.mode) {
					//archive data
					ActivityDataRow activity = new ActivityDataRow();
					activity.hour = periodStartHour;
					activity.minute = periodStartMin;
					activity.mode = periodMode;
					
					activity.data = new SparseArray<Double>();
					activity.data.append(ActivityDataRow.DataType.DATA_COL_STEP, periodSteps);
					activity.data.append(ActivityDataRow.DataType.DATA_COL_DIST, periodDistance);
					
					agregatedData.add(activity);
				}
				
				periodMode = row.mode;
				periodSteps += row.data.get(ActivityDataRow.DataType.DATA_COL_STEP, 0.0);
				periodDistance += row.data.get(ActivityDataRow.DataType.DATA_COL_DIST, 0.0);
				

				//Log.v("periodMode", String.format("periodMode is %f", periodMode));
				Log.v("periodMode", Integer.toString(periodMode));
				Log.v("periodSteps", Double.toString(periodSteps));
				Log.v("periodDistance", Double.toString(periodDistance));
				//Log.v("periodSteps", String.format("periodSteps is %f", periodSteps));
				//Log.v("periodDistance", String.format("periodDistance is %f", periodDistance));
	        }  
			

			//steps is total steps now
		}	
			break;
		}

	}
}
