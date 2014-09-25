package com.kreyos.kreyosandroid.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.TextView;

import com.kreyos.kreyosandroid.R;
import com.kreyos.kreyosandroid.utilities.Constants;
import com.kreyos.kreyosandroid.utilities.KreyosUtility;


/**
 * LOGIN: 3
 */


public class FragmentLogin3 extends BaseFragmentLogin
        implements
        View.OnClickListener
{
    //----------------------------------------------------------------------------------------------> Variables
    private EditText    mFieldFirstName     = null;
    private EditText    mFieldLastName      = null;
    private TextView    mBirthday           = null;
    private TextView    mGenderMale         = null;
    private TextView    mGenderFemale       = null;
    private TextView    mHeightInFeet       = null;
    private TextView    mHeightInInches     = null;
    private TextView    mWeightInPounds     = null;
    private Button      mBtnDone            = null;

    byte mGenderValue = Constants.GENDER_NULL;

    // + ET 040714 : Sliders
    boolean isHeightOnFt = false;

    float m_heightInCentValue = 0;
    float m_weightInLbsValue = 0;

    int m_feetFixFeet = 0;
    int m_feetFixInch = 0;
    int m_cmFixInch = 0;

    private AlertDialog m_alertHeight = null;
    private Button m_btnFeet = null;
    private Button m_btnCentimeters = null; // this can be centimeters or inches
    private NumberPicker m_npFeet = null;
    private NumberPicker m_npCentimeters = null; // this can be centimeters or inches
    private AlertDialog m_alertWeight = null;
    private Button m_btnLbs = null;
    private Button m_btnKg = null;
    private NumberPicker m_npLbs = null;
    private NumberPicker m_npKg = null;

    //----------------------------------------------------------------------------------------------> onCreate
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_fragment_3, container, false);
        Log.d(Constants.TAG_DEBUG, "( LOGIN_FRAGMENT_3 )");

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(Constants.TAG_DEBUG, "( LOGIN_FRAGMENT_3 ) - onStart");

        // setup fonts
        KreyosUtility.overrideFonts(    this.getActivity().getBaseContext(),
                                        getView(),
                                        Constants.FONT_NAME.LEAGUE_GOTHIC_REGULAR);

        setupViewsAndCallbacks();
    }

    private void setupViewsAndCallbacks() {

        mFieldFirstName = (EditText) getView().findViewById(R.id.field_first_name_create_acct);
        mFieldLastName  = (EditText) getView().findViewById(R.id.field_last_name_create_acct);
        mBirthday       = (TextView) getView().findViewById(R.id.field_birthday_create_acct);
        mGenderMale     = (TextView) getView().findViewById(R.id.field_gender_male_create_acct);
        mGenderFemale   = (TextView) getView().findViewById(R.id.field_gender_female_create_acct);
        mHeightInFeet   = (TextView) getView().findViewById(R.id.field_height_ft_create_acct);
        mHeightInInches = (TextView) getView().findViewById(R.id.field_height_in_create_acct);
        mWeightInPounds = (TextView) getView().findViewById(R.id.field_weight_create_acct);

        mBtnDone 		= (Button) getView().findViewById(R.id.btn_done_create_acct);
        mBtnDone.setOnClickListener(this);



        mBirthday.setOnClickListener(this);
        mGenderMale.setOnClickListener(this);
        mGenderFemale.setOnClickListener(this);
        mWeightInPounds.setOnClickListener(this);
        mHeightInFeet.setOnClickListener(this);
        mHeightInInches.setOnClickListener(this);

        mHeightInFeet.setText("" + Constants.MIN_HEIGHT_FEET + "Ft.");
        mHeightInInches.setText("0In.");
        mWeightInPounds.setText("" + Constants.MIN_WEIGHT_LBS + "Lbs.");
    }



    //----------------------------------------------------------------------------------------------> Button
    @Override
    public void onClick(View pView) {
        switch (pView.getId()) {
            case R.id.field_birthday_create_acct :
                Log.d(Constants.TAG_DEBUG, "( LOGIN_FRAGMENT_3 ) - Birthday!");
                mListener.onBirthdayClick(Constants.DIALOG_ID_DATE);
                break;

            case R.id.field_weight_create_acct :
                showWeightSlider();
                break;

            case R.id.field_height_ft_create_acct :
                isHeightOnFt = true;
                showHeightSlider();
                break;

            case R.id.field_height_in_create_acct :
                isHeightOnFt = false;
                showHeightSlider();
                break;

            case R.id.field_gender_male_create_acct :
                mGenderValue = Constants.GENDER_MALE;
                mGenderFemale.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_right_rounded_border_white));
                pView.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_left_rounded_border_selected));
                break;

            case R.id.field_gender_female_create_acct :
                mGenderValue = Constants.GENDER_FEMALE;
                mGenderMale.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_left_rounded_border_white));
                pView.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_right_rounded_border_selected));
                break;

            case R.id.btn_done_create_acct:
                createProfile();
                break;
        }
    }


    //----------------------------------------------------------------------------------------------> Enabler
    @Override
    public void enableViews (boolean pEnabled) {
        Log.d(Constants.TAG_DEBUG, "( LOGIN_FRAGMENT_3 ) - " + (pEnabled ? "enabled" : "disabled") );

        super.enableViews(pEnabled);
        mBtnDone.setClickable(pEnabled);
    }


    //---------------------------------------------------------------------------------- Button functions


    public void changeBirthdayValue(String pText) {
        mBirthday.setText(pText);
    }



    public void showHeightSlider() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String title = "SET HEIGHT";
        builder.setTitle(title);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.picker, null));

        // + ET 040714 : Button Callbacks
        builder.setPositiveButton("DONE", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // In Centimeters
                if( !m_btnCentimeters.isEnabled() ) {
                    m_heightInCentValue = m_npCentimeters.getValue();

                    mHeightInFeet.setText( "" + m_heightInCentValue + "Cm." );
                    mHeightInInches.setText( "" );
                }
                // In Ft & Inches
                else {
                    final int foot = m_npFeet.getValue() * 12; // foot in inches
                    final int inches = m_npCentimeters.getValue();
                    int totalInches = foot + inches;
                    m_heightInCentValue = (float)totalInches * 2.54f;

                    mHeightInFeet.setText( "" + m_npFeet.getValue() + "Ft." );
                    mHeightInInches.setText( "" + m_npCentimeters.getValue() + "In." );
                }
            }
        });

        //<!-- Ft | Lbs = np_option1 -->
        //<!-- In| Cm | Kg = np_option2 -->
        //<!-- Ft.In | Lbs = btn_option1 -->
        //<!-- In| Cm | Kg = btn_option2 -->
        // + ET 040714 : Create and show dialog
        m_alertHeight = builder.create();
        m_alertHeight.show();


        m_btnFeet = (Button)m_alertHeight.findViewById( R.id.btn_option1 );
        m_btnCentimeters = (Button)m_alertHeight.findViewById( R.id.btn_option2 );
        m_btnCentimeters.setEnabled( false );

        m_npFeet = (NumberPicker)m_alertHeight.findViewById(R.id.np_option1);
        m_npFeet.setMinValue( Constants.MIN_HEIGHT_FEET );
        m_npFeet.setMaxValue( Constants.MAX_HEIGHT_FEET );
        m_npFeet.setVisibility( View.GONE );
        m_npFeet.setOnValueChangedListener(new OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if ( !m_btnFeet.isEnabled() ) {
                    final int foot = m_npFeet.getValue() * 12; // foot in inches
                    final int inches = m_npCentimeters.getValue();
                    int totalInches = foot + inches;
                    m_heightInCentValue = (float)totalInches * 2.54f;

                    m_feetFixFeet = newVal;
                }
            }
        });

        // @Note: m_npCentimeters can be CM or INCHES
        m_npCentimeters = (NumberPicker)m_alertHeight.findViewById( R.id.np_option2 );
        m_npCentimeters.setMinValue( Constants.MIN_HEIGHT_CENTIMETERS );
        m_npCentimeters.setMaxValue( Constants.MAX_HEIGHT_CENTIMETERS );
        m_npCentimeters.setVisibility( View.VISIBLE );
        m_npCentimeters.setValue( (int)m_heightInCentValue );
        m_npCentimeters.setOnValueChangedListener(new OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if( !m_btnFeet.isEnabled() ) {
                    final int foot = m_npFeet.getValue() * 12; // foot in inches
                    final int inches = m_npCentimeters.getValue();
                    int totalInches = foot + inches;
                    m_heightInCentValue = (float)totalInches * 2.54f;

                    m_feetFixInch = newVal;
                }
                else {
                    m_heightInCentValue = (float)newVal;

                    // Convert to inches
                    m_cmFixInch = (int)(newVal * 0.39379f);
                    // Log.d("HeightFix", "" + inches);

                }
            }
        });

        m_btnFeet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_heightInCentValue = m_npCentimeters.getValue();
                int totalInches = (int)(m_heightInCentValue * 0.39370f);

                m_npFeet.setMinValue( Constants.MIN_HEIGHT_FEET );
                m_npFeet.setMaxValue( Constants.MAX_HEIGHT_FEET );
                m_npFeet.setVisibility( View.VISIBLE );
                //m_npFeet.setValue( totalInches / 12 );

                m_npCentimeters.setMinValue( Constants.MIN_HEIGHT_INCHES );
                m_npCentimeters.setMaxValue( Constants.MAX_HEIGHT_INCHES );
                // m_npCentimeters.setValue( totalInches % 12 );

                // Convert cm to inch
                m_npFeet.setValue((int)m_cmFixInch / 12);
                m_npCentimeters.setValue((int)m_cmFixInch % 12);

                m_btnFeet.setEnabled( false );
                m_btnCentimeters.setEnabled( true );
            }
        });

        m_btnCentimeters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_npFeet.setVisibility(View.GONE);

                final int foot = m_npFeet.getValue() * 12; // foot in inches
                final int inches = m_npCentimeters.getValue();
                int totalInches = foot + inches;
                m_heightInCentValue = (float)totalInches * 2.54f;
                m_npCentimeters.setMinValue( Constants.MIN_HEIGHT_CENTIMETERS );
                m_npCentimeters.setMaxValue( Constants.MAX_HEIGHT_CENTIMETERS );

                // m_npCentimeters.setValue( (int)m_heightInCentValue );
                // Convert feet and inch to cm
                int feet = (int)(((m_feetFixFeet * 12) + m_feetFixInch) / 0.39370);
                m_npCentimeters.setValue(feet);

                m_btnFeet.setEnabled( true );
                m_btnCentimeters.setEnabled( false );
            }
        });
    }

    public void showWeightSlider() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String title = "SET WEIGHT";
        builder.setTitle(title);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.picker, null));

        // + ET 040714 : Button Callbacks
        builder.setPositiveButton("DONE", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // In Kg
                if( m_btnKg.isEnabled() == false ) {
                    mWeightInPounds.setText("" + m_npKg.getValue() + " Kg.");
                    //m_weightInKgValue = m_npKg.getValue();
                    m_weightInLbsValue = m_npKg.getValue() * 2.2046f;
                }
                // In Lbs
                else {
                    mWeightInPounds.setText("" + m_npLbs.getValue() + " Lbs.");
                    m_weightInLbsValue = m_npLbs.getValue();
                }
            }
        });

        //<!-- Ft | Lbs = np_option1 -->
        //<!-- In| Cm | Kg = np_option2 -->
        //<!-- Ft.In | Lbs = btn_option1 -->
        //<!-- In| Cm | Kg = btn_option2 -->
        // + ET 040714 : Create and show dialog
        m_alertWeight = builder.create();
        m_alertWeight.show();

        m_btnLbs = (Button)m_alertWeight.findViewById( R.id.btn_option1 );
        m_btnKg = (Button)m_alertWeight.findViewById( R.id.btn_option2 );
        m_btnLbs.setText( "lbs" );
        m_btnLbs.setEnabled( false );
        m_btnKg.setText( "kg" );

        m_npLbs = (NumberPicker)m_alertWeight.findViewById( R.id.np_option1 );
        m_npLbs.setMinValue( Constants.MIN_WEIGHT_LBS );
        m_npLbs.setMaxValue( Constants.MAX_WEIGHT_LBS  );
        m_npLbs.setValue( (int)m_weightInLbsValue );
        m_npLbs.setOnValueChangedListener(new OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                m_weightInLbsValue = picker.getValue();
            }
        });

        m_npKg = (NumberPicker)m_alertWeight.findViewById( R.id.np_option2 );
        m_npKg.setMinValue( Constants.MIN_WEIGHT_KG );
        m_npKg.setMaxValue( Constants.MAX_WEIGHT_KG );
        m_npKg.setVisibility( View.GONE );
        m_npKg.setOnValueChangedListener(new OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                m_weightInLbsValue = (float)(newVal * 2.2046f);
            }
        });

        m_btnLbs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_weightInLbsValue = (m_npKg.getValue() * 2.2046f);
                m_npLbs.setVisibility( View.VISIBLE );
                m_npLbs.setValue( (int)m_weightInLbsValue );
                m_npKg.setVisibility( View.GONE );
                m_btnLbs.setEnabled(false);
                m_btnKg.setEnabled(true);
            }
        });

        m_btnKg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_weightInLbsValue = m_npLbs.getValue();
                float kg = (m_weightInLbsValue / 2.2046f);
                m_npLbs.setVisibility( View.GONE );
                m_npKg.setVisibility( View.VISIBLE );
                m_npKg.setValue( (int)kg );
                m_btnLbs.setEnabled(true);
                m_btnKg.setEnabled(false);
            }
        });
    }

    private void createProfile () {
        mListener.onDoneClick(  mFieldFirstName.getText().toString(),
                                mFieldLastName.getText().toString(),
                                mBirthday.getText().toString(),
                                mGenderValue,
                                "" + m_heightInCentValue,
                                mWeightInPounds.getText().toString());


    }



}
