package com.kreyos.watch;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;

import com.coboltforge.slidemenu.SlideMenuInterface.OnSlideMenuItemClickListener;
import com.kreyos.watch.R;
import com.kreyos.watch.adapter.ActivityCellAdapter;
import com.kreyos.watch.bluetooth.BluetoothAgent;
import com.kreyos.watch.bluetooth.Protocol;
import com.kreyos.watch.dataobjects.ActivityData;
import com.kreyos.watch.db.DBManager;
import com.kreyos.watch.listeners.EndScrollListener;
import com.kreyos.watch.listeners.IEndScroll;
import com.kreyos.watch.listeners.IQueryEvent;
import com.kreyos.watch.managers.AppHelper;
import com.kreyos.watch.managers.AppHelper.WATCH_STATE_VALUE;
import com.kreyos.watch.objectdata.ActivityDataDoc;
import com.kreyos.watch.objectdata.ActivityDataRow;
import com.kreyos.watch.utils.Utils;

public class ActivityStatsActivity extends KreyosActivity 
implements 
	OnSlideMenuItemClickListener,
	OnScrollListener,
	IEndScroll,
	IQueryEvent
{
	private static final String USER_ACTIVITIES_QUERY	= "USER_ACTIVITIES";
	
	private BTDataHandler btMsgHandler 					= null;
	private Map<String, RowDataContainer> m_epochCache 	= null; // String: Epoch date | ArrayList: Array of data
	private SlidingMenu slidemenu						= null;
	private SlidingMenu slidemenu_right					= null;
	private boolean isLeftMenuSelected 					= false;
	private ListView m_cellListView						= null;
	
	// falgs
	private boolean m_bIsFetchingActivityies			= false;
	private int m_diffDays								= 7;
	private EndScrollListener m_scrollListener			= null;
	
	// Added this members for running some functions on other thread
	private long m_headEpoch = 0L;
	private long m_tailEpoch = 0L;
	private ActivityCellAdapter m_cellAdapter;

	public ActivityStatsActivity() 
	{
		btMsgHandler 		= new BTDataHandler( this );
		m_epochCache 		= new HashMap<String, RowDataContainer>();
		//m_progressDialogs	= new ArrayList<ProgressDialog>();
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        this.requestWindowFeature( Window.FEATURE_NO_TITLE );
        this.setContentView( R.layout.activity_stats );
 		KreyosUtility.overrideFonts( ActivityStatsActivity.this, 
 				((ViewGroup)findViewById(android.R.id.content)).getChildAt(0), 
 				KreyosUtility.FONT_NAME.LEAGUE_GOTHIC_REGULAR );
        
        this.setupSlideMenu();
		this.onCallonCreate();
		
        try {
        	if( AppHelper.instance().WATCH_STATE == WATCH_STATE_VALUE.CONNECTED ) {
        		Protocol p = new Protocol( BluetoothAgent.getInstance( btMsgHandler ), ActivityStatsActivity.this );
            	p.getActivityData();
        	}
        }
        catch(NullPointerException e) {
        	// Error! either no connection or not connected to watch
        	Log.i( "ActivityStatsActivity::Constructor", "No connection! Handle load data from database here" );
        }
        
        // Load Activity Stats data from watch & db
        m_cellListView = (ListView)this.findViewById( R.id.lv_activity_cell );
        m_cellListView.setOnScrollListener( this );
        m_scrollListener = new EndScrollListener( (IEndScroll)this );
        m_cellListView.setOnScrollListener( m_scrollListener );
	 	m_cellListView.setMinimumHeight( 20 );
        this.loadMultipleScrolls();
	}
	
	private void loadMultipleScrolls() {
		// Cell list container
        m_headEpoch = Utils.epoch();
        m_tailEpoch = Utils.epochMinusDay( m_diffDays );
        String headDateStr = Utils.dateString( Utils.calendar( m_headEpoch ) );
        String tailDateStr = Utils.dateString( Utils.calendar( m_tailEpoch ) );
        
        Log.i( "ActivityStatsActivity::loadMultipleScrolls", "loadMultipleScrolls DATE_CHECK Head:" + m_headEpoch + headDateStr + " Tail:" + m_tailEpoch + tailDateStr + " diffDays:" + m_diffDays + "" );
        
        // Start Query
        DBManager queryObject = new DBManager( (Context)this, this );
        queryObject.init();
        // queryObject.query( USER_ACTIVITIES_QUERY, "SELECT * FROM Kreyos_User_Activities WHERE CreatedTime <= " + m_headEpoch + " AND " + "CreatedTime >= " + m_tailEpoch + " ORDER BY CreatedTime ASC" );
        queryObject.query( USER_ACTIVITIES_QUERY, "SELECT * FROM Kreyos_User_Activities ORDER BY CreatedTime ASC" );
	}
	
	@SuppressWarnings("unused") // Used by the other class. 'ActivityCellAdapter'
	private void fetchDBActivityData() {
		// Still fetching..
		if( m_bIsFetchingActivityies ) {
			return;
		}
		
		m_bIsFetchingActivityies = true;
		m_diffDays++;
		this.loadMultipleScrolls();
	}
	
	private void dataRowForActivityStats(
		ArrayList<ActivityDataRow> p_list,
		Cursor p_query
	) {
		// always clear the cache here
		m_epochCache.clear();
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
				
				Calendar calendar 	= Utils.calendar( epoch );
				String dateString 	= Utils.dateString( calendar );
				
				Log.i( "ActivityStatsActivity::dataRowForActivityStats", "BLE_Fetch.. dateString:" + dateString + " epoch:" + epoch + "" );
				
				/** +AS:05152014
				 *	TODO:
				 *		Check the epochDate container if it has the current date,
				 *		Instantiate a new array inside the map
				 *		Push the row data to the newly created map.
				 *		Push the array elements sequentially to p_list.
				 *
				 **/
				if ( !m_epochCache.containsKey( dateString ) ) {
					
					container = new RowDataContainer();
					container.dateString = dateString;
					container.unitValues = new ArrayList<ActivityDataRow>();
					container.epoch = Long.parseLong( epochTime );
					container.totalSteps += Integer.parseInt( steps );
					container.totalDistance += Double.parseDouble( distance );
					container.totalCalories += Double.parseDouble( calories );
					m_epochCache.put( dateString, container );
					Log.i( ">>>", "New Date! DATE_CHECK date:" + dateString + " epoch:" + epochTime + "" );
					
					// push the data to the list
					epochOrder.add( container );
				}
				else {
					
					container = m_epochCache.get( dateString );
					
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
		
		m_bIsFetchingActivityies = false;
	}
	
	private void pushTo( ActivityDataRow dataRow, ArrayList<ActivityDataRow> p_list ) {
		Log.i( "ActivityTest::pushTo", "displayType:" + dataRow.displayType + " listCount:" + p_list.size() + "" );
		p_list.add( dataRow );
	}

	private void setupSlideMenu() {
	    /*
	     * Overload the init method, added boolean to check if left or right animations
	     */
	    slidemenu = (SlidingMenu) findViewById(R.id.slideMenu);
		slidemenu.init(this, R.menu.slide, this, 333, true); // left animation
		
		slidemenu_right = (SlidingMenu) findViewById(R.id.slideMenu_right);
		slidemenu_right.init(this, R.menu.right_slide, this, 333, false); //right animation

		// connect the fallback button in case there is no ActionBar
		ImageView b = (ImageView) findViewById(R.id.imageView_menu1);
		b.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick( View v ) {
				isLeftMenuSelected = true;
				slidemenu.show();
			}
		});
		
		
		ImageView imageView_menu2 = (ImageView)findViewById(R.id.imageView_menu2);
		imageView_menu2.setOnClickListener( new OnClickListener()  {
			@Override
			public void onClick( View v ) {
				isLeftMenuSelected = false;
				slidemenu_right.show();
			}
		});
	}
	
	@Override
	public void onSlideMenuItemClick( int itemId ) {
		AppHelper.instance().onSwitchActivity( isLeftMenuSelected, this, itemId );
	}
	
	/**
	 * @author Kreyos
	 *
	 * Bluetooth Listener
	 *
	 */
	private static class BTDataHandler extends Handler {
		private final WeakReference<ActivityStatsActivity> mService;

		public BTDataHandler( ActivityStatsActivity service ) {
			mService = new WeakReference<ActivityStatsActivity>( service );
		}

		@Override
		public void handleMessage( Message msg ) {
			Log.i( "BTDataHandler::handleMessage", "Blue Message received." );
			ActivityStatsActivity service = mService.get();
			if (service != null) {
				Log.i( "BTDataHandler::handleMessage", "Has Valid Message!" );
				service.handleBTData( msg );
			}
			else {
				Log.i( "BTDataHandler::handleMessage", "NULL Service!" );
			}
		}
	}
	
	private void handleBTData( Message msg ) {
		switch (msg.what)
		{
			// + ET 04242014 : Move to Sports Mode	
			case Protocol.MessageID.MSG_ACTIVITY_PREPARE: 
			{
				Intent i3 = new Intent(ActivityStatsActivity.this, SportsActivity.class);
				startActivity(i3);
				finish();
			}
			break;
			
			case Protocol.MessageID.MSG_BLUETOOTH_STATUS: 
			{
				//TextView text = (TextView) findViewById(R.id.textViewBTStatus);
				//String value = String.format("BTAgent:%s", msg.obj.toString());
				//text.setText(value);
				Log.e("MSG_BLUETOOTH_STATUS", ":"+msg.obj.toString());
				if( msg.obj.toString() == "Running" ) {
					//m_progressDialog.dismiss();
					connecBluetoothHeadset();
				} else {
					AppHelper.instance().WATCH_STATE = AppHelper.WATCH_STATE_VALUE.DISCONNECTED;
					setHeaderByConnection();
				}
			}
			break;
			
			case Protocol.MessageID.MSG_FIRMWARE_VERSION:
			{
				//TextView text = (TextView) findViewById(R.id.textViewWatchFWVer);
				String version = (String) msg.obj;
				//String value = String.format("Watch FW Ver:%s", version);
				//text.setText(value);
				Log.e("MSG_FIRMWARE_VERSION", ":"+version);
			}
			break;
			
			case Protocol.MessageID.MSG_FILE_RECEIVED: 
			{
				Log.i( "Bluetooth", "BLE_Fetch.. MSG_FILE_RECEIVED" );
				
				// +AS:05142014
				/*
				m_progressDialog = ProgressDialog.show(this, "Please wait",	"Syncing Activity Data", true);
				
				ActivityDataDoc dataDoc = (ActivityDataDoc) msg.obj;
				ActivityData stats =  AppHelper.instance().displayActivityData(dataDoc, this, AppHelper.ACTIVITY_STATS);
				listview.setAdapter(stats.m_adapter);	
				textView_totalStepsActivityStats.setText(stats.m_steps);
				textView_totalDistanceActivityStats.setText(stats.m_distance);
				textView_totalCaloriesActivityStats.setText(stats.m_calories);
				
				m_progressDialog.dismiss();
				//*/
				
				// +AS:05142014
				// TODO: Adjust the adapter here and update the data latest from watch
				// Save/Update the db data here
				ActivityDataDoc dataDoc = (ActivityDataDoc)msg.obj;
				@SuppressWarnings("unused")
				ActivityData stats =  AppHelper.instance().displayActivityData( dataDoc, this, AppHelper.ACTIVITY_STATS );
				
				// Now.. update the adapter
				this.loadMultipleScrolls();
			}
			break;
		}
	}

	/************************************************************
	 * Scroll Listener Functions
	 **/
	@Override
	public void onScroll(
		AbsListView view, 
		int firstVisibleItem,
		int visibleItemCount, 
		int totalItemCount
	) {
		/*
		// TODO Auto-generated method stub
		if( m_cellListView.getLastVisiblePosition() == m_cellListView.getAdapter().getCount() - 1
			&& m_cellListView.getChildAt(m_cellListView.getChildCount() - 1).getBottom() <= m_cellListView.getHeight() 
		) {
			Log.i( "ActivityStatsActivity::onScroll", "Last page of the list view!" );
			this.fetchDBActivityData();
		}
		//*/
	}

	@Override
	public void onScrollStateChanged( AbsListView view, int scrollState ) {
		// TODO Auto-generated method stub
	}

	/*****************************************************************************
	 * End Scroll Delegate Method
	 **/
	@Override
	public void loadMoreScroll() {
		// TODO Auto-generated method stub
		m_diffDays++;
		Log.i( "ActivityStatsActivity::loadMoreScroll", "ScrollCount:" + m_diffDays + "" );
        this.loadMultipleScrolls();
	}

	/*****************************************************************************
	 * Query Manager callbacks
	 **/
	@Override
	public void onQueryStart( String p_queryKey ) {
		Log.i( "ActivityStatsActivity::onQueryStart", "NEW_DB_QUERY p_queryKey:"+p_queryKey+"" );
		// Show progress dialog that let's the user know
        //m_progressDialogs.add( ProgressDialog.show(this, "Please wait",	"Syncing Activity Data", true) );
		m_progressDialog = ProgressDialog.show(this, "Please wait",	"Syncing Activity Data", true);
	}
	
	@Override
	public void onQueryComplete( String p_queryKey, Cursor p_query ) {
		Log.i( "ActivityStatsActivity::onQueryComplete", "NEW_DB_QUERY p_queryKey:"+p_queryKey+" p_query:"+p_query+"" );
		
		m_progressDialog.dismiss();
		
		ArrayList<ActivityDataRow> localActivities = new ArrayList<ActivityDataRow>();
 		ActivityStatsActivity.this.dataRowForActivityStats( localActivities, p_query ); // with total 01
 		
 		/*
 		Map<String, RowDataContainer> epochCache = m_epochCache;
	 	int prevSize = m_epochCache.size();
 		
 		if( prevSize != 0 && prevSize <= m_epochCache.size() && m_epochCache.size() != 0 ) {
 			m_epochCache = epochCache;
 			m_scrollListener.noScrollLoaded();
 			//m_progressDialogs.get( 0 ).dismiss();
 			//m_progressDialogs.remove( 0 );
 			m_progressDialog.dismiss();
 			KreyosUtility.overrideFonts( ActivityStatsActivity.this, ((ViewGroup)findViewById(android.R.id.content)).getChildAt(0), KreyosUtility.FONT_NAME.LEAGUE_GOTHIC_REGULAR );	
 			return;
 		}
 		//*/
 		
 		try {
 			m_cellAdapter = new ActivityCellAdapter( (Context)ActivityStatsActivity.this, R.layout.activity_stats_cell, localActivities );
 			ActivityStatsActivity.this.runOnUiThread(new Runnable() {
 			    public void run() {
 			    	if( m_cellListView.getAdapter() == null ) {
 		 				m_cellListView.setAdapter( m_cellAdapter );
 		 			}
 			    }
 			});
 		} 
 		catch(NullPointerException e) {
        	// Error! either no connection or not connected to watch
        	Log.i( "ActivityStatsActivity::onQueryComplete", "Exception!" );
        	e.printStackTrace();
        }
 		
		/*
 		KreyosUtility.overrideFonts( ActivityStatsActivity.this, 
 				((ViewGroup)findViewById(android.R.id.content)).getChildAt(0), 
 				KreyosUtility.FONT_NAME.LEAGUE_GOTHIC_REGULAR );	
 		//*/

	}

	@Override
	public void onQueryError( String p_queryKey, String p_error ) {
		Log.i( "ActivityStatsActivity::onQueryError", "NEW_DB_QUERY p_queryKey:"+p_queryKey+" p_error:"+p_error+"" );
		//m_progressDialogs.get( 0 ).dismiss();
 		//m_progressDialogs.remove( 0 );
		m_progressDialog.dismiss();
	}
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
