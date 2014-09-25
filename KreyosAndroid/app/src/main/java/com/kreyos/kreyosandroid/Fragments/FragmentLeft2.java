package com.kreyos.kreyosandroid.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.kreyos.kreyosandroid.R;
import com.kreyos.kreyosandroid.adapter.ActivityCellAdapter;
import com.kreyos.kreyosandroid.adapter.ActivityStatsAdapter;
import com.kreyos.kreyosandroid.database.DatabaseManager;
import com.kreyos.kreyosandroid.dataobjects.ActivityData;
import com.kreyos.kreyosandroid.listeners.EndScrollListener;
import com.kreyos.kreyosandroid.listeners.IEndScroll;
import com.kreyos.kreyosandroid.managers.BluetoothManager;
import com.kreyos.kreyosandroid.managers.PreferencesManager;
import com.kreyos.kreyosandroid.managers.RequestManager;
import com.kreyos.kreyosandroid.objectdata.ActivityDataDoc;
import com.kreyos.kreyosandroid.objectdata.ActivityDataRow;
import com.kreyos.kreyosandroid.utilities.Constants;
import com.kreyos.kreyosandroid.utilities.KreyosUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * OVERALL ACTIVITIES
 */

// TODO: 1) listeners, query, BTMessageHandler

public class FragmentLeft2 extends BaseFragmentMain
    implements
        View.OnClickListener,
        AbsListView.OnScrollListener,
        IEndScroll
{

//--------------------------------------------------------------------------------------------------- Variables
    private ListView            mCellListView           = null;

    private int                 mDiffDays               = 7;
    private ActivityCellAdapter mCellAdapter;

    private Map<String, RowDataContainer> mEpochCache   = null; // String: Epoch date | ArrayList: Array of data
    private boolean m_bIsFetchingActivities = false;



//--------------------------------------------------------------------------------------------------- onCreate
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:Overall )");

        return inflater.inflate(R.layout.activity_fragment_left_2, container, false);
    }

    @Override
    public void onStart() {
        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:Overall ) - On Start");

        super.onStart();

        // setup fonts
        KreyosUtility.overrideFonts( this.getActivity().getBaseContext(),
                                     getView(),
                                     Constants.FONT_NAME.LEAGUE_GOTHIC_REGULAR);

        // setup views
        setupViewsAndCallbacks();

        BluetoothManager.getInstance().checkConnectionAndGetData();

        mEpochCache = new HashMap<String, RowDataContainer>();

        // Load activity stats data from watch & database
        EndScrollListener   scrollListener = null;

        mCellListView = (ListView)getView().findViewById(R.id.listview_activity_cell);
        mCellListView.setOnScrollListener( this );

        scrollListener = new EndScrollListener( this );
        mCellListView.setOnScrollListener( scrollListener );
        mCellListView.setMinimumHeight( 20 );
        this.loadMultipleScrolls();
    }

    private void setupViewsAndCallbacks() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

    }

    public void loadMultipleScrolls() {

        // Cell list container
        long headEpoch = KreyosUtility.epoch();
        long tailEpoch = KreyosUtility.epochMinusDay( mDiffDays );

        String headDateStr = KreyosUtility.dateString( KreyosUtility.calendar( headEpoch ) );
        String tailDateStr = KreyosUtility.dateString( KreyosUtility.calendar( tailEpoch ) );

        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:Overall ) - Load Multiple Scrolls: DATE_CHECK Head:"
                + headEpoch + headDateStr + " Tail:" + tailEpoch + tailDateStr + " diffDays:" + mDiffDays + "" );

        mListener.onActivitiesStartQuery( Constants.DBKEY_OVERALL_ACTIVITIES,
                                          headEpoch,
                tailEpoch);
    }


//--------------------------------------------------------------------------------------------------- Button functions
    @Override
    public void onClick(View pView) {

    }


//--------------------------------------------------------------------------------------------------- Listener (scroll)
    @Override
    public void onScroll(
            AbsListView view,
            int firstVisibleItem,
            int visibleItemCount,
            int totalItemCount
    ) {}

    @Override
    public void onScrollStateChanged( AbsListView view, int scrollState ) {}

    @Override
    public void loadMoreScroll() {
        mDiffDays++;
        Log.i( "ActivityStatsActivity::loadMoreScroll", "ScrollCount:" + mDiffDays + "" );
        this.loadMultipleScrolls();
    }

    @SuppressWarnings("unused") // Used by the other class. 'ActivityCellAdapter'
	private void fetchDBActivityData() {
		// Still fetching..
		if(m_bIsFetchingActivities) {
			return;
		}

		m_bIsFetchingActivities = true;
		mDiffDays++;
		loadMultipleScrolls();
	}

    public void createDataForRow( ArrayList<ActivityDataRow> pLocalActivities, Cursor pQuery ) {
        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:Overall ) - Create Data For Row");

        dataRowForActivityStats( pLocalActivities, pQuery ); // with total 01
    }

    public void createCellAdapter( Context pContext, ArrayList<ActivityDataRow> pLocalActivities ) {
        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:Overall ) - Create Cell Adapter");

        mCellAdapter = new ActivityCellAdapter( pContext, R.layout.item_cell, pLocalActivities );
    }

    public void setCellAdapter() {
        if( mCellListView.getAdapter() == null ) {
            Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:Overall ) - Set Cell Adapter");

            mCellListView.setAdapter( mCellAdapter );
        }
    }

    private void dataRowForActivityStats( ArrayList<ActivityDataRow> p_list, Cursor p_query) {
        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:Overall ) - Data Row For Activity Stats: query = " + p_query);

        // always clear the cache here
        mEpochCache.clear();
        ArrayList<RowDataContainer> epochOrder = new ArrayList<RowDataContainer>();

        if ( p_query != null && p_query.moveToFirst() ) {

            final int INDEX_STEPS = p_query.getColumnIndex( "ActivitySteps" );
            final int INDEX_DISTANCE = p_query.getColumnIndex( "ActivityDistance" );
            final int INDEX_CALORIES = p_query.getColumnIndex( "ActivityCalories" );
            final int INDEX_EPOCH_ITEM = p_query.getColumnIndex( "CreatedTime" );
            final int INDEX_SPORT_ID = p_query.getColumnIndex( "Sport_ID" );
            RowDataContainer container;

            // Compute for unit values
            do {

                String steps 		= p_query.getString( INDEX_STEPS );
                String distance 	= p_query.getString( INDEX_DISTANCE );
                String calories 	= p_query.getString( INDEX_CALORIES );
                String epochTime 	= p_query.getString( INDEX_EPOCH_ITEM );
                long epoch 			= Long.parseLong( epochTime );
                String sportId		= p_query.getString( INDEX_SPORT_ID );

                Calendar calendar 	= KreyosUtility.calendar( epoch );
                String dateString 	= KreyosUtility.dateString( calendar );

                Log.i( "ActivityStatsActivity::dataRowForActivityStats", "BLE_Fetch.. dateString:" + dateString + " epoch:" + epoch + "" );


                if ( !mEpochCache.containsKey( dateString ) ) {

                    container = new RowDataContainer();
                    container.dateString = dateString;
                    container.unitValues = new ArrayList<ActivityDataRow>();
                    container.epoch = Long.parseLong( epochTime );
                    container.totalSteps += Integer.parseInt( steps );
                    container.totalDistance += Double.parseDouble( distance );
                    container.totalCalories += Double.parseDouble( calories );
                    mEpochCache.put( dateString, container );
                    Log.i( ">>>", "New Date! DATE_CHECK date:" + dateString + " epoch:" + epochTime + "" );

                    // push the data to the list
                    epochOrder.add( container );
                }
                else {

                    container = mEpochCache.get( dateString );

                    // Only add normal activity
                    if(sportId.equals("0")) {
                        container.totalSteps += Integer.parseInt( steps );
                        container.totalDistance += Double.parseDouble( distance );
                        container.totalCalories += Double.parseDouble( calories );
                    }
                    Log.i( ">>>", "Existing Date! DATE_CHECK date:" + dateString + " epoch:" + epochTime + "" );

                }

                ActivityDataRow dataRow = new ActivityDataRow();

                dataRow.dateString = dateString;
                dataRow.epoch = Long.parseLong( epochTime );
                dataRow.displayType = ActivityDataRow.TYPE_ACTIVITY;
                dataRow.hour = calendar.get(Calendar.HOUR_OF_DAY);
                dataRow.minute = calendar.get(Calendar.MINUTE);
                dataRow.mode = Integer.parseInt( sportId ); //new Random().nextInt(2); // 0,1,2. TODO: Apply
                // the actual type
                dataRow.data = new SparseArray<Double>();
                dataRow.data.append( ActivityDataRow.DataType.DATA_COL_STEP, Double.parseDouble(steps) );
                dataRow.data.append( ActivityDataRow.DataType.DATA_COL_DIST, (Double.parseDouble(distance)/1000));
                dataRow.data.append( ActivityDataRow.DataType.DATA_COL_CALS, (Double.parseDouble(calories)/1000));

                // push the values to your container
                container.unitValues.add( dataRow );

            } while ( p_query.moveToNext() );

            // Push data to p_list
            for( int i = epochOrder.size()-1; i >= 0; i-- )
            {
                RowDataContainer dataCon = epochOrder.get(i);

                // Push & Compute total values
                ActivityDataRow totalValueRow 	= new ActivityDataRow();
                totalValueRow.displayType 		= ActivityDataRow.TYPE_TITLE;
                totalValueRow.totalSteps 		= dataCon.totalSteps;
                // TODO: Create conversion utils
                totalValueRow.totalDistance 	= dataCon.totalDistance/1000;
                totalValueRow.totalCalories 	= dataCon.totalCalories/1000;
                totalValueRow.epoch 			= dataCon.epoch;
                totalValueRow.dateString		= dataCon.dateString;
                this.pushTo( totalValueRow, p_list );

                // sub categories
                ArrayList<ActivityDataRow> categories = new ArrayList<ActivityDataRow>();
                categories.add( new ActivityDataRow() );
                categories.add( new ActivityDataRow() );
                categories.add( new ActivityDataRow() );

                // Filter Unit Values
                for ( ActivityDataRow unitVal : dataCon.unitValues ) {
                    Log.i( ">>>", "Unit Values! DATE_CHECK date:" + totalValueRow.dateString + " epoch:" + totalValueRow.epoch + "" );

                    ActivityDataRow sub = categories.get( unitVal.mode );

                    // first object
                    if ( sub.dateString == null ) {
                        sub.dateString		= unitVal.dateString;
                        sub.epoch			= unitVal.epoch;
                        sub.displayType		= unitVal.displayType;
                        sub.hour			= unitVal.hour;
                        sub.minute			= unitVal.minute;
                        sub.mode			= unitVal.mode;
                        sub.data			= unitVal.data;
                    }
                    // append data
                    else {
                        double updatedSteps 	= (double)sub.data.get( ActivityDataRow.DataType.DATA_COL_STEP ) + (double)unitVal.data.get( ActivityDataRow.DataType.DATA_COL_STEP );
                        double updatedDistance 	= (double)sub.data.get( ActivityDataRow.DataType.DATA_COL_DIST ) + (double)unitVal.data.get( ActivityDataRow.DataType.DATA_COL_DIST );
                        double updateCalories 	= (double)sub.data.get( ActivityDataRow.DataType.DATA_COL_CALS ) + (double)unitVal.data.get( ActivityDataRow.DataType.DATA_COL_CALS );

                        sub.data.append( ActivityDataRow.DataType.DATA_COL_STEP, updatedSteps );
                        sub.data.append( ActivityDataRow.DataType.DATA_COL_DIST, updatedDistance );
                        sub.data.append( ActivityDataRow.DataType.DATA_COL_CALS, updateCalories );
                    }
                }

                // Push Unit Values
                for ( ActivityDataRow unitVal : categories ) {
                    if( unitVal.dateString != null ) {
                        this.pushTo( unitVal, p_list );
                    }
                }
            }
        }

        m_bIsFetchingActivities = false;
    }

    private void pushTo( ActivityDataRow dataRow, ArrayList<ActivityDataRow> p_list ) {
        Log.i( "ActivityTest::pushTo", "displayType:" + dataRow.displayType + " listCount:" + p_list.size() + "" );
        p_list.add( dataRow );
    }


    public void manageActivityData(ActivityDataDoc p_data )
    {
        Log.i( "AppHelper::displayActivityData", "Display Activity Stats" );

        int steps 			= 0;
        double distance 	= 0;
        double calories 	= 0;
        double heartRate 	= 0;

        Log.i( "AppHelper::displayActivityData", "WATCH date year:" + p_data.year + " month:" + p_data.month + " day:" + p_data.day + "" );

        Calendar ca 		= KreyosUtility.calendar();
        ca.set(Calendar.YEAR, p_data.year + 2000);
        ca.set(Calendar.MONTH, p_data.month - 1);
        ca.set(Calendar.DAY_OF_MONTH, p_data.day);

        String dateString =  KreyosUtility.dateString( ca );
        Log.i( "Bluetooth", "BLE_Fetch.. AriesA dateString:" + dateString + "" );

        JSONArray jArray = new JSONArray();

        for (ActivityDataRow row: p_data.data)
        {
            SparseArray<Double> rowData = row.data;

            try
            {
                steps += rowData.get( ActivityDataRow.DataType.DATA_COL_STEP );
            }
            catch(Exception e)
            {
                steps = 0;
            }

            try
            {
                distance += rowData.get(ActivityDataRow.DataType.DATA_COL_DIST);
            }
            catch(Exception e)
            {
                distance = 0;
            }

            try
            {
                heartRate += rowData.get(ActivityDataRow.DataType.DATA_COL_HR);
            }
            catch(Exception e)
            {
                heartRate = 0;
            }

            try
            {
                calories += rowData.get(ActivityDataRow.DataType.DATA_COL_CALS);
            }
            catch(Exception e)
            {
                calories = 0;
            }

            // recompute epoch here
            ca.set( Calendar.HOUR_OF_DAY, row.hour );
            ca.set( Calendar.MINUTE, row.minute );
            long epochTime = KreyosUtility.epoch( ca ); // Time in sec since 01/01/1970

            row.epoch = epochTime;
            row.dateString = dateString;

            Log.i( "Bluetooth", "BLE_Fetch.. AriesA dateString:" + dateString + " epoch:" + epochTime + "" );

        }

        //Log.v("steps", String.format("steps is %f", steps));
        Log.v("Distance", String.format("distance is %f", distance));
        Log.v("Heart Rate", String.format("Heart Rate is %f", heartRate));
        Log.v("calories", String.format("calories is %f", calories));

        ArrayList<ActivityDataRow> arrayHolder = new ArrayList<ActivityDataRow>();

        int periodStartHour = 0;
        int periodStartMin = 0;
        int periodMode = 0;

        double periodNormalAndSports = 0;

        double periodSteps = 0;
        double periodDistance = 0;

        double periodCalories = 0;
        double periodHR = 0;
        double periodCadence = 0;

        for ( ActivityDataRow row: p_data.data )  {

            periodMode = row.mode;

            if ( periodMode == 0 ) {

                periodSteps 		+= row.data.get(ActivityDataRow.DataType.DATA_COL_STEP, 0.0);
                periodDistance 		+= row.data.get(ActivityDataRow.DataType.DATA_COL_DIST, 0.0);
                periodCalories 		+= row.data.get(ActivityDataRow.DataType.DATA_COL_CALS, 0.0);
                periodHR 			+= row.data.get(ActivityDataRow.DataType.DATA_COL_HR, 0.0);
                periodCadence 		+= row.data.get(ActivityDataRow.DataType.DATA_COL_CADN, 0.0);

            }

            periodNormalAndSports += row.data.get(ActivityDataRow.DataType.DATA_COL_STEP, 0.0);

            arrayHolder.add(row);

            try  {

                JSONObject jObject 	= new JSONObject();
                jObject.put("Steps", row.data.get(ActivityDataRow.DataType.DATA_COL_STEP, 0.0));
                jObject.put("Distance", row.data.get(ActivityDataRow.DataType.DATA_COL_DIST, 0.0));
                jObject.put("Calories", row.data.get(ActivityDataRow.DataType.DATA_COL_CALS, 0.0));
                jArray.put(jObject);

            }
            catch ( JSONException e )  {
                // TODO Auto-generated catch block
                e.printStackTrace();//
            }

            //Log.v("periodMode", String.format("periodMode is %f", periodMode));
            Log.v("periodStartHour", Integer.toString(periodStartHour));
            Log.v("periodMode", Integer.toString(periodMode));
            Log.v("periodSteps", Double.toString(periodSteps));
            Log.v("periodDistance", Double.toString(periodDistance));
            //Log.v("periodSteps", String.format("periodSteps is %f", periodSteps));
            //Log.v("periodDistance", String.format("periodDistance is %f", periodDistance));

            Log.i( "Bluetooth", "BLE_Fetch.. AriesB pushing.. dateString:" + row.dateString+ " epoch:" + row.epoch + "" );
        }

        Log.d("Total Steps", "0:" + Double.toString(periodSteps));
        Log.d("Total Steps", "0 and 1: " +periodNormalAndSports);

        // Debug test on saving activity on web
        saveActivityStats(p_data );

        // layout
        getActivityOnLocal();

    }

    private void saveActivityStats(ActivityDataDoc p_dataDoc )
    {
        for(ActivityDataRow row : p_dataDoc.data)
        {
            // + ET 05062014 : Convert time and date to milliseconds
            Calendar ca = KreyosUtility.calendar();
            ca.set(Calendar.YEAR, p_dataDoc.year + 2000);
            ca.set(Calendar.MONTH, p_dataDoc.month - 1);
            ca.set(Calendar.DAY_OF_MONTH, p_dataDoc.day);
            ca.set(Calendar.HOUR_OF_DAY, row.hour);
            ca.set(Calendar.MINUTE, row.minute);
            ca.set(Calendar.SECOND, row.mode == 0 ? 0 : 1);

            String epochTime = Long.toString(KreyosUtility.epoch(ca));
            double steps 	= 0;
            double distance = 0;
            double calories = 0;
            double heart	= 0;
            int rowMode		= row.mode;

            if(row.data.get(ActivityDataRow.DataType.DATA_COL_STEP) != null) {
                steps = row.data.get(ActivityDataRow.DataType.DATA_COL_STEP);
            }
            if(row.data.get(ActivityDataRow.DataType.DATA_COL_DIST) != null) {
                distance = row.data.get(ActivityDataRow.DataType.DATA_COL_DIST);
            }
            if(row.data.get(ActivityDataRow.DataType.DATA_COL_CALS) != null) {
                calories = row.data.get(ActivityDataRow.DataType.DATA_COL_CALS);
            }
            if(row.data.get(ActivityDataRow.DataType.DATA_COL_HR) != null) {
                heart = row.data.get(ActivityDataRow.DataType.DATA_COL_HR);
            }

            JSONObject params = new JSONObject();

            try {

                // Construct database values
                String tableName		 		= "Kreyos_User_Activities";
                ContentValues values 			= new ContentValues();
                //values.put( "ActivitySpeed", 0.0f );
                values.put( "CreatedTime", epochTime );
                values.put( "Sport_ID", row.mode );
                values.put( "ActivityDistance", distance );
                values.put( "ActivityCalories", calories );
                values.put( "ActivitySteps", steps );


                if(DatabaseManager.instance( (Context)getActivity() ).insert( tableName, values )) {
                    Log.d("DatabaseManager", "Saved on local db");

                    boolean isActivityWillbeSavedonStack = true;

                    // Check if there's internet to send request to web
                    if( KreyosUtility.hasConnection( getActivity() ) ) {

                        params.put("auth_token", PreferencesManager.getInstance().retrieveDataForString( Constants.PREFKEY_USER_KREYOS_TOKEN ));
                        params.put("email", 	 PreferencesManager.getInstance().retrieveDataForString( Constants.PREFKEY_USER_EMAIL ));
                        params.put("time", 		epochTime );
                        params.put("sport_id", 	rowMode);
                        params.put("steps", 	steps);
                        params.put("distance", 	distance);
                        params.put("calories", 	calories);
                        params.put("heart", 	heart);

                        // Process request
                        String response = RequestManager.instance().post(Constants.PREFKEY_URL_USER_ACTIVITIES, params);

                        try {

                            JSONObject jsonResponse = new JSONObject(response);

                            // Check if request is successful else move to Stack table
                            if(jsonResponse.has("success")) {

                                // Flag that the activity is already send to web
                                isActivityWillbeSavedonStack = false;
                                Log.d("DatabaseManager", "Activity sent on web");
                            }

                        } catch(Exception ex) {

                            ex.printStackTrace();
                        }

                    }

                    // Save the activity on stack table to be send later
                    if(isActivityWillbeSavedonStack) {

                        tableName	= "Stack_Activities";
                        values 		= new ContentValues();
                        values.put( "Epoch", epochTime);
                        values.put( "RequestKey", Constants.PREFKEY_URL_USER_ACTIVITIES );
                        values.put( "JSONData", params.toString() );

                        // Saving on database
                        if(DatabaseManager.instance( (Context)getActivity() ).insert( tableName, values )) {
                            Log.d("DatabaseManager", "Save activity on stack table");
                        } else {
                            Log.e("DatabaseManager", "Saving Error:");
                        }
                    }



                } else {

                    Log.d("DatabaseManager", "Already saved");
                }

            } catch ( Exception ex ) {

                Log.d("DatabaseManager", "" + ex);
            }
        }
    }

    public void getActivityOnLocal() {

        int m_diffDays = 0;
        long headEpoch = KreyosUtility.epoch();
        long tail = KreyosUtility.epochMinusDay( m_diffDays );
        long epochMinute = 60;

        Log.i( "ActivityStatsActivity::loadMultipleScrolls", "loadMultipleScrolls DATE_CHECK Head:" + headEpoch + " Tail:" + tail + " diffDays:" + m_diffDays + "" );

        // Create custom adapter(array) and set it to the list view
        Cursor queriedData = DatabaseManager.instance( (Context)getActivity() ).queryData("SELECT * FROM Kreyos_User_Activities WHERE CreatedTime <= " + headEpoch + " AND " + "CreatedTime >= " + (tail + epochMinute) + " ORDER BY CreatedTime ASC" );
        Log.d("Queried Data Count", "" + queriedData.getCount());

        ArrayList<ActivityDataRow> testRow = new ArrayList<ActivityDataRow>();
        double totalSteps 		= 0;
        double totalDistance 	= 0;
        double totalCalories 	= 0;
        if(queriedData != null && queriedData.moveToFirst()) {
            do {

                // Log.d("Queried Data Value","" + queriedData.getString(queriedData.getColumnIndex("CreatedTime")));
                ActivityDataRow dataRow = new ActivityDataRow();
                dataRow.data = new SparseArray<Double>();

                String epochValue 		= queriedData.getString(queriedData.getColumnIndex("CreatedTime"));
                Calendar rowCalendar 	= KreyosUtility.calendar(Long.parseLong(epochValue));
                dataRow.hour 			= rowCalendar.get(Calendar.HOUR_OF_DAY);
                dataRow.minute 			= rowCalendar.get(Calendar.MINUTE);
                dataRow.mode			= Integer.parseInt(queriedData.getString(queriedData.getColumnIndex("Sport_ID")));

                dataRow.data.put(ActivityDataRow.DataType.DATA_COL_INVALID, (double) 0);
                dataRow.data.put(ActivityDataRow.DataType.DATA_COL_STEP, 	Double.parseDouble(queriedData.getString(queriedData.getColumnIndex("ActivitySteps"))));
                dataRow.data.put(ActivityDataRow.DataType.DATA_COL_DIST, 	Double.parseDouble(queriedData.getString(queriedData.getColumnIndex("ActivityDistance"))));
                dataRow.data.put(ActivityDataRow.DataType.DATA_COL_CALS, 	Double.parseDouble(queriedData.getString(queriedData.getColumnIndex("ActivityCalories"))));
                dataRow.data.put(ActivityDataRow.DataType.DATA_COL_CADN, 	(double) 0);
                dataRow.data.put(ActivityDataRow.DataType.DATA_COL_HR, 		(double) 0);

                if(dataRow.mode == 0) {
                    totalSteps += Double.parseDouble(queriedData.getString(queriedData.getColumnIndex("ActivitySteps")));
                    totalDistance += Double.parseDouble(queriedData.getString(queriedData.getColumnIndex("ActivityDistance")));
                    totalCalories += Double.parseDouble(queriedData.getString(queriedData.getColumnIndex("ActivityCalories")));
                }

                Log.d("Hour", "" + rowCalendar.get(Calendar.HOUR_OF_DAY));
                Log.d("Steps", "" + queriedData.getString(queriedData.getColumnIndex("ActivitySteps")));

                testRow.add(dataRow);

            } while(queriedData.moveToNext());
        }


        ActivityStatsAdapter adapter = new ActivityStatsAdapter( getActivity(), R.layout.item_unit_activity_values, testRow );

        ActivityData activityData = new ActivityData(
                adapter,
                KreyosUtility.setDataForDisplay((double)totalSteps),
                KreyosUtility.setDataForDisplay((double)(totalDistance/1000)),
                KreyosUtility.setDataForDisplay((double)(totalCalories /1000)));

//        return activityData;
    }



    // NEW_DB_QUERY
// loadMultipleScrolls DATE_CHECK Head:
// Title
    class RowDataContainer
    {
        public int totalSteps 						= 0;
        public float totalDistance 					= 0f;
        public float totalCalories 					= 0f;
        public String dateString 					= null;
        public Long epoch 							= 0L;
        // Cell
        public ArrayList<ActivityDataRow> unitValues = null;
    }


}
