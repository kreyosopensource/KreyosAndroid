package com.kreyos.kreyosandroid.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.kreyos.kreyosandroid.R;
import com.kreyos.kreyosandroid.utilities.Constants;
import com.kreyos.kreyosandroid.utilities.KreyosUtility;

/**
 * LOGIN: 2
 */


public class FragmentLogin2 extends BaseFragmentLogin
        implements
        View.OnClickListener
{
    //----------------------------------------------------------------------------------------------> Variables
    private EditText mFieldEmail			= null;
    private EditText mFieldPassword 		= null;
    private EditText mFieldPasswordConfirm  = null;
    private Button mBtnNext 				= null;


    //----------------------------------------------------------------------------------------------> onCreate
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(Constants.TAG_DEBUG, "( LOGIN_FRAGMENT_2 )");

        View view = inflater.inflate(R.layout.login_fragment_2, container, false);

        return view;
    }

    @Override
    public void onStart() {
        Log.d(Constants.TAG_DEBUG, "( LOGIN_FRAGMENT_2 ) - onStart");

        super.onStart();

        // setup fonts
        KreyosUtility.overrideFonts(    this.getActivity().getBaseContext(),
                                        getView(),
                                        Constants.FONT_NAME.LEAGUE_GOTHIC_REGULAR);

        setupViewsAndCallbacks();
    }

    private void setupViewsAndCallbacks() {
        mFieldEmail 			= (EditText) getView().findViewById(R.id.field_email_create_acct);
        mFieldPassword          = (EditText) getView().findViewById(R.id.field_password_create_acct);
        mFieldPasswordConfirm   = (EditText) getView().findViewById(R.id.field_password_conf_create_acct);

        mBtnNext                = (Button) getView().findViewById(R.id.btn_next_create_acct);
        mBtnNext.setOnClickListener(this);
    }


    //----------------------------------------------------------------------------------------------> Button
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_next_create_acct: {
                mListener.onNextClick(mFieldEmail, mFieldPassword, mFieldPasswordConfirm);
            } break;
        }
    }


    //----------------------------------------------------------------------------------------------> Enabler
    @Override
    public void enableViews (boolean pEnabled) {
        Log.d(Constants.TAG_DEBUG, "( LOGIN_FRAGMENT_2 ) - " + (pEnabled ? "enabled" : "disabled") );

        super.enableViews(pEnabled);
        mBtnNext.setClickable(pEnabled);
    }
}
