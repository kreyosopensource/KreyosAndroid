package com.kreyos.watch;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import android.R.bool;
import android.R.drawable;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
//import android.widget.Toast;

import com.coboltforge.slidemenu.SlideMenu;
import com.coboltforge.slidemenu.SlideMenuInterface.OnSlideMenuItemClickListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.kreyos.watch.R;
import com.kreyos.watch.bluetooth.BluetoothAgent;
import com.kreyos.watch.bluetooth.Protocol;
import com.kreyos.watch.gps.GPSTracker;
import com.kreyos.watch.managers.AppHelper;
import com.kreyos.watch.managers.AppHelper.WATCH_STATE_VALUE;
import com.kreyos.watch.objectdata.SportsDataRow;
import com.kreyos.watch.utils.LocationUpdater;

public class SportsActivity  extends KreyosActivity implements 
GooglePlayServicesClient.ConnectionCallbacks, 
GooglePlayServicesClient.OnConnectionFailedListener, 
OnMyLocationChangeListener, 
OnSlideMenuItemClickListener, com.google.android.gms.location.LocationListener
{	
	private SlidingMenu slidemenu;
	private SlidingMenu slidemenu_right;
	private boolean isLeftMenuSelected = false;
	
	RelativeLayout gridA;
	RelativeLayout gridB;
	RelativeLayout gridC;
	int gridTotal = 0;
	static int finalTotalGrid = 4;
	
	List<Integer> gridItemIndex;
	int[] GRID_ITEM = new int[] 
	{
			R.id.layout_gridItem1,
			R.id.layout_gridItem2,
			R.id.layout_gridItem3,
			R.id.layout_gridItem4
	};
	int TOTAL_GRID_LABEL = 19;
	
	//handling the bluetooth service
	private BTDataHandler btMsgHandler = null;
	
	//play pause finish resume
	LinearLayout linearlayout_play;
	LinearLayout linearlayout_pause;
	LinearLayout linearlayout_resumefinish;
	
	//layouts
	RelativeLayout relativeLayout_mode;
	
	//widgets
	TextView textView_timer;
	ImageView imageView_setworkout;
	ImageView imageView_mode1;
	ImageView imageView_mode2;
	ImageView imageView_mode3;	
	
	RelativeLayout m_bgTimer = null;
	
	//handle time
	public static final int MSG_TIME_SYNC = 1;
	private TimeSyncThread  workThread = null;
	private TimeSyncHandler msgHandler = null;
	private Integer secondCounter = 0;
	private boolean syncTime = false;
	

	//sport mode UI
	private ImageView m_sportsModeRunning = null;
	private ImageView m_sportsModeBiking = null;
	
	// GPS variables
	LocationClient m_locationClient 	= null;
	LocationRequest m_locationRequest 	= null;
	boolean mIsWritingWatchEnable 		= false;
	Context mGPSDialogContext			= null;
	
	public SportsActivity() 
	{
		msgHandler = new TimeSyncHandler(this);
		workThread = new TimeSyncThread(msgHandler);
		btMsgHandler = new BTDataHandler(this);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature( Window.FEATURE_NO_TITLE );
		setContentView(R.layout.sports_layout);
		
		KreyosUtility.overrideFonts(this, ((ViewGroup)findViewById(android.R.id.content)).getChildAt(0), KreyosUtility.FONT_NAME.LEAGUE_GOTHIC_REGULAR);
		
		setupSlideMenu();
		
		// Check if KreyosService is still alive
		onCallonCreate();
		
		declarations();
		
		gridA = (RelativeLayout)findViewById(R.id.layout_gridTypeA);
		gridB = (RelativeLayout)findViewById(R.id.layout_gridTypeB);
		gridC = (RelativeLayout)findViewById(R.id.layout_gridTypeC);

		
		gridItemIndex = new ArrayList<Integer>();
		gridItemIndex.add(17);
		gridItemIndex.add(4);
		gridItemIndex.add(1);
		//gridItemIndex.add(3);
		setupGrid(3); // 2 - startGrid
		
		refreshDisplay();
		
		//binding the Message Handler
		// BluetoothAgent.getInstance(btMsgHandler).bindMessageHandler(btMsgHandler);
		BluetoothAgent.getInstance(btMsgHandler);
		
		// Create loction request
		m_locationRequest = LocationRequest.create();
	       
		// Use high accuracy
		m_locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        
		// Set the update interval to 3 seconds
		m_locationRequest.setInterval(3000);
       
		// Set the fastest update interval to 1 second
		m_locationRequest.setFastestInterval(1000); 
		
		// Start connection on location client
		m_locationClient = new LocationClient(this, this, this);
		m_locationClient.connect();
		
		dataPrepare();
		
		// Checking of gps
		if (!isGPSEnable()) {
			// Show gps settings dialog
			showGPSSetttingsDialog();
		}
	}
	
	
	private void setupSlideMenu()
	{
	    /*
	     * Overload the init method, added boolean to check if left or right animations
	     */
	    slidemenu = (SlidingMenu) findViewById(R.id.slideMenu);
		slidemenu.init(this, R.menu.slide, this, 333, true); // left animation
		
		slidemenu_right = (SlidingMenu) findViewById(R.id.slideMenu_right);
		slidemenu_right.init(this, R.menu.right_slide, this, 333, false); //right animation

		// connect the fallback button in case there is no ActionBar
		ImageView b = (ImageView) findViewById(R.id.imageView_menu1);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				isLeftMenuSelected = true;
				slidemenu.show();
			}
		});
		
		
		ImageView imageView_menu2 = (ImageView) findViewById(R.id.imageView_menu2);
		imageView_menu2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				isLeftMenuSelected = false;
				slidemenu_right.show();
			}
		});
	}
	
	
	@Override
	public void onSlideMenuItemClick(int itemId) 
	{
		AppHelper.instance().onSwitchActivity(isLeftMenuSelected, this, itemId);
	}

	
	private void declarations() {

		// widgets
		textView_timer = (TextView) findViewById(R.id.textView_timer);
		m_bgTimer = (RelativeLayout)findViewById(R.id.relativeLayout2);
//		imageView_setworkout = (ImageView) findViewById(R.id.imageView_setworkout);
//		imageView_mode1 = (ImageView) findViewById(R.id.imageView_mode1);
//		imageView_mode2 = (ImageView) findViewById(R.id.imageView_mode2);
//		imageView_mode3 = (ImageView) findViewById(R.id.imageView_mode3);
		
		
		m_sportsModeRunning = (ImageView) findViewById(R.id.image_mode_running);
		m_sportsModeBiking = (ImageView) findViewById(R.id.image_mode_biking);
		setupSportsModeUI( 0 );
		
	}
	
	
	private boolean isGPSEnable() {
		// Get gps status
		LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        boolean gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		return gpsStatus;
	}
	
	
	private void showGPSSetttingsDialog() {
		
		// Set context for dialog
		mGPSDialogContext  = this;
		
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mGPSDialogContext);
	      
        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");
  
        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
  
        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mGPSDialogContext.startActivity(intent);
            }
        });
  
        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            }
        });
  
        // Showing Alert Message
        alertDialog.show();
	}
	
	
	private void loadPauseOperation()
	{
		linearlayout_play.setVisibility(View.INVISIBLE);
		linearlayout_pause.setVisibility(View.VISIBLE);
		linearlayout_resumefinish.setVisibility(View.INVISIBLE);
	}

	
	@Override
	public void onMyLocationChange(Location arg0) 
	{
		// TODO Auto-generated method stub
	}
	

@Override
	public void onConnectionFailed(ConnectionResult result) 
	{
		// TODO Auto-generated method stub
		
	}
	

@Override	
	public void onConnected(Bundle connectionHint)
	{
		// TODO Auto-generated method stub
		try {
			// 
			m_locationClient.requestLocationUpdates(m_locationRequest, this);	
		} catch (Exception ex) {
			// Log exception
			Log.e("GPS", "" + ex);
		}
			
	}
	

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}

   	
   	public static int getGridNumber() {
		return finalTotalGrid;
	}
		
	
   	/*public void addGrid(View view) {
		if(gridTotal == 4){
			finalTotalGrid = 4;
		}else{
			finalTotalGrid = gridTotal+1;
			setupGrid(gridTotal+1);
		}
		
	}
	
	public void removeGrid(View view){
		finalTotalGrid = gridTotal-1;
		setupGrid(gridTotal-1);
	} */
   	
   	
	//--------------------------------------
	public void changeValue(View view) 
	{
		
		int targetIndex = -1;
		
		int parentId = ((ViewGroup)view.getParent()).getId();
		for( int i = 0; i < GRID_ITEM.length; i++ ) 
		{
			if( parentId == GRID_ITEM[i] )
			{
				targetIndex = i;
				break;
			}
		}
		
		int valueType = gridItemIndex.get(targetIndex);
		valueType = generateKey(targetIndex, valueType);
	
		gridItemIndex.set(targetIndex, valueType);
		Log.d("TAGSTAGS", "ID: "+ gridItemIndex.get(targetIndex));
		setupGrid(gridTotal);
		refreshDisplay();
	}
	

	private int generateKey( int p_index, int p_value )
	{
		
		while ( true ) 
		{
			boolean isGoodKey = true;
			p_value++;
			if( p_value > TOTAL_GRID_LABEL ) 
			{
				p_value = 1;
			}
			
			for(int i = 0; i < gridItemIndex.size(); i++)
			{
				if( i != p_index ) 
				{
					if( p_value == gridItemIndex.get(i) )
					{
						isGoodKey = false;
					}
				}
			}
			
			if( isGoodKey )
			{
				break;
			}
		}
		return p_value;
	}
	
	
	public void addGrid(View view) {
		//error handling
		if ( gridTotal > 3 ) {
			gridTotal = 4;
			return;
		} else {
			gridItemIndex.add(1);
			int targetIndex = gridItemIndex.size() - 1;
			int valueType = generateKey(targetIndex, 1);
			gridItemIndex.set(targetIndex, valueType);
			Log.d("LOGSLOGS",  "ADDED " + gridItemIndex.size());
			setupGrid(gridTotal+1);
		}
	}
	
	
	public void removeGrid(View view) {
		//error handling
		if( gridTotal < 3 ) {
			return;
		}
		int parentId = ((ViewGroup)view.getParent()).getId();
		for ( int i = 0; i < GRID_ITEM.length; i++ ) {
			if ( parentId == GRID_ITEM[i] ) {
				gridItemIndex.remove(i);
				break;
			}
		}
		setupGrid(gridTotal-1);
	}
	
	
	/**
	 * Hiding of layout for optimization tricks
	 * @param p_gridTotal
	 */
	private void setupGrid(int p_gridTotal) {
		
		if(p_gridTotal > 4 || p_gridTotal < 2)  
		{
			return;
		}
		
		finalTotalGrid = p_gridTotal;
		gridTotal = p_gridTotal;
		
		gridA.setVisibility(View.INVISIBLE);
		gridB.setVisibility(View.INVISIBLE);
		gridC.setVisibility(View.INVISIBLE);
		
		switch(gridTotal)
		{
		case 2:
			gridA.setVisibility(View.VISIBLE);
			break;
		case 3:
			gridB.setVisibility(View.VISIBLE);
			break;
		case 4:
			gridC.setVisibility(View.VISIBLE);
			break;
		}
		
		try
		{
			if( gridItemIndex.size() > 0 )
			{
				sports_grids_data[1] = gridItemIndex.get(0);
			}
			if( gridItemIndex.size() > 1 ) 
			{
				sports_grids_data[2] = gridItemIndex.get(1);
			}
			if( gridItemIndex.size() > 2 ) 
			{
				sports_grids_data[3] = gridItemIndex.get(2);
			}
			if( gridItemIndex.size() > 3 ) 
			{
				sports_grids_data[4] = gridItemIndex.get(3);
			}
			

			writeWatchUIConfig();
		}
		catch(NullPointerException e)
		{
			
		}
		
		refreshDisplay();
		
		//displaySportsData();
	}
	
	
	public void displaySportsData( SportsDataRow p_datarow ) 
	{
//		if (sportsDataRow == null || sportsGridBuf == null)
//			return;
//		
//		Log.e("Timer", Integer.toString(sportsDataRow.seconds_elapse));
//		
//		//LinearLayout layout = (LinearLayout) inflater.inflate(gridLayoutId, null).findViewById(gridViewId);
//
//		//TODO: enumerate data in sportsDataRow to display
//		//unit for each type:
//		//speed    - km/hour
//		//distance - meter
//		//altitude - meter
//		//calories = kcal
//		
//		Log.e("sportsDataRow.data", ""+sportsDataRow.data.valueAt(2));
		
		Log.d("Log", "GRID " + finalTotalGrid);
		
		setupSportsModeUI( p_datarow.sports_mode );
		
		RelativeLayout grid = null;
		int gridNumber = 0;
		switch(finalTotalGrid) {
			case 4:
				grid = gridC;
				gridNumber = 4;
				break;
			
			case 3:
				grid = gridB;
				gridNumber = 3;
				break;
				
			case 2:
				grid = gridA;
				gridNumber = 2;
				break;
		}

		for( int i = 0; i < gridNumber; i++ )
		{
			RelativeLayout parent = (RelativeLayout)grid.findViewById(GRID_ITEM[i]);
			
			TextView statText = (TextView)parent.findViewById( R.id.txt_statsDisplay );
			statText.setText("" + getDataStatsOrLabel(gridItemIndex.get(i), false));
			
			TextView valueText = (TextView)parent.findViewById(R.id.txt_valueDisplay);
			valueText.setText("" +p_datarow.data.get(gridItemIndex.get(i)));
			
			TextView lblText = (TextView)parent.findViewById(R.id.txt_labelValue);
			lblText.setText("" + getDataStatsOrLabel(gridItemIndex.get(i), true));
		}
	}
	
	
	int[] m_sportValues = new int[8];
	private void calculateSportsValues(SportsDataRow p_datarow)
	{
		
		// + ET 05092014 : Display Index
		// 0 - Speed
		// 1 - Average Speed
		// 2 - Top Speed
		
		double steps 	 	= p_datarow.data.get(gridItemIndex.get(0));
		double distance 	= p_datarow.data.get(gridItemIndex.get(1));
		double speed		= p_datarow.data.get(gridItemIndex.get(2));
		double calories		= p_datarow.data.get(gridItemIndex.get(3));
		
		
	}
	
	
	private void refreshDisplay()
	{
		RelativeLayout grid = null;
		int gridNumber = 0;
		switch( finalTotalGrid  ) 
		{
			case 4:
				grid = gridC;
				gridNumber = 4;
				break;
			
			case 3:
				grid = gridB;
				gridNumber = 3;
				break;
				
			case 2:
				grid = gridA;
				gridNumber = 2;
				break;
		}

		for( int i = 0; i < gridNumber; i++ )
		{
			RelativeLayout parent = (RelativeLayout)grid.findViewById(GRID_ITEM[i]);
			
			TextView statText = (TextView)parent.findViewById( R.id.txt_statsDisplay );
			statText.setText("" + getDataStatsOrLabel(gridItemIndex.get(i), false));
			
			TextView lblText = (TextView)parent.findViewById(R.id.txt_labelValue);
			lblText.setText("" + getDataStatsOrLabel(gridItemIndex.get(i), true));
		}
	}

	
	private String getDataStatsOrLabel( int p_index, boolean p_isLabel ) 
	{
		String label = "";
		
		switch( p_index )
		{
			case SportsDataRow.DataType.DATA_SPEED:
				label = p_isLabel ? "MPH" : "SPEED";
				break;
		
			case SportsDataRow.DataType.DATA_HEARTRATE:
				label = p_isLabel ? "BPM" : "HEARTRATE";
				break;
			
			case SportsDataRow.DataType.DATA_CALS:
				label = p_isLabel ? "CAL" : "CALORIES";
				break;
			
			case SportsDataRow.DataType.DATA_DISTANCE:
				label = p_isLabel ? "MT" : "DISTANCE";
				break;
				
			case SportsDataRow.DataType.DATA_SPEED_AVG:
				label = p_isLabel ? "MPH" : "AVE SPEED";
				break;
			
			case SportsDataRow.DataType.DATA_ALTITUTE:
				label = p_isLabel ? "MT" : "ALTITUDE";
				break;
			
			case SportsDataRow.DataType.DATA_TIME:
				label = p_isLabel ? "" : "TIME";
				break;
			
			case SportsDataRow.DataType.DATA_SPEED_TOP:
				label = p_isLabel ? "MPH" : "TOP SPEED";
				break;
			
			case SportsDataRow.DataType.DATA_CADENCE:
				label = p_isLabel ? "CPM" : "CADENCE";
				break;
			
			case SportsDataRow.DataType.DATA_PACE:
				label = p_isLabel ? "MIN" : "PACE";
				break;
			
			case SportsDataRow.DataType.DATA_HEARTRATE_AVG:
				label = p_isLabel ? "BPM" : "AVG HEART";
				break;
			
			case SportsDataRow.DataType.DATA_HEARTRATE_TOTAL:
				label = p_isLabel ? "BPM" : "TOP HEART";
				break;
			
			case SportsDataRow.DataType.DATA_ELEVATION_GAIN:
				label = p_isLabel ? "MT" :"ELEVATION GAIN";
				break;
			
			case SportsDataRow.DataType.DATA_CURRENT_LAP:
				label = p_isLabel ? "MIN" : "CURRENT LAP";
				break;
			
			
			case SportsDataRow.DataType.DATA_BEST_LAP:
				label = p_isLabel ? "MIN" : "BEST LAP";
				break;
			
			case SportsDataRow.DataType.DATA_FLOORS:
				label = p_isLabel ? "" : "FLOORS";
				break;
			
			case SportsDataRow.DataType.DATA_STEPS:
				label = p_isLabel ? "" : "STEPS";
				break;
			
			case SportsDataRow.DataType.DATA_PACE_AVG:
				label = p_isLabel ? "MIN" : "AVG PACE";
				break;
				
			case SportsDataRow.DataType.DATA_LAP_AVG:
				label = p_isLabel ? "" : "AVG LAP";
				break;
		}
		return label;
	}
	
	
	private void setupSportsModeUI( int p_mode )
	{
		Log.d("Log", "Sporst Mode: " +  p_mode);
		
		// sports mode 1 = running
		// sports mode 2 = biking
		if( p_mode == 1 )
		{
			m_sportsModeRunning.setImageResource(R.drawable.running_active);
			m_sportsModeBiking.setImageResource(R.drawable.cycling_inactive);
		}
		else if ( p_mode == 2 )
		{
			m_sportsModeRunning.setImageResource(R.drawable.running_inactive);
			m_sportsModeBiking.setImageResource(R.drawable.cycling_active);
		}
		else
		{
			m_sportsModeRunning.setImageResource(R.drawable.running_inactive);
			m_sportsModeBiking.setImageResource(R.drawable.cycling_inactive);
		}
	}
	
	
	private byte[] sportsGridBuf = null;
	private SportsDataRow sportsDataRow = null;
	private Date lastActivitySyncTime = null;
	
	public void handleBTData(Message msg)
	{

		Log.d("SportsData", String.format("Incoming BT Message:%x", msg.what));
		
		switch (msg.what) {
		case Protocol.MessageID.MSG_ACTIVITY_DATA_GOT: //5
		{
			if (sportsDataRow == null)
			{
				secondCounter = 2;
	    		syncTime = true;
	    		if (workThread.getState() == Thread.State.NEW)
	    			workThread.start();
			}
			
			

			lastActivitySyncTime = new Date();
			sportsDataRow = (SportsDataRow)msg.obj;
			
			if (!mIsWritingWatchEnable) {
				secondCounter = (int)sportsDataRow.seconds_elapse - 1;
			}
			
			// m_bgTimer.setBackground(getResources().getDrawable(R.color.active_color));
			m_bgTimer.setBackgroundDrawable(getResources().getDrawable(R.color.active_color));
			displaySportsData(sportsDataRow);
			fixFirstValueBasedonMode(sportsDataRow.sports_mode);
			
			
			
			
			// Enable flag to start writing on watch
			mIsWritingWatchEnable = true;
		}	
		break;

		case Protocol.MessageID.MSG_ACTIVITY_DATA_END: //7
			dataEnd();
			break;

		case Protocol.MessageID.MSG_GRID_GOT:
			/*
			sportsGridBuf = (byte[])msg.obj;
			for (int i = 0; i < sportsGridBuf.length; ++i)  
				Log.v("SportsData", String.format("GRID[%d]:%d", i, sportsGridBuf[i]));
			*/
			break;

		case Protocol.MessageID.MSG_ACTIVITY_PREPARE: //8
			dataPrepare();
			break;
			
		case Protocol.MessageID.MSG_BLUETOOTH_STATUS:
			if (msg.obj.toString() == "Running") {
				connecBluetoothHeadset();
			} else {
				AppHelper.instance().WATCH_STATE = AppHelper.WATCH_STATE_VALUE.DISCONNECTED;
				setHeaderByConnection();
			}
			break;	
		
		}
	}
	
	private void dataPrepare() {
		sportsDataRow = null;
		secondCounter = 0;
		syncTime = false;
		lastActivitySyncTime = null;
	}
	
	private void dataEnd() {
		sportsDataRow = null;			
		secondCounter = 0;
		syncTime = false;
		lastActivitySyncTime = null;
		//m_bgTimer.setBackground(getResources().getDrawable(R.color.lightgray_1));
		m_bgTimer.setBackgroundDrawable(getResources().getDrawable(R.color.lightgray_1));
		
		// Disable flag on writing to watch
		mIsWritingWatchEnable = false;
		
		// Enable setting of first value to display
		m_isDefaultAlreadySet = false;
		
		// Clear saved location;
		m_oldLocation = null;
	}
	
	// + ET 05082014 : Set default value based on mode
	/*
	Mode : Running
	- Steps
	- Distance
	- Speed
	
	Mode:  Cycling
	- Speed 
	- Distance
	- Altitude
	   
	*/
	
	
	private boolean m_isDefaultAlreadySet = false;
	
	
	private void fixFirstValueBasedonMode(int p_mode)
	{
		Log.d("SportsData", "Test");
		
		if(!m_isDefaultAlreadySet) {
			
			m_isDefaultAlreadySet = true;
			if(p_mode == 1) {
				
				gridItemIndex.set(0, 17);
				gridItemIndex.set(1, 4);
				gridItemIndex.set(2, 1);
				
				setupGrid( 3 ); // 2 - startGrid
				refreshDisplay();
				
			} else if(p_mode == 2) {
				
				gridItemIndex.set(0, 1);
				gridItemIndex.set(1, 4);
				gridItemIndex.set(2, 6);
				
				setupGrid( 3 ); // 2 - startGrid
				refreshDisplay();
			}
		}
	}

	
    private static class BTDataHandler extends Handler {
        private final WeakReference<SportsActivity> mActivity;

        public BTDataHandler(SportsActivity activity) {
        	mActivity = new WeakReference<SportsActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
        	SportsActivity activity = mActivity.get();
			if (activity != null) {
				activity.handleBTData(msg);
			}
        }
    }

    
	public void handleTimeSync(Message msg) {
		if (!syncTime)
			return;

		secondCounter++;
		if (secondCounter >= 24 * 60 * 60)
			secondCounter -= 24 * 60 * 60;

		int temp = secondCounter;

		int h = temp / (60 * 60);
		temp = temp % (60 * 60);
		int m = temp / 60;
		temp = temp % 60;
		int s = temp;
		Log.v("SportsData", String.format("Time:%d", temp));
		textView_timer.setText(String.format("%1$02d:%2$02d:%3$02d", h, m, s));
		//added joe for GPS Timer
		//textView_gpstimer.setText(String.format("%1$02d:%2$02d:%3$02d", h, m, s));

		Protocol p = new Protocol(BluetoothAgent.getInstance(btMsgHandler), this);
		if (sportsGridBuf == null && secondCounter % 5 == 1)
		{
			BluetoothAgent.getInstance(btMsgHandler).bindMessageHandler(btMsgHandler);
			p.getSportsGrid();
		}
		
//		loadPauseOperation();

		//temp displaySportsData(R.layout.sports_grid_4, R.id.sports_grid_4);
	}
	
	
	private class TimeSyncThread extends Thread {

		private TimeSyncHandler msgHandler;

		public TimeSyncThread(TimeSyncHandler h) {
			msgHandler = h;
		}

		public void run() {
			while (true) {
				Date now = new Date();

				//added joe to view the pause 
				//loadPauseOperation();
				try{
				if (syncTime && now.getTime() - SportsActivity.this.lastActivitySyncTime.getTime() < 15 * 1000) {
					msgHandler.obtainMessage(MSG_TIME_SYNC, null).sendToTarget();
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					return;
				}
				}catch(NullPointerException e){
					while (true) {
						msgHandler.obtainMessage(MSG_TIME_SYNC, null).sendToTarget();
						try {
							Thread.sleep(1000);
						} catch (InterruptedException ie) {
							return;
						}
					}
				}
			}
		}
	};

	
    private static class TimeSyncHandler extends Handler {
        private final WeakReference<SportsActivity> mActivity;

        public TimeSyncHandler(SportsActivity activity) {
        	mActivity = new WeakReference<SportsActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
        	SportsActivity activity = mActivity.get();
            if (activity != null) {
            	activity.handleTimeSync(msg);
            }
        }
    }
    
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	Log.d("SportsLog", "onDestroyed Closed");
		btMsgHandler = null;
		BluetoothAgent.getInstance(btMsgHandler);
    	dataEnd();
    	m_locationClient.disconnect();
    }
    
    
    Location m_oldLocation = null;
    
    
    @Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub

    	short distance = 0;
    	
    	//TODO: calculate this
		int calories = 0; 
		
		if (m_oldLocation != null 
		&& mIsWritingWatchEnable) {
			
			// Compute for distance
			distance = (short)Math.abs(m_oldLocation.distanceTo(location));
			
			// Send watch gps values
			if (BluetoothAgent.getInstance(null) != null) {
				Protocol p = new Protocol(BluetoothAgent.getInstance(null), null);
				p.sendGPSInfo(
						location, 
						distance, 
						calories);
			}
			
			// Logs on GPS
			
			// Log.d("GPS", "Location XLA:" + location.getLatitude());
			// Log.d("GPS", "Location XLO:" + location.getLongitude()); 
			
			
			// Logging Altitude
			// Log.d("GPS", "Location Altitiude: " + location.getAltitude());
    	}
		    	    		
		m_oldLocation  = location;
	}
      
}
