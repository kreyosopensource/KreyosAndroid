package com.kreyos.kreyosandroid.managers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;

import com.kreyos.kreyosandroid.R;
import com.kreyos.kreyosandroid.activities.MainActivity;
import com.kreyos.kreyosandroid.utilities.Constants;


public class KreyosDialogManager {

//--------------------------------------------------------------------------------------------------- Variables
    private static KreyosDialogManager  mInstance       = null;
    private Activity                    mActivity       = null;
    private ProgressDialog              mProgressDialog = null;



//--------------------------------------------------------------------------------------------------- Initialize singleton
    public static void initializeActivity(Activity pActivity) {
        if (mInstance == null) {
            mInstance = new KreyosDialogManager(pActivity);
        }
    }

    private KreyosDialogManager(Activity pActivity) {
        mActivity       = pActivity;
    }



//--------------------------------------------------------------------------------------------------- Get Instance
    public static KreyosDialogManager getInstance() {
        if (mInstance == null) {
            throw new IllegalStateException(PreferencesManager.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }
        return mInstance;
    }



//--------------------------------------------------------------------------------------------------- Public methods
    public void changeActivity(Activity pActivity) {

        mActivity       = null;
        mActivity       = pActivity;
    }

    // PROGRESS
    public void showProgressDialog(String pTitle, String pMessage) {
        try {
            mProgressDialog = ProgressDialog.show(mActivity, pTitle, pMessage, true);
            mProgressDialog.setCancelable(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void cancelProgressDialog() {
        try {
            mProgressDialog.dismiss();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ALERT
    public void showErrorDialog(String pTitle, String pMessage) {

        AlertDialog.Builder builder = new AlertDialog.Builder( mActivity );
        builder.setTitle( pTitle );
        builder.setMessage( pMessage );
        builder.setPositiveButton( "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void showInfoDialog( String pTitle, String pMessage ) {

        AlertDialog.Builder builder = new AlertDialog.Builder( mActivity );
        builder.setTitle( pTitle );
        builder.setMessage( pMessage );
        builder.setPositiveButton( "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }



//--------------------------------------------------------------------------------------------------- Height & Weight
    private Button          btnLbs  = null;
    private Button          btnKg   = null;
    private NumberPicker    npLbs   = null;
    private NumberPicker    npKg    = null;
    private float           mWeightValue = 0;


    public void showWeightSlider( float pWeightOrigValue ) {

        btnLbs = null;
        btnKg = null;
        npLbs = null;
        npKg = null;

        mWeightValue = pWeightOrigValue;


        String strTitle    = mActivity.getString( R.string.dialog_title_set_weight ) ;
        String strBtnPos   = mActivity.getString( R.string.dialog_btn_done );

        LayoutInflater inflater = mActivity.getLayoutInflater();

        AlertDialog.Builder builder = new AlertDialog.Builder( mActivity );
        builder.setTitle( strTitle );
        builder.setView(inflater.inflate(R.layout.picker, null));

        // Button Callbacks
        builder.setPositiveButton( strBtnPos, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                String weightStr = "";
                // In Kg
                if( !btnKg.isEnabled() ) {
                    weightStr = "" + npKg.getValue() + " kg";
                    mWeightValue = npKg.getValue() * 2.2046f;
                }
                // In Lbs
                else {
                    weightStr = "" + npLbs.getValue() + " lbs";
                    mWeightValue = npLbs.getValue();
                }

                if (mActivity instanceof MainActivity) {
                    ((MainActivity)mActivity).setWeightValue( weightStr, mWeightValue );
                }
            }
        });


        //---
        AlertDialog alertDialogWeight = builder.create();
        alertDialogWeight.show();

        btnLbs = (Button) alertDialogWeight.findViewById( R.id.btn_option1 );
        btnKg = (Button) alertDialogWeight.findViewById( R.id.btn_option2 );
        btnLbs.setText("lbs");
        btnLbs.setEnabled(false);
        btnKg.setText("kg");

        npLbs = (NumberPicker) alertDialogWeight.findViewById( R.id.np_option1 );
        npLbs.setMinValue(Constants.MIN_WEIGHT_LBS);
        npLbs.setMaxValue(Constants.MAX_WEIGHT_LBS);
        npLbs.setValue((int) pWeightOrigValue);
        npLbs.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mWeightValue = picker.getValue();
            }
        });

        npKg = (NumberPicker)alertDialogWeight.findViewById( R.id.np_option2 );
        npKg.setMinValue(Constants.MIN_WEIGHT_KG);
        npKg.setMaxValue(Constants.MAX_WEIGHT_KG);
        npKg.setVisibility(View.GONE);
        npKg.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mWeightValue = (float) (newVal * 2.2046f);
            }
        });

        btnLbs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWeightValue = (npKg.getValue() * 2.2046f);
                npLbs.setVisibility(View.VISIBLE);
                npLbs.setValue((int) mWeightValue);
                npKg.setVisibility(View.GONE);
                btnLbs.setEnabled(false);
                btnKg.setEnabled(true);
            }
        });

        btnKg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWeightValue = npLbs.getValue();
                float kg = (mWeightValue / 2.2046f);
                npLbs.setVisibility(View.GONE);
                npKg.setVisibility(View.VISIBLE);
                npKg.setValue((int) kg);
                btnLbs.setEnabled(true);
                btnKg.setEnabled(false);
            }
        });

        //----







        //<!-- Ft | Lbs = np_option1 -->
        //<!-- In| Cm | Kg = np_option2 -->
        //<!-- Ft.In | Lbs = btn_option1 -->
        //<!-- In| Cm | Kg = btn_option2 -->
        // + ET 040714 : Create and show dialog




    }


    private Button          btnCm  = null;
    private Button          btnFt   = null;
    private NumberPicker    npCm   = null;
    private NumberPicker    npFt    = null;
    private float           mHeightValueInCm = 0;
    private int             mFtFixFtValue = 0;
    private int             mFtFixInValue = 0;
    private int             mCmFixInValue = 0;

    public void showHeightSlider() {

        String strTitle    = mActivity.getString( R.string.dialog_title_set_height ) ;
        String strBtnPos   = mActivity.getString( R.string.dialog_btn_done );

        AlertDialog.Builder builder = new AlertDialog.Builder( mActivity );
        builder.setTitle( strTitle );
        LayoutInflater inflater = mActivity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.picker, null));

        builder.setPositiveButton(strBtnPos, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // In Centimeters
                if( !btnCm.isEnabled() ) {
                    mHeightValueInCm = npCm.getValue();

                    if ( mActivity instanceof MainActivity ) {
                        ((MainActivity)mActivity).setHeightValue( "" + mHeightValueInCm + "cm",
                                                                  "" ,
                                                                  mHeightValueInCm);
                    }
                }
                // In Ft & Inches
                else {
                    final int foot = npFt.getValue() * 12; // foot in inches
                    final int inches = npCm.getValue();
                    int totalInches = foot + inches;
                    mHeightValueInCm = (float)totalInches * 2.54f;

                    if ( mActivity instanceof MainActivity ) {
                        ((MainActivity)mActivity).setHeightValue( "" + npFt.getValue() + "ft",
                                                                  "" + npCm.getValue() + "in",
                                                                  mHeightValueInCm);
                    }
                }
            }
        });

        //<!-- Ft | Lbs = np_option1 -->
        //<!-- In| Cm | Kg = np_option2 -->
        //<!-- Ft.In | Lbs = btn_option1 -->
        //<!-- In| Cm | Kg = btn_option2 -->
        // + ET 040714 : Create and show dialog
        //---
        AlertDialog alertDialogHeight = builder.create();
        alertDialogHeight.show();

        btnFt = (Button)alertDialogHeight.findViewById( R.id.btn_option1 );
        btnCm = (Button)alertDialogHeight.findViewById( R.id.btn_option2 );
        btnCm.setEnabled( false );

        npFt = (NumberPicker)alertDialogHeight.findViewById(R.id.np_option1);
        npFt.setMinValue( Constants.MIN_HEIGHT_FEET );
        npFt.setMaxValue( Constants.MAX_HEIGHT_FEET );
        npFt.setVisibility( View.GONE );
        npFt.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if ( !btnFt.isEnabled() ) {
                    final int foot = npFt.getValue() * 12; // foot in inches
                    final int inches = npCm.getValue();
                    int totalInches = foot + inches;
                    mHeightValueInCm = (float)totalInches * 2.54f;

                    mFtFixFtValue = newVal;
                }
            }
        });

        // @Note: npCm can be CM or INCHES
        npCm = (NumberPicker)alertDialogHeight.findViewById( R.id.np_option2 );
        npCm.setMinValue( Constants.MIN_HEIGHT_CENTIMETERS );
        npCm.setMaxValue( Constants.MAX_HEIGHT_CENTIMETERS );
        npCm.setVisibility( View.VISIBLE );
        npCm.setValue( (int)mHeightValueInCm );
        npCm.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if( !btnFt.isEnabled() ) {
                    final int foot = npFt.getValue() * 12; // foot in inches
                    final int inches = npCm.getValue();
                    int totalInches = foot + inches;
                    mHeightValueInCm = (float)totalInches * 2.54f;

                    mFtFixInValue = newVal;
                }
                else {
                    mHeightValueInCm = (float)newVal;

                    // Convert to inches
                    mCmFixInValue = (int)(newVal * 0.39379f);
                    // Log.d("HeightFix", "" + inches);

                }
            }
        });

        btnFt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHeightValueInCm = npCm.getValue();
                int totalInches = (int)(mHeightValueInCm * 0.39370f);

                npFt.setMinValue( Constants.MIN_HEIGHT_FEET );
                npFt.setMaxValue( Constants.MAX_HEIGHT_FEET );
                npFt.setVisibility( View.VISIBLE );

                npCm.setMinValue( Constants.MIN_HEIGHT_INCHES );
                npCm.setMaxValue( Constants.MAX_HEIGHT_INCHES );

                // Convert cm to inch
                npFt.setValue((int) mCmFixInValue / 12);
                npCm.setValue((int) mCmFixInValue % 12);

                btnFt.setEnabled( false );
                btnCm.setEnabled( true );
            }
        });

        btnCm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                npFt.setVisibility(View.GONE);

                final int foot = npFt.getValue() * 12; // foot in inches
                final int inches = npCm.getValue();
                int totalInches = foot + inches;
                mHeightValueInCm = (float)totalInches * 2.54f;
                npCm.setMinValue( Constants.MIN_HEIGHT_CENTIMETERS );
                npCm.setMaxValue( Constants.MAX_HEIGHT_CENTIMETERS );

                // Convert feet and inch to cm
                int feet = (int)(((mFtFixFtValue * 12) + mFtFixInValue) / 0.39370);
                npCm.setValue(feet);

                btnFt.setEnabled( true );
                btnCm.setEnabled( false );
            }
        });
    }




}



