package com.kreyos.watch;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.TimeZone;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
//import android.widget.Toast;

import com.coboltforge.slidemenu.SlideMenuInterface.OnSlideMenuItemClickListener;
import com.kreyos.watch.R;
import com.kreyos.watch.bluetooth.BluetoothAgent;
import com.kreyos.watch.bluetooth.Protocol;
import com.kreyos.watch.managers.AppHelper;
import com.kreyos.watch.managers.AppHelper.WATCH_STATE_VALUE;
import com.kreyos.watch.utils.Utils;

public class DateTimeActivity extends KreyosActivity implements OnSlideMenuItemClickListener{
	
	private BTDataHandler btMsgHandler = null;
	
	public DateTimeActivity() 
	{
		btMsgHandler = new BTDataHandler(this);
		msgHandler = new TimeSyncHandler(this);
		workThread = new TimeSyncThread(msgHandler);
		if( AppHelper.instance().WATCH_STATE == WATCH_STATE_VALUE.CONNECTED ) {
 			BluetoothAgent.getInstance(btMsgHandler);
 		}
	}
	
	private static class BTDataHandler extends Handler 
	{
		private final WeakReference<DateTimeActivity> mService;

		public BTDataHandler(DateTimeActivity service)
		{
			mService = new WeakReference<DateTimeActivity>(service);
		}

		@Override
		public void handleMessage(Message msg)
		{
			DateTimeActivity service = mService.get();
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
				Intent i3 = new Intent(DateTimeActivity.this, SportsActivity.class);
				startActivity(i3);
				finish();
			}
			break;
			
			case Protocol.MessageID.MSG_BLUETOOTH_STATUS:
			{
				if ( msg.obj.toString() == "Running" ) {
					connecBluetoothHeadset();
				} else {
					AppHelper.instance().WATCH_STATE = AppHelper.WATCH_STATE_VALUE.DISCONNECTED;
					setHeaderByConnection();
				}
			}
			break;
		}
	}
	
		/* create new slideMenu for the right */
	private SlidingMenu slidemenu;
	private SlidingMenu slidemenu_right;
	private boolean isLeftMenuSelected = false;
	
	public static final int MSG_TIME_SYNC = 1;
	
	private TimeSyncThread  workThread = null;
	private TimeSyncHandler msgHandler = null;
	
	private TextView textViewDate = null;
	private TextView textViewTime = null;
	private TextView textViewTZ   = null;
	private TextView textViewSync = null;
	private ImageView btnAutoSyncSwitch = null;
	
	
	private ImageView m_btnSetAuto = null;
	private boolean m_isAutoOn = false;
	
	private TextView m_btnDate = null;
	private TextView m_btnTime = null;
	private Button m_btnUpdate = null;
	
	private Date m_date = new Date();
	private Date m_time = new Date();
	
	
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        setContentView(R.layout.more_datetime_activity);
        KreyosUtility.overrideFonts(this, ((ViewGroup)findViewById(android.R.id.content)).getChildAt(0), KreyosUtility.FONT_NAME.LEAGUE_GOTHIC_REGULAR);
        setupSlideMenu();
        SharedPreferences prefs = KreyosActivity.getPrefs();
        m_isAutoOn = prefs.getBoolean("auto_sync_time", true);
        setupLayoutAndTriggers();
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
			public void onClick(View v) 
			{
				isLeftMenuSelected = true;
				slidemenu.show();
			}
		});
		
		
		ImageView imageView_menu2 = (ImageView) findViewById(R.id.imageView_menu2);
		imageView_menu2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) 
			{
				isLeftMenuSelected = false;
				slidemenu_right.show();
			}
		});
	}
	
	
	private void setupLayoutAndTriggers()
	{
		m_btnSetAuto = (ImageView)((RelativeLayout)findViewById(R.id.layout_set_automatically)).findViewById(R.id.image_btn_on_off); 
		m_btnDate = (TextView)((RelativeLayout)findViewById(R.id.layout_time_date)).findViewById(R.id.txt_date); 
		m_btnTime = (TextView)((RelativeLayout)findViewById(R.id.layout_time_date)).findViewById(R.id.txt_time); 
		m_btnUpdate = (Button)findViewById(R.id.btn_update_watch); 
		
		if( m_isAutoOn ) {
			m_btnSetAuto.setImageResource(R.drawable.btn_switch_on);
			m_btnDate.setTextColor(Color.GRAY);
			m_btnTime.setTextColor(Color.GRAY);
		} else {
			m_btnSetAuto.setImageResource(R.drawable.btn_switch_off);
			m_btnDate.setTextColor(Color.BLACK);
			m_btnTime.setTextColor(Color.BLACK);
		}
		
        m_btnSetAuto.setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View p_view) {
				// TODO Auto-generated method stub
		    	setAuto(m_isAutoOn, p_view);
			}
		});
        
        Calendar ca = Utils.calendar();
		CharSequence dateStr = DateFormat.format("MMMM dd, yyyy", ca);
		CharSequence timeStr = DateFormat.format("hh:mm aa", ca);
		   
        m_btnDate.setText(dateStr);
        m_btnDate.setOnClickListener(new View.OnClickListener()  {
			@Override
			public void onClick(View p_view) {
				// TODO Auto-generated method stub
				if(m_isAutoOn) {
					return;
				}
				showDialog(1);
			}
		});
        
        m_btnTime.setText(timeStr);
        m_btnTime.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View p_view) {
				// TODO Auto-generated method stub
				if(m_isAutoOn) {
					return;
				}
				showDialog(2);
			}
		});
        
        m_btnUpdate.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				updateWatch(m_isAutoOn);
			}
		});
	}
	
	private void updateWatch(boolean p_isAuto) {
		
		if (AppHelper.instance().WATCH_STATE == WATCH_STATE_VALUE.CONNECTED) {
			if ( !p_isAuto) {
				Protocol p = new Protocol(BluetoothAgent.getInstance(null), DateTimeActivity.this);
		    	p.syncTimeFromInput(m_date, m_time);
				KreyosUtility.showPopUpDialog(this, "Update Watch", "Your watch is updated!");
			}
		} else {
			KreyosUtility.showErrorMessage(this, "Device not found"	, "Please connect your Kreyos Watch");
		}
	}
	
	private void setAuto(boolean p_isAuto, View p_view) {
		if (AppHelper.instance().WATCH_STATE == WATCH_STATE_VALUE.CONNECTED) {
			m_isAutoOn = !p_isAuto;
			SharedPreferences.Editor editor = KreyosActivity.getPrefs().edit();
	    	editor.putBoolean("auto_sync_time", m_isAutoOn);
	    	editor.commit();
	    	Log.d("DateTime", "" + m_isAutoOn);
	    	//*
			if (m_isAutoOn) {
				((ImageView)p_view).setImageResource(R.drawable.btn_switch_on);
				m_btnDate.setTextColor(Color.GRAY);
				m_btnTime.setTextColor(Color.GRAY);
				Protocol p = new Protocol(BluetoothAgent.getInstance(null), DateTimeActivity.this);
		    	p.syncTime();
			} else {
				((ImageView)p_view).setImageResource(R.drawable.btn_switch_off);
				m_btnDate.setTextColor(Color.BLACK);
				m_btnTime.setTextColor(Color.BLACK);
			}
			//*/
		} else {
			KreyosUtility.showErrorMessage(this, "Device not found"	, "Please connect your Kreyos Watch");
		}
	}
	
	@Override
    protected Dialog onCreateDialog(int id) 
	{
		// TODO Auto-generated method stub
		
		switch (id) 
		{
			case 1:
				return new DatePickerDialog(this, m_dateSetListener, 2014, 4, 16);
			case 2:
				return new TimePickerDialog(this, m_timeSetListener, 4, 56, false);
		}
		return super.onCreateDialog(id);
    }
    
    private DatePickerDialog.OnDateSetListener m_dateSetListener = new DatePickerDialog.OnDateSetListener()
    {
	    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) 
	    {
	    	m_date = new Date(year-1900, monthOfYear, dayOfMonth);
	    	
	    	CharSequence dateStr = DateFormat.format("MMMM dd, yyyy", m_date);
	    	m_btnDate.setText(dateStr);
	    }
    };
    
    private TimePickerDialog.OnTimeSetListener m_timeSetListener = new TimePickerDialog.OnTimeSetListener() 
    {
		
		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) 
		{
			m_time.setHours(hourOfDay);
			m_time.setMinutes(minute);
			
			// TODO Auto-generated method stub
			Log.d("LOG", "H " + hourOfDay + " M " + minute);
			
			String amPmLabel = "AM";
			String hourLabel = "" + hourOfDay;
			String minuteLabel = "" + minute;
			
			if( hourOfDay > 11 ) 
			{
				amPmLabel = "PM";
			}
			if( hourOfDay % 12 == 0  )
			{
				hourOfDay = 12;
			}
			else 
			{
				hourOfDay = hourOfDay % 12;
			}
			hourLabel = "" + hourOfDay;
			
			if( hourOfDay < 10 ) 
			{
				hourLabel = "0" + hourLabel;
			}
			if( minute < 10 ) 
			{
				minuteLabel = "0" + minuteLabel;
			}
			
			m_btnTime.setText( hourLabel + ":" + minuteLabel + " " + amPmLabel );
		}
	
	};
	
	
	public void handleTimeSync(Message msg) {
		
		Calendar ca = Utils.calendar();
		TimeZone tz = TimeZone.getDefault();
		textViewTZ.setText(tz.getDisplayName());

		CharSequence timeStr = DateFormat.format("hh:mm:ssaa", ca);
		CharSequence dateStr = DateFormat.format("EEEE MMMM dd, yyyy", ca);
		textViewTime.setText(timeStr);
		textViewDate.setText(dateStr);
	}
	
	private class TimeSyncThread extends Thread {

		private TimeSyncHandler msgHandler;
		
		public TimeSyncThread(TimeSyncHandler h) {
			msgHandler = h;
		}
		
		public void run() {
			while (true) {
				msgHandler.obtainMessage(MSG_TIME_SYNC, null).sendToTarget();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					return;
				}
			}
		}
	};
	
    private static class TimeSyncHandler extends Handler {
        private final WeakReference<DateTimeActivity> mActivity; 

        public TimeSyncHandler(DateTimeActivity activity) {
        	mActivity = new WeakReference<DateTimeActivity>(activity);
        }
        
        @Override
        public void handleMessage(Message msg)
        {
        	 DateTimeActivity activity = mActivity.get();
             if (activity != null) {
            	 activity.handleTimeSync(msg);
             }
        }
    } 
    	
    @Override
	public void onSlideMenuItemClick(int itemId) 
    {
    	AppHelper.instance().onSwitchActivity(isLeftMenuSelected, this, itemId);
	}
}
