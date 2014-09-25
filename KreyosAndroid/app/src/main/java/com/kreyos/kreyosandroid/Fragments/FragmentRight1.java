package com.kreyos.kreyosandroid.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.kreyos.kreyosandroid.R;
import com.kreyos.kreyosandroid.activities.MainActivity;
import com.kreyos.kreyosandroid.bluetooth.BluetoothAgent;
import com.kreyos.kreyosandroid.bluetooth.KreyosService;
import com.kreyos.kreyosandroid.managers.BluetoothManager;
import com.kreyos.kreyosandroid.managers.PreferencesManager;
import com.kreyos.kreyosandroid.utilities.Constants;
import com.kreyos.kreyosandroid.utilities.KreyosUtility;

/**
 * ACTIVE WATCH
 */

public class FragmentRight1 extends BaseFragmentMain
        implements
        View.OnClickListener {

//--------------------------------------------------------------------------------------------------- Variables
    private boolean     mBSavedConnection               = false;
    private boolean     mBNeedToUnlock                  = false;

    private TextView    mTextWatchStatus                = null;
    private Button      mBtnWatchConnectionTrigger      = null;

    private ProgressDialog mTempDialog                  = null;

//--------------------------------------------------------------------------------------------------- onCreate
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:ActiveWatch )");

        return inflater.inflate(R.layout.activity_fragment_right_1, container, false);
    }

    @Override
    public void onStart() {
        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:ActiveWatch ) - On Start");

        super.onStart();

        // setup fonts
        KreyosUtility.overrideFonts( this.getActivity().getBaseContext(),
                                     getView(),
                                     Constants.FONT_NAME.LEAGUE_GOTHIC_REGULAR);

        // setup views
        setupViewsAndCallbacks();

        initialize();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

    }

    private void setupViewsAndCallbacks() {

        mTextWatchStatus            = (TextView) getView().findViewById( R.id.txt_watch_connected );
        mBtnWatchConnectionTrigger  = (Button) getView().findViewById( R.id.btn_connect_device );
        mBtnWatchConnectionTrigger.setOnClickListener( this );
    }

    private void initialize() {

        if (PreferencesManager.getInstance().containsKey( Constants.PREFKEY_BT_DEVICE_NAME )) {
            mBSavedConnection = true;
        }

        // Check watch connection/status
        updateTextAndButtonLabel();

        mTempDialog = new ProgressDialog( getActivity() );
        mTempDialog.setCancelable(false);


    }



//--------------------------------------------------------------------------------------------------- Button functions
    @Override
    public void onClick(View pView) {
        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:ActiveWatch ) - button pressed");

        if ( pView.getId() == R.id.btn_connect_device ) {
            enableViews( false );

            if ( BluetoothManager.getInstance().getDeviceState() == Constants.WATCH_STATE.CONNECTED )
                disconnectWatch();
            else
                connectWatch();
        }
    }

    @Override
    public void enableViews (boolean pEnabled) {
        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:ActiveWatch ) - " + (pEnabled ? "enabled" : "disabled") );

        super.enableViews(pEnabled);
        mBtnWatchConnectionTrigger.setClickable(pEnabled);
    }

//--------------------------------------------------------------------------------------------------- Check connections
    private void updateTextAndButtonLabel() {

        // Check watch connection/status
        String connectedDeviceName = BluetoothManager.getInstance().getConnectedDeviceName();

        if (   BluetoothManager.getInstance().getDeviceState() == Constants.WATCH_STATE.CONNECTED
            && !connectedDeviceName.equals("") ) {
            mTextWatchStatus.setText( "WATCH CONNECTED: " + connectedDeviceName );
            mBtnWatchConnectionTrigger.setText( getString(R.string.fragmentR1_btn_disconnect_device) );

        } else {
            mTextWatchStatus.setText("WATCH CONNECTED: NONE");
            mBtnWatchConnectionTrigger.setText( getString(R.string.fragmentR1_btn_connect_device) );
        }
    }

    private void disconnectWatch() {

        BluetoothManager.getInstance().unpairConnectedDevice();
    }

    public void onUnpairDeviceFinish() {
        mBSavedConnection = false;
        enableViews( true );
        updateTextAndButtonLabel();
    }

    private void connectWatch() {

        mBNeedToUnlock = true;

        // reset BT Message Handler & init BT Agent
        BluetoothManager.getInstance().resetBluetoothConnection();

        enableViews( true );
    }

    public void onBluetoothHeadsetConnected() {

        if (!mBSavedConnection) {
            BluetoothManager.getInstance().restartBTAgentSession();
            mBSavedConnection = true;
            return;
        }

        updateTextAndButtonLabel();

        if ( mBNeedToUnlock ) {
            Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:ActiveWatch ) - Unlocking Watch" );

            BluetoothManager.getInstance().unlockDevice();

            // Sync watch config
            ( (MainActivity)getActivity()).writeWatchUIConfig();

            mBNeedToUnlock = false;
        }



    }

}
