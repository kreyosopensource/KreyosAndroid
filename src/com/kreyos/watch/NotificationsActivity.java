package com.kreyos.watch;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
//import android.widget.Toast;

import com.coboltforge.slidemenu.SlideMenu;
import com.coboltforge.slidemenu.SlideMenuInterface.OnSlideMenuItemClickListener;
import com.facebook.Session;
import com.facebook.SessionState;
import com.kreyos.watch.R;
import com.kreyos.watch.bluetooth.BluetoothAgent;
import com.kreyos.watch.bluetooth.Protocol;
import com.kreyos.watch.managers.AppHelper;
import com.kreyos.watch.managers.AppHelper.WATCH_STATE_VALUE;


public class NotificationsActivity extends KreyosActivity implements OnSlideMenuItemClickListener{
	
	
	private BTDataHandler btMsgHandler = null;

	public NotificationsActivity() 
	{
		btMsgHandler = new BTDataHandler(this);
		if( AppHelper.instance().WATCH_STATE == WATCH_STATE_VALUE.CONNECTED ) {
 			BluetoothAgent.getInstance(btMsgHandler);
 		}
	}
	
	private static class BTDataHandler extends Handler 
	{
		private final WeakReference<NotificationsActivity> mService;

		public BTDataHandler(NotificationsActivity service)
		{
			mService = new WeakReference<NotificationsActivity>(service);
		}

		@Override
		public void handleMessage(Message msg)
		{
			NotificationsActivity service = mService.get();
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
				Intent i3 = new Intent(NotificationsActivity.this, SportsActivity.class);
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

	private TextView btnBackToSettings = null;
	private String requestId;
	private SlidingMenu slidemenu;
	private SlidingMenu slidemenu_right;
	private boolean isLeftMenuSelected = false;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) 
	{    	
        super.onCreate(savedInstanceState);
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        setContentView(R.layout.more_settings_notification_activity);
        
        /*btnBackToSettings = (TextView)findViewById(R.id.btnBackToSettings);
        
        btnBackToSettings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	KreyosApp app = (KreyosApp)getApplicationContext();
            }
        }); */
        
        setupSlideMenu();
        onCallonCreate();
        
        KreyosUtility.overrideFonts(this, ((ViewGroup)findViewById(android.R.id.content)).getChildAt(0), KreyosUtility.FONT_NAME.LEAGUE_GOTHIC_REGULAR);

        //initNotificationSettingEntry(R.id.layoutAllNotifications, R.id.btnEnableNotification, "notification.all");
        initNotificationSettingEntry(R.id.layoutIncommingCall,    R.id.btnIncommingCall,      "notification.call");
        initNotificationSettingEntry(R.id.layoutWeather,          R.id.btnWeather,            "notification.weather");
        initNotificationSettingEntry(R.id.layoutTwitter,          R.id.btnTwitter,            "notification.twitter");
        initNotificationSettingEntry(R.id.layoutReminder,         R.id.btnReminder,           "notification.reminder");
        initNotificationSettingEntry(R.id.layoutEMail,            R.id.btnEmail,              "notification.email");
        initNotificationSettingEntry(R.id.layoutSMS,              R.id.btnSMS,                "notification.sms");
        initNotificationSettingEntry(R.id.layoutFacebook,         R.id.btnFacebook,           "notification.facebook");
        initNotificationSettingEntry(R.id.layoutLowBattery,       R.id.btnLowBattery,         "notification.low_battery");
        initNotificationSettingEntry(R.id.layoutPhoneOutofRange,  R.id.btnPhoneOutofRange,    "notification.bt_outof_range");
        
        
      //  loadFonts();
        
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
	
	@Override
	public void onSlideMenuItemClick(int itemId) 
	{
		AppHelper.instance().onSwitchActivity(isLeftMenuSelected, this, itemId);
	}
	
	private void onSessionStateChange(Session session, SessionState state, Exception exception)
	{
	    // Check if the user is authenticated and
	    // an incoming notification needs handling 
	    if (state.isOpened() && requestId != null) {
//	        Toast.makeText(this.getApplicationContext(), "Incoming request",
//	                Toast.LENGTH_SHORT).show();
	        requestId = null;
	    }
	    if (state.isOpened()) {
	       // sendRequestButton.setVisibility(View.VISIBLE);
	    } else if (state.isClosed()) {
	        //sendRequestButton.setVisibility(View.INVISIBLE);
	    }
	}
    	
/*private void loadFonts() {
		loadFontToTextView(R.id.title,             Fonts.ProximaNova_Semibold);
		loadFontToTextView(R.id.btnBackToSettings, Fonts.ProximaNova_Light);
		
		loadFontToTextView(R.id.titleAllNotifications, Fonts.ProximaNova_Light);
		
		loadFontToTextView(R.id.titleAllNotifications, Fonts.ProximaNova_Semibold);
		loadFontToTextView(R.id.incommerTitle,         Fonts.ProximaNova_Semibold);
		loadFontToTextView(R.id.smsNotificationsTitle, Fonts.ProximaNova_Semibold);
		loadFontToTextView(R.id.emailTitle,            Fonts.ProximaNova_Semibold);
		loadFontToTextView(R.id.weatherAlertsTitle,    Fonts.ProximaNova_Semibold);
		loadFontToTextView(R.id.facebookTitle,         Fonts.ProximaNova_Semibold);
		loadFontToTextView(R.id.twitterTitle,          Fonts.ProximaNova_Semibold);
		loadFontToTextView(R.id.weatherAlertsTitle,    Fonts.ProximaNova_Semibold);
		loadFontToTextView(R.id.reminderAlertsTitle,   Fonts.ProximaNova_Semibold);
		loadFontToTextView(R.id.phoneOutOfRangeTitle,  Fonts.ProximaNova_Semibold);
		loadFontToTextView(R.id.lowBatteryTitle,       Fonts.ProximaNova_Semibold);
	} */
	
	private void startServiceByLayoutId(final int layoutId) {

		switch (layoutId) {
		case R.id.layoutAllNotifications:
			HomeActivity.getService().startCallReceiver();	
			HomeActivity.getService().startFacebookNotification();
			HomeActivity.getService().startTwitterNotification();
			HomeActivity.getService().startSMSReceiver();
			HomeActivity.getService().startWeatherNotification();
			HomeActivity.getService().startReminderListener();
			HomeActivity.getService().startLowBatteryNotification();
			break;
		case R.id.layoutIncommingCall:
			HomeActivity.getService().startCallReceiver();			
			break;
		case R.id.layoutFacebook:
			HomeActivity.getService().startFacebookNotification();
			break;
		case R.id.layoutTwitter:
			HomeActivity.getService().startTwitterNotification();
			break;
		case R.id.layoutSMS:
			HomeActivity.getService().startSMSReceiver();
			break;
		case R.id.layoutWeather:
			HomeActivity.getService().startWeatherNotification();
			break;
		case R.id.layoutReminder:
			// HomeActivity.getService().startReminderListener();
			break;
		case R.id.layoutPhoneOutofRange:
			break;
		case R.id.layoutLowBattery:
			HomeActivity.getService().startLowBatteryNotification();
			break;
		case R.id.layoutEMail:
			//TODO: implement this
			break;
		}
	}
	
	private void stopServiceByLayoutId(final int layoutId) {
		
		switch (layoutId) {
		case R.id.layoutAllNotifications:
			HomeActivity.getService().stopCallReceiver();
			HomeActivity.getService().stopFacebookNotification();
			HomeActivity.getService().stopTwitterNotification();
			HomeActivity.getService().stopSMSReceiver();
			HomeActivity.getService().stopWeatherNotification();
			HomeActivity.getService().stopReminderListener();
			HomeActivity.getService().stopLowBatteryNotification();
			break;
		case R.id.layoutIncommingCall:
			HomeActivity.getService().stopCallReceiver();
			break;
		case R.id.layoutFacebook:
			HomeActivity.getService().stopFacebookNotification();
			break;
		case R.id.layoutTwitter:
			HomeActivity.getService().stopTwitterNotification();
			break;
		case R.id.layoutSMS:
			HomeActivity.getService().stopSMSReceiver();
			break;
		case R.id.layoutWeather:
			HomeActivity.getService().stopWeatherNotification();
			break;
		case R.id.layoutReminder:
			// HomeActivity.getService().stopReminderListener();
			break;
		case R.id.layoutPhoneOutofRange:
			break;
		case R.id.layoutLowBattery:
			HomeActivity.getService().stopLowBatteryNotification();
			break;
		case R.id.layoutEMail:
			//TODO: implement this
			break;
		}
	}
	
	private void initNotificationSettingEntry(final int layoutId, final int btnId, final String confKey) {
		RelativeLayout layout = (RelativeLayout)findViewById(layoutId);
		ImageView      button = (ImageView)     findViewById(btnId);
		
		final boolean enabled = KreyosActivity.getPrefs().getBoolean(confKey, false);
		
		Log.v("Notification", "Initialize Notification: " + confKey);
		
		/*if (layoutId == R.id.layoutSMS) {
			layout.setOnClickListener(new OnClickListener() {
	        	
	            @Override
	            public void onClick(View v) {
	            	KreyosApp app = (KreyosApp)getApplicationContext();
	            	app.getMainActivity().setActivePage(
	        			String.format("notificationConf[%s]", confKey),
	        			NotificationSettingActivity.class,
	        			confKey
	        			);
	            }
	
	        });
		} */
		
		
		button.setOnClickListener(new OnClickListener() {
        	
			private boolean switchFlag = enabled;
            @Override
            public void onClick(View v) 
            {
            	// + ET 04292014 : FB NOTIF CHECKER
            	if(v.getId() == R.id.btnFacebook  
            	|| v.getId() == R.id.btnEmail
            	|| v.getId() == R.id.btnTwitter
            	|| v.getId() == R.id.btnReminder)
            	{
            		if ( android.os.Build.VERSION.SDK_INT > 17 ) 
            		{
//            		if( Session.getActiveSession() != null && Session.getActiveSession().isOpened() )
//            		{
            			ContentResolver contentResolver = getApplicationContext().getContentResolver();
            			String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
            			String packageName = getApplicationContext().getPackageName();

            			// check to see if the enabledNotificationListeners String contains our package name
            			if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName))
            			{
            				// not on
            				startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")); 
            				return;
            			}
//            		}
//            		else
//            		{
//            			return;
//            		}
            		}
            		else
            		{
            			showNotificationError();
            			return;
            		}
            	}

            	ImageView image = (ImageView)v;
            	switchFlag = !switchFlag;
            	if (switchFlag) 
            	{
            		image.setImageResource(R.drawable.btn_switch_on);
            		startServiceByLayoutId(layoutId);
            	}
            	else
            	{
            		image.setImageResource(R.drawable.btn_switch_off);
            		stopServiceByLayoutId(layoutId);
            	}
            	SharedPreferences.Editor editor = KreyosActivity.getPrefs().edit();
            	editor.putBoolean(confKey, switchFlag);
            	editor.commit();
            }
        });
		
		if (enabled) {
			button.setImageResource(R.drawable.btn_switch_on);
			startServiceByLayoutId(layoutId);
		}
    	else {
    		button.setImageResource(R.drawable.btn_switch_off);
    		stopServiceByLayoutId(layoutId);
    	}
	}
	
	private void showNotificationError()
	{
		KreyosUtility.showErrorMessage(this, "Error", "Please update to API 4.3 to use selected notifcations");
	}
}
