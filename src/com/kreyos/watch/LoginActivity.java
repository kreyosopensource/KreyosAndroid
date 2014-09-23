package com.kreyos.watch;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.kreyos.watch.R;
import com.kreyos.watch.dataobjects.Profile;
import com.kreyos.watch.db.DBManager;
import com.kreyos.watch.db.DatabaseManager;
import com.kreyos.watch.listeners.IQueryEvent;
import com.kreyos.watch.utils.MasterTextListController;
import com.kreyos.watch.utils.RequestManager;
import com.kreyos.watch.utils.Utils;

@SuppressWarnings("deprecation")
public class LoginActivity extends KreyosActivity
implements
	View.OnClickListener,
	Session.StatusCallback,
	Request.GraphUserCallback,
	IQueryEvent
{
	private LoginButton m_btnfbLogin		= null;
	private Button mBtnlogin				= null;
	private EditText mLoginEmail			= null;
	private EditText mLoginPassword 		= null;
	private TextView mCreateAccountIcon 	= null;
	private TextView mCreateAccountMessge 	= null;
	private int FB_PERMISSION_STATE 		= 0;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature( Window.FEATURE_NO_TITLE );
		setContentView(R.layout.login1);
		
		KreyosActivity.initPrefs(this);
		declarations();
		setFont();
		
		
		// checkSession();
		moveToHome();
	} 
	

	private void declarations() {
		mLoginEmail 			= (EditText)findViewById(R.id.txt_login_email);
		mLoginPassword 			= (EditText)findViewById(R.id.txt_login_pasword);
		mCreateAccountIcon 		= (TextView) findViewById(R.id.txt_create_account_icon);
		mCreateAccountMessge 	= (TextView) findViewById(R.id.txt_create_account_message);
		mBtnlogin	 			= (Button) findViewById(R.id.btn_login);
		m_btnfbLogin 			= (LoginButton) findViewById(R.id.btn_fb_login);
	
		mBtnlogin.setOnClickListener(this);
		mCreateAccountIcon.setOnClickListener(this);
		mCreateAccountIcon.setOnClickListener(this);
	
		m_btnfbLogin.setReadPermissions(fbPermissions);
		m_btnfbLogin.setSessionStatusCallback(this);
	}
	
	private void setFont() {
		Typeface typeFace = Typeface.createFromAsset(getApplicationContext().getAssets(),
				"fonts/leaguegothic-regular-webfont.ttf");
		
		mLoginEmail.setTypeface(typeFace);
		mLoginPassword.setTypeface(typeFace);
		mCreateAccountIcon.setTypeface(typeFace);
		mCreateAccountMessge.setTypeface(typeFace);
		mBtnlogin.setTypeface(typeFace);
		((TextView)findViewById(R.id.txt_login_or)).setTypeface(typeFace);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()) {
		case R.id.txt_create_account_icon:
		case R.id.txt_create_account_message:
			Intent i = new Intent(LoginActivity.this, CreateEmailActivity.class);
			startActivity(i);
			finish();
			break;
		case R.id.btn_login:
			login();
			
			/****************************
			 * @debug
			 * - Load custom screen on Login
			 ****************************/
			/*
			Class c = PersonalInformationActivity.class;
			Intent a = new Intent(LoginActivity.this, c);
			startActivity(a);
			finish();
			//*/
			
			break;
		default:
			break;
		}
	}
	

	private void updatePermission() {
		Session.NewPermissionsRequest request = new Session.NewPermissionsRequest(LoginActivity.this, 
				"manage_notifications");
		Session.getActiveSession().requestNewPublishPermissions(request);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		 super.onActivityResult(requestCode, resultCode, data);
		 Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}
	
	@Override
	public void call(Session session, SessionState state, Exception exception) {
		// TODO Auto-generated method stub
		if (session.isOpened()) {
			showLoadingProgress(true);
			Request.executeMeRequestAsync(session, this);
		}
	}
	
	@Override
	public void onCompleted(GraphUser user, Response response) {
		// TODO Auto-generated method stub
		if (user != null) { 
			if (FB_PERMISSION_STATE == 0) {
				//Log.i("FB","User ID "+ user.getId());
				//Log.i("FB","Email "+ user.asMap().get("email"));
				//Log.d("FB", "Response:" + response);
				// + ET 04302014 : TEMP Saving of fb profile ID
				SharedPreferences.Editor editor = KreyosActivity.getPrefs().edit();
				editor.putString(KreyosPrefKeys.USER_FB_ID, user.getId());
				editor.commit();
				
				saveProfileFromFB(user, response);
				saveFbUserPic(user.getId());
				FB_PERMISSION_STATE = 2;
			}
		} else {
			//There's an error
			showLoadingProgress(false);
		}
	}
	
	private void login () {
		
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mLoginPassword.getWindowToken(), 0);
		
		if (!Utils.hasConnection(this)) {
			KreyosUtility.showErrorMessage(this, "Connection Problem:", 
					"Whoops! It seems you aren't connected to the Internet yet.");
			return;
		}
		
		// + ET 05072014 : Remove for debug test
		/* Commented for instant access
		if( mLoginEmail.getText().toString().length() < 1 
		||  mLoginPassword.getText().toString().length() < 1) {
			KreyosUtility.showErrorMessage(this, "User Authentication Error", 
					"Whoa, there! You haven't logged in your username and/or password yet.");
			return;
		}
		//*/
		
		showLoadingProgress(true);
		
		/* Old implementation
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		//*  Commented for instant access
		params.add(new BasicNameValuePair("email", 		m_loginEmail.getText().toString()));
		params.add(new BasicNameValuePair("password", 	m_loginPassword.getText().toString()));
		//*/
		/*/ Commented for intant access
		params.add(new BasicNameValuePair("email", "dev1@kreyos.com"));
		params.add(new BasicNameValuePair("password", "password"));
		//*/

		try {
			JSONObject params = new JSONObject();
			//*/
			params.put("email", mLoginEmail.getText().toString());
			params.put("password", mLoginPassword.getText().toString());

			String response = RequestManager.instance().post(KreyosPrefKeys.URL_LOGIN_CHECK, params);
			
			JSONObject jsonObject = new JSONObject(response);
			if (jsonObject.getInt("status") == 200) {
			
				Log.d("Response", "Response: " + jsonObject.toString() );
				saveProfileFromKreyos(jsonObject);
				Profile profile = new Profile();
				
				if (profile.save(jsonObject)) {
					moveToHome();
				}
				
			} else {
				
				showLoadingProgress(false);
				KreyosUtility.showErrorMessage(this, "ERROR", "" + jsonObject.getString("message"));
			
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			showLoadingProgress(false);
		}
	}

	private void saveProfileFromKreyos(JSONObject p_jsonObject)
	{
		try {
			JSONObject user 		= p_jsonObject.getJSONObject("user");
			Profile profile 		= new Profile();
			profile.EMAIL			= user.getString("email");
			profile.KREYOS_TOKEN 	= user.getString("auth_token");
			profile.FIRSTNAME 		= user.getString("first_name");
			profile.LASTNAME 		= user.getString("last_name");
			profile.BIRTHDAY 		= user.getString("birthday");
			profile.GENDER 			= user.getString("gender");
			
			if (!user.getString("dimensions").equalsIgnoreCase("null")) {
				JSONObject dimension 	= user.getJSONObject("dimensions");
				profile.HEIGHT			= dimension.getString("height");
				profile.WEIGHT			= dimension.getString("weight");
			}
			
			profile.saveToPrefs();

			SharedPreferences.Editor editor = getPrefs().edit();
	        editor.putString(KreyosPrefKeys.USER_IMAGE, "");
	        editor.commit();
	        
			// moveToHome();
		} catch( Exception ex ) {
			showLoadingProgress(false);
			Log.d("Profile", "Profile Error:" + ex);
		}
	}

	private void saveProfileFromFB(GraphUser p_user, Response p_response)
	{
		Profile profile 	= new Profile();
		if (p_user.asMap().get("email") == null
		|| (String) p_user.asMap().get("email") == "") {
			GraphObject fbObject = p_response.getGraphObject();
			profile.EMAIL		= (String) fbObject.asMap().get("username");
			profile.EMAIL += "@facebook.com";
		} else {
			profile.EMAIL		= (String) p_user.asMap().get("email");
		}
		
		profile.FB_TOKEN 	= Session.getActiveSession().getAccessToken();
		profile.FIRSTNAME 	= p_user.getFirstName();
		profile.LASTNAME 	= p_user.getLastName();
		profile.BIRTHDAY 	= p_user.getBirthday();
		profile.GENDER 		= (String) p_user.asMap().get("gender");
		profile.saveToPrefs();
		Log.d("Profile", "Save Profile");
	}
	
	private void checkSession()
	{
		///*
		Profile profile = new Profile();
		profile.loadfromPrefs();
		
		Log.d("Session", "email: " + profile.EMAIL);
		
		if ( profile.EMAIL == "" ) {
			/* Old implementation
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("email", profile.EMAIL));
			params.add(new BasicNameValuePair("auth_token", profile.KREYOS_TOKEN)); */
			return;
		}
		
		try {
			JSONObject params = new JSONObject();
			params.put("email", profile.EMAIL);
			params.put("auth_token", profile.KREYOS_TOKEN);
			
			String response = RequestManager.instance().post(KreyosPrefKeys.URL_SESSION_KEY, params);
			Log.d("Response", "" + response);
			
			// + ET 04292014 : Check if your logged in
			JSONObject jsonObject = new JSONObject(response.toString());
			if( jsonObject.getInt("status") == 200 )
			{
				moveToHome();
				return;
			}
		} catch(Exception ex) {}
		//*/
		
		if (Session.getActiveSession() == null 
		||  !Session.getActiveSession().isOpened()) {
			Log.d("FB", "Logged In");
			return;
			//Logout - Session.getActiveSession().closeAndClearTokenInformation();
		}
		moveToHome();
		
	}

	private void moveToHome() {

		Class targetClass = HomeActivity.class;
		boolean isGonnaShowTutorial = false;
		
		File logFile = new File(this.getExternalFilesDir(null)+ "/" + "Tutorial_Check" + ".txt");
		if(logFile.exists()) {
			SharedPreferences prefs = getPrefs();
			boolean isNewCreated = prefs.getBoolean(KreyosPrefKeys.USER_TUTORIAL_CHECK, false);
			if (isNewCreated) {
				SharedPreferences.Editor editor = getPrefs().edit();
				editor.putBoolean(KreyosPrefKeys.USER_TUTORIAL_CHECK, false);
				editor.commit();
				isGonnaShowTutorial = true;
			}
		} else {
			try {
				logFile.createNewFile();
				isGonnaShowTutorial = true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (isGonnaShowTutorial) {
			targetClass = TutorialActivity.class;
		}
		
		// Log.d("Test Log", "" + isShowTutorial);
		// Initialize db here
		DatabaseManager.instance( (Context)this ).initLocalDB();
		DBManager dbManager = new DBManager((Context)this, this);
		dbManager.init();
		
		try {
			finish();
			Intent i = new Intent(LoginActivity.this, targetClass);
			startActivity(i);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
		
	}

	private void saveFbUserPic(String userID) 
	{
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		URL imageURL = null;
		try {
			imageURL = new URL("https://graph.facebook.com/" + userID + "/picture?type=large");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			Log.e("FB IMAGE", "" + e);
		}
		
	    Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			Log.e("FB IMAGE", "" + e);
		}
		
		byte[] byteArray 	= KreyosUtility.convertBitmapToByteArray(bitmap);	
        SharedPreferences.Editor editor = getPrefs().edit();
        editor.putString(KreyosPrefKeys.USER_FB_IMAGE, KreyosUtility.convertByteArrayToString(byteArray));
        editor.commit();

        // + ET 05082014 : Saving fb token in api is broken
        saveFBLoginOnWeb();
        // moveToHome();
        
	}
	
	private void saveFBLoginOnWeb() {
		/* Old implementation
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("email", 	getPrefs().getString(KreyosPrefKeys.USER_EMAIL, "")));
		params.add(new BasicNameValuePair("uid", 	getPrefs().getString(KreyosPrefKeys.USER_FB_ID, "")));
		params.add(new BasicNameValuePair("auth",  	getPrefs().getString(KreyosPrefKeys.USER_FB_TOKEN, "")));*/
		
		try {
			
			JSONObject params = new JSONObject();
			params.put("email", 		getPrefs().getString(KreyosPrefKeys.USER_EMAIL, ""));
			params.put("uid", 			getPrefs().getString(KreyosPrefKeys.USER_FB_ID, ""));
			params.put("auth_token",	getPrefs().getString(KreyosPrefKeys.USER_FB_TOKEN, ""));
			
			String response = RequestManager.instance().post(KreyosPrefKeys.URL_FACEBOOK_LOGIN, params);
			Log.d("Response", "" + response);
			
			JSONObject jsonObject = new JSONObject(response);
			if(jsonObject.getBoolean("success")) {
				
				Log.d("Response", "Response: " + jsonObject.toString() );
				moveToHome();
			} else {
				
				KreyosUtility.showErrorMessage(this, "ERROR", "" + jsonObject.getString("message"));
				showLoadingProgress(false);
				if(Session.getActiveSession() != null) {
					Session.getActiveSession().closeAndClearTokenInformation();
					Log.d("FB Session", "Logout Session");
				}
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d("SAVING FB ON WEB", "" + e);
		}
	}

	private void showLoadingProgress(boolean p_status) {
		
		if( p_status) {
			m_progressDialog = ProgressDialog.show(this, "Login",	"Please wait", true);
			m_progressDialog.setCancelable(false);
		} else {
			
			if(m_progressDialog != null) {
				m_progressDialog.dismiss();
			}
		}		
	}


	@Override
	public void onQueryStart(String p_queryKey) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onQueryComplete(String p_queryKey, Cursor p_query) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onQueryError(String p_queryKey, String p_error) {
		// TODO Auto-generated method stub
		
	}

}
