package com.kreyos.watch;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;

import android.app.ActivityGroup;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
//import android.widget.Toast;

import com.coboltforge.slidemenu.SlideMenuInterface.OnSlideMenuItemClickListener;
import com.kreyos.watch.R;
import com.kreyos.watch.bluetooth.BluetoothAgent;
import com.kreyos.watch.bluetooth.KreyosService;
import com.kreyos.watch.bluetooth.Protocol;
import com.kreyos.watch.managers.AppHelper;
import com.kreyos.watch.managers.AppHelper.WATCH_STATE_VALUE;
import com.kreyos.watch.utils.Utils;

@SuppressWarnings("deprecation")

public class WatchAlarmKreyosActivityGroup extends KreyosActivity implements OnSlideMenuItemClickListener{

	
	private BTDataHandler btMsgHandler = null;

	public WatchAlarmKreyosActivityGroup() 
	{
		btMsgHandler = new BTDataHandler(this);
		if( AppHelper.instance().WATCH_STATE == WATCH_STATE_VALUE.CONNECTED ) {
 			BluetoothAgent.getInstance(btMsgHandler);
 		}
	}
	
	private static class BTDataHandler extends Handler 
	{
		private final WeakReference<WatchAlarmKreyosActivityGroup> mService;

		public BTDataHandler(WatchAlarmKreyosActivityGroup service)
		{
			mService = new WeakReference<WatchAlarmKreyosActivityGroup>(service);
		}

		@Override
		public void handleMessage(Message msg)
		{
			WatchAlarmKreyosActivityGroup service = mService.get();
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
				Intent i3 = new Intent(WatchAlarmKreyosActivityGroup.this, SportsActivity.class);
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
	
	
	private SharedPreferences prefs = null;
	private RelativeLayout container = null;
	
	
	/* create new slideMenu for the right */
	private SlidingMenu slidemenu;
	private SlidingMenu slidemenu_right;
	private boolean isLeftMenuSelected = false;

	private int m_alarmIndexSelected = 0;
	private Button m_btnUpdate = null;
	
	//bluetooth
	private static KreyosService mService = null;
	
	private ServiceConnection mServiceConnection = new ServiceConnection() 
	{
	       
        @Override
        public void onServiceDisconnected(ComponentName name) 
        {
            setService(null);
        }
       
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) 
        {
        	setService(((KreyosService.KreyosServiceBinder)binder).getService());
        	mService.initServiceTasks();
        }
    };
    
    
	
	@SuppressWarnings("deprecation")
	@Override
    protected void onCreate(Bundle savedInstanceState) 
	{    	
		
        super.onCreate(savedInstanceState);
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        setContentView(R.layout.more_watch_alarm_activity);
        
        KreyosUtility.overrideFonts(this, ((ViewGroup)findViewById(android.R.id.content)).getChildAt(0), KreyosUtility.FONT_NAME.LEAGUE_GOTHIC_REGULAR);
        
        
        KreyosApp app = (KreyosApp)getApplicationContext();
    	app.setKreyosActivityGroup(this);
    	
        syncWatchAlarm(); //sync the alarm at the beginning to ensure the alarm is set correct
        
        container = (RelativeLayout) findViewById(R.id.relativelayout_watchalarm);
        prefs = KreyosActivity.getPrefs();
        //initSwitchButton(R.id.btnEnableAlarm, prefs.getBoolean("alarm.enable", false));
        
        loadWatchAlarm("alarm_1", R.id.alarmMode1, R.id.alarmTime1, R.id.btnVibrateAlarm1);
        loadWatchAlarm("alarm_2", R.id.alarmMode2, R.id.alarmTime2, R.id.btnVibrateAlarm2);
        loadWatchAlarm("alarm_3", R.id.alarmMode3, R.id.alarmTime3, R.id.btnVibrateAlarm3);
        
        setSubActivityEntry(R.id.layoutWatchAlarm1, "WatchAlarmSetting", WatchAlarmSettingActivity.class, "alarm_1");
        setSubActivityEntry(R.id.layoutWatchAlarm2, "WatchAlarmSetting", WatchAlarmSettingActivity.class, "alarm_2");
        setSubActivityEntry(R.id.layoutWatchAlarm3, "WatchAlarmSetting", WatchAlarmSettingActivity.class, "alarm_3");
        
        Intent intent = new Intent(this, KreyosService.class);
        initCloudSync(prefs, intent);
        initNotifications(prefs, intent);
        initBluetoothAgent(prefs, intent);
        bindService(intent,	mServiceConnection, Context.BIND_AUTO_CREATE);
        
        setupSlideMenu();
        
        m_btnUpdate = (Button)findViewById(R.id.btn_update_watch_alarm);
        m_btnUpdate.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				onUpdateAlarm();
			}
		});
        
        
      //  loadFonts();
	}
	
	private void onUpdateAlarm()
	{
		if(AppHelper.instance().WATCH_STATE == WATCH_STATE_VALUE.CONNECTED)
		{
			syncWatchAlarm();
			loadWatchAlarm("alarm_1", R.id.alarmMode1, R.id.alarmTime1, R.id.btnVibrateAlarm1);
	        loadWatchAlarm("alarm_2", R.id.alarmMode2, R.id.alarmTime2, R.id.btnVibrateAlarm2);
	        loadWatchAlarm("alarm_3", R.id.alarmMode3, R.id.alarmTime3, R.id.btnVibrateAlarm3);
	        KreyosUtility.showPopUpDialog(this, "Update Watch", "Your watch is updated!");
		}
		else
		{
			KreyosUtility.showErrorMessage(this, "Device not found"	, "Please connect your Kreyos Watch");
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
		b.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				isLeftMenuSelected = true;
				slidemenu.show();
			}
		});
		
		
		ImageView imageView_menu2 = (ImageView) findViewById(R.id.imageView_menu2);
		imageView_menu2.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				isLeftMenuSelected = false;
				slidemenu_right.show();
			}
		});

	}
	
	/*private void loadFonts() {
	//	loadFontToTextView(R.id.title,             Fonts.ProximaNova_Semibold);
		//loadFontToTextView(R.id.btnBackToSettings, Fonts.ProximaNova_Light);
		loadFontToTextView(R.id.titleAllAlarm,     Fonts.ProximaNova_Semibold);
		
		loadFontToTextView(R.id.btnSync, Fonts.ProximaNova_Light);
		
		loadFontToTextView(R.id.titleAlarm1,  Fonts.ProximaNova_Semibold);
		loadFontToTextView(R.id.alarmMode1,   Fonts.ProximaNova_Light);
		loadFontToTextView(R.id.alarmTime1,   Fonts.ProximaNova_Light);
		loadFontToTextView(R.id.titleEnable1, Fonts.ProximaNova_Semibold);
		
		loadFontToTextView(R.id.titleAlarm2,  Fonts.ProximaNova_Semibold);
		loadFontToTextView(R.id.alarmMode2,   Fonts.ProximaNova_Light);
		loadFontToTextView(R.id.alarmTime2,   Fonts.ProximaNova_Light);
		loadFontToTextView(R.id.titleEnable2, Fonts.ProximaNova_Semibold);
		
		loadFontToTextView(R.id.titleAlarm3,  Fonts.ProximaNova_Semibold);
		loadFontToTextView(R.id.alarmMode3,   Fonts.ProximaNova_Light);
		loadFontToTextView(R.id.alarmTime3,   Fonts.ProximaNova_Light);
		loadFontToTextView(R.id.titleEnable3, Fonts.ProximaNova_Semibold);
		
	} */
	
	@Override
	public void onSlideMenuItemClick(int itemId)
	{
		AppHelper.instance().onSwitchActivity(isLeftMenuSelected, this, itemId);
	}
	
	private static final int alarmModeTable[] = { R.string.repeatOnce, R.string.everyday, R.string.weekly, R.string.hourly };
	
	private void loadWatchAlarm(final String confName, final int modeId, final int timeId, final int switchId)
	{
		TextView mode = (TextView) findViewById(modeId);
		TextView time = (TextView) findViewById(timeId);
		
		SharedPreferences prefs = KreyosActivity.getPrefs();
		final int modeStrId 	= R.string.repeatOnce;//alarmModeTable[prefs.getInt(confName + ".mode", 0)]; // R.string.everyday;//alarmModeTable[prefs.getInt(confName + ".mode", 0)];
		int hour 			= prefs.getInt(confName + ".hour", 0);
		final int minute 		= prefs.getInt(confName + ".minute", 0);
		final boolean enable 	= prefs.getBoolean(confName + ".enable", false);
		
		mode.setText(modeStrId);
		if( hour > 12 )
		{
			hour = hour % 12;
		}
		else if( hour == 0)
		{
			hour = 12;
		}
		time.setText(String.format("%02d:%02d", hour, minute));
		
		checkIfSliderEnable(enable, timeId);
		initSwitchButton(switchId, enable);
 	}
	
	private void syncWatchAlarm() 
	{
		SharedPreferences prefs = KreyosActivity.getPrefs();
		Protocol p = new Protocol(BluetoothAgent.getInstance(null), this);

		boolean enableAlarm1 = prefs.getBoolean("alarm_1.enable", false); //&& prefs.getBoolean("alarm.enable", false);
		p.setWatchAlarm(0, 
				enableAlarm1 ? 0x04 : 0x01, 0, 0, 
				prefs.getInt("alarm_1.hour", 0), 
				prefs.getInt("alarm_1.minute", 0));
		
		boolean enableAlarm2 = prefs.getBoolean("alarm_2.enable", false); //&& prefs.getBoolean("alarm.enable", false);
		p.setWatchAlarm(1, 
				enableAlarm2 ? 0x04 : 0x01, 0, 0, 
				prefs.getInt("alarm_2.hour", 0), 
				prefs.getInt("alarm_2.minute", 0));
		
		boolean enableAlarm3 = prefs.getBoolean("alarm_3.enable", false); //&& prefs.getBoolean("alarm.enable", false);
		p.setWatchAlarm(2, 
				enableAlarm3 ? 0x04 : 0x01, 0, 0, 
				prefs.getInt("alarm_3.hour", 0), 
				prefs.getInt("alarm_3.minute", 0));
		
		checkIfSliderEnable(enableAlarm1, R.id.alarmTime1);
		checkIfSliderEnable(enableAlarm2, R.id.alarmTime2);
		checkIfSliderEnable(enableAlarm3, R.id.alarmTime3);
	}
	
	private void checkIfSliderEnable(boolean p_isAlarmEnable, int p_textId) {

		if (p_isAlarmEnable) {
			((TextView)findViewById(p_textId)).setTextColor(Color.BLACK);
		} else {
			((TextView)findViewById(p_textId)).setTextColor(Color.LTGRAY);
		}
	}
	
	private void initSwitchButton(final int btnId, final boolean enable)
	{
		
		ImageView btn = (ImageView) findViewById(btnId);
		btn.setImageResource(enable ? R.drawable.btn_switch_on : R.drawable.btn_switch_off);
		btn.setOnClickListener(new OnClickListener() {
        	
        	private boolean switchFlag = enable;
            @Override
            public void onClick(View v) 
            {
            	ImageView btn = (ImageView)v;
            	switchFlag = !switchFlag;
            	btn.setImageResource(switchFlag ? R.drawable.btn_switch_on : R.drawable.btn_switch_off);
            	
            	SharedPreferences.Editor editor = prefs.edit();
            	switch (btnId) 
            	{
            	
            	case R.id.btnEnableAlarm:   
            		editor.putBoolean("alarm.enable", switchFlag); 
            		break;
            		
            	case R.id.btnVibrateAlarm1:
            		editor.putBoolean("alarm_1.enable", switchFlag); 
            		break;
            		
            	case R.id.btnVibrateAlarm2: 
            		editor.putBoolean("alarm_2.enable", switchFlag); 
            		break;
            		
            	case R.id.btnVibrateAlarm3: 
            		editor.putBoolean("alarm_3.enable", switchFlag); 
            		break;
            		
            	}
            	editor.commit();
            	
            	syncWatchAlarm();
            }
        });
	}
	
	private void initCloudSync(SharedPreferences prefs, Intent intent) 
	{
    	intent.putExtra("cloud_id",             prefs.getString("cloud_id",             ""));
    	intent.putExtra("access_token",         prefs.getString("access_token",         ""));
    	intent.putExtra("access_expires",       prefs.getLong("access_expires",      0));
    	intent.putExtra("twitter_access_token", prefs.getString("twitter_access_token", ""));
    	intent.putExtra("twitter_secret_token", prefs.getString("twitter_secret_token", ""));
	}
	    
	private void initNotifications(SharedPreferences prefs, Intent intent)
	{
    	intent.putExtra("notification.facebook",       prefs.getBoolean("notification.facebook",       false));
    	intent.putExtra("notification.weather",        prefs.getBoolean("notification.weather",        false));
    	intent.putExtra("notification.twitter",        prefs.getBoolean("notification.twitter",        false));
    	intent.putExtra("notification.reminder",       prefs.getBoolean("notification.reminder",       false));
    	intent.putExtra("notification.sms",            prefs.getBoolean("notification.sms",            false));
    	intent.putExtra("notification.call",           prefs.getBoolean("notification.call",           false));
    	intent.putExtra("notification.low_battery",    prefs.getBoolean("notification.low_battery",    false));
    	intent.putExtra("notification.bt_outof_range", prefs.getBoolean("notification.bt_outof_range", false));
	}
	
//	public View setSubActivityEntry(final int viewId, final String activityName, final Class<?> cls) 
//	{
//		View view = findViewById(viewId);
//        
//		view.setOnClickListener(new OnClickListener() 
//		{
//            @Override
//            public void onClick(View v) 
//            {
//            	KreyosApp app = (KreyosApp)getApplicationContext();
//            	app.getKreyosActivityGroup().setActivePage(activityName, cls);
//            }
//        }); 
//		
//		return view;
//	}
	
	public View setSubActivityEntry(final int viewId, final String activityName, final Class<?> cls, final String strPara) 
	{
		View view = findViewById(viewId);
        
		view.setOnClickListener(new OnClickListener() 
		{
            @Override
            public void onClick(View v) 
            {
            	
//            	KreyosApp app = (KreyosApp)getApplicationContext();
//            	app.getKreyosActivityGroup().setActivePage(activityName, cls, strPara);
            	showAlarmDialog(v);
            }
        }); 
		
		return view;
	}
	
	private void showAlarmDialog(View v)
	{
		SharedPreferences prefs = KreyosActivity.getPrefs(); 
		
		if(v.getId() == R.id.layoutWatchAlarm1 && prefs.getBoolean("alarm_1.enable", false))
    	{
    		Log.d("Log", "Here's we click the time!");
    		// 1
    		m_alarmIndexSelected = 1;
        	showDialog(2);
    	}
    	else if(v.getId() == R.id.layoutWatchAlarm2 && prefs.getBoolean("alarm_2.enable", false))
    	{
    		Log.d("Log", "Here's we click the time!");
    		// 2
    		m_alarmIndexSelected = 2;
        	showDialog(2);
    	}
    	else if(v.getId() == R.id.layoutWatchAlarm3 && prefs.getBoolean("alarm_3.enable", false))
    	{
    		Log.d("Log", "Here's we click the time!");
    		// 3
    		m_alarmIndexSelected = 3;
        	showDialog(2);
    	}
	}
	
	@Override
    protected Dialog onCreateDialog(int id) 
	{
		// TODO Auto-generated method stub
		Calendar ca = Utils.calendar();
		switch (id) 
		{
//			case 1:
//				return new DatePickerDialog(this, m_dateSetListener, 2014, 4, 16);
			case 2:
				return new TimePickerDialog(this, m_timeSetListener, ca.get(Calendar.HOUR_OF_DAY), ca.get(Calendar.MINUTE), false);
		}
		return super.onCreateDialog(id);
    }
	
	private TimePickerDialog.OnTimeSetListener m_timeSetListener = new TimePickerDialog.OnTimeSetListener() 
    {
		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) 
		{
			setWatchAlarm( hourOfDay, minute );
		}
	};
	
	public void setWatchAlarm( int p_hour, int p_minute ) 
	{
		SharedPreferences.Editor editor = KreyosActivity.getPrefs().edit();
		String alarmKey = "";
		
		switch(m_alarmIndexSelected)
		{
			case 1:
			{	
				alarmKey = "alarm_1";
			}
			break;
			
			case 2:
			{ 
				alarmKey = "alarm_2";
			}
			break;
			
			case 3:
			{
				alarmKey = "alarm_3";
			}
			break;
		}
		
		editor.putInt(alarmKey + ".hour",   p_hour);
		editor.putInt(alarmKey + ".minute", p_minute);
		editor.commit();
		
		switch(m_alarmIndexSelected)
		{
			case 1:
			{	
				loadWatchAlarm("alarm_1", R.id.alarmMode1, R.id.alarmTime1, R.id.btnVibrateAlarm1);
			}
			break;
			
			case 2:
			{
				loadWatchAlarm("alarm_2", R.id.alarmMode2, R.id.alarmTime2, R.id.btnVibrateAlarm2);
			}
			break;
			
			case 3:
			{
				loadWatchAlarm("alarm_3", R.id.alarmMode3, R.id.alarmTime3, R.id.btnVibrateAlarm3);
			}
			break;
		}
		
		
	}
	
//    public void setActivePage(final String viewName, final Class<?> activityClass) {
//   	 
//    	setActivePage(viewName, activityClass, "");
//	}
//    
//    public void setActivePage(final String viewName, final Class<?> activityClass, String para) 
//    {
//    	Intent intent = new Intent(WatchAlarmKreyosActivityGroup.this, activityClass);
//    	intent.putExtra("parameter", para);
//    	
//		container.removeAllViews();
//		container.addView(getLocalActivityManager().startActivity(
//				viewName,
//				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)).getDecorView());
//	}
    
	private void initBluetoothAgent(SharedPreferences prefs, Intent intent) {
    	intent.putExtra("bluetooth.device_name", prefs.getString("bluetooth.device_name", ""));
	}
 
	public static KreyosService getService() {
		return mService;
	}
	
	public static void setService(KreyosService service) {
		mService = service;
	}
	
	@Override
	public void onBackPressed()
	{
		//do nothing
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		try 
		{
			unbindService(mServiceConnection);
		} 
		catch (Exception e) 
		{
			// TODO: handle exception
		}
	}
}
