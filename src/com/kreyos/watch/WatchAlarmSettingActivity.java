package com.kreyos.watch;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.kreyos.watch.R;
import com.kreyos.watch.bluetooth.BluetoothAgent;
import com.kreyos.watch.bluetooth.Protocol;

public class WatchAlarmSettingActivity extends KreyosActivity {

	private String confName = "";
	private boolean mEnableAlarm = true;
	private boolean mEnableVibrate = true;
	
	@Override
    protected void onCreate(Bundle savedInstanceState)
	{    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.more_watch_alarm_setting_activity);   
//        setSubActivityEntry(R.id.imageView_back, "WatchAlarms", WatchAlarmKreyosActivityGroup.class);
        
        KreyosUtility.overrideFonts(this, ((ViewGroup)findViewById(android.R.id.content)).getChildAt(0), KreyosUtility.FONT_NAME.LEAGUE_GOTHIC_REGULAR);
        
        
        onCallonCreate();
        
        confName = getIntent().getStringExtra("parameter");        
        
        loadWatchAlarmConf(confName);
        
        initSyncButton(R.id.btnSync);
        
        loadFonts();
        
        ImageView imageView_back = (ImageView) findViewById(R.id.imageView_back);
        imageView_back.setOnClickListener(new View.OnClickListener() 
        {
			@Override
			public void onClick(View arg0) 
			{
				finish();
				Intent i = new Intent(WatchAlarmSettingActivity.this, WatchAlarmKreyosActivityGroup.class);
				startActivity(i);
			}
		});
	}
	
	
	private void loadFonts() {
		/*loadFontToTextView(R.id.btnBackToSettings, Fonts.ProximaNova_Light);
		
		loadFontToTextView(R.id.titleAllAlarm, Fonts.ProximaNova_Regular);
		loadFontToTextView(R.id.titleHour, Fonts.ProximaNova_Regular);
		loadFontToTextView(R.id.titleMinute, Fonts.ProximaNova_Regular);
		
		loadFontToTextView(R.id.btnSync, Fonts.ProximaNova_Light);
		loadFontToTextView(R.id.textHour, Fonts.ProximaNova_Semibold);
		loadFontToTextView(R.id.textMinute, Fonts.ProximaNova_Semibold); */
	}
	
	private void saveWatchAlarmConf(final String confName) 
	{
		SharedPreferences.Editor editor = KreyosActivity.getPrefs().edit();
		
		TextView textHour = (TextView)findViewById(R.id.textHour);
		int valueHour = Integer.parseInt(textHour.getText().toString());
		TextView textMinute = (TextView)findViewById(R.id.textMinute);
		int valueMinute = Integer.parseInt(textMinute.getText().toString());

		editor.putBoolean(confName + ".enable", mEnableAlarm);
		editor.putBoolean(confName + ".vibrate", mEnableVibrate);		
		editor.putInt(confName + ".hour",   valueHour);
		editor.putInt(confName + ".minute", valueMinute);
		editor.commit();
	}
	
	private void loadWatchAlarmConf(final String confName) 
	{
		SharedPreferences prefs = KreyosActivity.getPrefs();
		initSwitchButton(R.id.btnEnableAlarm,  prefs.getBoolean(confName + ".enable", true));
        initTimeText(R.id.textHour,   R.id.btnDecHourValue,   R.id.btnIncHourValue,   prefs.getInt(confName + ".hour", 0),   24);
        initTimeText(R.id.textMinute, R.id.btnDecMinuteValue, R.id.btnIncMinuteValue, prefs.getInt(confName + ".minute", 0), 60);
	}
	
	private void syncWatchAlarmConf(
			final int index, final boolean enable, final boolean vibrate, 
			final int mode, final int weekday, final int hour, final int minute
			) {
		int rawMode = 0;
		switch (mode) {
		case 0: rawMode = 0x02; break;
		case 1: rawMode = 0x04; break;
		case 2: rawMode = 0x05; break;
		case 3: rawMode = 0x03; break;
		}
		if (vibrate) rawMode = rawMode | 0x10;
		if (!enable) rawMode = 01;
		
		Protocol p = new Protocol(BluetoothAgent.getInstance(null), this);
		p.setWatchAlarm(index, rawMode, 0 /* monthDay */, weekday, hour, minute);
	}
	
	private void setTimeText(final int textId, final int value, final int maxValue) {
		int toSet = value;
		if (value < 0) {
			toSet = maxValue - 1;
		}
		else {
			toSet = value % maxValue;
		}
		TextView text = (TextView) findViewById(textId);
		text.setText(String.format("%02d", toSet));
	}
	
	private void initTimeText(
			final int textId, final int prevId, final int nextId, 
			final int value, final int maxValue
			) {
		setTimeText(textId, value, maxValue);
		
		View prev = findViewById(prevId);
		View next = findViewById(nextId);
		
		prev.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				TextView text = (TextView)findViewById(textId);
				int value = Integer.parseInt(text.getText().toString());
				setTimeText(textId, value - 1, maxValue);
				saveWatchAlarmConf(confName);
			}
			
		});
		
		next.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				TextView text = (TextView)findViewById(textId);
				int value = Integer.parseInt(text.getText().toString());
				setTimeText(textId, value + 1, maxValue);
				saveWatchAlarmConf(confName);
			}
			
		});
	}
	
	
	private void initSwitchButton(final int btnId, final boolean initFlag) {
		if (btnId == R.id.btnEnableAlarm) {
    		mEnableAlarm = initFlag;
    	}
    	else {
    		mEnableVibrate = initFlag;
    	}
		ImageView btn = (ImageView) findViewById(btnId);
		btn.setImageResource(initFlag ? R.drawable.btn_switch_on : R.drawable.btn_switch_off);
		btn.setOnClickListener(new OnClickListener() {
        	
        	private boolean switchFlag = initFlag;
            @Override
            public void onClick(View v) {
            	ImageView btn = (ImageView)v;
            	switchFlag = !switchFlag;
            	btn.setImageResource(switchFlag ? R.drawable.btn_switch_on : R.drawable.btn_switch_off);
            	if (btnId == R.id.btnEnableAlarm) {
            		mEnableAlarm = switchFlag;
            	}
            	else {
            		mEnableVibrate = switchFlag;
            	}
            	saveWatchAlarmConf(confName);
            }
        });
	}
	
	private void initSyncButton(final int btnId) 
	{
		View btn = findViewById(btnId);
		btn.setOnClickListener(new OnClickListener() 
		{

			@Override
			public void onClick(View v) 
			{

				String[] strs = confName.split("_");
				if (strs.length != 2)
					return;
					
				int index = Integer.parseInt(strs[1]);				
				TextView textHour = (TextView)findViewById(R.id.textHour);
				int valueHour = Integer.parseInt(textHour.getText().toString());
				TextView textMinute = (TextView)findViewById(R.id.textMinute);
				int valueMinute = Integer.parseInt(textMinute.getText().toString());
				syncWatchAlarmConf(index, mEnableAlarm, mEnableVibrate, 0, 0, valueHour, valueMinute);
				Log.d("Log", "Sync Watch Alarm");
			}
			
		});
	}
 }
