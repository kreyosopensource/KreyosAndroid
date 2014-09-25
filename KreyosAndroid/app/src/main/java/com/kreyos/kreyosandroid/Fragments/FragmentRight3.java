package com.kreyos.kreyosandroid.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
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

/**
 * SILENT ALARMS
 */

public class FragmentRight3 extends BaseFragmentMain
        implements
        View.OnClickListener {

//--------------------------------------------------------------------------------------------------- Variables
    // UI
    private Button mBtnUpdateWatch  = null;
    private int mAlarmIndexSelected = 0;


//--------------------------------------------------------------------------------------------------- onCreate
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:Alarms )");

        return inflater.inflate(R.layout.activity_fragment_right_3, container, false);
    }

    @Override
    public void onStart() {
        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:Alarms ) - onStart");

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
        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:Alarms ) - ON DESTROY");

        super.onDestroy();

        // nullify all
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void enableViews (boolean pEnabled) {
        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:Alarms ) - " + (pEnabled ? "enabled" : "disabled") );

        super.enableViews(pEnabled);
        mBtnUpdateWatch.setClickable(pEnabled);
    }



    private void setupViewsAndCallbacks() {
        mBtnUpdateWatch = (Button) getView().findViewById( R.id.btn_update_watch_alarm );
        mBtnUpdateWatch.setOnClickListener( this );
    }

    private void initialize() {

        /*********/
        syncWatchAlarm(); //sync the alarm at the beginning to ensure the alarm is set correct


        loadWatchAlarm("alarm_1", R.id.text_mode_alarm_1, R.id.text_time_alarm_1, R.id.btn_switch_vibrate_alarm_1);
        loadWatchAlarm("alarm_2", R.id.text_mode_alarm_2, R.id.text_time_alarm_2, R.id.btn_switch_vibrate_alarm_2);
        loadWatchAlarm("alarm_3", R.id.text_mode_alarm_3, R.id.text_time_alarm_3, R.id.btn_switch_vibrate_alarm_3);

        setSubActivityEntry(R.id.layout_alarm_1 );
        setSubActivityEntry(R.id.layout_alarm_2 );
        setSubActivityEntry(R.id.layout_alarm_3 );
    }

    private void syncWatchAlarm() {

        boolean enableAlarm1 = PreferencesManager.getInstance().retrieveDataForBoolean( "alarm_1.enable", false );
        boolean enableAlarm2 = PreferencesManager.getInstance().retrieveDataForBoolean( "alarm_2.enable", false );
        boolean enableAlarm3 = PreferencesManager.getInstance().retrieveDataForBoolean( "alarm_3.enable", false );

        BluetoothManager.getInstance().setWatchAlarm(   0, enableAlarm1 ? 0x04 : 0x01,
                                                        0, 0,
                                                        PreferencesManager.getInstance().retrieveDataForInt( "alarm_1.hour", 0 ),
                                                        PreferencesManager.getInstance().retrieveDataForInt( "alarm_1.minute", 0 ));
        BluetoothManager.getInstance().setWatchAlarm(   1, enableAlarm2 ? 0x04 : 0x01,
                                                        0, 0,
                                                        PreferencesManager.getInstance().retrieveDataForInt( "alarm_2.hour", 0 ),
                                                        PreferencesManager.getInstance().retrieveDataForInt( "alarm_2.minute", 0 ));
        BluetoothManager.getInstance().setWatchAlarm(   2, enableAlarm1 ? 0x04 : 0x01,
                                                        0, 0,
                                                        PreferencesManager.getInstance().retrieveDataForInt( "alarm_3.hour", 0 ),
                                                        PreferencesManager.getInstance().retrieveDataForInt( "alarm_3.minute", 0 ));

        checkIfSliderEnable( enableAlarm1, R.id.text_time_alarm_1);
        checkIfSliderEnable( enableAlarm2, R.id.text_time_alarm_2 );
        checkIfSliderEnable( enableAlarm3, R.id.text_time_alarm_3 );
    }

    private void loadWatchAlarm(final String confName, final int modeId, final int timeId, final int switchId) {
        TextView mode = (TextView) getView().findViewById(modeId);
        TextView time = (TextView) getView().findViewById(timeId);

        final int modeStrId     = R.string.fragmentR3_text_repeat_once;
        int hour 			    = PreferencesManager.getInstance().retrieveDataForInt( confName + ".hour", 0 );
        final int minute        = PreferencesManager.getInstance().retrieveDataForInt( confName + ".minute", 0 );
        final boolean enable    = PreferencesManager.getInstance().retrieveDataForBoolean( confName + ".enable", false );

//        SharedPreferences prefs = KreyosActivity.getPrefs();
//        final int modeStrId 	= R.string.repeatOnce;//alarmModeTable[prefs.getInt(confName + ".mode", 0)]; // R.string.everyday;//alarmModeTable[prefs.getInt(confName + ".mode", 0)];
//        int hour 			= prefs.getInt(confName + ".hour", 0);
//        final int minute 		= prefs.getInt(confName + ".minute", 0);
//        final boolean enable 	= prefs.getBoolean(confName + ".enable", false);

        mode.setText(modeStrId);
        if( hour > 12 )
        {
            hour = hour % 12;
        }
        else if( hour == 0)
        {
            hour = 12;
        }

        time.setText(String.format("%02d:%02d", hour, minute));

        checkIfSliderEnable(enable, timeId);
        initSwitchButton(switchId, enable);
    }

    public View setSubActivityEntry(final int viewId ) {

        View view = getView().findViewById(viewId);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlarmDialog(v);
            }
        });

        return view;
    }

    private void checkIfSliderEnable(boolean p_isAlarmEnable, int p_textId) {

        if (p_isAlarmEnable) {
            ((TextView)getView().findViewById(p_textId)).setTextColor(Color.BLACK);
        } else {
            ((TextView)getView().findViewById(p_textId)).setTextColor(Color.LTGRAY);
        }
    }

    private void initSwitchButton(final int btnId, final boolean enable) {

        ImageView btn = (ImageView) getView().findViewById(btnId);
        btn.setImageResource(enable ? R.drawable.btn_img_switch_on : R.drawable.btn_img_switch_off);
        btn.setOnClickListener(new View.OnClickListener() {

            private boolean switchFlag = enable;

            @Override
            public void onClick(View v) {

                ImageView btn   = (ImageView)v;
                switchFlag      = !switchFlag;
                btn.setImageResource(switchFlag ? R.drawable.btn_img_switch_on : R.drawable.btn_img_switch_off);

                switch (btnId) {
                    case R.id.btn_switch_vibrate_alarm_1:
                        PreferencesManager.getInstance().saveDataBoolean( "alarm_1.enable", switchFlag );
                        break;

                    case R.id.btn_switch_vibrate_alarm_2:
                        PreferencesManager.getInstance().saveDataBoolean( "alarm_2.enable", switchFlag );
                        break;

                    case R.id.btn_switch_vibrate_alarm_3:
                        PreferencesManager.getInstance().saveDataBoolean( "alarm_3.enable", switchFlag );
                        break;

                }

                syncWatchAlarm();
            }
        });
    }


    //--------------------------------------------------------------------------------------------------- Button functions
    @Override
    public void onClick(View pView) {
        switch ( pView.getId() ) {
            case R.id.btn_update_watch_alarm :
                updateWatch();
                break;

            default :
                break;
        }
    }

    private void updateWatch() {
        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:Alarms ) - Update Watch");

        if ( BluetoothManager.getInstance().getDeviceState() == Constants.WATCH_STATE.CONNECTED ) {
            syncWatchAlarm();

            loadWatchAlarm("alarm_1", R.id.text_mode_alarm_1, R.id.text_time_alarm_1, R.id.btn_switch_vibrate_alarm_1);
            loadWatchAlarm("alarm_2", R.id.text_mode_alarm_2, R.id.text_time_alarm_2, R.id.btn_switch_vibrate_alarm_2);
            loadWatchAlarm("alarm_3", R.id.text_mode_alarm_3, R.id.text_time_alarm_3, R.id.btn_switch_vibrate_alarm_3);

            KreyosUtility.showInfoMessage( getActivity(),
                                           getString( R.string.dialog_title_update_watch ),
                                           getString( R.string.dialog_msg_watch_updated ) );

        } else {
            KreyosUtility.showErrorMessage( getActivity(),
                                            getString( R.string.dialog_title_device_not_found ),
                                            getString( R.string.dialog_msg_connect_watch ));
        }
    }

    private void showAlarmDialog(View v)
    {


        if(   v.getId() == R.id.layout_alarm_1
           && PreferencesManager.getInstance().retrieveDataForBoolean("alarm_1.enable", false) )
        {
            mAlarmIndexSelected = 1;
            mListener.onAlarmTimeClick();
        }
        else if(v.getId() == R.id.layout_alarm_2
                && PreferencesManager.getInstance().retrieveDataForBoolean("alarm_2.enable", false) )
        {
            mAlarmIndexSelected = 2;
            mListener.onAlarmTimeClick();
        }
        else if(v.getId() == R.id.layout_alarm_3
                && PreferencesManager.getInstance().retrieveDataForBoolean("alarm_3.enable", false) )
        {
            mAlarmIndexSelected = 3;
            mListener.onAlarmTimeClick();
        }
    }

    // called by Main
    public void setWatchAlarm( int p_hour, int p_minute ) {
        String alarmKey = "";

        switch(mAlarmIndexSelected) {
            case 1: { alarmKey = "alarm_1"; } break;

            case 2: { alarmKey = "alarm_2"; } break;

            case 3: { alarmKey = "alarm_3"; } break;
        }

        PreferencesManager.getInstance().saveDataInt( alarmKey + ".hour",   p_hour );
        PreferencesManager.getInstance().saveDataInt( alarmKey + ".minute",   p_minute );


        switch(mAlarmIndexSelected) {
            case 1: {   loadWatchAlarm("alarm_1", R.id.text_mode_alarm_1, R.id.text_time_alarm_1, R.id.btn_switch_vibrate_alarm_1); } break;

            case 2: {   loadWatchAlarm("alarm_2", R.id.text_mode_alarm_2, R.id.text_time_alarm_2, R.id.btn_switch_vibrate_alarm_2); } break;

            case 3: {   loadWatchAlarm("alarm_3", R.id.text_mode_alarm_3, R.id.text_time_alarm_3, R.id.btn_switch_vibrate_alarm_3); } break;
        }


    }

}
