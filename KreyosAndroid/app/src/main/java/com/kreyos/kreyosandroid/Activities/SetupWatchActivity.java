package com.kreyos.kreyosandroid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.kreyos.kreyosandroid.R;
import com.kreyos.kreyosandroid.managers.PreferencesManager;
import com.kreyos.kreyosandroid.utilities.Constants;
import com.kreyos.kreyosandroid.utilities.KreyosUtility;

/**
 * TUTORIAL
 */

public class SetupWatchActivity extends FragmentActivity
    implements
        View.OnClickListener {

    //--------------------------------------------------------------------------- Variables
    private Fragment mCurrentFragment;

    //--------------------------------------------------------------------------- onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_setup_watch);
    }

    @Override
    protected void onStart() {
        Log.d(Constants.TAG_DEBUG, "[Activity:Tutorial] - onStart");

        super.onStart();

        // setup fonts
        KreyosUtility.overrideFonts(this,
                ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0),
                Constants.FONT_NAME.LEAGUE_GOTHIC_REGULAR);

        initialize();
    }

    private void initialize() {
//        switchFragment(Constants.LOGIN_FRAGMENTS.FRAGMENT_SIGNUP_CREDENTIALS);

        PreferencesManager.getInstance().saveDataBoolean( Constants.PREFKEY_IS_TUTORIAL_MODE, true );

    }

    private void switchFragment () {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_scr1_skip:


            default:
                break;
        }
    }

    public void btnBackToMainActivity(View view) {
        Intent intent = new Intent(SetupWatchActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
