package com.kreyos.kreyosandroid.fragments;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.kreyos.kreyosandroid.R;
import com.kreyos.kreyosandroid.managers.BluetoothManager;
import com.kreyos.kreyosandroid.managers.PreferencesManager;
import com.kreyos.kreyosandroid.utilities.Constants;
import com.kreyos.kreyosandroid.utilities.KreyosUtility;

/**
 * SETTINGS / NOTIFICATION
 */

public class FragmentLeft5 extends BaseFragmentMain
        implements
        View.OnClickListener {

//--------------------------------------------------------------------------------------------------- Variables



//--------------------------------------------------------------------------------------------------- onCreate
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:Settings )");

        return inflater.inflate(R.layout.activity_fragment_left_5, container, false);
    }

    @Override
    public void onStart() {
        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:Settings ) - On Start");

        super.onStart();

        // setup fonts
        KreyosUtility.overrideFonts( this.getActivity().getBaseContext(),
                                     getView(),
                                     Constants.FONT_NAME.LEAGUE_GOTHIC_REGULAR);

        // setup views
        setupViewsAndCallbacks();

        initialize();
    }

    private void setupViewsAndCallbacks() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }

    private void initialize() {
        initNotificationSettingEntry(R.id.layoutIncomingCall,       R.id.btnIncomingCall,       "notification.call");
        initNotificationSettingEntry(R.id.layoutWeather,            R.id.btnWeather,            "notification.weather");
        initNotificationSettingEntry(R.id.layoutTwitter,            R.id.btnTwitter,            "notification.twitter");
        initNotificationSettingEntry(R.id.layoutReminder,           R.id.btnReminder,           "notification.reminder");

        initNotificationSettingEntry(R.id.layoutEMail,              R.id.btnEmail,              "notification.email");
        initNotificationSettingEntry(R.id.layoutSMS,                R.id.btnSMS,                "notification.sms");
        initNotificationSettingEntry(R.id.layoutFacebook,           R.id.btnFacebook,           "notification.facebook");
        initNotificationSettingEntry(R.id.layoutLowBattery,         R.id.btnLowBattery,         "notification.low_battery");
    }


//--------------------------------------------------------------------------------------------------- Initialize
    private void initNotificationSettingEntry(final int pLayoutId, final int pBtnId, final String pConfKey) {
        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:Settings ) - Init Notif Settings Entry: \"" + pConfKey + "\"");

        RelativeLayout layout = (RelativeLayout)getView().findViewById(pLayoutId);
        ImageView      button = (ImageView)     getView().findViewById(pBtnId);

        final boolean enabled = PreferencesManager.getInstance().retrieveDataForBoolean( pConfKey, false );

        button.setOnClickListener(new View.OnClickListener() {

            private boolean switchFlag = enabled;

            @Override
            public void onClick(View v)
            {
                // + ET 04292014 : FB NOTIF CHECKER
                if(v.getId() == R.id.btnFacebook
                        || v.getId() == R.id.btnEmail
                        || v.getId() == R.id.btnTwitter
                        || v.getId() == R.id.btnReminder)
                {
                    if ( android.os.Build.VERSION.SDK_INT > 17 )
                    {
                        ContentResolver contentResolver = getActivity().getApplicationContext().getContentResolver();
                        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
                        String packageName = getActivity().getApplicationContext().getPackageName();

                        // check to see if the enabledNotificationListeners String contains our package name
                        if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName))
                        {
                            // not on
                            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                            return;
                        }
                    }
                    else
                    {
                        KreyosUtility.showErrorMessage( getActivity(),
                                                        getActivity().getString(R.string.dialog_title_error),
                                                        getActivity().getString(R.string.dialog_msg_update_api));
                        return;
                    }
                }

                ImageView image = (ImageView)v;
                switchFlag = !switchFlag;
                if (switchFlag)
                {
                    image.setImageResource(R.drawable.btn_img_switch_on);
                    startServiceByLayoutId(pLayoutId);
                }
                else
                {
                    image.setImageResource(R.drawable.btn_img_switch_off);
                    stopServiceByLayoutId(pLayoutId);
                }

                PreferencesManager.getInstance().saveDataBoolean( pConfKey, switchFlag );
            }
        });

        if (enabled) {
            button.setImageResource(R.drawable.btn_img_switch_on);
            startServiceByLayoutId(pLayoutId);
        }
        else {
            button.setImageResource(R.drawable.btn_img_switch_off);
            stopServiceByLayoutId(pLayoutId);
        }
    }

    private void startServiceByLayoutId(final int pLayoutId) {

        switch (pLayoutId) {
            case R.id.layoutIncomingCall:
                BluetoothManager.getInstance().notifStartCallReceiver();
                break;
            case R.id.layoutSMS:
                BluetoothManager.getInstance().notifStartSMSReceiver();
                break;
            case R.id.layoutEMail:

                break;
            case R.id.layoutWeather:
                BluetoothManager.getInstance().notifStartWeather();
                break;
            case R.id.layoutFacebook:
                BluetoothManager.getInstance().notifStartFacebook();
                break;
            case R.id.layoutTwitter:
                BluetoothManager.getInstance().notifStartTwitter();
                break;
            case R.id.layoutReminder:

                break;
            case R.id.layoutLowBattery:
                BluetoothManager.getInstance().notifStartLowBattery();
                break;

        }
    }

    private void stopServiceByLayoutId(final int pLayoutId) {

        switch (pLayoutId) {
            case R.id.layoutIncomingCall:
                BluetoothManager.getInstance().notifStopCallReceiver();
                break;
            case R.id.layoutSMS:
                BluetoothManager.getInstance().notifStopSMSReceiver();
                break;
            case R.id.layoutEMail:

                break;
            case R.id.layoutWeather:
                BluetoothManager.getInstance().notifStopWeather();
                break;
            case R.id.layoutFacebook:
                BluetoothManager.getInstance().notifStopFacebook();
                break;
            case R.id.layoutTwitter:
                BluetoothManager.getInstance().notifStopTwitter();
                break;
            case R.id.layoutReminder:

                break;
            case R.id.layoutLowBattery:
                BluetoothManager.getInstance().notifStopLowBattery();
                break;

        }
    }


//--------------------------------------------------------------------------------------------------- Button functions
    @Override
    public void onClick(View pView) {

    }
}

