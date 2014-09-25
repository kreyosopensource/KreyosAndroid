package com.kreyos.kreyosandroid.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kreyos.kreyosandroid.R;
import com.kreyos.kreyosandroid.managers.BluetoothManager;
import com.kreyos.kreyosandroid.managers.PreferencesManager;
import com.kreyos.kreyosandroid.objectdata.SportsDataRow;
import com.kreyos.kreyosandroid.utilities.Constants;
import com.kreyos.kreyosandroid.utilities.KreyosUtility;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

/**
 * SPORTS MODE
 */
public class FragmentLeft3 extends BaseFragmentMain
        implements
        View.OnClickListener {

//--------------------------------------------------------------------------------------------------- Variables
    private RelativeLayout  mGridA              = null;
    private RelativeLayout  mGridB              = null;
    private RelativeLayout  mGridC              = null;

    private int             mGridTotal          = 0;
    private static int      MTOTAL_GRID         = 4;

    private int             MTOTAL_GRID_LABEL   = 19;
    private List<Integer> mListGridItemIndex = null;
    private int[]           mArrGridItemID          = new int[]
            {
                    R.id.layout_grid_item1,
                    R.id.layout_grid_item2,
                    R.id.layout_grid_item3,
                    R.id.layout_grid_item4
            };

    private int[] mArrGridData = new int[] { 0, 1, 4, 3, 9 };

    //widgets
    private TextView mTextTimer;

    private RelativeLayout m_bgTimer = null;

    //handle time
    public static final int MSG_TIME_SYNC = 1;
    private TimeSyncHandler msgHandler = null;
    private TimeSyncThread  workThread = null;
    private Integer secondCounter = 0;
    private boolean syncTime = false;
    // Enable setting of first value to display
    private boolean mBDefaultAlreadySet = false;


    //sport mode UI
    private ImageView mIconRunning = null;
    private ImageView mIconBiking = null;



    private byte[] sportsGridBuf = null;
    private SportsDataRow sportsDataRow = null;
    private Date mLastActivitySyncTime = null;


//--------------------------------------------------------------------------------------------------- On Create
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:SportsMode )");

        return inflater.inflate(R.layout.activity_fragment_left_3, container, false);
    }

    @Override
    public void onStart() {
        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:SportsMode ) - On Start");

        super.onStart();

        // setup fonts
        KreyosUtility.overrideFonts( this.getActivity().getBaseContext(),
                                     getView(),
                                     Constants.FONT_NAME.LEAGUE_GOTHIC_REGULAR);

        // setup views
        setupViewsAndCallbacks();

        initialize();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }

    private void setupViewsAndCallbacks() {

        mIconRunning    = (ImageView) getView().findViewById(R.id.img_ic_mode_running);
        mIconBiking     = (ImageView) getView().findViewById(R.id.img_ic_mode_cycling);
        setActiveIcons( 0 );

        mTextTimer      = (TextView) getView().findViewById(R.id.textView_timer);
        m_bgTimer       = (RelativeLayout) getView().findViewById(R.id.layout_timer);

        mGridA          = (RelativeLayout)getView().findViewById(R.id.layout_gridA);
        mGridB          = (RelativeLayout)getView().findViewById(R.id.layout_gridB);
        mGridC          = (RelativeLayout)getView().findViewById(R.id.layout_gridC);
    }

    private void initialize() {
        msgHandler = new TimeSyncHandler(this);
        workThread = new TimeSyncThread(msgHandler);

        mListGridItemIndex = new ArrayList<Integer>();
        mListGridItemIndex.add(17);
        mListGridItemIndex.add(4);
        mListGridItemIndex.add(1);

        setupGrid(3); // 2 - startGrid

        refreshDisplay();

        prepareData();
    }

    private void setActiveIcons( int pMode ) {
        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:SportsMode ) - Set Active Icons ("+ pMode +")");

        // sports mode 1 = running
        // sports mode 2 = biking
        if( pMode == 1 )
        {
            mIconRunning.setImageResource(R.drawable.frag3_img_running_active);
            mIconBiking.setImageResource(R.drawable.frag3_img_cycling_inactive);
        }
        else if ( pMode == 2 )
        {
            mIconRunning.setImageResource(R.drawable.frag3_img_running_inactive);
            mIconBiking.setImageResource(R.drawable.frag3_img_cycling_active);
        }
        else
        {
            mIconRunning.setImageResource(R.drawable.frag3_img_running_inactive);
            mIconBiking.setImageResource(R.drawable.frag3_img_cycling_inactive);
        }
    }

    private void setupGrid(int pGridTotal) {

        if(pGridTotal > 4 || pGridTotal < 2) {
            return;
        }

        MTOTAL_GRID = pGridTotal;
        mGridTotal = pGridTotal;

        mGridA.setVisibility(View.INVISIBLE);
        mGridB.setVisibility(View.INVISIBLE);
        mGridC.setVisibility(View.INVISIBLE);

        switch( mGridTotal ) {
            case 2:
                mGridA.setVisibility(View.VISIBLE);
                break;
            case 3:
                mGridB.setVisibility(View.VISIBLE);
                break;
            case 4:
                mGridC.setVisibility(View.VISIBLE);
                break;
        }

        try {
            if( mListGridItemIndex.size() > 0 ) {
                mArrGridData[1] = mListGridItemIndex.get(0);
            }
            if( mListGridItemIndex.size() > 1 ) {
                mArrGridData[2] = mListGridItemIndex.get(1);
            }
            if( mListGridItemIndex.size() > 2 ) {
                mArrGridData[3] = mListGridItemIndex.get(2);
            }
            if( mListGridItemIndex.size() > 3 ) {
                mArrGridData[4] = mListGridItemIndex.get(3);
            }

            writeWatchUIConfig();
        }
        catch(NullPointerException e) {

        }

        refreshDisplay();
    }

    public void writeWatchUIConfig() {

        // TODO: fill these with real data begin
        String[] worldClockTable = new String[6];
        worldClockTable[0] = "TZone-0";
        worldClockTable[1] = "TZone-1";
        worldClockTable[2] = "TZone-2";
        worldClockTable[3] = "TZone-3";
        worldClockTable[4] = "TZone-4";
        worldClockTable[5] = "TZone-5";

        int[] worldClockOffsets = new int[6];
        worldClockOffsets[0] = 0;
        worldClockOffsets[1] = 1;
        worldClockOffsets[2] = 2;
        worldClockOffsets[3] = 3;
        worldClockOffsets[4] = 4;
        worldClockOffsets[5] = 5;

        boolean isDigit = true;

        int analog = 1;
        int digit = 2;

        // TODO: fill these with real data end
        int sports_grid = MTOTAL_GRID - 2;// temp

        int[] sports_grids = new int[5];
        sports_grids[0] = 0;

        // for testing
        sports_grids[1] = mArrGridData[1];
        sports_grids[2] = mArrGridData[2];
        sports_grids[3] = mArrGridData[3];
        sports_grids[4] = mArrGridData[4];

        // TODO: fill the preference with read goals set from UI and call this
        // function when changed (in HomeActivity)
        int[] goals = new int[3];

        goals[0] = PreferencesManager.getInstance().retrieveDataForInt( "sports_goals.steps", 1000 );
        goals[1] = PreferencesManager.getInstance().retrieveDataForInt( "sports_goals.distance", 2000 );
        goals[2] = PreferencesManager.getInstance().retrieveDataForInt( "sports_goals.calories", 500 );


        // 50 kg
        String defaultWeight = "110";
        // 5'6 on feet
        String defaultHeight = "67";

        String weightStr = PreferencesManager.getInstance().retrieveDataForString( Constants.PREFKEY_USER_WEIGHT );

        if ( weightStr.equals( "NO VALUE" ) )
            weightStr = defaultWeight;

        if (weightStr.equals("")) {
            // Set default value if we get blank
            weightStr = defaultWeight;
        } else {
            // Convert values
            try {
                weightStr = ""
                        + KreyosUtility.convertPoundsToKilograms(Integer.parseInt(weightStr));
            } catch (Exception ex) {
                weightStr = defaultHeight;
            }
        }


        String heightStr = PreferencesManager.getInstance().retrieveDataForString( Constants.PREFKEY_USER_HEIGHT );

        if ( weightStr.equals( "NO VALUE" ) )
            weightStr = defaultHeight;

        if (heightStr.equals("")) {
            // Set default value if we get blank
            heightStr = defaultHeight;
        } else {
            // Convert values
            try {
                heightStr = ""
                        + KreyosUtility.convertInchestoCentimeters(Integer.parseInt(heightStr));
            } catch (Exception ex) {
                heightStr = defaultHeight;
            }

        }

        // Convert to integer to write on watch
        int weight = Integer.parseInt(weightStr);
        int height = Integer.parseInt(heightStr);

        // TODO: fill the preference with read goals set from UI and call this
        // function when changed (in GestureActivity)
        boolean enableGesture = PreferencesManager.getInstance().retrieveDataForBoolean( "gesture.enable", true );
        boolean isLeftHandGesture = PreferencesManager.getInstance().retrieveDataForBoolean("gesture.watch_hand", true);

        int[] actionsTable = new int[4];
        actionsTable[0] = PreferencesManager.getInstance().retrieveDataForInt( "gesture.swipe_left", 0 );
        actionsTable[1] = PreferencesManager.getInstance().retrieveDataForInt( "gesture.swipe_right", 1 );
        actionsTable[2] = PreferencesManager.getInstance().retrieveDataForInt( "gesture.twist_left", 2 );
        actionsTable[3] = PreferencesManager.getInstance().retrieveDataForInt( "gesture.twist_right", 3 );

        boolean isUkUnit = !(PreferencesManager.getInstance().retrieveDataForBoolean( "user_profile.is_metric", true ));

        BluetoothManager.getInstance().syncDeviceConfig(worldClockTable, worldClockOffsets, isDigit, analog,
                                                        digit, sports_grid, sports_grids, goals, weight, height,
                                                        enableGesture, isLeftHandGesture, actionsTable, isUkUnit);
    }


    private void refreshDisplay()
	{
		RelativeLayout grid = null;
		int gridNumber = 0;
		switch( MTOTAL_GRID )
		{
			case 4:
				grid = mGridC;
				gridNumber = 4;
				break;

			case 3:
				grid = mGridB;
				gridNumber = 3;
				break;

			case 2:
				grid = mGridA;
				gridNumber = 2;
				break;
		}

		for( int i = 0; i < gridNumber; i++ )
		{
			RelativeLayout parent = (RelativeLayout)grid.findViewById(mArrGridItemID[i]);

			TextView statText = (TextView)parent.findViewById( R.id.text_item_title );
			statText.setText("" + getDataStatsOrLabel(mListGridItemIndex.get(i), false));

			TextView lblText = (TextView)parent.findViewById(R.id.text_item_unit);
			lblText.setText("" + getDataStatsOrLabel(mListGridItemIndex.get(i), true));
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
                label = p_isLabel ? "M" : "DISTANCE";
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

    public void prepareData() {
		sportsDataRow = null;
		secondCounter = 0;
		syncTime = false;
        mLastActivitySyncTime = null;
	}

    // moved to main
//    private void showGPSSetttingsDialog() {
//
//        // Set context for dialog
//        mGPSDialogContext  = getActivity();
//
//        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mGPSDialogContext);
//
//        // Setting Dialog Title
//        alertDialog.setTitle("GPS is settings");
//
//        // Setting Dialog Message
//        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
//
//        // On pressing Settings button
//        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog,int which) {
//                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                mGPSDialogContext.startActivity(intent);
//            }
//        });
//
//        // on pressing cancel button
//        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.cancel();
//            }
//        });
//
//        // Showing Alert Message
//        alertDialog.show();
//    }





//--------------------------------------------------------------------------------------------------- Class members
    private static class TimeSyncHandler extends Handler {
        private final WeakReference<FragmentLeft3> mFragment;

        public TimeSyncHandler(FragmentLeft3 fragment) {
            mFragment = new WeakReference<FragmentLeft3>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            FragmentLeft3 fragment = mFragment.get();
            if (fragment != null) {
                fragment.handleTimeSync(msg);
            }
        }
    }

    private class TimeSyncThread extends Thread {

        private TimeSyncHandler msgHandler;

        public TimeSyncThread(TimeSyncHandler h) {
            msgHandler = h;
        }

        public void run() {
            while (true) {
                Date now = new Date();

                try{
                    if (syncTime && now.getTime() - mLastActivitySyncTime.getTime() < 15 * 1000) {
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

//--------------------------------------------------------------------------------------------------- called by classes above
    private void handleTimeSync( Message pMessage ) {
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
        mTextTimer.setText(String.format("%1$02d:%2$02d:%3$02d", h, m, s));

        if (sportsGridBuf == null && secondCounter % 5 == 1) {
            BluetoothManager.getInstance().handleTimeSync();
        }
    }


//--------------------------------------------------------------------------------------------------- Button functions

    @Override
    public void onClick(View pView) {

    }


    private int generateKey( int p_index, int p_value ) {

        while ( true )
        {
            boolean isGoodKey = true;
            p_value++;
            if( p_value > MTOTAL_GRID_LABEL )
            {
                p_value = 1;
            }

            for(int i = 0; i < mListGridItemIndex.size(); i++)
            {
                if( i != p_index )
                {
                    if( p_value == mListGridItemIndex.get(i) )
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

    // button functions directly from Main (from layout)
    public void changeValue(View view) {

        int targetIndex = -1;

        int parentId = ((ViewGroup)view.getParent()).getId();
        for( int i = 0; i < mArrGridItemID.length; i++ )
        {
            if( parentId == mArrGridItemID[i] )
            {
                targetIndex = i;
                break;
            }
        }

        int valueType = mListGridItemIndex.get(targetIndex);
        valueType = generateKey(targetIndex, valueType);

        mListGridItemIndex.set(targetIndex, valueType);
        Log.d("TAGSTAGS", "ID: "+ mListGridItemIndex.get(targetIndex));
        setupGrid(mGridTotal);
        refreshDisplay();
    }

    public void addGrid(View view) {
        //error handling
        if ( mGridTotal > 3 ) {
            mGridTotal = 4;
            return;
        } else {
            mListGridItemIndex.add(1);
            int targetIndex = mListGridItemIndex.size() - 1;
            int valueType = generateKey(targetIndex, 1);
            mListGridItemIndex.set(targetIndex, valueType);
            Log.d("LOGSLOGS",  "ADDED " + mListGridItemIndex.size());
            setupGrid(mGridTotal+1);
        }
    }

    public void removeGrid(View view) {
        //error handling
        if( mGridTotal < 3 ) {
            return;
        }
        int parentId = ((ViewGroup)view.getParent()).getId();
        for ( int i = 0; i < mArrGridItemID.length; i++ ) {
            if ( parentId == mArrGridItemID[i] ) {
                mListGridItemIndex.remove(i);
                break;
            }
        }
        setupGrid(mGridTotal-1);
    }


    // called by onDestroy && handle BT Data Manager

    public void dataEnd() {
        sportsDataRow = null;
        secondCounter = 0;
        syncTime = false;
        mLastActivitySyncTime = null;
        m_bgTimer.setBackgroundDrawable(getResources().getDrawable(R.color.bg_silver));

        // Disable flag on writing to watch
//        mIsWritingWatchEnable = false;

        // Enable setting of first value to display
        mBDefaultAlreadySet = false;

//        // Clear saved location;
//        m_oldLocation = null;
    }


//--------------------------------------------------------------------------------------------------- Listeners


//--------------------------------------------------------------------------------------------------- Called by BT Message Receiver (Main Activity)
public void displaySportsData( SportsDataRow p_datarow ) {

    Log.d("Log", "GRID " + MTOTAL_GRID);

    setActiveIcons(p_datarow.sports_mode);

    RelativeLayout grid = null;
    int gridNumber = 0;
    switch(MTOTAL_GRID) {
        case 4:
            grid = mGridC;
            gridNumber = 4;
            break;

        case 3:
            grid = mGridB;
            gridNumber = 3;
            break;

        case 2:
            grid = mGridA;
            gridNumber = 2;
            break;
    }

    for( int i = 0; i < gridNumber; i++ )
    {
        RelativeLayout parent = (RelativeLayout)grid.findViewById(mArrGridItemID[i]);

        TextView statText = (TextView)parent.findViewById( R.id.text_item_title );
        statText.setText("" + getDataStatsOrLabel(mListGridItemIndex.get(i), false));

        TextView valueText = (TextView)parent.findViewById(R.id.text_item_value);
        valueText.setText("" +p_datarow.data.get(mListGridItemIndex.get(i)));

        TextView lblText = (TextView)parent.findViewById(R.id.text_item_unit);
        lblText.setText("" + getDataStatsOrLabel(mListGridItemIndex.get(i), true));
    }
}

    private void fixFirstValueBasedonMode(int p_mode)
    {
        Log.d("SportsData", "Test");

        if(!mBDefaultAlreadySet) {

            mBDefaultAlreadySet = true;
            if(p_mode == 1) {

                mListGridItemIndex.set(0, 17);
                mListGridItemIndex.set(1, 4);
                mListGridItemIndex.set(2, 1);

                setupGrid( 3 ); // 2 - startGrid
                refreshDisplay();

            } else if(p_mode == 2) {

                mListGridItemIndex.set(0, 1);
                mListGridItemIndex.set(1, 4);
                mListGridItemIndex.set(2, 6);

                setupGrid( 3 ); // 2 - startGrid
                refreshDisplay();
            }
        }
    }


    public void receivedActivityData ( SportsDataRow pDataRow, boolean pBWritingWatchEnable ) {
        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:SportsMode ) - Received Activity Data: " + pDataRow);

        if (sportsDataRow == null)
        {
            secondCounter = 2;
            syncTime = true;
            if (workThread.getState() == Thread.State.NEW)
                workThread.start();
        }



        mLastActivitySyncTime = new Date();
        sportsDataRow = pDataRow;


        if (!pBWritingWatchEnable) {
            secondCounter = (int)sportsDataRow.seconds_elapse - 1;
        }

        // m_bgTimer.setBackground(getResources().getDrawable(R.color.active_color));
        m_bgTimer.setBackgroundDrawable(getResources().getDrawable(R.color.bg_yellow_green));
        displaySportsData(sportsDataRow);
        fixFirstValueBasedonMode(sportsDataRow.sports_mode);
    }

//    @Override
//    protected void onDestroy() {
//        // TODO Auto-generated method stub
//        super.onDestroy();
//        Log.d("SportsLog", "onDestroyed Closed");
//        btMsgHandler = null;
//        BluetoothAgent.getInstance(btMsgHandler);
//        dataEnd();
//        m_locationClient.disconnect();
//    }

}
