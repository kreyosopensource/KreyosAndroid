package com.kreyos.kreyosandroid.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.LoginButton.UserInfoChangedCallback;
import com.kreyos.kreyosandroid.R;
import com.kreyos.kreyosandroid.activities.LoginActivity;
import com.kreyos.kreyosandroid.utilities.Constants;
import com.kreyos.kreyosandroid.utilities.KreyosUtility;

/**
 * LOGIN: 1
 */

public class FragmentLogin1 extends BaseFragmentLogin
        implements
        View.OnClickListener
{
    //----------------------------------------------------------------------------------------------> Variables
    private LoginButton mBtnLoginFacebook   = null;
    private Button mBtnLogin				= null;
    private EditText mFieldEmail			= null;
    private EditText mFieldPassword 		= null;


    //----------------------------------------------------------------------------------------------> onCreate
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(Constants.TAG_DEBUG, "( LOGIN_FRAGMENT_1 )");

        return inflater.inflate(R.layout.login_fragment_1, container, false);
    }

    @Override
    public void onStart() {
        Log.d(Constants.TAG_DEBUG, "( LOGIN_FRAGMENT_1 ) - onStart");

        super.onStart();

        // setup fonts
        KreyosUtility.overrideFonts(this.getActivity().getBaseContext(),
                                    getView(),
                                    Constants.FONT_NAME.LEAGUE_GOTHIC_REGULAR);

        setupViewsAndCallbacks();
    }

    private void setupViewsAndCallbacks() {
        mFieldEmail 			= (EditText) getView().findViewById(R.id.field_email);
        mFieldPassword          = (EditText) getView().findViewById(R.id.field_password);

        mBtnLogin               = (Button) getView().findViewById(R.id.btn_login);
        mBtnLoginFacebook       = (LoginButton) getView().findViewById(R.id.btn_login_facebook);

        mBtnLogin.setOnClickListener(this);

        mBtnLoginFacebook.setReadPermissions(Constants.FB_PERMISSIONS);
        mBtnLoginFacebook.setSessionStatusCallback((LoginActivity)getActivity());


        mBtnLoginFacebook.setUserInfoChangedCallback(new UserInfoChangedCallback() {
            @Override
            public void onUserInfoFetched(GraphUser user) {
                if (user != null) {
                    Log.d(Constants.TAG_DEBUG, "( LOGIN_FRAGMENT_1 ) - Facebook: Hello, " + user.getName());

                } else {
                    Log.d(Constants.TAG_DEBUG, "( LOGIN_FRAGMENT_1 ) - Facebook: \"You are not logged\"");
                }
            }
        });


        mBtnLoginFacebook.setTextSize(25f);

        if (Constants.DS_SET_ON) {
            mFieldEmail.setFocusable(false);
            mFieldPassword.setFocusable(false);

            mFieldEmail.setText(Constants.DS_DEF_EMAIL);
            mFieldPassword.setText(Constants.DS_DEF_PASSWORD);
        }
    }


    //----------------------------------------------------------------------------------------------> Button functions
    @Override
    public void onClick(View pView) {
        switch (pView.getId()) {
            case R.id.btn_login: {
                Log.d(Constants.TAG_DEBUG, "( LOGIN_FRAGMENT_1 ) - login button clicked");

                mListener.onLoginClick(mFieldEmail, mFieldPassword);
            } break;

            case R.id.btn_login_facebook: {
                Log.d(Constants.TAG_DEBUG, "( LOGIN_FRAGMENT_1 ) - facebook button clicked");
            } break;

            default:
                break;
        }
    }


    //----------------------------------------------------------------------------------------------> Enabler
    @Override
    public void enableViews (boolean pEnabled) {
        Log.d(Constants.TAG_DEBUG, "( LOGIN_FRAGMENT_1 ) - " + (pEnabled ? "enabled" : "disabled") );

        super.enableViews(pEnabled);
        mBtnLogin.setClickable(pEnabled);
        mBtnLoginFacebook.setClickable(pEnabled);
    }

}
