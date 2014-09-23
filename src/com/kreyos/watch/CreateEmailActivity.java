package com.kreyos.watch;

import java.util.ArrayList;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.kreyos.watch.R;
import com.kreyos.watch.utils.RequestManager;
import com.kreyos.watch.utils.Utils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class CreateEmailActivity extends KreyosActivity {

	private EditText email;
	private EditText password;
	private EditText clonePassword;
	private Button btn_next;
	private ImageView btn_backToLogin;
	private TextView m_createAccountIcon = null;
	private TextView m_createAccountMessge = null;
	
	public static String LOG_TAG = "CreateEmailActivity";
	
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate(savedInstanceState);
		requestWindowFeature( Window.FEATURE_NO_TITLE );
		setContentView(R.layout.create_email_2);
		
		// + ET 040714 : Override all fonts 
		KreyosUtility.overrideFonts(this, ((ViewGroup)findViewById(android.R.id.content)).getChildAt(0), KreyosUtility.FONT_NAME.LEAGUE_GOTHIC_REGULAR);
		setupLayoutAndCallbacks();
	} 
	
	@Override
	public void onBackPressed() {
		// + ET 040714 : Removed back button
	}
	
	private void setupLayoutAndCallbacks() {
		
		email = (EditText)findViewById(R.id.txt_create_email);
		password = (EditText)findViewById(R.id.txt_create_pasword);
		clonePassword = (EditText)findViewById(R.id.txt_create_pasword_confirm);
		btn_next = (Button)findViewById(R.id.btn_create_next);
		m_createAccountIcon = (TextView) findViewById(R.id.txt_create_account_icon);
		m_createAccountMessge = (TextView) findViewById(R.id.txt_create_account_message);
		
		btn_next.setOnClickListener( new View.OnClickListener() 
		{
			
			@Override
			public void onClick(View v) 
			{
				// TODO Auto-generated method stub
				registerEmail(v);
			}
		});
		
		m_createAccountIcon.setOnClickListener( new View.OnClickListener() 
		{
			
			@Override
			public void onClick(View v) 
			{
				// TODO Auto-generated method stub
				backToLogin(v);
			}
		});
		
		m_createAccountMessge.setOnClickListener( new View.OnClickListener() 
		{
			
			@Override
			public void onClick(View v) 
			{
				// TODO Auto-generated method stub
				backToLogin(v);
			}
		});
	}
	
	private boolean validEmail( String p_email )
	{
		    Pattern pattern = Patterns.EMAIL_ADDRESS;
		    return pattern.matcher(p_email).matches();
	}
	
	private boolean isValid( String p_emailValue, String p_passwordValue, String p_clonePasswordValue )
	{

		if( p_emailValue.length() < 1
		||  p_passwordValue.length() < 1
		||  p_clonePasswordValue.length() < 1 ) {
			KreyosUtility.showErrorMessage(this, "ERROR:", "There's an empty field");
			return false;
		}
		
		if (!validEmail(p_emailValue)) {
			KreyosUtility.showErrorMessage(this, "ERROR:", "Email entered is not valid");
			return false;
		}
		
		if (!p_passwordValue.equals(p_clonePasswordValue)) {
			KreyosUtility.showErrorMessage(this, "ERROR:", "Password didn't matched");
			return false;
		}
		
		if (p_passwordValue.length() < 8) {
			KreyosUtility.showErrorMessage(this, "ERROR:", "Password is too short (minimum is 8 characters)");
			return false;
		}
		
		return true;
	}
	
	private void registerEmail( View p_view ) 
	{

		if( !Utils.hasConnection( this ) )
		{
			KreyosUtility.showErrorMessage(this, "ERROR:", "No Internet connection");
			return;
		}
		
		
		p_view.setEnabled( false );
		
		String emailValue 			= email.getText().toString();
		String passwordValue 		= password.getText().toString();
		String clonePasswordValue 	= clonePassword.getText().toString();
		
		if(!isValid(emailValue, passwordValue, clonePasswordValue))
		{
			p_view.setEnabled( true );
			return;
		}
		
		/* Old implementation
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("email", emailValue));
		*/
		
		
		
		
		try 
		{
			JSONObject params = new JSONObject();
			params.put("email", emailValue);
			String response = RequestManager.instance().post(KreyosPrefKeys.URL_CHECK_MAIL, params);
			Log.d(LOG_TAG, "Response: " + response);
			
			JSONObject jsonFile = new JSONObject(response);
			if(jsonFile.has("success"))
			{
				Log.d(LOG_TAG, "GUD!");
				KreyosActivity.initPrefs(this);
				SharedPreferences.Editor editor = KreyosActivity.getPrefs().edit();
				editor.putString(KreyosPrefKeys.USER_EMAIL, emailValue);
				editor.putString(KreyosPrefKeys.USER_EMAIL_PASSWORD, passwordValue);
				editor.putString(KreyosPrefKeys.USER_EMAIL_PASSWORD_CONFIRM, clonePasswordValue);
				editor.commit();
				
				// + ET 040814 : Move to creation of account
				Intent i = new Intent(CreateEmailActivity.this, CreateAccountActivity.class);
				startActivity(i);
				finish();
			}
			else
			{
				if(jsonFile.has("error"))
				{
					JSONObject jsonObject = new JSONObject(jsonFile.getString("error"));
					KreyosUtility.showErrorMessage(this, "ERROR:", "" + jsonObject.getString("message"));
				}
			}
			
		}
		catch (JSONException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		p_view.setEnabled( true );
		
		
//		// + ET 040814 : Error Handling first
//		boolean haveErrors = false;
//	
//		p_view.setEnabled( false );
//		
//		String emailValue = email.getText().toString();
//		String passwordValue = password.getText().toString();
//		String clonePasswordValue = clonePassword.getText().toString();
//		
//		
//		if( emailValue.length() < 1
//		||  passwordValue.length() < 8
//		||  clonePasswordValue.length() < 8 )
//		{
//			haveErrors = true;
//		}
//		
//		if(!validEmail(emailValue))
//		{
//			haveErrors = true;
//		}
//	
//		if( !passwordValue.equals(clonePasswordValue) )
//		{
//			haveErrors = true;
//		}
//		
//		if( haveErrors)
//		{
//			KreyosUtility.showErrorMessage(this, "ERROR", "TEMP: ERROR MESSAGE");
//			p_view.setEnabled( true );
//		}
//		else 
//		{
//			
//			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
//			params.add(new BasicNameValuePair("email", emailValue));
//			String response = RequestManager.instance().post(KreyosPrefKeys.URL_CHECK_MAIL, params);
//			
//			Log.d("Response", "Response: " + response);
//			
//			try 
//			{
//				JSONObject jsonFile = new JSONObject(response);
//				if(jsonFile.has("success"))
//				{
//					Log.d("Response", "GUD!");
//					
//					KreyosActivity.initPrefs(this);
//					SharedPreferences.Editor editor = KreyosActivity.getPrefs().edit();
//					editor.putString(KreyosPrefKeys.USER_EMAIL, emailValue);
//					editor.putString(KreyosPrefKeys.USER_EMAIL_PASSWORD, passwordValue);
//					editor.putString(KreyosPrefKeys.USER_EMAIL_PASSWORD_CONFIRM, clonePasswordValue);
//					editor.commit();
//					
//					// + ET 040814 : Move to creation of account
//					Intent i = new Intent(CreateEmailActivity.this, CreateAccountActivity.class);
//					startActivity(i);
//					finish();
//				}
//				else
//				{
//					KreyosUtility.showErrorMessage(this, "ERROR", "Email already taken!");
//				}
//				
//			}
//			catch (JSONException e) 
//			{
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		p_view.setEnabled( true );
		
	}
	
	private void backToLogin( View p_view ) 
	{
		p_view.setEnabled(false);
		// + ET 040814 : Move back to login
		Intent i = new Intent(this, LoginActivity.class);
		startActivity(i);
		finish();
	}
	
	
	
	
}
