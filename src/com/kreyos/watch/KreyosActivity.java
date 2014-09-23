package com.kreyos.watch;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewDebug.HierarchyTraceType;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
//import android.widget.Toast;

import com.facebook.android.Facebook;
import com.facebook.model.GraphUser;
import com.kreyos.watch.R;
import com.kreyos.watch.R.color;
import com.kreyos.watch.bluetooth.BluetoothAgent;
import com.kreyos.watch.bluetooth.KreyosService;
import com.kreyos.watch.bluetooth.Protocol;
import com.kreyos.watch.bluetooth.BluetoothAgent.ErrorCode;
import com.kreyos.watch.managers.AppHelper;
import com.kreyos.watch.managers.AppHelper.WATCH_STATE_VALUE;
import com.kreyos.watch.objectdata.ActivityDataDoc;
import com.kreyos.watch.services.StackActivitiesService;
import com.kreyos.watch.utils.Utils;

public class KreyosActivity extends FragmentActivity {

	// bluetooth
	private static KreyosService mService = null;

	protected ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			setService(null);
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			setService(((KreyosService.KreyosServiceBinder) binder)
					.getService());
			mService.initServiceTasks();
		}
	};

	protected ServiceConnection mStackServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
		}
	};

	private ServiceListener m_headsetBluetoothListener = new ServiceListener() {

		@Override
		public void onServiceDisconnected(int profile) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onServiceConnected(int profile, BluetoothProfile proxy) {
			// TODO Auto-generated method stub
			try {
				Method connect = BluetoothHeadset.class.getDeclaredMethod(
						"connect", BluetoothDevice.class);
				try {
					// how to get the paired device?
					// available resources ? bluetooth name
					String deviceName = getPrefs().getString(
							"bluetooth.device_name", "");
					BluetoothDevice selectedDevice = null;
					Set<BluetoothDevice> bondedDevices = BluetoothAdapter
							.getDefaultAdapter().getBondedDevices();

					for (BluetoothDevice i : bondedDevices) {
						Log.d("KreyosActivity", "Condition = " + i.getName()
								+ ":" + deviceName);
						if (i.getName().equalsIgnoreCase(deviceName)) {
							Log.d("KreyosActivity", "Selected Device");
							selectedDevice = i;
							break;
						}
					}

					if (selectedDevice == null) {
						return;
					}

					m_bluetoohProfile = proxy;
					connect.setAccessible(true);
					connect.invoke((BluetoothHeadset) m_bluetoohProfile,
							selectedDevice);
					// connect.invoke(BluetoothProfile.HEADSET, selectedDevice);
					Log.d("KreyosActivity", "BluetoothHeadSet Connected");
					onBluetoothHeadsetConnected();

				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

	public class PrefKeys {
		public static final String FB_ACCESS_TOKEN = "CAAIZCqnLR918BANlpMZBB7aTE4EGU6ecl9b3ZBH9Ya1XYXJkIDa2aHldG6Ud5pYHWKKmbBc9PZB3DiPH7JiNUOcKDZCMrGZBHLjP1633jXjRfSM4oj9qpL4XVvCRARE0jIkr0Cnj8et4uwbvZBdZA3mWVIf0nSZCZBZC5N2pUQKu4sSdTr6wcevq4iZAO4CmN0KYssVtC2d7lZBU12QZDZD";
		public static final String FB_ACCESS_EXPIRES = "1396260000";
		public static final String TW_ACCESS_TOKEN = "twitter_access_token";
		public static final String TW_SECRET_TOKEN = "twitter_secret_token";
	}

	public static final String PREFS_NAME = "LoginPrefs";

	private static SharedPreferences mPrefs = null;
	private static Facebook mfacebook = null;
	private static final String fbAppId = "351545831658267";

	// + ET 041814 : Bluetooth headset
	public static BluetoothProfile m_bluetoohProfile;
	public static int BLUETOOTH_REQUEST_CODE_ENABLE = 10;

	// private static Twitter mTwitter = null;
	public static final String twConsummerKey = "IjYasmHaJB6suXkg53MbA";
	public static final String twConsummerSecret = "UhYewry7IYvWH6uPjjCNT4WEG4iZnI3S7pBHyD5av0";
	public static String[] fbPermissions = new String[] { "email",
			// "publish_checkins",
			"user_birthday",
			// "user_friends",
			"user_location",
	// "friends_location",
	// "publish_stream",
	// "read_mailbox",
	// "read_stream",
	// "sms"
	};

	public class Fonts {
		public final static String ProximaNova_Black = "fonts/ProximaNova_Black.ttf";
		public final static String ProximaNova_Bold = "fonts/ProximaNova_Bold.ttf";
		public final static String ProximaNova_BoldIt = "fonts/ProximaNova_BoldIt.ttf";
		public final static String ProximaNova_Extrabold = "fonts/ProximaNova_Extrabold.ttf";
		public final static String ProximaNova_Light = "fonts/ProximaNova_Light.ttf";
		public final static String ProximaNova_LightItalic = "fonts/ProximaNova_LightItalic.ttf";
		public final static String ProximaNova_RegItalic = "fonts/ProximaNova_RegItalic.ttf";
		public final static String ProximaNova_Regular = "fonts/ProximaNova_Regular.ttf";
		public final static String ProximaNova_RegularItalic = "fonts/ProximaNova_RegularItalic.ttf";
		public final static String ProximaNova_Semibold = "fonts/ProximaNova_Semibold.ttf";
		public final static String ProximaNova_SemiboldItalic = "fonts/ProximaNova_SemiboldItalic.ttf";
		public final static String ProximaNovaCond_Light = "fonts/ProximaNovaCond_Light.ttf";
		public final static String ProximaNovaCond_LightIt = "fonts/ProximaNovaCond_LightIt.ttf";
		public final static String ProximaNovaCond_Regular = "fonts/ProximaNovaCond_Regular.ttf";
		public final static String ProximaNovaCond_RegularIt = "fonts/ProximaNovaCond_RegularIt.ttf";
		public final static String ProximaNovaCond_Semibold = "fonts/ProximaNovaCond_Semibold.ttf";
		public final static String ProximaNovaCond_SemiboldIt = "fonts/ProximaNovaCond_SemiboldIt.ttf";
	};

	public static void initPrefs(Activity activity) {
		if (mPrefs == null) {
			mPrefs = activity.getPreferences(MODE_PRIVATE);
		}
	}

	public static SharedPreferences getPrefs() {
		return mPrefs;
	}

	public static void clearPrefs() {

	}

	@SuppressWarnings("deprecation")
	public static Facebook getFacebook(Context context) {
		if (mfacebook == null) {
			mfacebook = new Facebook(fbAppId);

			String access_token = getPrefs().getString(
					PrefKeys.FB_ACCESS_TOKEN, null);
			if (access_token != null) {
				mfacebook.setAccessToken(access_token);

				long expires = getPrefs()
						.getLong(PrefKeys.FB_ACCESS_EXPIRES, 0);
				if (expires != 0) {
					mfacebook.setAccessExpires(expires);
				} else {
					mfacebook.extendAccessTokenIfNeeded(context, null);
				}
			}
		}
		return mfacebook;
	}

	public void showNetworkError() {
		new AlertDialog.Builder(this)
				.setTitle("Network Error")
				.setMessage(
						"Please check your Internet Connection and try again.")
				.setPositiveButton("Ok", null).setIcon(R.drawable.errorr)
				.show();
	}

	public void clearFacebookTokenCache() {
		SharedPreferences.Editor editor = getPrefs().edit();
		editor.remove(PrefKeys.FB_ACCESS_TOKEN);
		editor.remove(PrefKeys.FB_ACCESS_EXPIRES);
		editor.commit();
	}

	/*
	 * public Twitter getTwitter() { if (mTwitter == null) { mTwitter = new
	 * Twitter(R.drawable.icon_twitter_unselected, twConsummerKey,
	 * twConsummerSecret);
	 * 
	 * String access_token = getPrefs().getString(PrefKeys.TW_ACCESS_TOKEN,
	 * null); String secret_token =
	 * getPrefs().getString(PrefKeys.TW_SECRET_TOKEN, null); if(access_token !=
	 * null && secret_token != null) {
	 * mTwitter.setOAuthAccessToken(access_token, secret_token); } }
	 * 
	 * return mTwitter; }
	 */

	public void clearTwitterTokenCache() {
		SharedPreferences.Editor editor = getPrefs().edit();
		editor.remove(PrefKeys.TW_ACCESS_TOKEN);
		editor.remove(PrefKeys.TW_SECRET_TOKEN);
		editor.commit();
	}

	public void loadFontToTextView(TextView view, String fontPath) {
		Typeface fontFace = Typeface.createFromAsset(getAssets(), fontPath);
		view.setTypeface(fontFace);
	}

	public TextView loadFontToTextView(final int viewId, String fontPath) {
		Typeface fontFace = Typeface.createFromAsset(getAssets(), fontPath);
		TextView text = (TextView) findViewById(viewId);
		text.setTypeface(fontFace);
		return text;
	}

	public TextView loadFontToTextView(View view, final int viewId,
			String fontPath) {
		Typeface fontFace = Typeface.createFromAsset(getAssets(), fontPath);
		TextView text = (TextView) view.findViewById(viewId);
		text.setTypeface(fontFace);
		return text;
	}

	public TextView loadFontToEditText(final int viewId, String fontPath) {
		Typeface fontFace = Typeface.createFromAsset(getAssets(), fontPath);
		EditText text = (EditText) findViewById(viewId);
		text.setTypeface(fontFace);
		return text;
	}

	/*
	 * public View setSubActivityEntry(final int viewId, final String
	 * activityName, final Class<?> cls) { View view = findViewById(viewId);
	 * 
	 * view.setOnClickListener(new OnClickListener() {
	 * 
	 * @Override public void onClick(View v) { KreyosApp app =
	 * (KreyosApp)getApplicationContext();
	 * app.getMainActivity().setActivePage(activityName, cls); } });
	 * 
	 * return view; }
	 */

	public TextView loadText(View view, final int viewId, String fontPath) {
		TextView text = (TextView) view.findViewById(viewId);
		text.setText(fontPath);
		return text;
	}

	public ImageView loadImage(View view, final int viewId, String url) {// Bitmap
																			// bitmap)
																			// {

		ImageView image = (ImageView) view.findViewById(viewId);
		Utils.setImg(image, url);
		return image;
	}

	public View dynamicFillLinearLayout(final int layoutId, final int viewId,
			final int containerId) {
		LinearLayout container = (LinearLayout) findViewById(containerId);
		View content = LayoutInflater.from(this).inflate(layoutId, null)
				.findViewById(viewId);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		content.setLayoutParams(params);

		container.removeAllViews();
		container.addView(content);
		return content;
	}

	public View dynamicApendViewToLinearLayout(final int layoutId,
			final int viewId, final int containerId,
			final LinearLayout.LayoutParams layoutPara) {
		LinearLayout container = (LinearLayout) findViewById(containerId);
		View content = LayoutInflater.from(this).inflate(layoutId, null)
				.findViewById(viewId);

		if (layoutPara != null) {
			content.setLayoutParams(layoutPara);
		}
		container.addView(content);
		return content;
	}

	public int dip2px(float dpValue) {
		float scale = this.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/*
	 * { "id": "100006814201447", "name": "Dev Dev", "first_name": "Dev",
	 * "last_name": "Dev", "link":
	 * "https://www.facebook.com/profile.php?id=100006814201447", "birthday":
	 * "01/01/1984", "gender": "male", "email": "dev@kreyos.com", "timezone": 8,
	 * "locale": "en_GB", "verified": true, "updated_time":
	 * "2013-09-23T05:50:29+0000" }
	 */

	public void cacheFBUserProfile(GraphUser user) {
		String userId = "";
		String userFirstName = "";
		String userLastName = "";
		String userBirthday = "";
		String userGender = "";
		String userEmail = "";

		try {
			userId = user.getId();
			userFirstName = user.getFirstName();
			userLastName = user.getLastName();
			userBirthday = user.getBirthday();
			userGender = user.asMap().get("gender").toString();
			userEmail = user.asMap().get("email").toString();

		} catch (NullPointerException e) {
			userId = user.getId();
			userFirstName = user.getFirstName();
			userLastName = user.getLastName();
			userBirthday = user.getBirthday();
			userGender = user.asMap().get("gender").toString();
			userEmail = user.getUsername();

		}

		SharedPreferences.Editor editor = getPrefs().edit();
		editor.putString("cloud_id", userId);
		editor.putString("user_profile_cache.first_name", userFirstName);
		editor.putString("user_profile_cache.last_name", userLastName);
		editor.putString("user_profile_cache.birthday", userBirthday);
		editor.putString("user_profile_cache.gender", userGender);
		editor.putString("user_profile_cache.email", userEmail);
		editor.commit();
	}

	public int[] sports_grids_data = new int[] { 0, 1, 4, 3, 9

	};

	// temporary uncommented for synch and writing to watch
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
		int sports_grid = SportsActivity.getGridNumber() - 2;// temp
																// SportsActivity.getGridNumber(getPrefs().getInt("watch_grid.active",
																// R.layout.sports_grid_3))
																// - 2; //-2 to
																// meet watch
																// define
//		Toast.makeText(this, "Total Grid: " + sports_grid, Toast.LENGTH_SHORT).show();
		int[] sports_grids = new int[5];
		sports_grids[0] = 0;

		// for testing
		sports_grids[1] = sports_grids_data[1];
		sports_grids[2] = sports_grids_data[2];
		sports_grids[3] = sports_grids_data[3];
		sports_grids[4] = sports_grids_data[4];
		// sports_grids[5] = 9;
		// temporary spped distance cadence calories
		// sports_grids[1] =
		// SportsActivity.multiLineToGridTypeId(getPrefs().getInt("watch_grid.grid_0",
		// R.string.heartRateML));
		// sports_grids[2] =
		// SportsActivity.multiLineToGridTypeId(getPrefs().getInt("watch_grid.grid_1",
		// R.string.paceML));
		// sports_grids[3] =
		// SportsActivity.multiLineToGridTypeId(getPrefs().getInt("watch_grid.grid_2",
		// R.string.speedML));
		// sports_grids[4] =
		// SportsActivity.multiLineToGridTypeId(getPrefs().getInt("watch_grid.grid_3",
		// R.string.eleGainML));

		// TODO: fill the preference with read goals set from UI and call this
		// function when changed (in HomeActivity)
		int[] goals = new int[3];
		// temp goals[0] =
		// SportsActivity.multiLineToGridTypeId(getPrefs().getInt("watch_grid.grid_0",
		// R.string.stepsML));
		goals[0] = getPrefs().getInt("sports_goals.steps", 1000);
		goals[1] = getPrefs().getInt("sports_goals.distance", 2000);
		goals[2] = getPrefs().getInt("sports_goals.calories", 500);

		// Commented : Old implementation
		/*
		 * String weightStr = getPrefs().getString("user_profile.weight", "50");
		 * if (weightStr == "") weightStr = "50"; String heightStr =
		 * getPrefs().getString("user_profile.height", "170"); if (heightStr ==
		 * "") heightStr = "170";
		 */

		// 50 kg
		String defaultWeight = "110";
		// 5'6 on feet
		String defaultHeight = "67";

		String weightStr = getPrefs().getString(KreyosPrefKeys.USER_WEIGHT,
				defaultWeight);
		if (weightStr.equals("")) {
			// Set default value if we get blank
			weightStr = defaultHeight;
		} else {
			// Convert values
			try {
				weightStr = ""
						+ Utils.convertLbstoKg(Integer.parseInt(weightStr));
			} catch (Exception ex) {
				weightStr = defaultHeight;
			}
		}

		String heightStr = getPrefs().getString(KreyosPrefKeys.USER_HEIGHT,
				defaultHeight);
		if (heightStr.equals("")) {
			// Set default value if we get blank
			heightStr = defaultHeight;
		} else {
			// Convert values
			try {
				heightStr = ""
						+ Utils.convertIntoCm(Integer.parseInt(heightStr));
			} catch (Exception ex) {
				heightStr = defaultHeight;
			}

		}

		// Convert to integer to write on watch
		int weight = Integer.parseInt(weightStr);
		int height = Integer.parseInt(heightStr);

		// TODO: fill the preference with read goals set from UI and call this
		// function when changed (in GestureActivity)
		boolean enableGesture = getPrefs().getBoolean("gesture.enable", true);
		boolean isLeftHandGesture = getPrefs().getBoolean("gesture.watch_hand",
				true);

		int[] actionsTable = new int[4];
		actionsTable[0] = getPrefs().getInt("gesture.swipe_left", 0);
		actionsTable[1] = getPrefs().getInt("gesture.swipe_right", 1);
		actionsTable[2] = getPrefs().getInt("gesture.twist_left", 2);
		actionsTable[3] = getPrefs().getInt("gesture.twist_right", 3);

		boolean isUkUnit = !getPrefs().getBoolean("user_profile.is_metric",
				true);

		Protocol p = new Protocol(BluetoothAgent.getInstance(null), this);
		p.syncWatchConfig(worldClockTable, worldClockOffsets, isDigit, analog,
				digit, sports_grid, sports_grids, goals, weight, height,
				enableGesture, isLeftHandGesture, actionsTable, isUkUnit);
	}

	public static KreyosActivity m_activity = null;

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		m_activity = this;
		setHeaderByConnection();

	}

	protected void setHeaderByConnection() {
		RelativeLayout header = (RelativeLayout) findViewById(R.id.relativeLayout1);
		if (header == null) {
			return;
		}
		if (AppHelper.instance().WATCH_STATE == AppHelper.WATCH_STATE_VALUE.CONNECTED) {
			header.setBackgroundDrawable(this.getResources().getDrawable(
					R.color.header_blue));
		} else {
			header.setBackgroundDrawable(this.getResources().getDrawable(
					R.color.red));
		}
	}

	protected void onCallonCreate() {

		// + ET 05062014 : Checking of watch state
		int watchState = getPrefs().getInt(KreyosPrefKeys.USER_WATCHED_STATE,
				AppHelper.WATCH_STATE_VALUE.DISCONNECTED.ordinal());
		AppHelper.instance().WATCH_STATE = WATCH_STATE_VALUE.values()[watchState];

		if (AppHelper.instance().WATCH_STATE == AppHelper.WATCH_STATE_VALUE.WAITING
				|| AppHelper.instance().WATCH_STATE == AppHelper.WATCH_STATE_VALUE.DISCONNECTED) {
			return;
		}

		SharedPreferences prefs = getPrefs();
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();

		if (mBluetoothAdapter == null) {
			// Device does not support Bluetooth
//			Toast.makeText(this, "Device does not support Bluetooth",
//					Toast.LENGTH_SHORT).show();
		} else {
			if (!mBluetoothAdapter.isEnabled()) {
				// Toast.makeText(this, "Please turn on bluetooth",
				// Toast.LENGTH_SHORT).show();
			} else {
				// bluetooth
				// Toast.makeText(this, "Test Log", Toast.LENGTH_SHORT).show();
				Log.d("KreyosActivity", "Called");

				Intent intent = new Intent(this, KreyosService.class);
				initCloudSync(prefs, intent);
				initNotifications(prefs, intent);
				initBluetoothAgent(prefs, intent);
				bindService(intent, mServiceConnection,
						Context.BIND_AUTO_CREATE);

				// Stack Service
				Intent stackIntent = new Intent(this,
						StackActivitiesService.class);
				bindService(stackIntent, mStackServiceConnection,
						Context.BIND_AUTO_CREATE);

				try {
					connecBluetoothHeadset();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
	}

	private void initCloudSync(SharedPreferences prefs, Intent intent) {
		intent.putExtra("cloud_id", prefs.getString("cloud_id", ""));
		intent.putExtra("access_token", prefs.getString("access_token", ""));
		intent.putExtra("access_expires", prefs.getLong("access_expires", 0));
		intent.putExtra("twitter_access_token",
				prefs.getString("twitter_access_token", ""));
		intent.putExtra("twitter_secret_token",
				prefs.getString("twitter_secret_token", ""));
	}

	private void initNotifications(SharedPreferences prefs, Intent intent) {
		intent.putExtra("notification.facebook",
				prefs.getBoolean("notification.facebook", false));
		intent.putExtra("notification.weather",
				prefs.getBoolean("notification.weather", false));
		intent.putExtra("notification.twitter",
				prefs.getBoolean("notification.twitter", false));
		intent.putExtra("notification.reminder",
				prefs.getBoolean("notification.reminder", false));
		intent.putExtra("notification.sms",
				prefs.getBoolean("notification.sms", false));
		intent.putExtra("notification.call",
				prefs.getBoolean("notification.call", false));
		intent.putExtra("notification.low_battery",
				prefs.getBoolean("notification.low_battery", false));
		intent.putExtra("notification.bt_outof_range",
				prefs.getBoolean("notification.bt_outof_range", false));
	}

	private void initBluetoothAgent(SharedPreferences prefs, Intent intent) {
		intent.putExtra("bluetooth.device_name",
				prefs.getString("bluetooth.device_name", ""));
	}

	public static KreyosService getService() {
		return mService;
	}

	public static void setService(KreyosService service) {
		mService = service;
	}

	@Override
	public void onBackPressed() {
		// do nothing
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		/*
		 * Old unbinding of service try { Log.d("Log",
		 * "Clean all service or receivers"); unbindService(mServiceConnection);
		 * unbindService(mStackServiceConnection); //
		 * unregisterReceiver(m_deviceReceiver);
		 * 
		 * BluetoothAdapter.getDefaultAdapter().closeProfileProxy(BluetoothProfile
		 * .HEADSET, m_bluetoohProfile); if (m_progressDialog == null) { return;
		 * } m_progressDialog.dismiss(); } catch( Exception e) {
		 * e.printStackTrace(); }
		 */

		if (mServiceConnection != null) {
			try {
				unbindService(mServiceConnection);
			} catch (Exception ex) {
				// ex.printStackTrace();
			}
		}

		if (mStackServiceConnection != null) {
			try {
				unbindService(mStackServiceConnection);
			} catch (Exception ex) {
				// ex.printStackTrace();
			}
		}

		try {
			BluetoothAdapter.getDefaultAdapter().closeProfileProxy(
					BluetoothProfile.HEADSET, m_bluetoohProfile);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (m_progressDialog == null) {
			return;
		}

		m_progressDialog.dismiss();
	}

	protected boolean enableBluetooth() {
		BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
		if (!bluetooth.isEnabled()) {
			// + ET 041314 : Show dialog for enabling bluetooth
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent,
					BLUETOOTH_REQUEST_CODE_ENABLE);
			return false;
		} else {
			// + ET 041314 : Bluetooth is already on
			return true;
		}
	}

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		// TODO Auto-generated method stub
		super.onActivityResult(arg0, arg1, arg2);
	}

	protected ArrayList<BluetoothDevice> m_devicesFound = new ArrayList<BluetoothDevice>();
	public ProgressDialog m_progressDialog = null;
	protected AlertDialog m_pairingDialog = null;
	public BluetoothDevice m_selectedDevice = null;

	protected void getAvailableWatchDevices() {
		BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();

		// Make it sure that no watch connected when searching
		try {
			AppHelper.instance().disconnectWatch(this);
		} catch (Exception ex) {
			ex.printStackTrace();
			Log.d("KreyosActivity::getAvailableWatchDevices",
					"Error on watch disconnection");
		}

		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		registerReceiver(m_deviceReceiver, filter);

		m_devicesFound.clear();
		bluetooth.startDiscovery();

		m_progressDialog = ProgressDialog.show(this, "Please wait",
				"Searching for devices", true);
		m_progressDialog.setCancelable(false);
	}

	protected BroadcastReceiver m_deviceReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub

			String action = intent.getAction();

			// + ET 041414 : If app found a bluetooth device
			try {
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device != null) {
					Log.d("KreyosActivity", "Found Watch:" + device.getName()
							+ "|| Device Lenght:" + device.getName().length());
					
					if (m_devicesFound.contains(device)) {
						return;
					}

					if (device.getName().length() > 11) {
						return;
					}
					

					
					if (device.getName().startsWith("Meteor")
					|| device.getName().startsWith("Kreyos")) {
						Log.d("KreyosActivity", "Device Added");
						m_devicesFound.add(device);
					}

					/*
					 * Old Implementation if
					 * (device.getName().startsWith("Meteor") ||
					 * device.getName().startsWith("Kreyos")) {
					 * if(!m_devicesFound.contains(device) &&
					 * device.getName().length() == 11) {
					 * m_devicesFound.add(device); } }
					 */
				}
				// + ET 041414 : After discover select device to pair
				else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
						.equals(action)) {
					// show pairing dialog
					unregisterReceiver(m_deviceReceiver);
					m_progressDialog.dismiss();
					showPairingDialog();
				}
			} catch (Exception ex) {
				Log.d("KreyosActivity", "" + ex);
			}
		}
	};

	private void showPairingDialog() {
		
		ArrayList<String> lisfOfWatch = new ArrayList<String>();
		
		for (BluetoothDevice btDevice : m_devicesFound) {
			if (btDevice.getName().startsWith("MeteorLE")) {
				Log.d("KreyosActivity", "Remove on Pairing Device: " + btDevice.getName());
				m_devicesFound.remove(btDevice);
			}
		}
		
		for (BluetoothDevice btDevice : m_devicesFound) {
			Log.d("KreyosActivity", "Pairing Device: " + btDevice.getName());
			// Log.d("TutorialActivity", "BondState" +
			// Integer.toString(getDeviceBondStateResId(btDevice.getBondState()))
			// );
			lisfOfWatch.add(btDevice.getName());
		}

		final CharSequence[] watchArray = lisfOfWatch.toArray(new CharSequence[lisfOfWatch.size()]);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Connect your Kreyos Watch");
		builder.setCancelable(false);
		builder.setItems(watchArray, new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int item) {
				BluetoothDevice[] itemArray = new BluetoothDevice[m_devicesFound
						.size()];
				BluetoothDevice[] devices = m_devicesFound.toArray(itemArray);
				pairDevice(devices[item]);
			}

		});

		builder.setPositiveButton("Search Again",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						m_pairingDialog.dismiss();
						getAvailableWatchDevices();

					}
				});

		if (!AppHelper.instance().IS_TUTORIAL_MODE) {
			builder.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							m_pairingDialog.dismiss();
							onCancelPairing();
						}
					});
		}

		m_pairingDialog = builder.create();
		m_pairingDialog.show();
	}

	protected void onCancelPairing() {
		// Super class
		// Override it on inherited class
		AppHelper.instance().WATCH_STATE = WATCH_STATE_VALUE.WAITING;
		SharedPreferences.Editor editor = getPrefs().edit();
		editor.putInt(KreyosPrefKeys.USER_WATCHED_STATE,
				WATCH_STATE_VALUE.WAITING.ordinal());
		editor.commit();
	}

	Timer mPairTime;
	TimerTask mPairTask;

	public void pairDevice(BluetoothDevice p_device) {

		m_selectedDevice = p_device;
		try {
			m_selectedDevice.getClass().getMethod("createBond", (Class[]) null)
					.invoke(m_selectedDevice, (Object[]) null);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		SharedPreferences.Editor editor = getPrefs().edit();
		editor.putString("bluetooth.device_name", m_selectedDevice.getName());
		editor.commit();

		mPairTime = new Timer();
		mPairTask = new TimerTask() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (m_selectedDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
					mPairTime.cancel();
					connecBluetoothHeadset();
				}
			}
		};
		mPairTime.schedule(mPairTask, 0, 1000);
		m_progressDialog = ProgressDialog.show(this, "Please wait",
				"Pairing with watch", true);
		m_progressDialog.setCancelable(false);

		/*
		 * while(true) { if(p_device.getBondState() ==
		 * BluetoothDevice.BOND_BONDED) { connecBluetoothHeadset(); break; } }
		 * 
		 * 
		 * try {
		 * 
		 * Log.d("Pairing", "Start Pairing..."); Method m =
		 * p_device.getClass().getMethod("createBond", (Class[]) null);
		 * m.invoke(p_device, (Object[]) null); Log.d("Pairing",
		 * "Pairing finished.");
		 * 
		 * m_selectedDevice = p_device;
		 * 
		 * // + ET 041814 : To secure that it is also connected as
		 * BluetoothHeadset m_progressDialog = ProgressDialog.show(this,
		 * "Please wait", "Pairing with watch", true);
		 * m_progressDialog.setCancelable(false);
		 * 
		 * while (true) { if (p_device.getBondState() ==
		 * BluetoothDevice.BOND_BONDED) { connecBluetoothHeadset(); break; } }
		 * 
		 * 
		 * 
		 * Handler delayHandler= new Handler(); Runnable r=new Runnable() {
		 * 
		 * @Override public void run() { if(m_selectedDevice.getBondState() ==
		 * m_selectedDevice.BOND_BONDED) { connecBluetoothHeadset(); } } };
		 * delayHandler.postDelayed(r, 5000); //5 secs. delay
		 * 
		 * 
		 * } catch (Exception e) { e.printStackTrace(); }
		 */
	}

	public void unpairDevice(BluetoothDevice p_device) {

		m_selectedDevice = p_device;
		try {
			m_selectedDevice.getClass().getMethod("removeBond", (Class[]) null)
					.invoke(m_selectedDevice, (Object[]) null);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		 * try { Method m = device.getClass() .getMethod("removeBond", (Class[])
		 * null); m.invoke(device, (Object[]) null); } catch (Exception e) {
		 * Log.e("Unpairing", e.getMessage()); }
		 */
	}

	protected void onBluetoothHeadsetConnected() {

		Log.d("KreyosActivity",
				"Bluetooth Headset Connected Override this for callback.");

		// + ET 05062014 : Change the watch state to connected
		SharedPreferences.Editor editor = getPrefs().edit();
		editor.putInt(KreyosPrefKeys.USER_WATCHED_STATE,
				WATCH_STATE_VALUE.CONNECTED.ordinal());
		if (m_selectedDevice != null) {
			editor.putString("bluetooth.device_name",
					m_selectedDevice.getName());
		}
		editor.commit();
		AppHelper.instance().WATCH_STATE = WATCH_STATE_VALUE.CONNECTED;
		setHeaderByConnection();

		// checkA2DPSupport();
		/*
		 * long delayTime = 3000; Handler delayHandler = new Handler(); Runnable
		 * r = new Runnable() {
		 * 
		 * @Override public void run() { // TODO Auto-generated method stub try
		 * { m_progressDialog.dismiss(); } catch(Exception ex) {
		 * 
		 * } } }; delayHandler.postDelayed(r, delayTime);
		 */

		try {
			m_progressDialog.dismiss();
		} catch (Exception ex) {

		}
	}

	public void checkA2DPSupport() {
		// to be implemented
	}

	protected void connecBluetoothHeadset() {

		final Context context = getApplicationContext();

		final Timer delayTimer = new Timer();
		TimerTask delayTask = new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Log.d("KreyosActivity", "Connecting");
				BluetoothAdapter.getDefaultAdapter().getProfileProxy(context,
						m_headsetBluetoothListener, BluetoothProfile.HEADSET);
				delayTimer.cancel();
			}
		};
		delayTimer.schedule(delayTask, 1000);

		/*
		 * Handler delayHandler = new Handler(); Runnable r = new Runnable() {
		 * 
		 * @Override public void run() { // TODO Auto-generated method stub
		 * 
		 * } }; delayHandler.postDelayed(r, 3000);
		 */

		/*
		 * Handler delayHandler = new Handler(); Runnable r = new Runnable() {
		 * 
		 * @Override public void run() { try { if(m_progressDialog.isShowing())
		 * { if(m_selectedDevice.getBondState() == m_selectedDevice.BOND_BONDED)
		 * { connecBluetoothHeadset(); } } } catch( Exception ex ) {
		 * 
		 * } } };
		 * 
		 * delayHandler.postDelayed(r, 5000); // 5secs.
		 */
	}

	protected void loadProfileImage(ImageView p_image) {

		boolean isFBDisplayed = false;
		String profileImage;
		Bitmap profileBitmap = null;

		profileImage = getPrefs().getString(KreyosPrefKeys.USER_FB_IMAGE, "");
		if (!profileImage.equals("")) {
			try {
				byte[] byteArray = KreyosUtility
						.convertStringToByteArray(profileImage);
				profileBitmap = KreyosUtility
						.convertByteArrayToBitmap(byteArray);
				p_image.setImageBitmap(Bitmap.createScaledBitmap(profileBitmap,
						800, 800, false));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return;
		}

		profileImage = getPrefs().getString(KreyosPrefKeys.USER_IMAGE, "");
		if (profileImage.equals("")) {
			return;
		}

		try {
			// byte[] byteArray =
			// KreyosUtility.convertStringToByteArray(profileImage);
			// profileBitmap =
			// KreyosUtility.convertByteArrayToBitmap(byteArray);
			profileBitmap = getScaledBitmap(profileImage, 800, 800);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (profileBitmap == null) {
			Log.d("KreyosActivity", "No Photo Selected");
			return;
		}

		try {
			// p_image.setImageBitmap(profileBitmap);
			p_image.setImageBitmap(Bitmap.createScaledBitmap(profileBitmap,
					800, 800, false));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	protected void unlockKreyosWatch() {
		BluetoothAgent agent = BluetoothAgent.getInstance(null);
		Protocol p = new Protocol(agent, KreyosActivity.this);
		p.unlockWatch();
	}

	protected Bitmap getScaledBitmap(String picturePath, int width, int height) {
		BitmapFactory.Options sizeOptions = new BitmapFactory.Options();
		sizeOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(picturePath, sizeOptions);

		Log.d("KreyosActivity", "Image Size: Height:" + sizeOptions.outHeight);
		Log.d("KreyosActivity", "Image Size: Width:" + sizeOptions.outWidth);

		/*
		 * if (sizeOptions.outHeight < 800 || sizeOptions.outWidth < 800) {
		 * Log.d("Photo", "Return Null"); return null; }
		 */

		int inSampleSize = calculateInSampleSize(sizeOptions, width, height);

		sizeOptions.inJustDecodeBounds = false;
		sizeOptions.inSampleSize = inSampleSize;

		return BitmapFactory.decodeFile(picturePath, sizeOptions);
	}

	protected int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and
			// width
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will
			// guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}
}
