package com.kreyos.kreyosandroid.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;

import com.kreyos.kreyosandroid.R;
import com.kreyos.kreyosandroid.activities.MainActivity;
import com.kreyos.kreyosandroid.bluetooth.BluetoothAgent;
import com.kreyos.kreyosandroid.customcarouselslider.CarouselManager;
import com.kreyos.kreyosandroid.customcarouselslider.CarouselPageAdapter;
import com.kreyos.kreyosandroid.managers.BluetoothManager;
import com.kreyos.kreyosandroid.managers.PreferencesManager;
import com.kreyos.kreyosandroid.utilities.Constants;
import com.kreyos.kreyosandroid.utilities.KreyosUtility;

/**
 * DAILY TARGET
 */

public class FragmentLeft4 extends BaseFragmentMain
        implements
        View.OnClickListener {

    //---------------------------------------------------------------------------------- Variables
// + ET 040714 : Daily Target Slider variables
    SeekBar m_stepsSlider = null;
    short[] targetValues = new short[] {
            9000,
            79,
            23
    };
    String[] targetLabel = new String[] {
            "STEPS",
            "KM",
            "HRS"
    };
    final short[] minTargetValues = new short[] {
            1000,
            1,
            1
    };
    int[] m_stepsSliderPoints = new int[] {
            2000,
            4000,
            6000,
            8000,
            10000
    };
    private CarouselPageAdapter mAdapter;
    public ViewPager mPager;
    private CarouselManager mCarouselManager;
    private int mTargetValue = 0;


    //---------------------------------------------------------------------------------- onCreate
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:DailyTarget )");

        return inflater.inflate(R.layout.activity_fragment_left_4, container, false);
    }

    @Override
    public void onStart() {
        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:DailyTarget ) - On Start");

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

        // Initialize update button
        Button updateButton = (Button)getView().findViewById(R.id.btn_update_daily_target);
        updateButton.setOnClickListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

    }

    private void initialize() {
        // Setup the carousel manager
        mPager = (ViewPager) getView().findViewById(R.id.view_pager);
        mAdapter = new CarouselPageAdapter(this, this.getChildFragmentManager());
        mCarouselManager = new CarouselManager(this, mPager, mAdapter);

        // Load previous target value
        DisplayMetrics dimension = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dimension);

        mTargetValue = PreferencesManager.getInstance().retrieveDataForInt( Constants.PREFKEY_SPORTS_GOAL_STEPS, 0 );

        if (mTargetValue == 0) {
            mTargetValue = 1;
        } else {
            mTargetValue = mTargetValue/1000;
        }
        mCarouselManager.init(mTargetValue, -(int)(dimension.widthPixels * 0.7f), 5);

        // Delay scaling of carousel because cannot find the rootview
        Handler delay = new Handler();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                mAdapter.adjustScale(CarouselManager.FIRST_PAGE + (mTargetValue - 1), 0, 0);
            }
        };
        delay.postDelayed(r, 300); // 0.3 secs
    }

    //---------------------------------------------------------------------------------- Button functions

    @Override
    public void onClick(View pView) {
        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:DailyTarget ) - button pressed");

        if ( pView.getId() == R.id.btn_update_daily_target ) {
            updateDailyTarget();
        }

    }

    private void updateDailyTarget() {

        if ( BluetoothManager.getInstance().isDeviceDisconnected() ) {
            return;
        }

        int targetValue = (mCarouselManager.getSelectedPage() % 30) * 1000;
        if (targetValue == 0) { targetValue = 30000; }
        if (targetValue < 0 ) { targetValue += 30000; }

        PreferencesManager.getInstance().saveDataInt( Constants.PREFKEY_SPORTS_GOAL_STEPS, targetValue );

        ((MainActivity) getActivity()).writeWatchUIConfig();

        KreyosUtility.showInfoMessage( getActivity(),
                                       getString(R.string.dialog_title_update_watch),
                                       getString(R.string.dialog_msg_watch_updated));
    }
}
