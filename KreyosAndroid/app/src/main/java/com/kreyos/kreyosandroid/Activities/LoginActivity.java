package com.kreyos.kreyosandroid.activities;

/**
 * Created by emman on 7/8/14.
 */

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.regex.Pattern;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.kreyos.kreyosandroid.R;
import com.kreyos.kreyosandroid.database.DBManager;
import com.kreyos.kreyosandroid.database.DatabaseManager;
import com.kreyos.kreyosandroid.fragments.BaseFragmentLogin;
import com.kreyos.kreyosandroid.fragments.FragmentLogin1;
import com.kreyos.kreyosandroid.fragments.FragmentLogin2;
import com.kreyos.kreyosandroid.fragments.FragmentLogin3;
import com.kreyos.kreyosandroid.listeners.IAsyncTaskLoginListener;
import com.kreyos.kreyosandroid.listeners.IFragmentLoginListener;
import com.kreyos.kreyosandroid.listeners.IQueryEvent;
import com.kreyos.kreyosandroid.managers.PreferencesManager;
import com.kreyos.kreyosandroid.managers.RequestManager;
import com.kreyos.kreyosandroid.utilities.Constants;
import com.kreyos.kreyosandroid.utilities.KreyosUtility;
import com.kreyos.kreyosandroid.utilities.Profile;

import org.json.JSONException;
import org.json.JSONObject;


// TODO: Handling Run-Time Configuration Changes (activity automatically re-instantiates existing fragments)
// please check this link: https://thenewcircle.com/s/post/1250/android_fragments_tutorial

public class LoginActivity extends FragmentActivity
    implements
        Session.StatusCallback,
        Request.GraphUserCallback,
        View.OnClickListener,
        IQueryEvent,
        IFragmentLoginListener,     // button callbacks from fragments
        IAsyncTaskLoginListener     // login listener
{
//--------------------------------------------------------------------------------------------------- Variables
    private BaseFragmentLogin           mCurrentFragment        = null;
    public int                          FB_PERMISSION_STATE 	= 0;
    private ProgressDialog              mProgDialog             = null;
    private TextView                    mBtnIcon                = null;
    private TextView                    mBtnBar                 = null;
    private View                        mLayoutIcon             = null;
    private View                        mLayoutBar              = null;

    private int mYear;
    private int mMonth;
    private int mDay;


//--------------------------------------------------------------------------------------------------- onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN )");

        super.onCreate( savedInstanceState );

        requestWindowFeature( Window.FEATURE_NO_TITLE );
        setContentView( R.layout.activity_login );
    }

    @Override
    protected void onStart() {
        Log.d(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN ) - onStart");

        super.onStart();

        PreferencesManager.initializePrefInstance( getApplicationContext() );

        // setup fonts
        KreyosUtility.overrideFonts(this,
                ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0),
                Constants.FONT_NAME.LEAGUE_GOTHIC_REGULAR);

        initialize();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

    private void initialize() {

        setupViewsAndCallbacks();
        switchFragment(Constants.LOGIN_FRAGMENTS.FRAGMENT_LOGIN);

        checkSession();
    }

    private void setupViewsAndCallbacks() {

        mBtnBar     = (TextView) findViewById(R.id.btn_bar);
        mBtnIcon    = (TextView) findViewById(R.id.btn_icon);
        mLayoutBar  = findViewById(R.id.layout_btn_bar);
        mLayoutIcon = findViewById(R.id.layout_btn_icon);

        mBtnBar.setOnClickListener(this);
        mBtnIcon.setOnClickListener(this);
        mLayoutBar.setOnClickListener(this);
        mLayoutIcon.setOnClickListener(this);

        Calendar ca = KreyosUtility.calendar();
        mYear 		= ca.get(Calendar.YEAR);
        mMonth  	= ca.get(Calendar.MONTH); // MONDAY
        mDay		= ca.get(Calendar.DAY_OF_MONTH);
    }

    private void checkSession() {

        // Check profile
        Profile profile = new Profile();
        profile.loadDataFromPreferences();

        Log.d(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN ) - Session: \"email\" = " + profile.EMAIL );

        if (profile.EMAIL.equals("")) {
            Log.d(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN ) - Session aborted. Null email. ");
            return;
        }

        // Check Session (kreyos account)
        try {
            JSONObject params = new JSONObject();
            params.put("email", profile.EMAIL);
            params.put("auth_token", profile.KREYOS_TOKEN);

            String response = RequestManager.instance().post(Constants.PREFKEY_URL_SESSION_KEY, params);
            Log.d(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN ) - Session: response = " + response );

            JSONObject jsonObject = new JSONObject(response);
            if( jsonObject.getInt("status") == 200 ) {
                Log.d(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN ) - Session: logged in (kreyos account)");
                proceedToHomePage();
                return;
            }

        } catch(Exception e) {
            e.printStackTrace();
        }


        // Check Session (facebook account)
        if (    Session.getActiveSession() == null
            ||  !Session.getActiveSession().isOpened() ) {
            Log.d(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN ) - Session: logged in (facebook account)");
            return;
        }

        // proceed to Home (facebook account)
        proceedToHomePage();

    }

    private void proceedToHomePage() {

        Class targetClass       = MainActivity.class;
        boolean bShowTutorial   = false;
        File fileTutorialCheck  = new File(this.getExternalFilesDir(null)+ "/" + "Tutorial_Check" + ".txt");

        // already installed
        if( fileTutorialCheck.exists() ) {
            boolean isNewCreated = PreferencesManager.getInstance().retrieveDataForBoolean( Constants.PREFKEY_USER_TUTORIAL_CHECK, false );
            if (isNewCreated) {
                PreferencesManager.getInstance().saveDataBoolean( Constants.PREFKEY_USER_TUTORIAL_CHECK, false );

                bShowTutorial = true;
            }

        // fresh install
        } else {
            try {
                fileTutorialCheck.createNewFile();
                bShowTutorial = true;

            } catch (IOException e) {
                Log.d(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN ) - ERROR: Can't create tutorial check file" );
                e.printStackTrace();
            }
        }

        // Tutorial check
        if ( bShowTutorial ) {
            targetClass = SetupWatchActivity.class;
        }

        // Initialize database
        DatabaseManager.instance( (Context)this ).initLocalDB();
        DBManager dbManager = new DBManager((Context)this, this);
        dbManager.init();

        // proceed to Home/Tutorial
        try {
            finish();
            Intent i = new Intent(LoginActivity.this, targetClass);
            startActivity(i);

        } catch(Exception e) {
            Log.d(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN ) - ERROR: Can't proceed to the next screen" );
            e.printStackTrace();
        }
    }

    private void reloadLoginPage() {
        Intent i = new Intent(LoginActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }



//--------------------------------------------------------------------------------------------------- Switch Fragments
    public void switchFragment(Constants.LOGIN_FRAGMENTS pFragment) {

        switch (pFragment) {
            case FRAGMENT_LOGIN:                mCurrentFragment = new FragmentLogin1();    break;
            case FRAGMENT_SIGNUP_EMAIL:         mCurrentFragment = new FragmentLogin2();    break;
            case FRAGMENT_SIGNUP_USER_INFO:     mCurrentFragment = new FragmentLogin3();    break;
            default:
                return;
        }

        mCurrentFragment.setListener(this);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.contentFragment, mCurrentFragment);
        transaction.commit();
    }

    private void changeActivityFooter (boolean pIsLogin) {

        if (pIsLogin) {
            mBtnIcon.setBackgroundDrawable(getResources().getDrawable(R.drawable.login_icon_new_acct));
            mBtnBar.setText(R.string.actlogin_text_create_acct);

        } else {
            mBtnIcon.setBackgroundDrawable(getResources().getDrawable(R.drawable.login_icon_email));
            mBtnBar.setText(R.string.actlogin_text_login_here);
        }
    }



//--------------------------------------------------------------------------------------------------- Button functions
    @Override
    public void onClick (View pView) {

        switch (pView.getId()) {
            case R.id.btn_bar:
            case R.id.btn_icon:
            case R.id.layout_btn_bar:
            case R.id.layout_btn_icon: {
                Log.d(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN ) - Create Account/Login!!!!!");

                if (mCurrentFragment instanceof FragmentLogin1) {
                    switchFragment(Constants.LOGIN_FRAGMENTS.FRAGMENT_SIGNUP_EMAIL);
                    changeActivityFooter(false);

                } else {
                    switchFragment(Constants.LOGIN_FRAGMENTS.FRAGMENT_LOGIN);
                    changeActivityFooter(true);
                }

            } break;
        }
    }



//--------------------------------------------------------------------------------------------------- fragment listener callbacks
    public void onLoginClick(EditText pEmail, EditText pPassword) {
        // FragmentLogin1 listener callback

        mCurrentFragment.enableViews(false);

        InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(pPassword.getWindowToken(), 0);

        // connection err
        if (!KreyosUtility.hasConnection(this)) {
            KreyosUtility.showErrorMessage( this,
                                            getString(R.string.dialog_title_error_connection),
                                            getString(R.string.dialog_msg_error_connection));
            mCurrentFragment.enableViews(true);
            return;
        }

        // missing fields err
        if (    pEmail.getText().toString().equals("")
            ||  pPassword.getText().toString().equals("")) {
            KreyosUtility.showErrorMessage( this,
                                            getString(R.string.dialog_title_error),
                                            getString(R.string.actlogin_dialog_msg_error_fill_out));
            mCurrentFragment.enableViews(true);
            return;
        }

        // login
        if (Constants.DS_SET_ON) {
            new ATLogin(    getString(R.string.actlogin_dialog_title_login),
                            getString(R.string.dialog_msg_please_wait),
                            Constants.DS_DEF_EMAIL,
                            Constants.DS_DEF_PASSWORD,
                            this).execute();

        } else {
            new ATLogin(    getString(R.string.actlogin_dialog_title_login),
                            getString(R.string.dialog_msg_please_wait),
                            pEmail.getText().toString(),
                            pPassword.getText().toString(),
                            this).execute();
        }
    }

    public void onNextClick(EditText pEmail, EditText pPassword, EditText pPasswordConf) {
        // FragmentLogin2 listener callback

        mCurrentFragment.enableViews(false);

        String textEmail        = pEmail.getText().toString();
        String textPassword     = pPassword.getText().toString();
        String textPasswordConf = pPasswordConf.getText().toString();

        // connection err
        if (!KreyosUtility.hasConnection(this)) {
            KreyosUtility.showErrorMessage( this,
                    getString(R.string.dialog_title_error_connection),
                    getString(R.string.dialog_msg_error_connection));
            mCurrentFragment.enableViews(true);
            return;
        }

        // check validity
        if (!areSignupDetailsValid(textEmail, textPassword, textPasswordConf)) {
            mCurrentFragment.enableViews(true);
            return;
        }

        // create
        new ATCreateEmail(  getString(R.string.actlogin_dialog_title_create_account),
                            getString(R.string.dialog_msg_please_wait),
                            pEmail.getText().toString(),
                            pPassword.getText().toString(),
                            pPasswordConf.getText().toString(),
                            this).execute();
    }

    public void onDoneClick(String pFirstNameText, String pLastNameText, String pBirthdayText,
                            byte pGenderValue, String pHeightValue, String pWeight) {
        // FragmentLogin3 listener callback

        mCurrentFragment.enableViews(false);

        // connection err
        if (!KreyosUtility.hasConnection(this)) {
            KreyosUtility.showErrorMessage( this,
                    getString(R.string.dialog_title_error_connection),
                    getString(R.string.dialog_msg_error_connection));
            mCurrentFragment.enableViews(true);
            return;
        }

        String emailText        = PreferencesManager.getInstance().retrieveDataForString( Constants.PREFKEY_USER_EMAIL );
        String passwordText     = PreferencesManager.getInstance().retrieveDataForString(Constants.PREFKEY_USER_EMAIL_PASSWORD);
        String passwordConfText = PreferencesManager.getInstance().retrieveDataForString(Constants.PREFKEY_USER_EMAIL_PASSWORD_CONFIRM);

        Log.d(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN ) - create account"
                        + "\n\t\t\t\t\t\t first name --- " + pFirstNameText
                        + "\n\t\t\t\t\t\t last name --- " + pLastNameText
                        + "\n\t\t\t\t\t\t birthday name --- " + pBirthdayText
                        + "\n\t\t\t\t\t\t g --- " + pGenderValue
                        + "\n\t\t\t\t\t\t height --- " + pHeightValue
                        + "\n\t\t\t\t\t\t weight --- " + pWeight
                        + "\n\t\t\t\t\t\t email --- " + emailText
                        + "\n\t\t\t\t\t\t pw --- " + passwordText
                        + "\n\t\t\t\t\t\t pwc --- " + passwordConfText
        );

        float fHeightValue = Float.parseFloat(pHeightValue);

        if(     pFirstNameText.length() < 1
           ||   pLastNameText.length() < 1
           ||   pBirthdayText.equals("MM/DD/YYYY")
           ||   pGenderValue < 1
           ||   fHeightValue < 1.0
           ||   pWeight.length() < 1
           ||   emailText.length() < 1
           ||   passwordText.length() < 1
           ||   passwordConfText.length() < 1) {
            KreyosUtility.showErrorMessage( this,
                                            getString(R.string.dialog_title_error),
                                            getString(R.string.actlogin_dialog_msg_error_fill_out));
            mCurrentFragment.enableViews(true);
            return;
        }


        String genderText;
        if (pGenderValue == Constants.GENDER_MALE)
            genderText = "Male";
        else
            genderText = "Female";


        // create
        new ATCreateUserInformation(getString(R.string.actlogin_dialog_title_create_account),
                                    getString(R.string.dialog_msg_please_wait),
                                    emailText, passwordText, passwordConfText,
                                    pFirstNameText, pLastNameText, pBirthdayText, genderText, pHeightValue, pWeight, this).execute();

    }

    public void onBirthdayClick(int pId) {

        showDialog(pId);
    }

    @Override
    protected Dialog onCreateDialog(int id) {

        switch (id) {
            case Constants.DIALOG_ID_DATE:
                return new DatePickerDialog(this, mDateSetListener, mYear, mMonth, mDay);
        }
        return super.onCreateDialog(id);
    }

    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

            mYear = year;
            mMonth = monthOfYear+1;
            mDay = dayOfMonth;

            if (mCurrentFragment instanceof FragmentLogin3) {
                ((FragmentLogin3)mCurrentFragment).changeBirthdayValue("" + mDay + "/" + mMonth + "/" + mYear);
            }
        }
    };



//--------------------------------------------------------------------------------------------------- asynctask listener callbacks
    public void onLoginResult (boolean pResult, String pResponse, JSONObject pJsonObject) {
        Log.d(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN ) - login result = " + (pResult ? "SUCCESSFUL" : "FAILED") );

        if (pResult && pJsonObject!= null ) {
            saveProfileFromKreyos( pJsonObject );

            Profile profile = new Profile();
            if (profile.save(pJsonObject)) {
                proceedToHomePage();
            }

        } else {
            mCurrentFragment.enableViews(true);

            KreyosUtility.showErrorMessage( this,
                                            getString(R.string.dialog_title_error),
                                            pResponse);
        }
    }

    public void onCreateEmailResult(boolean pResult, String pResponse,
                                    String pEmailText, String pPasswordText, String pPasswordConf) {
        Log.d(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN ) - create email result = " + (pResult ? "SUCCESSFUL" : "FAILED") );

        if (pResult) {
            PreferencesManager.getInstance().saveDataString(Constants.PREFKEY_USER_EMAIL, pEmailText);
            PreferencesManager.getInstance().saveDataString(Constants.PREFKEY_USER_EMAIL_PASSWORD, pPasswordText);
            PreferencesManager.getInstance().saveDataString(Constants.PREFKEY_USER_EMAIL_PASSWORD_CONFIRM, pPasswordConf);

            switchFragment(Constants.LOGIN_FRAGMENTS.FRAGMENT_SIGNUP_USER_INFO);

        } else {
            mCurrentFragment.enableViews(true);

            KreyosUtility.showErrorMessage( this,
                                            getString(R.string.dialog_title_error),
                                            pResponse);
        }
    }

    public void onCreateUserInformationResult(boolean pResult, String pResponse, JSONObject pJsonObject) {
        Log.d(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN ) - create info result = " + (pResult ? "SUCCESSFUL" : "FAILED") );

        if (pResult) {
            Profile profile = new Profile();

            if (profile.save(pJsonObject)) {
                Log.d(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN ) - create info result = saved." );

                PreferencesManager.getInstance().saveDataBoolean( Constants.PREFKEY_USER_TUTORIAL_CHECK, true );
                reloadLoginPage();
            }

        } else {
            mCurrentFragment.enableViews(true);

            KreyosUtility.showErrorMessage( this,
                    getString(R.string.dialog_title_error),
                    getString(R.string.actlogin_dialog_msg_error_create_acct));
        }
    }



//--------------------------------------------------------------------------------------------------- FB Instance methods
    @Override
    public void call(Session session, SessionState state, Exception exception) {
        Log.d(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN ) - Session StatusCallback: " + (state.isOpened() ? "Opened ":"? ") + (state.isClosed() ? "Closed" : "?") );

        if ( session.isOpened() ) {
            mCurrentFragment.enableViews(false);
            Request.newMeRequest(session, this).executeAsync();
        }
    }

    @Override
    public void onCompleted(GraphUser user, Response response) {

        if (user != null) {
            Log.d(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN ) - on completed: User valid" );
            if (FB_PERMISSION_STATE == 0) {
                PreferencesManager.getInstance().saveDataString( Constants.PREFKEY_USER_FB_ID, user.getId() );

                saveProfileFromFacebook(user, response);
                saveFacebookProfilePicture(user.getId());
                FB_PERMISSION_STATE = 2;
            }

        } else {
            // from 'call'
            Log.d(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN ) - on completed: User null" );
            mCurrentFragment.enableViews(true);
            KreyosUtility.showLoadingProgress( false, "", "", getApplicationContext() );
        }
    }



//--------------------------------------------------------------------------------------------------- Facebook profile handler
    private void saveProfileFromFacebook(GraphUser pUser, Response pResponse) {

        Profile profile 	= new Profile();
        if (    pUser.asMap().get("email") == null
            ||  pUser.asMap().get("email") == "") {
            GraphObject fbObject    = pResponse.getGraphObject();
            profile.EMAIL           = (String) fbObject.asMap().get("username");
            profile.EMAIL           += "@facebook.com";

        } else {
            profile.EMAIL		    = (String) pUser.asMap().get("email");
        }

        profile.FB_TOKEN 	= Session.getActiveSession().getAccessToken();
        profile.FIRSTNAME 	= pUser.getFirstName();
        profile.LASTNAME 	= pUser.getLastName();
        profile.BIRTHDAY 	= pUser.getBirthday();
        profile.GENDER 		= (String) pUser.asMap().get("gender");

        profile.saveDataToPreferences();

        Log.d(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN ) - Facebook: saved profile" );
    }

    private void saveFacebookProfilePicture(String pUserID) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        URL imageURL = null;

        try {
            imageURL = new URL("https://graph.facebook.com/" + pUserID + "/picture?type=large");

        } catch (MalformedURLException e) {
            Log.e(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN ) - Facebook: image" + e);
        }

        Bitmap bitmap = null;

        try {
            bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());

        } catch (IOException e) {
            Log.e(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN ) - Facebook: image" + e);
        }

        byte[] byteArray 	= KreyosUtility.convertBitmapToByteArray(bitmap);

        PreferencesManager.getInstance().saveDataString( Constants.PREFKEY_USER_FB_IMAGE, KreyosUtility.convertByteArrayToString(byteArray) );


        // Saving fb token in api is broken
        saveFacebookLoginOnWeb();
    }

    private void saveFacebookLoginOnWeb() {
		try {
            JSONObject params = new JSONObject();
            params.put("email",         PreferencesManager.getInstance().retrieveDataForString(Constants.PREFKEY_USER_EMAIL) );
            params.put("uid", 			PreferencesManager.getInstance().retrieveDataForString(Constants.PREFKEY_USER_FB_ID) );
            params.put("auth_token",	PreferencesManager.getInstance().retrieveDataForString(Constants.PREFKEY_USER_FB_TOKEN) );

            String response = RequestManager.instance().post(Constants.PREFKEY_URL_FACEBOOK_LOGIN, params);
            Log.e(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN ) - Facebook: response = " + response);

            JSONObject jsonObject = new JSONObject(response);
            if(jsonObject.getBoolean("success")) {
                Log.e(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN ) - Facebook: response = " + jsonObject.toString() );
                proceedToHomePage();

            } else {
                KreyosUtility.showErrorMessage( this,
                                            getString(R.string.dialog_title_error),
                                            "" + jsonObject.getString("message"));

                KreyosUtility.showLoadingProgress( false, "", "", getApplicationContext() );

                if(Session.getActiveSession() != null) {
                    Session.getActiveSession().closeAndClearTokenInformation();
                    Log.e( Constants.TAG_DEBUG, "( ACTIVITY:LOGIN ) - Session: not null; logout" );
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e( Constants.TAG_DEBUG, "( ACTIVITY:LOGIN ) - Facebook: saving on web failed = " + e );
        }
    }



//--------------------------------------------------------------------------------------------------- Kreyos profile handler
    private void saveProfileFromKreyos(JSONObject pJsonObject) {
        try {
            JSONObject user 		= pJsonObject.getJSONObject("user");

            Profile profile 		= new Profile();
            profile.EMAIL			= user.getString("email");
            profile.KREYOS_TOKEN 	= user.getString("auth_token");
            profile.FIRSTNAME 		= user.getString("first_name");
            profile.LASTNAME 		= user.getString("last_name");
            profile.BIRTHDAY 		= user.getString("birthday");
            profile.GENDER 			= user.getString("gender");

            if ( !user.getString("dimensions").equalsIgnoreCase("null") ) {
                JSONObject dimension 	= user.getJSONObject("dimensions");
                profile.HEIGHT			= dimension.getString("height");
                profile.WEIGHT			= dimension.getString("weight");
            }

            profile.saveDataToPreferences();
            PreferencesManager.getInstance().saveDataString( Constants.PREFKEY_USER_IMAGE, "" );
            Log.e(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN ) - Save profile from kreyos = success!");

        } catch( Exception e ) {
            KreyosUtility.showLoadingProgress( false, "", "", getApplicationContext() );
            Log.e(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN ) - Save profile from kreyos = failed! " + e);
        }
    }



//--------------------------------------------------------------------------------------------------- Signup helpers
    private boolean areSignupDetailsValid(String pEmailText, String pPasswordText, String pPasswordConfText) {

        if(     pEmailText.length() < 1
           ||   pPasswordText.length() < 1
           ||   pPasswordConfText.length() < 1 ) {
            KreyosUtility.showErrorMessage( this,
                                            getString(R.string.dialog_title_error),
                                            getString(R.string.actlogin_dialog_msg_error_fill_out));
            return false;
        }

        if (!isEmailValid(pEmailText)) {
            KreyosUtility.showErrorMessage( this,
                                            getString(R.string.dialog_title_error),
                                            getString(R.string.actlogin_dialog_msg_error_invalid_email));
            return false;
        }

        if (!pPasswordText.equals(pPasswordConfText)) {
            KreyosUtility.showErrorMessage( this,
                                            getString(R.string.dialog_title_error),
                                            getString(R.string.actlogin_dialog_msg_error_passwords_mismatch));
            return false;
        }

        if (pPasswordText.length() < 8) {
            KreyosUtility.showErrorMessage( this,
                                            getString(R.string.dialog_title_error),
                                            getString(R.string.actlogin_dialog_msg_error_short_password));
            return false;
        }

        return true;
    }

    private boolean isEmailValid( String p_email ) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(p_email).matches();
    }



    /*  AsyncTask < doInBackground() input , progress information , returned by doInBackground() &&  onPostExecute() input > */
//--------------------------------------------------------------------------------------------------- Async Task (AT)
    private class ATLogin extends AsyncTask<String,Integer,Integer> {

        String mTitle;
        String mMessage;
        String mEmail;
        String mPassword;
        String mErrMessage;
        Integer mResult;
        IAsyncTaskLoginListener mListener;
        JSONObject mJsonObject;

        public ATLogin(String pTitle, String pMessage, String pEmail, String pPassword, IAsyncTaskLoginListener pCurrentActivity) {
            this.mTitle         = pTitle;
            this.mMessage       = pMessage;
            this.mEmail         = pEmail;
            this.mPassword      = pPassword;
            this.mListener      = pCurrentActivity;
            this.mResult        = 0;
            this.mErrMessage    = "";
            this.mJsonObject    = null;
        }

        @Override
        protected void onPreExecute() {
            Log.d(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN: AT Login ) - onPreExecute");

            mProgDialog= new ProgressDialog(LoginActivity.this);
            mProgDialog.setIndeterminate(true);
            mProgDialog.setCancelable(false);
            mProgDialog.setTitle(mTitle);
            mProgDialog.setMessage(mMessage);
            mProgDialog.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            Log.d(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN: AT Login ) - doInBackground");

            // NOTE: don't interact with UI; for background process ONLY
            // coding instruction which should be performed in a background thread. This method runs automatically in a separate Thread.

            try {
                JSONObject parameters = new JSONObject();

                parameters.put("email", mEmail);
                parameters.put("password", mPassword);

                String response = RequestManager.instance().post(Constants.PREFKEY_URL_LOGIN_CHECK, parameters);

                mJsonObject = new JSONObject(response);
                if (mJsonObject.getInt("status") == 200) {
                    Log.d(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN: AT Login ) - Success!");
                    mResult = 1;

                } else {
                    Log.d(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN: AT Login ) - Error !!!");
                    mErrMessage = mJsonObject.getString("message");
                }

                Log.d(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN: AT Login ) - response (" + mJsonObject.toString() + ")");

                return mResult;

            } catch (JSONException e) {
                e.printStackTrace();
                return mResult;
            }
        }

        @Override
        protected void onPostExecute(Integer pIResult) {
            Log.d(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN: AT Login ) - onPostExecute = result: " + pIResult);

            // synchronizes itself again with the user interface thread
            // called by the framework once the doInBackground() method finishes

            super.onPostExecute(pIResult);

            mProgDialog.dismiss();

            if (pIResult == 1)
                mListener.onLoginResult(true, mErrMessage, mJsonObject);
            else
                mListener.onLoginResult(false, mErrMessage, null);
        }

    } //end AsyncTask

    private class ATCreateEmail extends AsyncTask<String,Integer,Integer> {

        String mTitle;
        String mMessage;
        String mEmail;
        String mPassword;
        String mPasswordConf;
        String mErrMessage;
        Integer mResult;
        IAsyncTaskLoginListener mListener;

        public ATCreateEmail(String pTitle, String pMessage, String pEmail, String pPassword, String pPasswordConf, IAsyncTaskLoginListener pCurrentActivity) {
            this.mTitle         = pTitle;
            this.mMessage       = pMessage;
            this.mEmail         = pEmail;
            this.mPassword      = pPassword;
            this.mPasswordConf  = pPasswordConf;
            this.mListener      = pCurrentActivity;
            this.mResult        = 0;
            this.mErrMessage    = "";
        }

        @Override
        protected void onPreExecute() {
            Log.d(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN: AT CreateEmail ) - onPreExecute");

            mProgDialog= new ProgressDialog(LoginActivity.this);
            mProgDialog.setIndeterminate(true);
            mProgDialog.setCancelable(false);
            mProgDialog.setTitle(mTitle);
            mProgDialog.setMessage(mMessage);
            mProgDialog.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            Log.d(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN: AT CreateEmail ) - doInBackground");

            try {
                JSONObject parameters = new JSONObject();
                parameters.put("email", mEmail);

                String response = RequestManager.instance().post(Constants.PREFKEY_URL_CHECK_MAIL, parameters);

                JSONObject jsonObject = new JSONObject(response);
                if (jsonObject.has("success")) {
                    Log.d(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN: AT CreateEmail ) - Success!");
                    mResult = 1;

                } else if (jsonObject.has("error")) {
                    Log.d(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN: AT CreateEmail ) - Error !!!");

                    JSONObject jsonObject2 = new JSONObject(jsonObject.getString("error"));
                    mErrMessage = jsonObject2.getString("message");
                }

                Log.d(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN: AT CreateEmail ) - response (" + jsonObject.toString() + ")");

                return mResult;

            } catch (JSONException e) {
                e.printStackTrace();
                return mResult;
            }
        }

        @Override
        protected void onPostExecute(Integer pIResult) {
            Log.d(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN: AT CreateEmail ) - onPostExecute = result: " + pIResult);

            super.onPostExecute(pIResult);

            mProgDialog.dismiss();

            if (pIResult == 1)
                mListener.onCreateEmailResult(true, mErrMessage, mEmail, mPassword, mPasswordConf);
            else
                mListener.onCreateEmailResult(false, mErrMessage, null, null, null);
        }

    } //end AsyncTask

    private class ATCreateUserInformation extends AsyncTask<String,Integer,Integer> {

        String mTitle;
        String mMessage;
        String mEmail;
        String mPassword;
        String mPasswordConf;

        String mFirstName;
        String mLastName;
        String mBirthday;
        String mGender;
        String mHeight;
        String mWeight;

        String mErrMessage;
        Integer mResult;
        IAsyncTaskLoginListener mListener;
        JSONObject mJsonObject;

        public ATCreateUserInformation(String pTitle, String pMessage,
                                       String pEmail, String pPassword, String pPasswordConf,
                                       String pFirstNameText, String pLastNameText, String pBirthdayText, String pGenderText, String pHeight, String pWeight,
                                       IAsyncTaskLoginListener pCurrentActivity) {
            this.mTitle         = pTitle;
            this.mMessage       = pMessage;

            this.mEmail         = pEmail;
            this.mPassword      = pPassword;
            this.mPasswordConf  = pPasswordConf;
            this.mFirstName     = pFirstNameText;
            this.mLastName      = pLastNameText;
            this.mBirthday      = pBirthdayText;
            this.mGender        = pGenderText;
            this.mHeight        = pHeight;
            this.mWeight        = pWeight;

            this.mListener      = pCurrentActivity;
            this.mResult        = 0;
            this.mErrMessage    = "";
            this.mJsonObject    = null;
        }

        @Override
        protected void onPreExecute() {
            Log.d(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN: AT CreateUserInformation ) - onPreExecute");

            mProgDialog= new ProgressDialog(LoginActivity.this);
            mProgDialog.setIndeterminate(true);
            mProgDialog.setCancelable(false);
            mProgDialog.setTitle(mTitle);
            mProgDialog.setMessage(mMessage);
            mProgDialog.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            Log.d(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN: AT CreateUserInformation ) - doInBackground");

            try {
                JSONObject parameters = new JSONObject();

                parameters.put("email", mEmail);
                parameters.put("password", mPassword);
                parameters.put("password_confirmation", mPasswordConf);
                parameters.put("first_name", mFirstName);
                parameters.put("last_name", mLastName);
                parameters.put("birthday", mBirthday);
                parameters.put("gender", mGender);
                parameters.put("height", mHeight);
                parameters.put("weight", mWeight);

                String response = RequestManager.instance().post(Constants.PREFKEY_URL_CREATE_ACCOUNT, parameters);

                mJsonObject = new JSONObject(response);
                Log.d(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN: AT CreateUserInformation ) - response (" + mJsonObject.toString() + ")");

                if (mJsonObject.has("error")) {
                    Log.d(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN: AT CreateUserInformation ) - Error !!!");

                    JSONObject jsonObject2 = new JSONObject(mJsonObject.getString("error"));
                    mErrMessage = jsonObject2.getString("message");

                    return mResult;
                }

                Log.d(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN: AT CreateUserInformation ) - Success!");
                mResult = 1;

                return mResult;

            } catch (JSONException e) {
                e.printStackTrace();
                return mResult;
            }
        }

        @Override
        protected void onPostExecute(Integer pIResult) {
            Log.d(Constants.TAG_DEBUG, "( ACTIVITY:LOGIN: AT CreateUserInformation ) - onPostExecute = result: " + pIResult);

            super.onPostExecute(pIResult);

            mProgDialog.dismiss();

            if (pIResult == 1)
                mListener.onCreateUserInformationResult(true, mErrMessage, mJsonObject);
            else
                mListener.onCreateUserInformationResult(false, mErrMessage, null);
        }

    } //end AsyncTask



//--------------------------------------------------------------------------------------------------- Query (for database)
    public void onQueryStart( String p_queryKey ) {}
    public void onQueryComplete( String p_queryKey, Cursor p_query ) {}
    public void onQueryError( String p_queryKey, String p_error ) {}

}




