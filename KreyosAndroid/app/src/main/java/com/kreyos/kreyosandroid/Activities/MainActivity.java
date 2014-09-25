package com.kreyos.kreyosandroid.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.coboltforge.slidemenu.SlideMenuInterface;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationListener;
import com.kreyos.kreyosandroid.bluetooth.BluetoothAgent;
import com.kreyos.kreyosandroid.bluetooth.Protocol;
import com.kreyos.kreyosandroid.database.DBManager;
import com.kreyos.kreyosandroid.fragments.BaseFragmentMain;
import com.kreyos.kreyosandroid.fragments.FragmentLeft4;
import com.kreyos.kreyosandroid.fragments.FragmentLeft5;
import com.kreyos.kreyosandroid.fragments.FragmentLogin3;
import com.kreyos.kreyosandroid.fragments.FragmentRight1;
import com.kreyos.kreyosandroid.fragments.FragmentRight2;
import com.kreyos.kreyosandroid.fragments.FragmentRight3;
import com.kreyos.kreyosandroid.fragments.FragmentRight4;
import com.kreyos.kreyosandroid.fragments.FragmentRight5;
import com.kreyos.kreyosandroid.listeners.IBluetoothListener;
import com.kreyos.kreyosandroid.listeners.IFragmentMainListener;
import com.kreyos.kreyosandroid.listeners.IKreyosListener;
import com.kreyos.kreyosandroid.listeners.IQueryEvent;
import com.kreyos.kreyosandroid.managers.BluetoothManager;
import com.kreyos.kreyosandroid.managers.KreyosDialogManager;
import com.kreyos.kreyosandroid.managers.PreferencesManager;
import com.kreyos.kreyosandroid.managers.RequestManager;
import com.kreyos.kreyosandroid.objectdata.ActivityDataDoc;
import com.kreyos.kreyosandroid.objectdata.ActivityDataRow;
import com.kreyos.kreyosandroid.objectdata.SportsDataRow;
import com.kreyos.kreyosandroid.objectdata.TodayActivity;
import com.kreyos.kreyosandroid.others.SlidingMenu;
import com.kreyos.kreyosandroid.R;

// Kreyos activities
import com.kreyos.kreyosandroid.fragments.FragmentLeft1;
import com.kreyos.kreyosandroid.fragments.FragmentLeft2;
import com.kreyos.kreyosandroid.fragments.FragmentLeft3;

// Kreyos utilities
import com.kreyos.kreyosandroid.utilities.Constants;
import com.kreyos.kreyosandroid.utilities.KreyosUtility;
import com.kreyos.kreyosandroid.utilities.Profile;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

// TODO: CASES

public class MainActivity extends FragmentActivity
    implements
          IFragmentMainListener
        , IBluetoothListener
        , IKreyosListener
        , SlideMenuInterface.OnSlideMenuItemClickListener
        , View.OnClickListener
        , IQueryEvent

    // Sports Mode
        , GooglePlayServicesClient.ConnectionCallbacks
        , GooglePlayServicesClient.OnConnectionFailedListener
        , LocationListener
{

//--------------------------------------------------------------------------------------------------- Variables
    // UI
    private SlidingMenu             mSlideMenuLeft;
    private SlidingMenu             mSlideMenuRight;
    private TextView                mHeader;
    private BaseFragmentMain        mCurrentFragment;

    // for activities
    public ProgressDialog           mProgressDialog = null;


    // FOR SPORTS MODE
    // GPS variables
    private LocationClient mLocationClient = null;
    private LocationRequest mLocationRequest = null;
    boolean mIsWritingWatchEnable 		= false;
    private Location       mLocationOld = null;

    // FOR DAILY TARGET (temporary)
    private static int      MTOTAL_GRID         = 4;
    private int[] mArrGridData = new int[] { 0, 1, 4, 3, 9 };

    // FOR DATE & TIME
    private Date mDate = null;
    private Date mTime = null;

//--------------------------------------------------------------------------------------------------- (Date & Time) classes


//--------------------------------------------------------------------------------------------------- onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(Constants.TAG_DEBUG, "( ACTIVITY:MAIN )");

        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        KreyosDialogManager.initializeActivity( this );
    }

    @Override
    protected void onStart() {
        Log.d(Constants.TAG_DEBUG, "( ACTIVITY:MAIN ) - onStart");

        super.onStart();

        // setup fonts
        KreyosUtility.overrideFonts(this,
                ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0),
                Constants.FONT_NAME.LEAGUE_GOTHIC_REGULAR);

        initialize();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        BluetoothManager.getInstance().onActivityResult(requestCode, resultCode, data);
    }

    private void initialize() {

        // manage views
        setupSlideMenu();
        setupViewsAndCallbacks();
        showDefaultActivity();

        // for non-cancelable dialog
        PreferencesManager.getInstance().saveDataBoolean( Constants.PREFKEY_IS_TUTORIAL_MODE, false );

        // start Bluetooth setup/connection
        BluetoothManager.getInstance().init( this );
        BluetoothManager.getInstance().setListener( this );
        BluetoothManager.getInstance().turnOn();
    }

    private void setupSlideMenu() {
        mSlideMenuLeft  = (SlidingMenu) findViewById(R.id.slideMenu_left);
        mSlideMenuRight = (SlidingMenu) findViewById(R.id.slideMenu_right);

        mSlideMenuLeft.init(this, R.menu.left_slide, this, 333, true); // left animation
        mSlideMenuRight.init(this, R.menu.right_slide, this, 333, false); //right animation

        ImageView slideLeft     = (ImageView)findViewById(R.id.imageView_slide_left);
        ImageView slideRight    = (ImageView)findViewById(R.id.imageView_slide_right);

        slideLeft.setOnClickListener(this);
        slideRight.setOnClickListener(this);
    }

    private void setupViewsAndCallbacks() {
        mHeader = (TextView) findViewById(R.id.textView_title_header);

        updateHeaderColor( false );
    }

    private void showDefaultActivity() {
        Log.d(Constants.TAG_DEBUG, "( ACTIVITY:MAIN ) - show default fragment");

        mCurrentFragment = new FragmentLeft1();
        mCurrentFragment.setListener( this );
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.contentFragment, mCurrentFragment);
        transaction.commit();
    }

    private void updateHeaderColor( boolean pBIsConnected ) {
        LinearLayout _headerBackground = (LinearLayout) findViewById(R.id.textView_header);

        if (_headerBackground == null) {
            return;
        }

        // FIXME: drawables
        if (pBIsConnected) {
            _headerBackground.setBackgroundResource(R.color.bg_blue);
        } else {
            _headerBackground.setBackgroundResource(R.color.bg_red);
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        // unbind service (needed for stop Service from leaking)
        BluetoothManager.getInstance().unbindService();
    }


//--------------------------------------------------------------------------------------------------- BT Message Handler
    public void handleBluetoothMessage(Message pMessage) {
        Log.d(Constants.TAG_DEBUG, "( ACTIVITY:MAIN ) - Watch message: \"" + pMessage.what + "\"");

        switch (pMessage.what) {

            //-- SAVE DEVICE ID
            case Protocol.MessageID.MSG_DEVICE_ID_GOT: {
                Log.d(Constants.TAG_DEBUG, "( ACTIVITY:MAIN ) - Watch message: DEVICE_ID \"" + pMessage.obj + "\"");

                String device_id = new String((byte[]) (pMessage.obj));
                PreferencesManager.getInstance().saveDataString( Constants.PREFKEY_DEVICE_ID, device_id );
            } break;

            //-- SAVE FIRMWARE VERSION
            case Protocol.MessageID.MSG_FIRMWARE_VERSION: {
                Log.d(Constants.TAG_DEBUG, "( ACTIVITY:MAIN ) - Watch message: FIRMWARE_VERSION \"" + pMessage.obj + "\"");

                String version = (String) pMessage.obj;
                PreferencesManager.getInstance().saveDataString( Constants.PREFKEY_FIRMWARE_VERSION, version );
            } break;

            //-- BLUETOOTH STATUS
            case Protocol.MessageID.MSG_BLUETOOTH_STATUS: {

                if ( mCurrentFragment == null ) { return; }

                Log.d(Constants.TAG_DEBUG, "( ACTIVITY:MAIN ) - Watch message: BT_STATUS \"" + pMessage.obj + "\"");

                if ( pMessage.obj.toString().equals("Connecting") ) {

                }

                // for home only
                else if (   pMessage.obj.toString().equals("Running")
                         && mCurrentFragment instanceof FragmentLeft1 ) {
                    BluetoothManager.getInstance().connectBluetoothHeadset();
                    // unlock watch
                    BluetoothManager.getInstance().unlockDevice();
                    // set stats
                    BluetoothManager.getInstance().setActivityData();
                }

                // for the rest of the fragments
                else if (   pMessage.obj.toString().equals("Running") ) {
                    BluetoothManager.getInstance().connectBluetoothHeadset();
                }

                // not connected
                else {
                    BluetoothManager.getInstance().changeDeviceConnectionState( Constants.WATCH_STATE.DISCONNECTED );
                    updateHeaderColor(false);
                }

            } break;

            //-- FILE RECEIVED
            case Protocol.MessageID.MSG_FILE_RECEIVED: {

                if ( mCurrentFragment == null ) { return; }

                Log.d(Constants.TAG_DEBUG, "( ACTIVITY:MAIN ) - Watch message: FILE_RECEIVED \"" + pMessage.obj + "\"");

                if ( mCurrentFragment instanceof FragmentLeft2 ) {
                    // +AS:05142014
                    // TODO: Adjust the adapter here and update the data latest from watch
                    // Save/Update the db data here
                    ActivityDataDoc dataDoc = (ActivityDataDoc)pMessage.obj;
                    ((FragmentLeft2) mCurrentFragment).manageActivityData( dataDoc );

                    // Now.. update the adapter
                    ((FragmentLeft2) mCurrentFragment).loadMultipleScrolls();
                }
            } break;

            //-- ACTIVITY PREPARE
            case Protocol.MessageID.MSG_ACTIVITY_PREPARE: {

                if ( mCurrentFragment == null ) { return; }

                Log.d(Constants.TAG_DEBUG, "( ACTIVITY:MAIN ) - Watch message: ACTIVITY_PREPARE \"" + pMessage.obj + "\"");

                // SPORTS MODE
                if ( mCurrentFragment instanceof FragmentLeft3 ) {
                    ((FragmentLeft3) mCurrentFragment).prepareData();
                }
//                Log.d("HomeActivity", "Preparing to move on SportsActivity");
//                Intent i3 = new Intent(HomeActivity.this, SportsActivity.class);
//                startActivity(i3);
//                finish();
            } break;

            //-- TODAY'S ACTIVITY
            case Protocol.MessageID.MSG_TODAY_ACTIVITY: {

                if ( mCurrentFragment == null ) { return; }

                Log.d(Constants.TAG_DEBUG, "( ACTIVITY:MAIN ) - Watch message: TODAY_ACTIVITY \"" + pMessage.obj + "\"");

                if ( mCurrentFragment instanceof FragmentLeft1 ) {
                    TodayActivity act = (TodayActivity) pMessage.obj;
                    BluetoothManager.getInstance().displayProgressBarUpdate(act);
                }
            } break;

            //-- ACTIVITY DATA RECEIVED
            case Protocol.MessageID.MSG_ACTIVITY_DATA_GOT: {

                if ( mCurrentFragment == null ) { return; }

                Log.d(Constants.TAG_DEBUG, "( ACTIVITY:MAIN ) - Watch message: ACTIVITY_DATA_GOT \"" + pMessage.obj + "\"");

                if ( mCurrentFragment instanceof FragmentLeft3 ) {
                    SportsDataRow sportsDataRow = (SportsDataRow)pMessage.obj;
                    ((FragmentLeft3) mCurrentFragment).receivedActivityData( sportsDataRow , mIsWritingWatchEnable );

//                    if (sportsDataRow == null)
//                    {
//                        secondCounter = 2;
//                        syncTime = true;
//                        if (workThread.getState() == Thread.State.NEW)
//                            workThread.start();
//                    }
//
//                    lastActivitySyncTime = new Date();
//                    sportsDataRow = (SportsDataRow)msg.obj;
//
//                    if (!mIsWritingWatchEnable) {
//                        secondCounter = (int)sportsDataRow.seconds_elapse - 1;
//                    }
//
//                    // m_bgTimer.setBackground(getResources().getDrawable(R.color.active_color));
//                    m_bgTimer.setBackgroundDrawable(getResources().getDrawable(R.color.active_color));
//                    displaySportsData(sportsDataRow);
//                    fixFirstValueBasedonMode(sportsDataRow.sports_mode);


                    // Enable flag to start writing on watch
                    mIsWritingWatchEnable = true;
                }

            }
            break;

            //-- ACTIVITY DATA END
            case Protocol.MessageID.MSG_ACTIVITY_DATA_END: //7

                if ( mCurrentFragment == null ) { return; }

                Log.d(Constants.TAG_DEBUG, "( ACTIVITY:MAIN ) - Watch message: ACTIVITY_DATA_END \"" + pMessage.obj + "\"");

                // SPORTS MODE
                if ( mCurrentFragment instanceof FragmentLeft3 ) {
                    ((FragmentLeft3)mCurrentFragment).dataEnd();
                    mIsWritingWatchEnable = false;
                    mLocationOld = null;
                }

                break;

        } // end of switch
    }



//--------------------------------------------------------------------------------------------------- Switch Fragments/ Slide Menu
    @Override
    public void onSlideMenuItemClick(int itemId) {

        boolean bIsSetupWatch = false;

        String strHeader = "";
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        if (itemId == R.id.item_right_seven) {

            // connection err
            if ( !KreyosUtility.hasConnection(this) ) {
                KreyosUtility.showErrorMessage( this,
                        getString(R.string.dialog_title_error_connection),
                        getString(R.string.dialog_msg_error_connection));
                return;
            }

            // logout user
            logout();
            return;
        }

        mCurrentFragment = null;

        switch (itemId) {
            case R.id.item_left_one:    { mCurrentFragment = new FragmentLeft1();     strHeader = getString(R.string.fragmentL1_header); } break;
            case R.id.item_left_two:    { mCurrentFragment = new FragmentLeft2();     strHeader = getString(R.string.fragmentL2_header); } break;
            case R.id.item_left_three:  { mCurrentFragment = new FragmentLeft3();     strHeader = getString(R.string.fragmentL3_header); prepareFragmentSportsMode(); } break;
            case R.id.item_left_four:   { mCurrentFragment = new FragmentLeft4();     strHeader = getString(R.string.fragmentL4_header); } break;
            case R.id.item_left_five:   { mCurrentFragment = new FragmentLeft5();     strHeader = getString(R.string.fragmentL5_header); } break;

            case R.id.item_right_one:   { mCurrentFragment = new FragmentRight1();    strHeader = getString(R.string.fragmentR1_header); } break;
            case R.id.item_right_two:   { mCurrentFragment = new FragmentRight2();    strHeader = getString(R.string.fragmentR2_header); prepareFragmentDateAndTime(); } break;
            case R.id.item_right_three: { mCurrentFragment = new FragmentRight3();    strHeader = getString(R.string.fragmentR3_header); } break;
            case R.id.item_right_four:  { mCurrentFragment = new FragmentRight4();    strHeader = getString(R.string.fragmentR4_header); } break;
            case R.id.item_right_five:  { mCurrentFragment = new FragmentRight5();    strHeader = getString(R.string.fragmentR5_header); } break;
            case R.id.item_right_six:   bIsSetupWatch = true; break;

            default:
                return;
        }

        // Tutorial Activity
        if (bIsSetupWatch) {
            Intent intent = new Intent(MainActivity.this, SetupWatchActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        mCurrentFragment.setListener( this );
        ft.replace(R.id.contentFragment, mCurrentFragment);
        ft.commit();

        mHeader.setText(strHeader);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageView_slide_left:
                mSlideMenuLeft.show();
                break;
            case R.id.imageView_slide_right:
                mSlideMenuRight.show();
                break;
            default:
                break;
        }
    }



//--------------------------------------------------------------------------------------------------- Logout
    private void logout() {
        BluetoothManager.getInstance().disconnectWatch();

        // Facebook logout
        if(com.facebook.Session.getActiveSession() != null) {
            com.facebook.Session.getActiveSession().closeAndClearTokenInformation();
        }

        Profile profile = new Profile();
        profile.loadDataFromPreferences();

        try {
            JSONObject params = new JSONObject();
            params.put("email", profile.EMAIL);
            params.put("auth_token", profile.KREYOS_TOKEN);

            String response = RequestManager.instance().post(Constants.PREFKEY_URL_DELETE_SESSION, params);
            Log.d(Constants.TAG_DEBUG, "( ACTIVITY:MAIN ) - Logout. response = " + response);

            // Check if your logged in
            JSONObject jsonObject = new JSONObject( response );
            if( jsonObject.getInt("status") == 200 ) {
                Log.d(Constants.TAG_DEBUG, "( ACTIVITY:MAIN ) - Logged out. ");
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }

        // Delete all settings and information
        PreferencesManager.getInstance().deleteAllData();

        // create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle( getString(R.string.dialog_title_logout) );
        builder.setMessage( getString(R.string.dialog_msg_logout_successful) );
        builder.setPositiveButton( getString(R.string.dialog_btn_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        //show dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }



//--------------------------------------------------------------------------------------------------- Bluetooth Handler
    public void updateProgressBar(float pMaxSteps, float pSteps, float pDistance, float pCalories) {
        Log.d(Constants.TAG_DEBUG, "( ACTIVITY:MAIN ) - Update progress bar");

        if ( !(mCurrentFragment instanceof FragmentLeft1) ) {
            return;
        }

        ((FragmentLeft1) mCurrentFragment).updateProgressBar(pMaxSteps,
                                                             pSteps,
                                                             pDistance,
                                                             pCalories);

    }

    public void onBluetoothHeadsetConnected() {
        Log.d(Constants.TAG_DEBUG, "( ACTIVITY:MAIN ) - Bluetooth Headset Connected!");

        updateHeaderColor( true );

        if (   mCurrentFragment != null
            && !(mCurrentFragment instanceof FragmentRight1)) {
            BluetoothManager.getInstance().restartBTAgentSession();
        }

        // if ACTIVE WATCH screen
        if (   mCurrentFragment != null
            && mCurrentFragment instanceof FragmentRight1 ) {
            ((FragmentRight1)mCurrentFragment).onBluetoothHeadsetConnected();
        }
    }

    // for Active Watch
    public void onUnpairConnectedDevice() {
        // if ACTIVE WATCH screen
        if (   mCurrentFragment != null
            && mCurrentFragment instanceof FragmentRight1 ) {
            ((FragmentRight1)mCurrentFragment).onUnpairDeviceFinish();
        }
    }


//--------------------------------------------------------------------------------------------------- Watch Handler
    @Override
    public void onWatchConnected() {

    }

    @Override
    public void onGetFirmwareVersion(Message pMessage) {

    }

    @Override
    public void onActivityDataReceived(Message pMessage) {

    }

    @Override
    public void onTodaysDataReceived(Message pMessage) {

    }

//--------------------------------------------------------------------------------------------------- methods accessed by fragments
    public void onHomeAvatarClick() {

    }

    public void onHomeTargetButtonClick( boolean pBProceedToDailyTargetFragment ) {
        PreferencesManager.getInstance().saveDataBoolean( Constants.PREFKEY_USER_FIRST_VIEW, true );

        String strHeader;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        if ( pBProceedToDailyTargetFragment ) {
            mCurrentFragment = new FragmentLeft4();
            strHeader = getString(R.string.fragmentL4_header);
        } else {
            mCurrentFragment = new FragmentLeft2();
            strHeader = getString(R.string.fragmentL2_header);
        }

        ft.replace(R.id.contentFragment, mCurrentFragment);
        ft.commit();

        mHeader.setText(strHeader);
    }

    public void onActivitiesStartQuery( String pKey, long pHead, long pTail ) {

        Log.d(Constants.TAG_DEBUG, "( ACTIVITY:MAIN ) - Activity Start Query: QUERY" +
                "\" SELECT * FROM Kreyos_User_Activities WHERE CreatedTime <= " + pHead +
                " AND CreatedTime >= " + pTail + " ORDER BY CreatedTime ASC\"");

        DBManager queryObject = new DBManager( this, this );
        queryObject.init();
        queryObject.query( pKey,
                           "SELECT * FROM Kreyos_User_Activities WHERE CreatedTime <= " + pHead
                                   + " AND " + "CreatedTime >= " + pTail + " ORDER BY CreatedTime ASC" );
    }


//--------------------------------------------------------------------------------------------------- IQUERY listener
    @Override
    public void onQueryStart( String p_queryKey ) {

        if ( mCurrentFragment == null )
            return;

        if ( mCurrentFragment instanceof FragmentLeft2 ) {
            Log.d(Constants.TAG_DEBUG, "( ACTIVITY:MAIN ) - On Query Start: Overall Activities with query key \""+p_queryKey+"\"");

            if( mProgressDialog != null )
                mProgressDialog = null;

            mProgressDialog = ProgressDialog.show( this,
                                                   getString(R.string.fragmentL2_dialog_title_activities_update),
                                                   getString(R.string.fragmentL2_dialog_msg_synching_activity_data),
                                                   true);
            return;
        }


    }

    @Override
    public void onQueryComplete( String p_queryKey, Cursor p_query ) {

        if ( mCurrentFragment == null )
            return;

        if ( mCurrentFragment instanceof FragmentLeft2 ) {
            Log.d(Constants.TAG_DEBUG, "( ACTIVITY:MAIN ) - On Query Complete: Overall Activities with query key \""+p_queryKey+"\"");

            if( mProgressDialog != null )
                mProgressDialog.dismiss();

            ArrayList<ActivityDataRow> localActivities = new ArrayList<ActivityDataRow>();
            ((FragmentLeft2) mCurrentFragment).createDataForRow( localActivities, p_query );

            try {
                ((FragmentLeft2) mCurrentFragment).createCellAdapter( (Context)this, localActivities );

                runOnUiThread(new Runnable() {
                    public void run() {
                        ((FragmentLeft2) mCurrentFragment).setCellAdapter();
                    }
                });
            }
            catch(NullPointerException e) {
                // Error! either no connection or not connected to watch
                e.printStackTrace();
            }

            return;
        }




    }

    @Override
    public void onQueryError( String p_queryKey, String p_error ) {

        if ( mCurrentFragment == null )
            return;

        if ( mCurrentFragment instanceof FragmentLeft2 ) {

            if( mProgressDialog != null )
                mProgressDialog.dismiss();

            return;
        }
    }


//--------------------------------------------------------------------------------------------------- PREPARATIONS BEFORE INITIALIZING FRAGMENTS
    private void prepareFragmentHome() {

    }

    private void prepareFragmentSportsMode() {
        // Create loction request
        mLocationRequest = LocationRequest.create();

        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Set the update interval to 3 seconds
        mLocationRequest.setInterval(3000);

        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(1000);

        // Start connection on location client
        mLocationClient = new LocationClient(this, this, this);
        mLocationClient.connect();

        // Checking of gps
        // Checking of gps
        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        boolean gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!gpsStatus) {
            // Show gps settings dialog

            // Set context for dialog

            AlertDialog.Builder alertDialog = new AlertDialog.Builder( this );

            // Setting Dialog Title
            alertDialog.setTitle("GPS is settings");

            // Setting Dialog Message
            alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

            // On pressing Settings button
            alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });

            // on pressing cancel button
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            // Showing Alert Message
            alertDialog.show();
        }


    }

    private void prepareFragmentDateAndTime() {

        mDate = null;
        mTime = null;

        mDate = new Date();
        mTime = new Date();
    }

    private void exitFragmentSportsMode() {

    }

//--------------------------------------------------------------------------------------------------- Listeners (Sports Mode)
    /************** GooglePlayServicesClient */
    @Override
    public void onConnected(Bundle pConnectionHint) {
        try {
            mLocationClient.requestLocationUpdates(mLocationRequest, this);

        } catch (Exception e) {
            Log.e("GPS", "" + e);
        }
    }

    @Override
	public void onConnectionFailed(ConnectionResult pResult)  { }

    @Override
    public void onDisconnected() { }


    /************** LocationListener */
    @Override
    public void onLocationChanged(Location location) {

        short distance = 0;

        //TODO: calculate this
        int calories = 0;

        if (mLocationOld != null
                && mIsWritingWatchEnable) {

            // Compute for distance
            distance = (short)Math.abs(mLocationOld.distanceTo(location));

            // Send watch gps values
            if (BluetoothAgent.getInstance(null) != null) {
                Protocol p = new Protocol(BluetoothAgent.getInstance(null), null);
                p.sendGPSInfo(
                        location,
                        distance,
                        calories);
            }

            // Logs on GPS

             Log.d("GPS", "Location XLA:" + location.getLatitude());
             Log.d("GPS", "Location XLO:" + location.getLongitude());


            // Logging Altitude
             Log.d("GPS", "Location Altitiude: " + location.getAltitude());
        }

        mLocationOld  = location;
    }


    /************** OnMyLocationChangeListener */ // no implementation
//    @Override
//    public void onMyLocationChange(Location arg0) { }
    /***************************************/

    // button functions directly from layout
    public void changeValue(View view) {

        ((FragmentLeft3) mCurrentFragment).changeValue(view);
    }

    public void addGrid(View view) {
        ((FragmentLeft3) mCurrentFragment).addGrid(view);
    }

    public void removeGrid(View view) {
        ((FragmentLeft3) mCurrentFragment).removeGrid(view);
    }


//--------------------------------------------------------------------------------------------------- (Sports Mode & Daily Target)
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
        int sports_grid = MTOTAL_GRID - 2;// temp

        int[] sports_grids = new int[5];
        sports_grids[0] = 0;

        // for testing
        sports_grids[1] = mArrGridData[1];
        sports_grids[2] = mArrGridData[2];
        sports_grids[3] = mArrGridData[3];
        sports_grids[4] = mArrGridData[4];

        // TODO: fill the preference with read goals set from UI and call this
        // function when changed (in HomeActivity)
        int[] goals = new int[3];

        goals[0] = PreferencesManager.getInstance().retrieveDataForInt( "sports_goals.steps", 1000 );
        goals[1] = PreferencesManager.getInstance().retrieveDataForInt( "sports_goals.distance", 2000 );
        goals[2] = PreferencesManager.getInstance().retrieveDataForInt( "sports_goals.calories", 500 );


        // 50 kg
        String defaultWeight = "110";
        // 5'6 on feet
        String defaultHeight = "67";

        String weightStr = PreferencesManager.getInstance().retrieveDataForString( Constants.PREFKEY_USER_WEIGHT );

        if ( weightStr.equals( "NO VALUE" ) )
            weightStr = defaultWeight;

        if (weightStr.equals("")) {
            // Set default value if we get blank
            weightStr = defaultWeight;
        } else {
            // Convert values
            try {
                weightStr = ""
                        + KreyosUtility.convertPoundsToKilograms(Integer.parseInt(weightStr));
            } catch (Exception ex) {
                weightStr = defaultHeight;
            }
        }


        String heightStr = PreferencesManager.getInstance().retrieveDataForString( Constants.PREFKEY_USER_HEIGHT );

        if ( weightStr.equals( "NO VALUE" ) )
            weightStr = defaultHeight;

        if (heightStr.equals("")) {
            // Set default value if we get blank
            heightStr = defaultHeight;
        } else {
            // Convert values
            try {
                heightStr = ""
                        + KreyosUtility.convertInchestoCentimeters(Integer.parseInt(heightStr));
            } catch (Exception ex) {
                heightStr = defaultHeight;
            }

        }

        // Convert to integer to write on watch
        int weight = Integer.parseInt(weightStr);
        int height = Integer.parseInt(heightStr);

        // TODO: fill the preference with read goals set from UI and call this
        // function when changed (in GestureActivity)
        boolean enableGesture = PreferencesManager.getInstance().retrieveDataForBoolean( "gesture.enable", true );
        boolean isLeftHandGesture = PreferencesManager.getInstance().retrieveDataForBoolean("gesture.watch_hand", true);

        int[] actionsTable = new int[4];
        actionsTable[0] = PreferencesManager.getInstance().retrieveDataForInt( "gesture.swipe_left", 0 );
        actionsTable[1] = PreferencesManager.getInstance().retrieveDataForInt( "gesture.swipe_right", 1 );
        actionsTable[2] = PreferencesManager.getInstance().retrieveDataForInt( "gesture.twist_left", 2 );
        actionsTable[3] = PreferencesManager.getInstance().retrieveDataForInt( "gesture.twist_right", 3 );

        boolean isUkUnit = !(PreferencesManager.getInstance().retrieveDataForBoolean( "user_profile.is_metric", true ));

        BluetoothManager.getInstance().syncDeviceConfig(worldClockTable, worldClockOffsets, isDigit, analog,
                digit, sports_grid, sports_grids, goals, weight, height,
                enableGesture, isLeftHandGesture, actionsTable, isUkUnit);
    }

//--------------------------------------------------------------------------------------------------- (Date & Time)
    // listeners
    public void onUpdateWatchTimeFromInput() {

        Protocol p = new Protocol( BluetoothAgent.getInstance(null), this );
        p.syncTimeFromInput( mDate, mTime );

        KreyosUtility.showInfoMessage( this,
                                       getString( R.string.dialog_title_update_watch ),
                                       getString( R.string.dialog_msg_watch_updated ) );

    }

    public void onDateClick() {
        showDialog( Constants.DIALOG_ID_DATE );
    }

    public void onTimeClick() {
        showDialog( Constants.DIALOG_ID_TIME );
    }

    @Override
    protected Dialog onCreateDialog(int id) {

        switch (id) {


            case Constants.DIALOG_ID_BIRTHDAY: {
                Profile profile = new Profile();
                profile.loadDataFromPreferences();

                int month, day, year;

                String[] birthdayValue = profile.BIRTHDAY.split("/");
                if (birthdayValue.length == 3) {
                    month   = Integer.parseInt(birthdayValue[1]);
                    if( month < 0) { month = 0; }
                    day     = Integer.parseInt(birthdayValue[0]);
                    year    = Integer.parseInt(birthdayValue[2]);
                } else {
                    break;
                }

                return new DatePickerDialog(this, mDateSetListener, year, month-1, day);
            }
            case Constants.DIALOG_ID_DATE:
                return new DatePickerDialog(this, mDateSetListener, 2014, 8, 20);
            case Constants.DIALOG_ID_TIME:
                return new TimePickerDialog(this, mTimeSetListener, 4, 56, false);
            case Constants.DIALOG_ID_ALARM: {
                Calendar ca = KreyosUtility.calendar();
                return new TimePickerDialog(this, m_alarmTimeSetListener, ca.get(Calendar.HOUR_OF_DAY), ca.get(Calendar.MINUTE), false);
            }

        }
        return super.onCreateDialog(id);
    }

    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int pYear, int pMonth, int pDay) {

            mDate = null;
            mDate = new Date(pYear-1900, pMonth, pDay);

            CharSequence dateStr = DateFormat.format("MMMM dd, yyyy", mDate);

            if ( mCurrentFragment == null )
                return;

            if ( mCurrentFragment instanceof FragmentRight2 ) {
                ((FragmentRight2)mCurrentFragment).setDateText( dateStr );

                // personal info
            } else if ( mCurrentFragment instanceof FragmentRight5 ) {
                    ((FragmentRight5)mCurrentFragment).updateBirthdayField("" + pDay + "/" + (pMonth+1) + "/" + pYear);
            }
        }
    };

    private TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mTime.setHours(hourOfDay);
            mTime.setMinutes(minute);

            Log.d("LOG", "H " + hourOfDay + " M " + minute);

            String amPmLabel = "AM";
            String hourLabel = "" + hourOfDay;
            String minuteLabel = "" + minute;

            if( hourOfDay > 11 )
            {
                amPmLabel = "PM";
            }
            if( hourOfDay % 12 == 0  )
            {
                hourOfDay = 12;
            }
            else
            {
                hourOfDay = hourOfDay % 12;
            }
            hourLabel = "" + hourOfDay;

            if( hourOfDay < 10 )
            {
                hourLabel = "0" + hourLabel;
            }
            if( minute < 10 )
            {
                minuteLabel = "0" + minuteLabel;
            }

            if (   mCurrentFragment != null
                && mCurrentFragment instanceof FragmentRight2 ) {
                ((FragmentRight2)mCurrentFragment).setTimeText( hourLabel + ":" + minuteLabel + " " + amPmLabel );
            }
        }

    };


//--------------------------------------------------------------------------------------------------- (Silent Alarms)
    public void onAlarmTimeClick() {
        showDialog( Constants.DIALOG_ID_ALARM );
    }

    private TimePickerDialog.OnTimeSetListener m_alarmTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

            if (   mCurrentFragment != null
                && mCurrentFragment instanceof FragmentRight3 )
                ((FragmentRight3)mCurrentFragment).setWatchAlarm( hourOfDay, minute );
        }
    };

//--------------------------------------------------------------------------------------------------- Personal Information
    public void onBirthdayClick() {
        showDialog( Constants.DIALOG_ID_BIRTHDAY );
    }

    public void onWeightClick( float pWeightOrigValue ) {
        KreyosDialogManager.getInstance().showWeightSlider( pWeightOrigValue );
    }

    public void onHeightClick() {
        KreyosDialogManager.getInstance().showHeightSlider();
    }

    public void setWeightValue ( String pText, float pValue ) {
        if ( mCurrentFragment == null )
            return;

        if ( mCurrentFragment instanceof FragmentRight5 ) {
            ((FragmentRight5)mCurrentFragment).updateWeightValue( pValue );
            ((FragmentRight5)mCurrentFragment).updateWeightField( pText );
        }
    }

    public void setHeightValue ( String pTextInches, String pTextFeet, float pValue ) {
        if ( mCurrentFragment == null )
            return;

        if ( mCurrentFragment instanceof FragmentRight5 ) {
            ((FragmentRight5)mCurrentFragment).updateHeightField( pTextInches, pTextFeet, pValue );
        }
    }

    public void loadProfile() {

        if ( mCurrentFragment == null )
            return;

        if ( !(mCurrentFragment instanceof FragmentRight5) )
            return;

        FragmentRight5 fragment = (FragmentRight5)mCurrentFragment;

        Profile profile = new Profile();
        profile.loadDataFromPreferences();

        fragment.updateFirstNameField( profile.FIRSTNAME );
        fragment.updateLastNameField( profile.LASTNAME );
        fragment.updateBirthdayField( profile.BIRTHDAY );
        fragment.updateGenderField( profile.GENDER.toUpperCase() );

        int heightValue =  0;
        int weightValue =  0;

        try {
            heightValue = Math.round(Float.parseFloat(profile.HEIGHT));
            weightValue = Math.round(Float.parseFloat(profile.WEIGHT));
        } catch (Exception e) {
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
        fragment.updateWeightField( "" + weightValue + " Lbs" );
        fragment.updateHeightField( "" + ftInInches + " In",
                                    "" + ft + " Ft", heightValue);

        // Set weight and height value
//        m_weightInLbsValue 	= weightValue;
    }

    public void updateProfile(String pFirstNameText, String pLastNameText, String pBirthdayText,
                              byte pGenderValue, String pHeightValue, String pWeightValue) {

        boolean haveErrors = false;

        pBirthdayText = pBirthdayText.replace("'\'", "");

        Log.d(Constants.TAG_DEBUG, "( ACTIVITY:MAIN ) - create account"
                        + "\n\t\t\t\t\t\t first name --- " + pFirstNameText
                        + "\n\t\t\t\t\t\t last name --- " + pLastNameText
                        + "\n\t\t\t\t\t\t birthday name --- " + pBirthdayText
                        + "\n\t\t\t\t\t\t g --- " + pGenderValue
                        + "\n\t\t\t\t\t\t height --- " + pHeightValue
                        + "\n\t\t\t\t\t\t weight --- " + pWeightValue );

        mCurrentFragment.enableViews(false);

        // connection err
        if (!KreyosUtility.hasConnection(this)) {
            KreyosDialogManager.getInstance().showErrorDialog( getString(R.string.dialog_title_error_connection),
                                                               getString(R.string.dialog_msg_error_connection));
            mCurrentFragment.enableViews(true);
            return;
        }

        if(     pFirstNameText.length() < 1
           ||   pLastNameText.length() < 1
           ||   pBirthdayText.length() < 1
           ||   pHeightValue.length() < 1
           ||   pWeightValue.length() < 1) {
            KreyosDialogManager.getInstance().showErrorDialog( getString(R.string.dialog_title_error),
                                                               getString(R.string.actlogin_dialog_msg_error_fill_out));
            mCurrentFragment.enableViews(true);
            return;
        }

        String genderText;
        if (pGenderValue == Constants.GENDER_MALE)
            genderText = "Male";
        else
            genderText = "Female";

        Profile profile = new Profile();
        profile.loadDataFromPreferences();

        // Update profile on web
        try {
            JSONObject params = new JSONObject();

            params.put("first_name", 	pFirstNameText);
            params.put("last_name", 	pLastNameText);
            params.put("birthday", 		pBirthdayText);
            params.put("gender", 		genderText);
            params.put("weight", 		pWeightValue);
            params.put("height", 		pHeightValue);

            JSONObject user = new JSONObject();
            user.put("user", params);
            user.put("email", 		profile.EMAIL);
            user.put("auth_token", 	profile.KREYOS_TOKEN);

            String response =  RequestManager.instance().put( Constants.PREFKEY_URL_USER_UPDATE, user);
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
                KreyosDialogManager.getInstance().showInfoDialog(getString(R.string.dialog_title_update_watch),
                        getString(R.string.dialog_msg_info_updated));

                // Success on web next update on local
                profile 			= new Profile();
                profile.FIRSTNAME 	= pFirstNameText;
                profile.LASTNAME 	= pLastNameText;
                profile.BIRTHDAY 	= pBirthdayText;
                profile.GENDER		= genderText;
                profile.WEIGHT 		= pWeightValue;
                profile.HEIGHT 		= pHeightValue;
                profile.saveDataToPreferences();
            }

        } catch(Exception ex) {
            ex.printStackTrace();
            KreyosDialogManager.getInstance().showErrorDialog(getString(R.string.dialog_title_error),
                    getString(R.string.dialog_msg_error_info_update));
        }

    }

    public void loadProfileImage(ImageView pImageAvatar) {

        String profileImage;
        Bitmap profileBitmap = null;

        // local only

        profileImage = PreferencesManager.getInstance().retrieveDataForString( Constants.PREFKEY_USER_IMAGE );
        if (profileImage.equals("NO VALUE")) { return; }

        try {
            profileBitmap = KreyosUtility.getScaledBitmap(profileImage, 800, 800);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (profileBitmap == null) {
            Log.d(Constants.TAG_DEBUG, "( ACTIVITY:MAIN ) - No Photo");
            return;
        }

        try {
            pImageAvatar.setImageBitmap(Bitmap.createScaledBitmap(profileBitmap, 800, 800, false));
            Log.d(Constants.TAG_DEBUG, "( ACTIVITY:MAIN ) - Load Photo");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
