package com.kreyos.watch;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.http.impl.conn.Wire;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.coboltforge.slidemenu.SlideMenuInterface.OnSlideMenuItemClickListener;
import com.kreyos.watch.R;
import com.kreyos.watch.adapter.ActivityStatsAdapter;
import com.kreyos.watch.bluetooth.BluetoothAgent;
import com.kreyos.watch.bluetooth.Protocol;
import com.kreyos.watch.customcarouselslider.CarouselManager;
import com.kreyos.watch.customcarouselslider.CarouselPageAdapter;
import com.kreyos.watch.managers.AppHelper;
import com.kreyos.watch.managers.AppHelper.WATCH_STATE_VALUE;
import com.kreyos.watch.objectdata.ActivityDataDoc;
import com.kreyos.watch.objectdata.ActivityDataRow;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
//import android.widget.Toast;

public class DailyTargetActivity extends KreyosActivity 
implements 
	OnSlideMenuItemClickListener,
	OnClickListener
{
	
	private BTDataHandler btMsgHandler = null;

	public DailyTargetActivity() 
	{
		btMsgHandler = new BTDataHandler(this);
		
		// Register message handler
 		if( AppHelper.instance().WATCH_STATE == WATCH_STATE_VALUE.CONNECTED ) {
 			BluetoothAgent.getInstance(btMsgHandler);
 		}
	}
	
	private static class BTDataHandler extends Handler 
	{
		private final WeakReference<DailyTargetActivity> mService;

		public BTDataHandler(DailyTargetActivity service)
		{
			mService = new WeakReference<DailyTargetActivity>(service);
		}

		@Override
		public void handleMessage(Message msg)
		{
			DailyTargetActivity service = mService.get();
			if (service != null) 
			{
				service.handleBTData(msg);
			}
		}
	}
	
	private void handleBTData(Message msg)
	{

		switch (msg.what) 
		{
			// + ET 04242014 : Move to Sports Mode	
			case Protocol.MessageID.MSG_ACTIVITY_PREPARE: 
			{
				Log.d("Log", "Aaron PUNKS");
				Intent i3 = new Intent(DailyTargetActivity.this, SportsActivity.class);
				startActivity(i3);
				finish();
			}
			break;
			
			
			case Protocol.MessageID.MSG_BLUETOOTH_STATUS:
			{
				if( msg.obj.toString() == "Running" ) {
					connecBluetoothHeadset();
				} else {
					AppHelper.instance().WATCH_STATE = AppHelper.WATCH_STATE_VALUE.DISCONNECTED;
					setHeaderByConnection();
				}
			}
			break;
		}
	}
	
	
	// + ET 040714 : Slide menu variables
	private SlidingMenu slidemenu;
	private SlidingMenu slidemenu_right;
	private boolean isLeftMenuSelected = false;
	
	// + ET 040714 : Daily Target Slider variables
	private RelativeLayout[] gridItems;
	SeekBar m_stepsSlider = null;
	short[] targetValues = new short[] {
		9000,
		79,
		23
	};		
	String[] targetLabel = new String[] {
		"STEPS",
		"KMS",
		"HRS"
	};
	final short[] minTargetValues = new short[] {
		1000,
		1,
		1
	};
	int[] m_stepsSliderPoints = new int[] {
		2000,
		4000,
		6000,
		8000,
		10000
	};
	public CarouselPageAdapter mAdapter;
	public ViewPager mPager;
	CarouselManager mCarouselManager;
	private int mTargetValue = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature( Window.FEATURE_NO_TITLE );
		setContentView(R.layout.daily_target1);
		initialize();
	} 
	
	/************************************************
	 * Setup/Initialize Methods
	 **/
	private void initialize() {
		
		setupSlideMenu();
		
		// Override Font style and size
		KreyosUtility.overrideFonts(this, 
				((ViewGroup)findViewById(android.R.id.content)).getChildAt(0), 
				KreyosUtility.FONT_NAME.LEAGUE_GOTHIC_REGULAR);
		
		// Check watch connection
		onCallonCreate();
		
		// Setup the carousel manager
		mPager = (ViewPager) findViewById(R.id.myviewpager);
		mAdapter = new CarouselPageAdapter(this, this.getSupportFragmentManager());
		mCarouselManager = new CarouselManager(this, mPager, mAdapter);
		
		// Load previous target value
		DisplayMetrics dimension = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dimension);
		SharedPreferences prefs = getPrefs();
		mTargetValue = prefs.getInt(KreyosPrefKeys.SPORTS_GOAL_STEPS, 0);
		if (mTargetValue == 0) { 
			mTargetValue = 1; 
		} else {
			mTargetValue = mTargetValue/1000;
		}
		mCarouselManager.init(mTargetValue, -(int)(dimension.widthPixels * 0.7f), 5);
		
		// Delay scaling of carousel because cannot find the rootview
		Handler delay = new Handler();
		Runnable r = new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mAdapter.adjustScale(CarouselManager.FIRST_PAGE + (mTargetValue - 1), 0, 0);
			}
		};
		delay.postDelayed(r, 300); // 0.3 secs
		
		// Initialize update button
		Button updateButton = ((Button)findViewById(R.id.btn_update_daily_target));
		updateButton.setOnClickListener(this);
	}
	
	private void setupSlideMenu() {

		slidemenu = (SlidingMenu) findViewById(R.id.slideMenu);
		slidemenu.init(this, R.menu.slide, this, 333, true); // left animation
		slidemenu_right = (SlidingMenu) findViewById(R.id.slideMenu_right);
		slidemenu_right.init(this, R.menu.right_slide, this, 333, false); //right animation
        ImageView leftSlide = (ImageView) findViewById(R.id.imageView_menu1);
        ImageView rightSlide = (ImageView) findViewById(R.id.imageView_menu2);
        leftSlide.setOnClickListener(this);
        rightSlide.setOnClickListener(this);
	}
	
	
	/************************************************
	 * Callback/Listeners Methods
	 **/
	@Override
	public void onSlideMenuItemClick(int itemId) {
		AppHelper.instance().onSwitchActivity(isLeftMenuSelected, this, itemId);
	}
	
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.imageView_menu1:
			isLeftMenuSelected = true;
			slidemenu.show();
			break;
		case R.id.imageView_menu2:
			isLeftMenuSelected = false;
			slidemenu_right.show();
			break;
		case R.id.btn_update_daily_target:
			// int modulo = mCarouselManager.getSelectedPage() % 30;
			//Log.d("Log", "" + modulo);
			updateDailyTarget();
			break;
		default:
			break;
		}
	}
	
	
	/************************************************
	 * Other Methods
	 **/
	private void updateDailyTarget() {

		if (AppHelper.instance().WATCH_STATE == WATCH_STATE_VALUE.DISCONNECTED) {
			KreyosUtility.showErrorMessage(this, "Device not found"	, "Please connect your Kreyos Watch");
			return;
		}
		
		int targetValue = (mCarouselManager.getSelectedPage() % 30) * 1000;
		if (targetValue == 0) { targetValue = 30000; }
		if (targetValue < 0 ) { targetValue += 30000; }
		// Log.d("TargetValue", "" + targetValue);
		//*
		SharedPreferences.Editor editor = getPrefs().edit();
		editor.putInt(KreyosPrefKeys.SPORTS_GOAL_STEPS, targetValue);
		editor.commit();
		writeWatchUIConfig();
		KreyosUtility.showPopUpDialog(this, "Update Watch", "Your watch is updated!");
		//*/
	}
	
	private void setupDailyTargetSlider() {
		/*
		gridItems = new RelativeLayout[3];
		gridItems[0] = (RelativeLayout)findViewById(R.id.daily_target_grid1);
		gridItems[1] = (RelativeLayout)findViewById(R.id.daily_target_grid2);
		gridItems[2] = (RelativeLayout)findViewById(R.id.daily_target_grid3);
	
		// + ET 040714 : Set target values
		for( byte i = 0; i < gridItems.length; i++ ) 
		{
			SeekBar slider = (SeekBar)gridItems[i].findViewById(R.id.daily_slider);
			slider.setMax( targetValues[i] );
			
			TextView label 	= (TextView)gridItems[i].findViewById(R.id.daily_label );
			label.setText( targetLabel[i] );
			
			TextView value 	= (TextView)gridItems[i].findViewById(R.id.daily_value );
			value.setText( "" + minTargetValues[i] );
		}
		
		// + ET 040714 : Assign callbacks and triggers for every slider on the grid
		for( byte i = 0; i < gridItems.length; i++ ) 
		{
			final byte counter = i;
			SeekBar slider 		 	= (SeekBar)gridItems[i].findViewById(R.id.daily_slider);
			final TextView value 	= (TextView)gridItems[i].findViewById(R.id.daily_value );
			
			slider.setOnSeekBarChangeListener( new OnSeekBarChangeListener()
			{
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) 
				{
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) 
				{
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) 
				{
					progress += minTargetValues[counter];
					value.setText("" + progress);
				}
			});
			
		}
		
		m_stepsSlider = (SeekBar)findViewById(R.id.target_slider_steps);
		m_stepsSlider.setMax(100);
		m_stepsSlider.setProgress(50);
		m_stepsSlider.setOnSeekBarChangeListener(new OnSeekBarChangeListener() 
		{
			
			@Override
			public void onStopTrackingTouch(SeekBar arg0)
			{
				// TODO Auto-generated method stub
				onSnapOnPoint();
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar arg0)
			{
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar arg0, int progress, boolean arg2)
			{
				// TODO Auto-generated method stub
				
			}
		});
		*/
		
	}
	
	
	private void onSnapOnPoint()
	{
		int progress = m_stepsSlider.getProgress();
		
			 if(progress > 87)
		{
			progress = progress > 90 ? 100 : 75;
			m_stepsSlider.setProgress(progress);
			return;
		}
		else if(progress > 62)
		{
			progress = progress > 50 ? 75 : 50;
			m_stepsSlider.setProgress(progress);
			return;
		}
		else if(progress > 37)
		{
			progress = progress > 30 ? 50 : 25;
			m_stepsSlider.setProgress(progress);
			return;
		}
		else if(progress > 12)
		{
			progress = progress > 10 ? 25 : 0;
			m_stepsSlider.setProgress(progress);
			return;
		}
		else
		{
			m_stepsSlider.setProgress(0);
			return;
		}
	}
	
	
	private int getStepsValue(int p_value)
	{
		switch(p_value)
		{
		case 0:
			p_value = 4000;
			break;
			
		case 25:
			p_value = 6000;
			break;
				
		case 50:
			p_value = 8000;
			break;
			
		case 75:
			p_value = 10000;
			break;
			
		case 100:
			p_value = 12000;
			break;
		}
		return p_value;
	}

	

}
