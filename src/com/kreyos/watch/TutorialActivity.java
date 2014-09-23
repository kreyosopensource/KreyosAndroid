package com.kreyos.watch;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.internal.ee;
import com.kreyos.watch.R;
import com.kreyos.watch.R.drawable;
import com.kreyos.watch.bluetooth.BluetoothAgent;
import com.kreyos.watch.bluetooth.KreyosService;
import com.kreyos.watch.bluetooth.Protocol;
import com.kreyos.watch.managers.AppHelper;
import com.kreyos.watch.utils.RequestManager;
import com.kreyos.watch.managers.AppHelper.ACTIVITY_METRIC;
import com.kreyos.watch.managers.AppHelper.WATCH_STATE_VALUE;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.ActionBar.LayoutParams;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothProfile.ServiceListener;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TutorialActivity extends KreyosActivity {
	
	// + ET 041414 : HUD
	private HorizontalScrollView m_scrollableController;
	private short m_panelWidth 	= -1;
	private byte  m_pageTotal  	= 6;
	private byte  m_currentPage = 0;
	private ProgressBar	m_sliderTwoProgressBar;
	private ProgressBar m_sliderFourProgressBar;
	private Button m_slideFourButtonNext;
	private Button m_slideFiveButtonNext;
	
	private TextView m_checkUpdateText = null;
	boolean m_isAlreadyConnected = false;
	
	// + ET 041314 : Bluetooth
	private BluetoothAdapter m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	private String m_activeDeviceName;
	ArrayList<BluetoothDevice> m_deviceWatches = new ArrayList<BluetoothDevice>();
	private MsgBTHandler msgBTHanlder;
	private static KreyosService mService = null;
	private BluetoothProfile m_bluetoohProfile;
	
	
	// + ET 041814 : Firmware update
	private static String defaultFileUrl = "http://freebsd.cloudapp.net/~howardsu/upgrade.bin";
	
	// Animations
	Timer mAnimTimer= null;
	TimerTask mAnimTask = null;
	int mAnimIndex = 0;
	ImageView[] mAnimCircles = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature( Window.FEATURE_NO_TITLE );
		setContentView(R.layout.tutorial);
		
		setupLayout();
		adjustSliderChildWidth();
		enableBluetooth();
		
		AppHelper.instance().disconnectWatch(this);
		AppHelper.instance().clearConnectedWatch(this);
		AppHelper.instance().IS_TUTORIAL_MODE = true;
		
		// Start downloading web data
		// AppHelper.instance().getActivityStatsonWeb( this, ACTIVITY_METRIC.DAILY );
		
		storeAnimationVariables();
		for (int i = 0; i < slides.length; i++) {
			unloadSlide(i);
		}
		
		loadSlide(0);
	} 
	
	
	// Slide 1
	int[] slideOne_ID = new int[] {
			R.id.tutorial_header_slide_1,
			R.id.tutorial_body_slide_1,
			R.id.tutorial_turn_on_graphics_slide_1
	};
	
	int[] slideOne_DRAWABLE = new int [] {
			R.drawable.tutorial_turn_your_meteor_on,
			R.drawable.tutorial_turn_your_meteor_on_body,
			R.drawable.tutorial_turn_your_meteor_on_graphics
	};
	
	// Slide 2
	int[] slideTwo_ID = new int[] {
			R.id.tutorial_header_slide_2,
			R.id.tutorial_body_slide_2,
			R.id.tutorial_pairing_your_meteor_graphictext,
			R.id.tutorial_pairing_your_meteor_bluebar,
			R.id.tutorial_turn_on_graphics
	};
	
	int[] slideTwo_DRAWABLE = new int [] {
			R.drawable.tutorial_pairing_your_meteor,
			R.drawable.tutorial_pairing_your_meteor_body,
			R.drawable.tutorial_pairing_your_meteor_graphictext,
			R.drawable.tutorial_pairing_your_meteor_bluebar,
			R.drawable.tutorial_pairing_your_meteor_graphics
			
	};
	
	// Slide 3
	int[] slideThree_ID = new int[] {
			R.id.tutorial_header_slide_3,
			R.id.tutorial_body_slide_3,
			R.id.tutorial_turn_on_graphics_slide_3
	};
	
	int[] slideThree_DRAWABLE = new int [] {
			R.drawable.tutorial_pairing_your_meteor,
			R.drawable.tutorial_pairing_your_meteor_paired,
			R.drawable.tutorial_pairing_connected_graphic
	};
	
	
	// Slide 4
	int[] slideFour_ID = new int[] {
			R.id.tutorial_header_slide_4,
			R.id.tutorial_body_slide_4
	};
	
	int[] slideFour_DRAWABLE = new int [] {
			R.drawable.tutorial_software_update,
			R.drawable.tutorial_software_update_body
	};
	
	// Slide 5
	int[] slideFive_ID = new int[] {
			R.id.tutorial_header_slide_5,
			R.id.tutorial_body_slide_5,
			R.id.tutorial_turn_on_graphics_slide_5
	};
	
	int[] slideFive_DRAWABLE = new int [] {
			R.drawable.tutorial_software_update,
			R.drawable.tutorial_software_update_body2,
			R.drawable.tutorial_software_update_pairing_meteor
	};

	// Slide 6
	int[] slideSix_ID = new int[] {
			R.id.tutorial_header_slide_6,
			R.id.tutorial_body_slide_6,
			R.id.tutorial_turn_on_graphics_slide_6,
			R.id.imageView1_slide_6
	};
	
	int[] slideSix_DRAWABLE = new int [] {
			R.drawable.tutorial_software_update,
			R.drawable.tutorial_software_update_completer,
			R.drawable.tutorial_software_update_icon,
			R.drawable.tutorial_software_update_congratulation
	};

	
	int[][] slides = new int[][] {
		slideOne_ID,	
		slideTwo_ID,
		slideThree_ID,
		slideFour_ID,
		slideFive_ID,
		slideSix_ID
	};
	
	int[][] slidesDrawable = new int[][] {
			slideOne_DRAWABLE,	
			slideTwo_DRAWABLE,
			slideThree_DRAWABLE,
			slideFour_DRAWABLE,
			slideFive_DRAWABLE,
			slideSix_DRAWABLE
		};
	
	
	private void loadSlide(int p_slideNumber) {
		int[] selectedSlide = slides[p_slideNumber];
		int[] selectedDrawable = slidesDrawable[p_slideNumber];
		for (int i = 0; i < selectedSlide.length; i++) {
			ImageView view = (ImageView)findViewById(selectedSlide[i]);
			view.setImageResource(selectedDrawable[i]);
		} 
	}
	
	private void unloadSlide(int p_slideNumber) {
		int[] selectedSlide = slides[p_slideNumber];
		for (int i = 0; i < selectedSlide.length; i++) {
			ImageView view = (ImageView)findViewById(selectedSlide[i]);
			view.setImageBitmap(null);
		} 
	}
	
	private void adjustSliderChildWidth() {
		
		DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        
		int[] sliderId = new int[] {
				R.id.tutorial_slider_item1,
				R.id.tutorial_slider_item2,
				R.id.tutorial_slider_item3,
				R.id.tutorial_slider_item4,
				R.id.tutorial_slider_item5,
				R.id.tutorial_slider_item6
			};
		
		for(int i = 0; i < sliderId.length; i++) {
			RelativeLayout layout = (RelativeLayout)findViewById(sliderId[i]);
			layout.getLayoutParams().width = dm.widthPixels;
		}
	}
	
	
	
	private void setupLayout() 
	{
		
		m_scrollableController = (HorizontalScrollView)findViewById(R.id.tutorial_slider);
		
		//+ ET 041414 : Removed event functions to disable scrolling
		m_scrollableController.setOnTouchListener(new View.OnTouchListener() 
		{
			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				// TODO Auto-generated method stub
				return true;
			}
		});
		m_scrollableController.setHorizontalScrollBarEnabled(false);
		
		RelativeLayout slide2 = (RelativeLayout)findViewById(R.id.tutorial_slider_item2);
		m_sliderTwoProgressBar = (ProgressBar)slide2.findViewById(R.id.tutorial_progress);
		m_sliderTwoProgressBar.setVisibility(View.INVISIBLE);
		
		
		RelativeLayout slide4 = (RelativeLayout)findViewById(R.id.tutorial_slider_item4);
		m_sliderFourProgressBar = (ProgressBar)slide4.findViewById(R.id.tutorial_progress);
		m_sliderFourProgressBar.setVisibility(View.INVISIBLE);
		
		m_slideFourButtonNext = (Button)slide4.findViewById(R.id.tutorial_btn_next);
		m_slideFourButtonNext.setVisibility(View.INVISIBLE);
		
		
		RelativeLayout slide5 = (RelativeLayout)findViewById(R.id.tutorial_slider_item5);
		m_slideFiveButtonNext = (Button)slide5.findViewById(R.id.tutorial_btn_next);
		m_slideFiveButtonNext.setVisibility(View.INVISIBLE);
		
		TextView btnSkipTutorial =  (TextView)findViewById(R.id.txt_skip_tutorial);
		btnSkipTutorial.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) 
			{
				// TODO Auto-generated method stub
				Intent intent = new Intent(TutorialActivity.this, HomeActivity.class);
				startActivity(intent);
				finish();
			}
		});
		
		m_checkUpdateText = (TextView) findViewById(R.id.txt_checking_update);
		/*
		RelativeLayout slide2 = (RelativeLayout)findViewById(R.id.tutorial_slider_item2);
		RelativeLayout slide4 = (RelativeLayout)findViewById(R.id.tutorial_slider_item4);
		//RelativeLayout slide5 = (RelativeLayout)findViewById(R.id.tutorial_slider_item5);
		
		
		
		m_sliderFourProgressBar = (ProgressBar)slide4.findViewById(R.id.tutorial_slider_progress_bar);
		m_sliderFourProgressBar.setVisibility(View.INVISIBLE);
		
		m_slideFourButtonNext = (Button)slide4.findViewById(R.id.btn_moveNext);
		m_slideFourButtonNext.setVisibility(View.INVISIBLE);
		
		//m_slideFiveButtonNext = (Button)slide5.findViewById(R.id.btn_moveNext);
		//m_slideFiveButtonNext.setVisibility(View.INVISIBLE);
		 */
	}
	
	public void moveToNext( View view ) 
	{
		Log.d("TutorialActivity", "Next button clicked!");
		onNextSlide();
	}
	
	
	private void onNextSlide() {
		
		if( m_scrollableController == null || m_currentPage > m_pageTotal - 1  ) 
		{
			return;
		}
		
		//+ ET 040214 : Check if already set up
		if( m_panelWidth == -1 ) 
		{
			m_panelWidth = (short)findViewById(android.R.id.content).getWidth();
		}
		
		m_scrollableController.smoothScrollBy( m_panelWidth, 0 );
		m_currentPage++;
		
		
		Log.d("TutorialActivity", "currentPage = " + m_currentPage);
		
		//+ ET 0-0214 : Serves a state machine also for tutorial
		switch( m_currentPage )
		{
		case 1:
			if (enableBluetooth()) {
				initialize();
			}
			unloadSlide(0);
			loadSlide(1);
			break;
			
		case 2:
			unloadSlide(1);
			loadSlide(2);
			break;
			
		case 3:
			unloadSlide(2);
			loadSlide(3);
			
			m_sliderFourProgressBar.setVisibility(View.VISIBLE);
			setupKreyos();
			break;
			
		case 4:
			unloadSlide(3);
			loadSlide(4);
			
			startUpdating();
			break;
			
		case 5:
			unloadSlide(4);
			loadSlide(5);
			
			stopAnimation();
			break;
			
		case 6:
			
			unlockWatch();
			break;
		// So on
		}
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) 
	{
		if( requestCode == BLUETOOTH_REQUEST_CODE_ENABLE ) 
		{
			if (resultCode == 0) 
			{
		    	// Cancel
				Log.d("TutorialActivity", "Cancel");
				enableBluetooth();
		    } 
			else 
			{
		    	// Ok 
		    	Log.d("TutorialActivity", "OK");
		    	//initialize();
		    }
		}
	}
	
	
	private void initialize() 
	{
	
		KreyosActivity.initPrefs(this);
		SharedPreferences prefs = KreyosActivity.getPrefs();
		
		// + ET 041814 : Remove save device name to abort auto start
		SharedPreferences.Editor editor = prefs.edit();
	    editor.putString("bluetooth.device_name", "");
	    editor.commit();
	        
		
		// + ET 041314 : Init BTAgent
		BluetoothAgent.initBluetoothAgent(this);
		BluetoothAgent.getInstance(null).initialize();
    	
    	Intent intent = new Intent(this, KreyosService.class);
        initCloudSync(prefs, intent);
        initNotifications(prefs, intent);
        initBluetoothAgent(prefs, intent);
        
        bindService(intent,	mServiceConnection, Context.BIND_AUTO_CREATE);
        msgBTHanlder = new MsgBTHandler(this);
        
		getAvailableWatchDevices();		
	}

	
	private void setupKreyos() {
		
	 	m_activeDeviceName = m_selectedDevice.getName();
	 	
		SharedPreferences.Editor editor = getPrefs().edit();
		editor.putString("bluetooth.device_name", m_activeDeviceName);
		editor.commit();  
		
		BluetoothAgent agent = BluetoothAgent.getInstance(msgBTHanlder);
		agent.bindDevice(m_activeDeviceName);
		agent.restartSession();
		 
		getService().initBluetooth(m_activeDeviceName);
		
		// + ET 04212014 : TODO checking of firmware version
		// onCheckVersion();
		
		
		Handler delayHandler= new Handler();
        Runnable r = new Runnable() {
           @Override
           public void run() {
        	   onCheckVersion(); 
           }
        };
        delayHandler.postDelayed(r, 2000); //5 secs. delay
        
	}
	
	
	public void onCheckVersion()
	{
		// + ET 05062014 : TODO Implement checking of latest firmware		
		if(AppHelper.instance().WATCH_STATE == WATCH_STATE_VALUE.CONNECTED) {
			try {
				JSONObject params = new JSONObject();
				params.put("email", getPrefs().getString(KreyosPrefKeys.USER_EMAIL, ""));
				params.put("auth_token", getPrefs().getString(KreyosPrefKeys.USER_KREYOS_TOKEN , ""));
				String response = RequestManager.instance().post(KreyosPrefKeys.URL_FIRMWARE, params);
				Log.d("Response", "" + response);
				
				JSONObject jsonResponse = new JSONObject(response);
				if (jsonResponse.has("success")) {
					String versionNo = jsonResponse.getString("version_number");
					if(versionNo.equals(getPrefs().getString(KreyosPrefKeys.FIRMWARE_VERSION, ""))) {
						m_checkUpdateText.setText("SOFTWARE ALREADY UPDATED");
						m_slideFourButtonNext.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View arg0) {
								// TODO Auto-generated method stub
								Intent intent = new Intent(TutorialActivity.this, HomeActivity.class);
				        	    startActivity(intent);
				        	    finish();
							}
						});
					} else {
						defaultFileUrl = jsonResponse.getString("attachment");
						defaultFileUrl = "http:" + defaultFileUrl;
						Log.d("Firmware", defaultFileUrl);
						m_checkUpdateText.setText("NEW SOFTWARE AVAILABLE");
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				onErrorOccured();
			}
		} else {
			onShowErrorDialog(1);
		}

		m_sliderFourProgressBar.setVisibility(View.INVISIBLE);
		m_slideFourButtonNext.setVisibility(View.VISIBLE);
	}
	
	
	private void startUpdating()
	{
		// + ET 05082014 : Show progress dialog
		// m_progressDialog = ProgressDialog.show(this, "Please wait",	"Updating Firmware", true);
		
		AsyncTask<Void, Void, InputStream> t = new AsyncTask<Void, Void, InputStream>()  {
			
			protected InputStream doInBackground(Void... p) {
				try {
					//URL aURL = new URL(urlText.getText().toString());
					URL aURL = new URL(defaultFileUrl);
					URLConnection conn = aURL.openConnection();
					conn.setUseCaches(true);
					conn.connect();
					InputStream is = conn.getInputStream();
					
					Protocol btp = new Protocol(BluetoothAgent.getInstance(null), TutorialActivity.this);
					btp.sendStream("firmware", is);
					is.close();
					
					m_isAlreadyConnected = false;
					
				} catch (IOException e) {
					e.printStackTrace();
					
					// + ET 05082014 : There's an error;
					onErrorOccured();
				}
				return null;
			}

			protected void onPostExecute(InputStream is) {
				
			}
		};
		t.execute();
		playAnimation();
	}
	
	
	private void onErrorOccured()
	{
		this.runOnUiThread(new Runnable()
		{
			
			@Override
			public void run() 
			{
				// TODO Auto-generated method stub
				onShowErrorDialog(2);
				m_slideFiveButtonNext.setVisibility(View.VISIBLE);
				m_slideFiveButtonNext.setOnClickListener(new View.OnClickListener() 
				{
					
					@Override
					public void onClick(View v) 
					{
						// TODO Auto-generated method stub
						startUpdating();
					}
				});
			}
		});
	}
	
	
	private void onShowErrorDialog(int p_index)
	{
		switch(p_index)
		{
			case 1:
				KreyosUtility.showErrorMessage(this, "Device not found"	, "Please connect your Kreyos Watch");
			break;
			
			case 2:
			{
				// m_progressDialog.dismiss();
				KreyosUtility.showErrorMessage(this, "Firmware Update"	, "File not found");
			}
			break;
			
			case 3:
				KreyosUtility.showErrorMessage(this, "Firmware Update"	, "Firmware already updated");
			break;
		}
	}
	
	
	public void onCompleteUpdate() 
	{
		m_sliderFourProgressBar.setVisibility(View.INVISIBLE);
	}

	
	private void unlockWatch() 
	{
		Log.d("TutorialActivity", "Unlock Watch");
		BluetoothAgent agent = BluetoothAgent.getInstance(msgBTHanlder);
		Protocol p = new Protocol(agent, TutorialActivity.this);
		p.unlockWatch();
		p.getDeviceID();
		
		Handler delayHandler= new Handler();
        Runnable r=new Runnable()
        {
           @Override
           public void run() 
           {
        	   Intent intent = new Intent(TutorialActivity.this, HomeActivity.class);
        	   startActivity(intent);
        	   finish();
           }
        };
        delayHandler.postDelayed(r, 5000);
	}
	
	
	//+ ET 041314 : Message Handler
	private static class MsgBTHandler extends Handler 
	{		
		private final WeakReference<TutorialActivity> mActivity;

		public MsgBTHandler(TutorialActivity activity)
		{
			mActivity = new WeakReference<TutorialActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) 
		{
			TutorialActivity activity = mActivity.get();
			if (activity != null) 
			{
				activity.handleBTMessage(msg);
			}
		}
	}
	
	public void handleBTMessage( Message msg )
	{
		Log.d("Watch Message", "watch msg : " + msg.what );
		
		switch ( msg.what)
		{
		case Protocol.MessageID.MSG_BLUETOOTH_STATUS:
			Log.d("TutorialActivity", "BLUETOOTH STATUS : " + msg.obj );
			if( msg.obj.toString() == "Running" )
			{
				connecBluetoothHeadset();
			}
			break;

		case Protocol.MessageID.MSG_FIRMWARE_VERSION:
			Log.d("TutorialActivity", "FW VERSION:" + msg.obj.toString());
			SharedPreferences.Editor editor = getPrefs().edit();
			editor.putString(KreyosPrefKeys.FIRMWARE_VERSION, msg.obj.toString());
			editor.commit();
			break;
			
		default:
			break;
		}
		
	}

	//+ ET 041314 : Service Handler	
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
    
    private void storeAnimationVariables() {
		
		int[] mAnimViews = new int[] {
			R.id.tutorial_loader_on1,
			R.id.tutorial_loader_on2,
			R.id.tutorial_loader_on3,
			R.id.tutorial_loader_on4,
			R.id.tutorial_loader_on5,
			R.id.tutorial_loader_on6,
			R.id.tutorial_loader_on7
		};
		
		mAnimCircles = new ImageView[mAnimViews.length];
		for (int index = 0; index < mAnimViews.length; index++) {
			mAnimCircles[index] = (ImageView)findViewById(mAnimViews[index]); 
		}
	}
	
	private void playAnimation() {
		
		mAnimTimer = new Timer();
		mAnimTask  = new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						onUpdateAnimation();
					}
				});
			}
		};
		
		mAnimTimer.schedule(mAnimTask, 0, 500);
	}
	
	private void onUpdateAnimation() {
		
		Log.d("Animation", "Index:" + mAnimIndex);
		for (int index = 0; index < mAnimCircles.length; index++) {
			mAnimCircles[index].setImageResource(R.drawable.tutorial_loader_off);
		}
		for (int index = 0; index < mAnimIndex; index++) {
			mAnimCircles[index].setImageResource(R.drawable.tutorial_loader_on);
		}
		mAnimIndex++;
		if (mAnimIndex > mAnimCircles.length) {
			mAnimIndex = 0;
		}
	}
	
	private void stopAnimation() {
		
		mAnimTimer.cancel();
		mAnimTimer = null;
	}
 
	public static KreyosService getService() 
	{
		return mService;
	}

	
	public static void setService(KreyosService service) 
	{
		mService = service;
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
	    
	
	private void initBluetoothAgent(SharedPreferences prefs, Intent intent) 
	{
	    	intent.putExtra("bluetooth.device_name", prefs.getString("bluetooth.device_name", ""));
	}
		
	
	@Override
	protected void onDestroy()
	{
		// TODO Auto-generated method stub
		super.onDestroy();
		try {
			unregisterReceiver(m_deviceReceiver);
			unbindService(mServiceConnection);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	
	@Override
	protected void onBluetoothHeadsetConnected() 
	{
		// TODO Auto-generated method stub
		super.onBluetoothHeadsetConnected();
		if (!m_isAlreadyConnected) {	
			m_isAlreadyConnected = true;
			onNextSlide();
		}
		
	}

}
