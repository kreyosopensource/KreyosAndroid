package com.kreyos.kreyosandroid.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kreyos.kreyosandroid.R;
import com.kreyos.kreyosandroid.utilities.Constants;
import com.kreyos.kreyosandroid.utilities.KreyosUtility;

/**
 * UPDATE FIRMWARE
 */

public class FragmentRight4 extends BaseFragmentMain
        implements
        View.OnClickListener {

//--------------------------------------------------------------------------------------------------- Variables



//--------------------------------------------------------------------------------------------------- onCreate
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:Firmware )");

        return inflater.inflate(R.layout.activity_fragment_right_4, container, false);
    }

    @Override
    public void onStart() {
        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:Firmware ) - onStart");

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
        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:Firmware ) - ON DESTROY");

        super.onDestroy();

        // nullify all
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }


    private void setupViewsAndCallbacks() {

    }

    private void initialize() {

    }

//--------------------------------------------------------------------------------------------------- Button functions
    @Override
    public void onClick(View pView) {

    }
}

