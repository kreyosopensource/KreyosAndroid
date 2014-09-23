/* System Flow
 * 
 *
 *
 *
 * 
 * 
 * 
 */


package com.kreyos.watch;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ParseException;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
//import android.widget.Toast;
import android.widget.NumberPicker.OnValueChangeListener;

import com.coboltforge.slidemenu.SlideMenuInterface.OnSlideMenuItemClickListener;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.model.GraphObject;
import com.google.android.gms.internal.bm;
import com.google.android.gms.internal.bn;
import com.kreyos.watch.R;
import com.kreyos.watch.bluetooth.BluetoothAgent;
import com.kreyos.watch.bluetooth.Protocol;
import com.kreyos.watch.dataobjects.Profile;
import com.kreyos.watch.managers.AppHelper;
import com.kreyos.watch.utils.KreyosConstants;
import com.kreyos.watch.utils.RequestManager;
import com.kreyos.watch.managers.AppHelper.WATCH_STATE_VALUE;
import com.kreyos.watch.utils.Utils;

public class PersonalInformationActivity extends KreyosActivity
implements 
	OnSlideMenuItemClickListener,
	OnClickListener,
	DatePickerDialog.OnDateSetListener
{
	
	private BTDataHandler btMsgHandler = null;

	public PersonalInformationActivity() {
		btMsgHandler = new BTDataHandler(this);
		if( AppHelper.instance().WATCH_STATE == WATCH_STATE_VALUE.CONNECTED ) {
 			BluetoothAgent.getInstance(btMsgHandler);
 		}
	}
	
	private static class BTDataHandler extends Handler {
		private final WeakReference<PersonalInformationActivity> mService;

		public BTDataHandler(PersonalInformationActivity service) {
			mService = new WeakReference<PersonalInformationActivity>(service);
		}

		@Override
		public void handleMessage(Message msg) {
			PersonalInformationActivity service = mService.get();
			if (service != null) {
				service.handleBTData(msg);
			}
		}
	}
	
	private void handleBTData(Message msg) {

		switch (msg.what) {
			// + ET 04242014 : Move to Sports Mode	
			case Protocol.MessageID.MSG_ACTIVITY_PREPARE: {
				Intent i3 = new Intent(PersonalInformationActivity.this, SportsActivity.class);
				startActivity(i3);
				finish();
			}
			break;
			
			
			case Protocol.MessageID.MSG_BLUETOOTH_STATUS: {
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
	
	
	// + ET 040714 : Slide menu variables
	private SlidingMenu slidemenu;
	private SlidingMenu slidemenu_right;
	private boolean isLeftMenuSelected = false;

    private int mYear;
    private int mMonth;
    private int mDay;
	
    ImageView mAvatarImage = null;
    
	EditText firstname;
    EditText lastname;
    
    TextView birthday;
    TextView heightFt;
    TextView heightIn;
    TextView weight;
    TextView genderMale;
    TextView genderFemale;
    
    Button btn_updateInfo;
    Button btn_importFromFb;
    
    byte gender = 0;
    
    // + ET 040714 : Sliders
    NumberPicker weightSlider;
    NumberPicker heightSlider;
    boolean isHeightOnFt = false;
    
    float m_heightInCentValue = 0;
    float m_weightInLbsValue = 0;

    // Picker properties
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
    
    int m_feetFixFeet = 0;
    int m_feetFixInch = 0;
    int m_cmFixInch = 0;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature( Window.FEATURE_NO_TITLE );
		setContentView(R.layout.more_settings_user_profile_activity);
		setup();
	} 
	
    
    /****************************************************************
	 * Setups 
	 ****************************************************************/
    /****************************************************************
	 * Overall setup 
	 **/
    private void setup() {
    	// Initialize Preference
		KreyosActivity.initPrefs(this);
		
		// Override all fonts 
		KreyosUtility.overrideFonts(this, 
				((ViewGroup)findViewById(android.R.id.content)).getChildAt(0), 
				KreyosUtility.FONT_NAME.LEAGUE_GOTHIC_REGULAR);
		
		// Check/Establish connection on watch
		onCallonCreate();
		setupSlideMenu();
		setupLayoutAndCallbacks();
		
		// Set variables on today's date
		Calendar ca = Utils.calendar();
		mYear 		= ca.get(Calendar.YEAR);
		mMonth 		= ca.get(Calendar.MONTH);
		mDay		= ca.get(Calendar.DAY_OF_MONTH);
		
		// Load save profile on user preference
		onLoadProfile();
		
		// Load profile image
		if(mAvatarImage == null) {
			return;
		}
		loadProfileImage(mAvatarImage);
		
    }
    
    /****************************************************************
	 * Setup left and right slide menu
	 **/
	private void setupSlideMenu() {
	    slidemenu = (SlidingMenu) findViewById(R.id.slideMenu);
		slidemenu.init(this, R.menu.slide, this, 333, true); // left animation
		slidemenu_right = (SlidingMenu) findViewById(R.id.slideMenu_right);
		slidemenu_right.init(this, R.menu.right_slide, this, 333, false); //right animation

		// connect the fallback button in case there is no ActionBar
		ImageView slideMenuLeft = (ImageView) findViewById(R.id.imageView_menu1);
		ImageView slideMenuRight = (ImageView) findViewById(R.id.imageView_menu2);
		slideMenuLeft.setOnClickListener(this);
		slideMenuRight.setOnClickListener(this);
	}
	
	/****************************************************************
	 * Setup the view and callbacks
	 **/
	private void setupLayoutAndCallbacks() {
		// Setup view variables and callbacks
		mAvatarImage		= (ImageView)findViewById(R.id.img_avatarImage);
		firstname 			= (EditText)findViewById(R.id.create_account_firstname);
		lastname 			= (EditText)findViewById(R.id.create_account_lastname);
		birthday 			= (TextView)findViewById(R.id.create_account_birthday_value);
		weight 				= (TextView)findViewById(R.id.create_account_weight_value);
		heightFt 			= (TextView)findViewById(R.id.create_account_height_value_ft);
		heightIn 			= (TextView)findViewById(R.id.create_account_height_value_in);
		genderMale 			= (TextView)findViewById(R.id.create_account_male);
		genderFemale 		= (TextView)findViewById(R.id.create_account_female);
		btn_updateInfo 		= (Button)findViewById(R.id.btn_update_info);
		btn_importFromFb	= (Button)findViewById(R.id.btn_import_from_fb);

		// Disable and hide view because web does not yet implement
		// firstname.setEnabled(false);
		// lastname.setEnabled(false);
		btn_importFromFb.setVisibility(View.GONE);
		
		// Set view callbacks
		birthday.setOnClickListener(this);
		genderMale.setOnClickListener(this);
		genderFemale.setOnClickListener(this);
		weight.setOnClickListener(this);
		heightFt.setOnClickListener(this);
		heightIn.setOnClickListener(this);
		btn_importFromFb.setOnClickListener(this);
		btn_updateInfo.setOnClickListener(this);
	}
    
	/****************************************************************
	 * Load profile from user preferences
	 **/
	private void onLoadProfile() {
    	Log.d("Profile","loading Profile");
    	Profile profile = new Profile();
    	profile.loadfromPrefs();
    	firstname.setText(profile.FIRSTNAME);
    	lastname.setText(profile.LASTNAME);
    	birthday.setText(profile.BIRTHDAY);
    	
    	String[] birthdayValue = profile.BIRTHDAY.split("/");
    	if (birthdayValue.length == 3) {
    		mMonth = Integer.parseInt(birthdayValue[1]);
    		if( mMonth < 0) { mMonth = 0; }
        	mDay = Integer.parseInt(birthdayValue[0]);
        	mYear = Integer.parseInt(birthdayValue[2]);
    	}
    	
    	String genderValue = profile.GENDER.toUpperCase();	
    	if(genderValue.equals("MALE")) {    		
    		gender = 0;
    		genderMale.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_left_rounded_border_selected));
    		genderFemale.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_right_rounded_border_white));
    	} else {
    		gender = 1;
    		genderMale.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_left_rounded_border_white));
    		genderFemale.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_right_rounded_border_selected));
    	}
    	
    	int heightValue =  0; 
    	int weightValue =  0;
    
    	try {
    		heightValue = Math.round(Float.parseFloat(profile.HEIGHT));
    		weightValue = Math.round(Float.parseFloat(profile.WEIGHT));
		} catch (Exception e) {
			// TODO: handle exception
    		heightValue =  0; 
    		weightValue =  0;
		}
    
    	m_heightInCentValue = heightValue;
    	m_weightInLbsValue = weightValue;

    	/*
        Log.d("Personal", "" + f.format(d));
    	final float inches = heightValue * 0.39370079f;//  cm to inches
    	int ft = (int)inches / 12;
    	int ftInInches = (int)inches / 12;
    	
    	String heightToString = String.format("%.2f", (float)(heightValue / 30.48f));
    	heightToString = heightToString.replace(".", "/");
    	String[] heightSplitted = heightToString.split("/");
    	*/

    	if (m_heightInCentValue == 0) {
    		return;
    	}
    	
    	if (m_weightInLbsValue == 0) {
    		return;
    	}
    	
    	DecimalFormat f = new DecimalFormat("##.0");
        float cmToFt = heightValue / 30.48f;
        String heightToBeSplitted = "" + f.format(cmToFt);
        heightToBeSplitted = heightToBeSplitted.replace(".", "/");
        String[] splittedHeight = heightToBeSplitted.split("/");
    	int ft = Integer.parseInt(splittedHeight[0]);
    	int ftInInches = Math.round(Float.parseFloat(splittedHeight[1]));
    	
    	// Conversion of feet and inches
    	heightFt.setText("" + ft + " Ft.");
    	heightIn.setText("" + ftInInches + " In.");
    	weight.setText("" + weightValue + " Lbs.");
    	
    	// Set weight and height value
    	m_weightInLbsValue 	= weightValue;
    	
    }
	
    
    
    /**************************************************************** 
	 * Listeners 
	 ****************************************************************/

	@Override
	public void onSlideMenuItemClick(int itemId) {
		AppHelper.instance().onSwitchActivity(isLeftMenuSelected, this, itemId);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			
			case R.id.create_account_birthday_value: 
				//showDialog(DIALOG_ID_DATE);
				DatePickerDialog datePicker = new DatePickerDialog(this, this, mYear, mMonth-1, mDay);
				datePicker.show();
				break;	
			
			case R.id.create_account_weight_value: 
				showWeightSlider();
				break;
			
			case R.id.create_account_height_value_ft: 
				isHeightOnFt = true;
				showHeightSlider();
				break;
			
			case R.id.create_account_height_value_in: 
				isHeightOnFt = false;
				showHeightSlider();
				break;
			
			case R.id.create_account_male: 
				gender = 0;
				genderFemale.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_right_rounded_border_white));
				v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_left_rounded_border_selected));
				break;
			
			case R.id.create_account_female: 
				gender = 1;
				genderMale.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_left_rounded_border_white));
				v.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_right_rounded_border_selected));
				break;
			
			case R.id.btn_update_info: 
				onUpdateProfile();
				break;
			
			case R.id.btn_import_from_fb: 
				onSyncFromFB();
				break;
			
			case R.id.imageView_menu1:
				isLeftMenuSelected = true;
				slidemenu.show();
				break;
				
			case R.id.imageView_menu2:
				isLeftMenuSelected = false;
				slidemenu_right.show();
				break;
		}
	}
	
	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		// TODO Auto-generated method stub
		mYear = year;
        mMonth = monthOfYear + 1;
        mDay = dayOfMonth; 
        birthday.setText("" + mDay + "/" + mMonth + "/" + mYear);
	}
	

	/****************************************************************
	 * Dialogs 
	 ****************************************************************/
	/****************************************************************
	 * Weight Slider Dialogs 
	 **/
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
    
    /****************************************************************
	 * Height Slider Dialogs 
	 **/
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
            	if( m_btnFeet.isEnabled() == false ) {
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
            		
            		m_cmFixInch = (int)(newVal * 0.39379f);
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
				// m_npFeet.setValue( totalInches / 12 );
				
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
    
    
    /****************************************************************
	 * Main Functions
	 ****************************************************************/
    /****************************************************************
   	 * Sync facebook profile information
   	 **/
    private void onSyncFromFB() {
    	if( Session.getActiveSession() != null ) {
    		if( Session.getActiveSession().isOpened() ) {
				new Request(
			    Session.getActiveSession(),
			    "/me",
			    null,
			    HttpMethod.GET,
			    new Request.Callback() {
			        public void onCompleted(Response response) {
			        	//Log.d("FB", "Request:" + response);
			            /* handle the result */
			        	GraphObject graphObject = response.getGraphObject();
			        	if( graphObject!= null) {
			        		JSONObject jsonObject = graphObject.getInnerJSONObject();
			        		Log.d("FB", "Request:" + jsonObject);
			        	}
			        }
			    }).executeAsync();
    		}
    	} else {
    		KreyosUtility.showErrorMessage(this, "ERROR", "Please sync your facebook first");
    	}
    }
    
    /****************************************************************
	 * Update profile on web and user preferences
	 **/
    private void onUpdateProfile() {
    	// + ET 040814 : First handles error then create account
		boolean haveErrors = false;
		
		String firstnameValue = firstname.getText().toString();
		String lastnameValue  = lastname.getText().toString();
		String birthdayValue  = birthday.getText().toString().replace("'\'", "");
		String weightValue 	  = "" + m_weightInLbsValue;
		String heightFtValue  = heightFt.getText().toString();
		String heightInValue  = heightIn.getText().toString();
		
		String heightValue = "" + m_heightInCentValue;
				
		// + ET 040814 : Check if one of the value is null
		if( firstnameValue.length() < 1 
		||  lastnameValue.length() < 1
		||  birthdayValue.length() < 1
		||  weightValue.length() < 1
		||  heightValue.length() < 1) {
			haveErrors = true;	
		}
		
		if( haveErrors ) {
			KreyosUtility.showErrorMessage(this, "ERROR", "TEMP: There some are empty fields!");
		} else {
			
			/*
			// Log values 
			Log.d("Profile", "Height: " + heightValue);
			Log.d("Profile", "Weight: " + m_weightInLbsValue);
			Log.d("Profile", "Birthday: " + birthdayValue);
			*/
			
			// Check connection first
			/*
			if( !Utils.hasConnection( this ) ) {
				KreyosUtility.showErrorMessage(this, "ERROR:", "No Internet connection");
				return;
			}
			*/
			
			// Update flow
			// � Update on web
			// � Update on local
			
			// Get an instance to get the email and auth token
			Profile profile = new Profile();
			profile.loadfromPrefs();
			
			String genderValue = "";
			if ( gender == 0 ) {
				genderValue = "Male";
			} else {
				genderValue = "Female";
			}
			
			KreyosUtility.showPopUpDialog(this, "Update Watch", "Your profile information was updated");
			
			// Success on web next update on local
			profile 			= new Profile();
			profile.FIRSTNAME 	= firstnameValue;
			profile.LASTNAME 	= lastnameValue;
			profile.BIRTHDAY 	= birthdayValue;
			profile.GENDER		= genderValue;
			profile.WEIGHT 		= "" + m_weightInLbsValue;
			profile.HEIGHT 		= heightValue;
			profile.saveToPrefs();
			
			/*
			// Update profile on web
			try {
				
				
				JSONObject params = new JSONObject();
				
				params.put("first_name", 	firstnameValue);
				params.put("last_name", 	lastnameValue);
				params.put("birthday", 		birthdayValue);
				params.put("gender", 		genderValue);
				params.put("weight", 		"" + m_weightInLbsValue);
				params.put("height", 		heightValue); 
				
				JSONObject user = new JSONObject();
				user.put("user", params);
				user.put("email", 		profile.EMAIL);
				user.put("auth_token", 	profile.KREYOS_TOKEN);
				
				String response =  RequestManager.instance().put(KreyosPrefKeys.URL_USER_UPDATE, user);
				Log.d("Profile", "Response: " + response);
        
				// Update on web
				boolean isResponseValid = true;
				JSONObject jsonFile = new JSONObject(response);
				if (jsonFile.has("success")) {
					if(!jsonFile.getBoolean("success")) {
						isResponseValid = false;
					}
				}
				if (isResponseValid) {
					KreyosUtility.showPopUpDialog(this, "Update Watch", "Your profile information was updated");
					// Success on web next update on local
					profile 			= new Profile();
					profile.FIRSTNAME 	= firstnameValue;
					profile.LASTNAME 	= lastnameValue;
					profile.BIRTHDAY 	= birthdayValue;
					profile.GENDER		= genderValue;
					profile.WEIGHT 		= "" + m_weightInLbsValue;
					profile.HEIGHT 		= heightValue;
					profile.saveToPrefs();
					
					Log.d("Profile","loading Profile" + birthdayValue);
				}
				
			} catch(Exception ex) {
				ex.printStackTrace();
				KreyosUtility.showErrorMessage(this, "ERROR", "There's an error updating your account!");
			}	
			*/
		}
    }


	
}
