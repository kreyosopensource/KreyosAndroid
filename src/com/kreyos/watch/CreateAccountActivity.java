package com.kreyos.watch;

import java.util.ArrayList;
import java.util.Calendar;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.kreyos.watch.R;
import com.kreyos.watch.dataobjects.Profile;
import com.kreyos.watch.utils.KreyosConstants;
import com.kreyos.watch.utils.RequestManager;
import com.kreyos.watch.utils.Utils;

import android.R.drawable;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.SyncStateContract.Constants;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.TimePicker;

public class CreateAccountActivity extends KreyosActivity 
implements View.OnClickListener
{
	
	static final int DIALOG_ID_DATE = 1;
    static final int DIALOG_ID_HEIGHT = 0;
    private int mYear;
    private int mMonth;
    private int mDay;
    
    // + ET 040714 : Layout variables
    EditText firstname;
    EditText lastname;
    
    TextView birthday;
    TextView heightFt;
    TextView heightIn;
    TextView weight;
    TextView genderMale;
    TextView genderFemale;
    
    Button btn_createAccount;
    TextView m_createAccountIcon = null;
	TextView m_createAccountMessge = null;
    
    byte gender = 0;
    
    // + ET 040714 : Sliders
    boolean isHeightOnFt = false;

    float m_heightInCentValue = 0;
    float m_weightInLbsValue = 0;
    
    int m_fixHeightValue = 0;
    int m_feetFixFeet = 0;
    int m_feetFixInch = 0;
    int m_cmFixInch = 0;
    
    private AlertDialog m_alertHeight = null;
    private Button m_btnFeet = null;
    private Button m_btnCentimeters = null; // this can be centimeters or inches
    private NumberPicker m_npFeet = null;
    private NumberPicker m_npCentimeters = null; // this can be centimeters or inches
    private AlertDialog m_alertWeight = null;
    private Button m_btnLbs = null;
    private Button m_btnKg = null;
    private NumberPicker m_npLbs = null; 
    private NumberPicker m_npKg = null; 
    
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate(savedInstanceState);
		requestWindowFeature( Window.FEATURE_NO_TITLE );
		setContentView(R.layout.create_account2);
		
		// + ET 040714 : Override all fonts 
		KreyosUtility.overrideFonts(this, ((ViewGroup)findViewById(android.R.id.content)).getChildAt(0), KreyosUtility.FONT_NAME.LEAGUE_GOTHIC_REGULAR);
		setupLayoutAndCallbacks();
	} 
	
	private void setupLayoutAndCallbacks() 
	{
		
		firstname 				= (EditText)findViewById(R.id.create_account_firstname);
		lastname 				= (EditText)findViewById(R.id.create_account_lastname);
		birthday 				= (TextView)findViewById(R.id.create_account_birthday_value);
		weight 					= (TextView)findViewById(R.id.create_account_weight_value);
		heightFt 				= (TextView)findViewById(R.id.create_account_height_value_ft);
		heightIn 				= (TextView)findViewById(R.id.create_account_height_value_in);
		genderMale 				= (TextView)findViewById(R.id.create_account_male);
		genderFemale 			= (TextView)findViewById(R.id.create_account_female);
		btn_createAccount 		= (Button)findViewById(R.id.create_account_btn_create);
		m_createAccountIcon 	= (TextView) findViewById(R.id.txt_create_account_icon);
		m_createAccountMessge 	= (TextView) findViewById(R.id.txt_create_account_message);
		
		Calendar ca = Utils.calendar();
		mYear 		= ca.get(Calendar.YEAR);
		mMonth  	= ca.get(Calendar.MONDAY);
		mDay		= ca.get(Calendar.DAY_OF_MONTH);
		
		birthday.setOnClickListener(this);
		genderMale.setOnClickListener(this);
		genderFemale.setOnClickListener(this);
		weight.setOnClickListener(this);
		heightFt.setOnClickListener(this);
		heightIn.setOnClickListener(this);
		m_createAccountIcon.setOnClickListener(this);
		m_createAccountMessge.setOnClickListener(this);
		btn_createAccount.setOnClickListener(this);
		
		heightFt.setText("" + KreyosConstants.MIN_HEIGHT_FEET + "Ft.");
		heightIn.setText("0In.");
		weight.setText("" + KreyosConstants.MIN_WEIGHT_LBS + "Lbs.");
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		
			case R.id.create_account_firstname:
			case R.id.create_account_lastname :
			break;
			
			case R.id.create_account_birthday_value :
				showDialog(DIALOG_ID_DATE);
			break;	
			
			case R.id.create_account_weight_value : 
				showWeightSlider();
			break;
			
			case R.id.create_account_height_value_ft :
				isHeightOnFt = true;
				showHeightSlider();
			break;
			
			case R.id.create_account_height_value_in :
				isHeightOnFt = false;
				showHeightSlider();
			break;
			
			case R.id.create_account_male :
				gender = 0;
				genderFemale.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_right_rounded_border_white));
				v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_left_rounded_border_selected));
			break;
			
			case R.id.create_account_female :
				gender = 1;
				genderMale.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_left_rounded_border_white));
				v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_right_rounded_border_selected));
			break;
			
			case R.id.txt_create_account_icon: 
			case R.id.txt_create_account_message:
				backToLogin(v);
			break;
			
			case R.id.create_account_btn_create:
				createProfile();
			break;
		}
	}
	
	@Override
    protected Dialog onCreateDialog(int id) 
	{
		// TODO Auto-generated method stub
		switch (id) {
			case DIALOG_ID_DATE:
				return new DatePickerDialog(this, mDateSetListener, mYear, mMonth, mDay);	
		}
		return super.onCreateDialog(id);
    }
    
    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() 
    {
	    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
	    	mYear = year;
	        mMonth = monthOfYear+1;
	        mDay = dayOfMonth; 
	        birthday.setText("" + mDay + "/" + mMonth + "/" + mYear);
	    }
    };
    
    public void showHeightSlider() {
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	String title = "SET HEIGHT";
    	builder.setTitle(title);
    	LayoutInflater inflater = this.getLayoutInflater();
    	builder.setView(inflater.inflate(R.layout.picker, null));

        // + ET 040714 : Button Callbacks
    	builder.setPositiveButton("DONE", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	// In Centimeters
            	if( m_btnCentimeters.isEnabled() == false ) {
            		m_heightInCentValue = m_npCentimeters.getValue();
            		
            		heightFt.setText( "" + m_heightInCentValue + "Cm." );
            		heightIn.setText( "" );
            	}
            	// In Ft & Inches
            	else {
            		final int foot = m_npFeet.getValue() * 12; // foot in inches
            		final int inches = m_npCentimeters.getValue();
            		int totalInches = foot + inches;
            		m_heightInCentValue = (float)totalInches * 2.54f;
            		
            		heightFt.setText( "" + m_npFeet.getValue() + "Ft." );
            		heightIn.setText( "" + m_npCentimeters.getValue() + "In." );
            	}
            }
        });
    	
    	//<!-- Ft | Lbs = np_option1 -->
        //<!-- In| Cm | Kg = np_option2 -->
    	//<!-- Ft.In | Lbs = btn_option1 -->
        //<!-- In| Cm | Kg = btn_option2 -->
		// + ET 040714 : Create and show dialog
    	m_alertHeight = builder.create();
    	m_alertHeight.show();
    	
    	
    	m_btnFeet = (Button)m_alertHeight.findViewById( R.id.btn_option1 );
        m_btnCentimeters = (Button)m_alertHeight.findViewById( R.id.btn_option2 );
        m_btnCentimeters.setEnabled( false );
    	
        m_npFeet = (NumberPicker)m_alertHeight.findViewById(R.id.np_option1);
        m_npFeet.setMinValue( KreyosConstants.MIN_HEIGHT_FEET );
        m_npFeet.setMaxValue( KreyosConstants.MAX_HEIGHT_FEET );
        m_npFeet.setVisibility( View.GONE );
        m_npFeet.setOnValueChangedListener(new OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            	if ( m_btnFeet.isEnabled() == false ) {
            		final int foot = m_npFeet.getValue() * 12; // foot in inches
            		final int inches = m_npCentimeters.getValue();
            		int totalInches = foot + inches;
            		m_heightInCentValue = (float)totalInches * 2.54f;
            		
            		m_feetFixFeet = newVal;
            	}
            }
        });
		
        // @Note: m_npCentimeters can be CM or INCHES
		m_npCentimeters = (NumberPicker)m_alertHeight.findViewById( R.id.np_option2 );
		m_npCentimeters.setMinValue( KreyosConstants.MIN_HEIGHT_CENTIMETERS );
		m_npCentimeters.setMaxValue( KreyosConstants.MAX_HEIGHT_CENTIMETERS );
		m_npCentimeters.setVisibility( View.VISIBLE );
		m_npCentimeters.setValue( (int)m_heightInCentValue );
		m_npCentimeters.setOnValueChangedListener(new OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            	if( m_btnFeet.isEnabled() == false ) {
            		final int foot = m_npFeet.getValue() * 12; // foot in inches
            		final int inches = m_npCentimeters.getValue();
            		int totalInches = foot + inches;
            		m_heightInCentValue = (float)totalInches * 2.54f;
            		
            		m_feetFixInch = newVal;
            	}
            	else {
            		m_heightInCentValue = (float)newVal;
            		
            		// Convert to inches
            		m_cmFixInch = (int)(newVal * 0.39379f);
            		// Log.d("HeightFix", "" + inches);
            		
            	}
            }
        });
    	
		m_btnFeet.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				m_heightInCentValue = m_npCentimeters.getValue();
				int totalInches = (int)(m_heightInCentValue * 0.39370f);
				
				m_npFeet.setMinValue( KreyosConstants.MIN_HEIGHT_FEET );
				m_npFeet.setMaxValue( KreyosConstants.MAX_HEIGHT_FEET );
				m_npFeet.setVisibility( View.VISIBLE );
				//m_npFeet.setValue( totalInches / 12 );
				
				m_npCentimeters.setMinValue( KreyosConstants.MIN_HEIGHT_INCHES );
				m_npCentimeters.setMaxValue( KreyosConstants.MAX_HEIGHT_INCHES );
				// m_npCentimeters.setValue( totalInches % 12 );
				
				// Convert cm to inch
				m_npFeet.setValue((int)m_cmFixInch / 12);
				m_npCentimeters.setValue((int)m_cmFixInch % 12);
				
				m_btnFeet.setEnabled( false );
				m_btnCentimeters.setEnabled( true );
			}
		});
  	
		m_btnCentimeters.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				m_npFeet.setVisibility(View.GONE);
				
				final int foot = m_npFeet.getValue() * 12; // foot in inches
        		final int inches = m_npCentimeters.getValue();
        		int totalInches = foot + inches;
        		m_heightInCentValue = (float)totalInches * 2.54f;
				m_npCentimeters.setMinValue( KreyosConstants.MIN_HEIGHT_CENTIMETERS );
				m_npCentimeters.setMaxValue( KreyosConstants.MAX_HEIGHT_CENTIMETERS );
				
				// m_npCentimeters.setValue( (int)m_heightInCentValue );
				// Convert feet and inch to cm
				int feet = (int)(((m_feetFixFeet * 12) + m_feetFixInch) / 0.39370);
				m_npCentimeters.setValue(feet);
				
				m_btnFeet.setEnabled( true );
				m_btnCentimeters.setEnabled( false );
			}
		});
    }
    
    public void showWeightSlider() {
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	String title = "SET WEIGHT";
    	builder.setTitle(title);
    	LayoutInflater inflater = this.getLayoutInflater();
    	builder.setView(inflater.inflate(R.layout.picker, null));

        // + ET 040714 : Button Callbacks
    	builder.setPositiveButton("DONE", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	// In Kg
            	if( m_btnKg.isEnabled() == false ) {
            		weight.setText("" + m_npKg.getValue() + " Kg.");
            		//m_weightInKgValue = m_npKg.getValue();
            		m_weightInLbsValue = m_npKg.getValue() * 2.2046f;
            	}
            	// In Lbs
            	else {
            		weight.setText("" + m_npLbs.getValue() + " Lbs.");
            	    m_weightInLbsValue = m_npLbs.getValue();
            	}
            }
        });
    	
    	//<!-- Ft | Lbs = np_option1 -->
        //<!-- In| Cm | Kg = np_option2 -->
    	//<!-- Ft.In | Lbs = btn_option1 -->
        //<!-- In| Cm | Kg = btn_option2 -->
		// + ET 040714 : Create and show dialog
    	m_alertWeight = builder.create();
    	m_alertWeight.show();
    	
    	m_btnLbs = (Button)m_alertWeight.findViewById( R.id.btn_option1 );
    	m_btnKg = (Button)m_alertWeight.findViewById( R.id.btn_option2 );
    	m_btnLbs.setText( "lbs" );
    	m_btnLbs.setEnabled( false );
    	m_btnKg.setText( "kg" );
    	
    	m_npLbs = (NumberPicker)m_alertWeight.findViewById( R.id.np_option1 );
    	m_npLbs.setMinValue( KreyosConstants.MIN_WEIGHT_LBS );
    	m_npLbs.setMaxValue( KreyosConstants.MAX_WEIGHT_LBS  );
    	m_npLbs.setValue( (int)m_weightInLbsValue );
    	m_npLbs.setOnValueChangedListener(new OnValueChangeListener() {
    	    @Override
    	    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
    	    	m_weightInLbsValue = picker.getValue();
    	    }
    	});
		
        m_npKg = (NumberPicker)m_alertWeight.findViewById( R.id.np_option2 );
        m_npKg.setMinValue( KreyosConstants.MIN_WEIGHT_KG );
        m_npKg.setMaxValue( KreyosConstants.MAX_WEIGHT_KG );
        m_npKg.setVisibility( View.GONE );
        m_npKg.setOnValueChangedListener(new OnValueChangeListener() {
    	    @Override
    	    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
    	    	m_weightInLbsValue = (float)(newVal * 2.2046f);
    	    }
    	});
        
		m_btnLbs.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				m_weightInLbsValue = (m_npKg.getValue() * 2.2046f);
				m_npLbs.setVisibility( View.VISIBLE );
				m_npLbs.setValue( (int)m_weightInLbsValue );
				m_npKg.setVisibility( View.GONE );
				m_btnLbs.setEnabled(false);
				m_btnKg.setEnabled(true);
			}
		});
  	
		m_btnKg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				m_weightInLbsValue = m_npLbs.getValue();
				float kg = (m_weightInLbsValue / 2.2046f);
				m_npLbs.setVisibility( View.GONE );
				m_npKg.setVisibility( View.VISIBLE );
				m_npKg.setValue( (int)kg );
				m_btnLbs.setEnabled(true);
				m_btnKg.setEnabled(false);
			}
		});
    }
    
	private void createProfile () {
		if( !Utils.hasConnection( this ) ) {
			KreyosUtility.showErrorMessage(this, "ERROR:", "No Internet connection");
			return;
		}
	
		String firstnameValue = firstname.getText().toString();
		String lastnameValue = lastname.getText().toString();
		String birthdayValue = birthday.getText().toString();
		String weightValue = weight.getText().toString();
		String heightValue = "" + m_heightInCentValue;
	
		SharedPreferences prefs = KreyosActivity.getPrefs();
		String emailValue = prefs.getString(KreyosPrefKeys.USER_EMAIL, "");
		String emailPasswordValue = prefs.getString(KreyosPrefKeys.USER_EMAIL_PASSWORD, "");
		String emailClonePasswordValue = prefs.getString(KreyosPrefKeys.USER_EMAIL_PASSWORD_CONFIRM, "");
		
		// + ET 040814 : Check if one of the value is null
		if( firstnameValue.length() < 1 
		||  lastnameValue.length() < 1
		||  birthdayValue.length() < 1
		||  weightValue.length() < 1
		||  heightValue.length() < 1
		||  emailValue.length() < 1
		||  emailPasswordValue.length() < 1
		||  emailClonePasswordValue.length() < 1) {
			KreyosUtility.showErrorMessage(this, "ERROR", "TEMP: There some are empty fields!");
			return;
		}
		
		try {
			JSONObject params = new JSONObject();
			String genderValue = "";
			if ( gender == 0 ) {
				genderValue = "Male";
			} else {
				genderValue = "Female";
			}
			params.put("email", emailValue);
			params.put("password", emailPasswordValue);
			params.put("password_confirmation", emailClonePasswordValue); 
			params.put("first_name", firstnameValue);
			params.put("last_name", lastnameValue);
			params.put("birthday", birthdayValue);
			params.put("gender", genderValue);
			params.put("height", heightValue); 
			params.put("weight", ""+heightValue);
			
			String response =  RequestManager.instance().post(KreyosPrefKeys.URL_CREATE_ACCOUNT, params);
			Log.d("CreateAccountActivity", "" + response);
			
			JSONObject jsonFile = new JSONObject( response );
			if (jsonFile.has("error")) {
				KreyosUtility.showErrorMessage(this, "ERROR", "There's an error creating an account!");
				return;
			}
			
			Profile profile = new Profile();
			if (profile.save(jsonFile)) {
				SharedPreferences.Editor editor = getPrefs().edit();
				editor.putBoolean(KreyosPrefKeys.USER_TUTORIAL_CHECK, true);
				editor.commit();
				
				// + ET 040814 : Move to login
				Intent i = new Intent(CreateAccountActivity.this, LoginActivity.class);
				startActivity(i);
				finish();
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
		
	private void backToLogin( View p_view ) {
		p_view.setEnabled(false);
		// + ET 040814 : Move back to login
		Intent i = new Intent(this, LoginActivity.class);
		startActivity(i);
		finish();
	}

	
	
}
