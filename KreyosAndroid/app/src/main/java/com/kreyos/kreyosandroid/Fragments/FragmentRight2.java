package com.kreyos.kreyosandroid.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.kreyos.kreyosandroid.R;
import com.kreyos.kreyosandroid.managers.BluetoothManager;
import com.kreyos.kreyosandroid.managers.PreferencesManager;
import com.kreyos.kreyosandroid.utilities.Constants;
import com.kreyos.kreyosandroid.utilities.KreyosUtility;

import java.lang.ref.WeakReference;
import java.util.Calendar;

/**
 * DATE AND TIME
 */

public class FragmentRight2 extends BaseFragmentMain
        implements
        View.OnClickListener {

//--------------------------------------------------------------------------------------------------- Variables
    // UI
    private ImageView           mBtnSetAuto             = null;
    private TextView            mBtnDate                = null;
    private TextView            mBtnTime                = null;
    private Button              mBtnUpdate              = null;
    private ImageView           mDividerDateAndTime     = null;

    private static final int    MSG_TIME_SYNC           = 1;
    private boolean             mBAutoOn                = false;
    private boolean             mBStopDataTimeUpdate    = false;

    private TimeSyncHandler     msgHandler              = null;
    private TimeSyncThread      workThread              = null;

//--------------------------------------------------------------------------------------------------- onCreate
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:Date & Time )");

        return inflater.inflate(R.layout.activity_fragment_right_2, container, false);
    }

    @Override
    public void onStart() {
        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:Date & Time ) - onStart");

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
    public void onDestroy() {
        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:Date & Time ) - ON DESTROY");

        super.onDestroy();

        if ( workThread != null ) {
            workThread.interrupt();
            workThread = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }



    private void setupViewsAndCallbacks() {
        mBtnSetAuto = (ImageView) getView().findViewById( R.id.image_switch_on_off );
        mBtnDate    = (TextView) (getView().findViewById( R.id.layout_time_date )).findViewById( R.id.text_date );
        mBtnTime    = (TextView) (getView().findViewById( R.id.layout_time_date )).findViewById( R.id.text_time );
        mBtnUpdate  = (Button) getView().findViewById( R.id.btn_update_watch );
        mDividerDateAndTime = (ImageView) getView().findViewById( R.id.divider_date_time );

        mBtnSetAuto.setOnClickListener( this );
        mBtnDate.setOnClickListener( this );
        mBtnTime.setOnClickListener( this );
        mBtnUpdate.setOnClickListener( this );
    }

    private void initialize() {

        mBAutoOn = PreferencesManager.getInstance().retrieveDataForBoolean( Constants.PREFKEY_AUTO_SYNC_TIME, true );

        if ( mBAutoOn ) {
            mBtnSetAuto.setImageResource(R.drawable.btn_img_switch_on);
            mBtnDate.setVisibility( View.INVISIBLE );
            mBtnTime.setVisibility( View.INVISIBLE );
            mDividerDateAndTime.setVisibility( View.INVISIBLE );

        } else {
            mBtnSetAuto.setImageResource(R.drawable.btn_img_switch_off);
            mBtnDate.setVisibility( View.VISIBLE );
            mBtnTime.setVisibility( View.VISIBLE );
            mDividerDateAndTime.setVisibility( View.VISIBLE );
        }

        Calendar calendar       = KreyosUtility.calendar();
        CharSequence dateStr    = DateFormat.format("MMMM dd, yyyy", calendar);
        CharSequence timeStr    = DateFormat.format("hh:mm aa", calendar);

        mBtnDate.setText(dateStr);
        mBtnTime.setText(timeStr);

        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:Date & Time ) - init \""+ dateStr +"\" " + timeStr);

        // thread to update time
        msgHandler = new TimeSyncHandler(this);
        workThread = new TimeSyncThread(msgHandler);
        if (workThread.getState() == Thread.State.NEW)
            workThread.start();
    }



//--------------------------------------------------------------------------------------------------- Enabler
    @Override
    public void enableViews (boolean pEnabled) {
        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:Date & Time ) - " + (pEnabled ? "enabled" : "disabled") );

        super.enableViews(pEnabled);
        mBtnUpdate.setClickable(pEnabled);
    }



//--------------------------------------------------------------------------------------------------- Time Sync
    public void handleTimeSync(Message pMessage) {

        if ( mBStopDataTimeUpdate )
            return;

        Calendar calendar       = KreyosUtility.calendar();
        CharSequence dateStr    = DateFormat.format("MMMM dd, yyyy", calendar);
        CharSequence timeStr    = DateFormat.format("hh:mm aa", calendar);

        mBtnDate.setText(dateStr);
        mBtnTime.setText(timeStr);

        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:Date & Time ) - handle Time Sync \""+ dateStr +"\" " + timeStr);

    }

    private class TimeSyncThread extends Thread {

        private TimeSyncHandler msgHandler;

        public TimeSyncThread(TimeSyncHandler h) {
            msgHandler = h;
        }

        public void run() {
            while (true) {
                msgHandler.obtainMessage(MSG_TIME_SYNC, null).sendToTarget();
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    private static class TimeSyncHandler extends Handler {
        private final WeakReference< FragmentRight2 > mFragment;

        public TimeSyncHandler(FragmentRight2 pFragment) {
            mFragment = new WeakReference<FragmentRight2> (pFragment);
        }

        @Override
        public void handleMessage(Message msg)
        {
            FragmentRight2 fragment = mFragment.get();
            if (fragment != null) {
                fragment.handleTimeSync(msg);
            }
        }
    }



//--------------------------------------------------------------------------------------------------- Button functions
    @Override
    public void onClick(View pView) {
        switch (pView.getId()) {
            case R.id.image_switch_on_off :
                Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:Date & Time ) - switch");
                setAuto( mBAutoOn, pView );
                break;

            case R.id.text_date :
                mListener.onDateClick();
                break;

            case R.id.text_time :
                mListener.onTimeClick();
                break;

            case R.id.btn_update_watch :
                updateWatch();
                break;
        }
    }

    private void setAuto(boolean pBAuto, View pView) {

        if (BluetoothManager.getInstance().getDeviceState() == Constants.WATCH_STATE.CONNECTED) {

            mBAutoOn = !pBAuto;
            PreferencesManager.getInstance().saveDataBoolean( Constants.PREFKEY_AUTO_SYNC_TIME, mBAutoOn );

            Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:Date & Time ) - Set Auto: " + mBAutoOn);

            if ( mBAutoOn ) {
                ((ImageView)pView).setImageResource( R.drawable.btn_img_switch_on );
                mBtnDate.setVisibility( View.INVISIBLE );
                mBtnTime.setVisibility( View.INVISIBLE );
                mDividerDateAndTime.setVisibility( View.INVISIBLE );
                BluetoothManager.getInstance().syncTime();

            } else {
                ((ImageView)pView).setImageResource( R.drawable.btn_img_switch_off );
                mBtnDate.setVisibility( View.VISIBLE );
                mBtnTime.setVisibility( View.VISIBLE );
                mDividerDateAndTime.setVisibility( View.VISIBLE );
            }

        } else {
            KreyosUtility.showErrorMessage( getActivity(),
                                            getString( R.string.dialog_title_device_not_found ),
                                            getString( R.string.dialog_msg_connect_watch ));

        }
    }

    private void updateWatch() {

        if ( BluetoothManager.getInstance().getDeviceState() == Constants.WATCH_STATE.CONNECTED ) {

            if ( !mBAutoOn )
                mListener.onUpdateWatchTimeFromInput();

        } else {
            KreyosUtility.showErrorMessage( getActivity(),
                                            getString( R.string.dialog_title_device_not_found ),
                                            getString( R.string.dialog_msg_connect_watch ));
        }
    }


//--------------------------------------------------------------------------------------------------- Accessed by Main Activity
    public void setDateText (CharSequence pDateStr) {
        mBStopDataTimeUpdate = true;
        mBtnDate.setText( pDateStr );
    }

    public void setTimeText (String pTimeStr) {
        mBStopDataTimeUpdate = true;
        mBtnTime.setText(pTimeStr);
    }


}

