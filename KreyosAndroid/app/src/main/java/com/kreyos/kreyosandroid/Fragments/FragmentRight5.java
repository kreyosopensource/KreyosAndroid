package com.kreyos.kreyosandroid.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.kreyos.kreyosandroid.R;
import com.kreyos.kreyosandroid.activities.MainActivity;
import com.kreyos.kreyosandroid.managers.PreferencesManager;
import com.kreyos.kreyosandroid.utilities.Constants;
import com.kreyos.kreyosandroid.utilities.KreyosUtility;

import java.text.DecimalFormat;

/**
 * PERSONAL INFORMATION
 */

public class FragmentRight5 extends BaseFragmentMain
        implements
        View.OnClickListener {

//--------------------------------------------------------------------------------------------------- Variables
    private ImageView   mAvatarImage        = null;

    private EditText    mFieldFirstName     = null;
    private EditText    mFieldLastName      = null;

    private TextView    mFieldBirthday      = null;
    private TextView    mFieldGenderMale    = null;
    private TextView    mFieldGenderFemale  = null;
    private TextView    mFieldHeightInches  = null;
    private TextView    mFieldHeightFeet    = null;
    private TextView    mFieldWeight        = null;

    private Button      mUpdateButton       = null;

    private byte        mValueGender        = Constants.GENDER_NULL;
    private float       mValueWeight        = 0;
    private float       mValueHeightCm      = 0;


//--------------------------------------------------------------------------------------------------- onCreate
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:PersonalInfo )");

        return inflater.inflate(R.layout.activity_fragment_right_5, container, false);
    }

    @Override
    public void onStart() {
        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:PersonalInfo ) - onStart");

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
        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:PersonalInfo ) - ON DESTROY");

        super.onDestroy();

        // nullify all
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }


    private void setupViewsAndCallbacks() {

        mAvatarImage		= (ImageView) getView().findViewById(R.id.img_avatar_personalinfo);

        mFieldFirstName     = (EditText) getView().findViewById(R.id.field_first_name);
        mFieldLastName      = (EditText) getView().findViewById(R.id.field_last_name);

        mFieldBirthday      = (TextView) getView().findViewById(R.id.field_birthday);
        mFieldGenderMale    = (TextView) getView().findViewById(R.id.field_gender_male);
        mFieldGenderFemale  = (TextView) getView().findViewById(R.id.field_gender_female);
        mFieldHeightInches  = (TextView) getView().findViewById(R.id.field_height_in);
        mFieldHeightFeet    = (TextView) getView().findViewById(R.id.field_height_ft);
        mFieldWeight        = (TextView) getView().findViewById(R.id.field_weight);

        mUpdateButton       = (Button) getView().findViewById( R.id.btn_update_info );


        mFieldBirthday.setOnClickListener( this );
        mFieldGenderMale.setOnClickListener( this );
        mFieldGenderFemale.setOnClickListener( this );
        mFieldHeightInches.setOnClickListener( this );
        mFieldHeightFeet.setOnClickListener( this );
        mFieldWeight.setOnClickListener( this );
        mUpdateButton.setOnClickListener( this );
    }

    private void initialize() {

//        // Load save profile on user preference
        mListener.loadProfile();

        // Load profile image
        if(mAvatarImage == null) {
            return;
        }

        ((MainActivity)getActivity()).loadProfileImage( mAvatarImage );
    }



//--------------------------------------------------------------------------------------------------- Button functions
    @Override
    public void onClick(View pView) {
        switch (pView.getId()) {
            case R.id.field_birthday :
                mListener.onBirthdayClick();
                break;

            case R.id.field_weight :
                mListener.onWeightClick( 2 );
                break;

            case R.id.field_height_ft :
            case R.id.field_height_in :
                mListener.onHeightClick();
                break;

            case R.id.field_gender_male :
                mValueGender = Constants.GENDER_MALE;
                mFieldGenderFemale.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_right_rounded_border_white));
                pView.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_left_rounded_border_selected));
                break;

            case R.id.field_gender_female :
                mValueGender = Constants.GENDER_FEMALE;
                mFieldGenderMale.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_left_rounded_border_white));
                pView.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_right_rounded_border_selected));
                break;

            case R.id.btn_update_info:
                ((MainActivity)getActivity()).updateProfile( mFieldFirstName.getText().toString(),
                                                             mFieldLastName.getText().toString(),
                                                             mFieldBirthday.getText().toString(),
                                                             mValueGender,
                                                             "" + mValueHeightCm,
                                                             "" + mValueWeight);
                break;
        }
    }



//--------------------------------------------------------------------------------------------------- called by Main Activity
    public void updateFirstNameField( String pText ) {
        mFieldFirstName.setText( pText );
    }

    public void updateLastNameField( String pText ) {
        mFieldLastName.setText( pText );
    }

    public void updateBirthdayField( String pText ) {
        mFieldBirthday.setText( pText );
    }

    public void updateGenderField( String pValue ) {

        if(pValue.equals("MALE")) {
            mValueGender = Constants.GENDER_MALE;
            mFieldGenderMale.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_left_rounded_border_selected));
            mFieldGenderFemale.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_right_rounded_border_white));

        } else {
            mValueGender = Constants.GENDER_FEMALE;
            mFieldGenderMale.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_left_rounded_border_white));
            mFieldGenderFemale.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_right_rounded_border_selected));
        }
    }

    public void updateWeightField( String pText ) {
        mFieldWeight.setText( pText );
    }

    public void updateWeightValue( float pValue ) {
        mValueWeight = pValue;
    }

    public void updateHeightField( String pTextInches, String pTextFeet, float pValue ) {
        mFieldHeightInches.setText( pTextInches );
        mFieldHeightFeet.setText( pTextFeet );
        mValueHeightCm = pValue;
    }
}

