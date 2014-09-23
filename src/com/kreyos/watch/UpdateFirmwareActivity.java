package com.kreyos.watch;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Set;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
//import android.widget.Toast;

import com.coboltforge.slidemenu.SlideMenuInterface.OnSlideMenuItemClickListener;
import com.kreyos.watch.R;
import com.kreyos.watch.bluetooth.BluetoothAgent;
import com.kreyos.watch.bluetooth.Protocol;
import com.kreyos.watch.managers.AppHelper;
import com.kreyos.watch.utils.RequestManager;
import com.kreyos.watch.managers.AppHelper.WATCH_STATE_VALUE;

public class UpdateFirmwareActivity extends KreyosActivity implements
		OnSlideMenuItemClickListener, OnClickListener {

	private static String defaultFileUrl = "http://freebsd.cloudapp.net/~howardsu/upgrade.bin";

	private BTDataHandler btMsgHandler = null;

	/* create new slideMenu for the right */
	private SlidingMenu slidemenu;
	private SlidingMenu slidemenu_right;
	private boolean isLeftMenuSelected = false;
	private TextView m_versionNo = null;
	private boolean mIsNeedToUnlock = false;

	private int ERROR_DISCONNECTED = 1;
	private int ERROR_FILE_NOT_FOUND = 2;
	private int ERROR_SAME_FIRMWARE = 3;

	public UpdateFirmwareActivity() {
		btMsgHandler = new BTDataHandler(this);
		if (AppHelper.instance().WATCH_STATE == WATCH_STATE_VALUE.CONNECTED) {
			BluetoothAgent.getInstance(btMsgHandler);
		}
	}

	private static class BTDataHandler extends Handler {
		private final WeakReference<UpdateFirmwareActivity> mService;

		public BTDataHandler(UpdateFirmwareActivity service) {
			mService = new WeakReference<UpdateFirmwareActivity>(service);
		}

		@Override
		public void handleMessage(Message msg) {
			UpdateFirmwareActivity service = mService.get();
			if (service != null) {
				service.handleBTData(msg);
			}
		}
	}

	private void handleBTData(Message msg) {
		switch (msg.what) {
		case Protocol.MessageID.MSG_BLUETOOTH_STATUS: {
			Log.d("TutorialActivity", "BLUETOOTH STATUS : " + msg.obj);
			if (msg.obj.toString() == "Running") {
				m_progressDialog.dismiss();
				connecBluetoothHeadset();
			} else {
				AppHelper.instance().WATCH_STATE = AppHelper.WATCH_STATE_VALUE.DISCONNECTED;
				setHeaderByConnection();
			}
		}
			break;
		case Protocol.MessageID.MSG_FIRMWARE_VERSION: {
			String version = (String) msg.obj;
			Log.e("MSG_FIRMWARE_VERSION", ":" + version);
			AppHelper.instance().saveFirmwareVersion(this, version);
			m_versionNo.setText("CURRENT VERSION:\n" + version);
		}
			break;

		// + ET 04242014 : Move to Sports Mode
		case Protocol.MessageID.MSG_ACTIVITY_PREPARE: {
			Intent i3 = new Intent(UpdateFirmwareActivity.this,
					SportsActivity.class);
			startActivity(i3);
			finish();
		}
			break;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_updatefirmware);
		initialize();
	}

	/************************************************
	 * Setup/Initialize Methods
	 **/
	private void initialize() {
		setupSlideMenu();
		// Override Font style and size
		KreyosUtility.overrideFonts(this,
				((ViewGroup) findViewById(android.R.id.content)).getChildAt(0),
				KreyosUtility.FONT_NAME.LEAGUE_GOTHIC_REGULAR);
		// Check watch connection
		onCallonCreate();
		// Register message handler
		BluetoothAgent.getInstance(btMsgHandler).bindMessageHandler(
				btMsgHandler);
		// Set the textView to current version installed
		m_versionNo = (TextView) findViewById(R.id.txt_firmware_version);
		m_versionNo.setText("CURRENT VERSION:\n"
				+ getPrefs()
						.getString(KreyosPrefKeys.FIRMWARE_VERSION, "0.0.0"));
		Button button_updatefirmware = (Button) findViewById(R.id.btn_update_firmware);
		button_updatefirmware.setOnClickListener(this);
	}

	private void setupSlideMenu() {
		//
		slidemenu = (SlidingMenu) findViewById(R.id.slideMenu);
		slidemenu.init(this, R.menu.slide, this, 333, true); // left animation
		slidemenu_right = (SlidingMenu) findViewById(R.id.slideMenu_right);
		slidemenu_right.init(this, R.menu.right_slide, this, 333, false); // right
																			// animation
		ImageView leftSlide = (ImageView) findViewById(R.id.imageView_menu1);
		ImageView rightSlide = (ImageView) findViewById(R.id.imageView_menu2);
		leftSlide.setOnClickListener(this);
		rightSlide.setOnClickListener(this);
	}

	/************************************************
	 * Callback/Listeners Methods
	 **/
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
		case R.id.btn_update_firmware:
			updateFirmware();
		default:
			break;
		}
	}

	@Override
	public void onSlideMenuItemClick(int itemId) {
		AppHelper.instance().onSwitchActivity(isLeftMenuSelected, this, itemId);
	}

	/************************************************
	 * Other Methods
	 **/
	private void updateFirmware() {
		// TODO Implement checking of latest firmware
		if (AppHelper.instance().WATCH_STATE != WATCH_STATE_VALUE.CONNECTED) {
			onShowErrorDialog(ERROR_DISCONNECTED);
			return;
		}
		try {
			JSONObject params = new JSONObject();
			params.put("email", getPrefs().getString(KreyosPrefKeys.USER_EMAIL, ""));
			
			params.put("auth_token", getPrefs().getString(KreyosPrefKeys.USER_KREYOS_TOKEN, ""));
			
			// String response = RequestManager.instance().post(KreyosPrefKeys.URL_FIRMWARE, params);
			String response = RequestManager.instance().get(KreyosPrefKeys.URL_FIRMWARE);
			
			
			// Log.d("Response", "" + response);
			if (response == "ERROR") {
				onShowErrorDialog(ERROR_FILE_NOT_FOUND);
				return;
			}
			
			JSONObject jsonResponse = new JSONObject(response);
			if (!jsonResponse.has("success")) {
				onShowErrorDialog(ERROR_FILE_NOT_FOUND);
				return;
			}
			
			String versionNo = jsonResponse.getString("version_number");
			Log.d("Firmware Version", "Web:" + versionNo);
			Log.d("Firmware Version","Prefs"+ getPrefs().getString(KreyosPrefKeys.FIRMWARE_VERSION, ""));
			
			if (versionNo.equals(getPrefs().getString(KreyosPrefKeys.FIRMWARE_VERSION, ""))) {
				
				onShowErrorDialog(ERROR_SAME_FIRMWARE);
				
			} else {
				
				defaultFileUrl = jsonResponse.getString("attachment");
				defaultFileUrl = "http:" + defaultFileUrl;
				startUpdating();
				
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void startUpdating() {
		// + ET 05082014 : Show progress dialog
		m_progressDialog = ProgressDialog.show(this, "Please wait", "Updating Firmware", true);
		mIsNeedToUnlock = true;
		AsyncTask<Void, Void, InputStream> t = new AsyncTask<Void, Void, InputStream>() {
			protected InputStream doInBackground(Void... p) {
				try {
					// URL aURL = new URL(urlText.getText().toString());
					URL aURL = new URL(defaultFileUrl);
					URLConnection conn = aURL.openConnection();
					conn.setUseCaches(true);
					conn.connect();
					InputStream is = conn.getInputStream();

					Protocol btp = new Protocol(
							BluetoothAgent.getInstance(null),
							UpdateFirmwareActivity.this);
					btp.sendStream("firmware", is);
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
					onErrorOccured();
				}
				return null;
			}

			protected void onPostExecute(InputStream is) {
			}
		};
		t.execute();
	}

	private void onErrorOccured() {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				onShowErrorDialog(ERROR_FILE_NOT_FOUND);
			}
		});
	}

	private void onShowErrorDialog(int p_index) {
		switch (p_index) {
		case 1:
			KreyosUtility.showErrorMessage(this, "Device not found", "Please connect your Kreyos Watch");
			break;
		case 2:
			try {
				m_progressDialog.dismiss();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			KreyosUtility.showErrorMessage(this, "Firmware Update", "File not found");
			break;
		case 3:
			KreyosUtility.showErrorMessage(this, "Firmware Update", "Firmware already updated");
			break;
		}
	}

	@Override
	protected void onBluetoothHeadsetConnected() {
		// TODO Auto-generated method stub
		super.onBluetoothHeadsetConnected();

		try {
			if (m_progressDialog != null) {
				m_progressDialog.dismiss();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (!mIsNeedToUnlock) {
			return;
		}

		Log.d("UpdateFirmware", "Update Firmware Complete");
		BluetoothAgent agent = BluetoothAgent.getInstance(btMsgHandler);
		Protocol p = new Protocol(agent, UpdateFirmwareActivity.this);
		p.unlockWatch();

		mIsNeedToUnlock = false;
	}

}
