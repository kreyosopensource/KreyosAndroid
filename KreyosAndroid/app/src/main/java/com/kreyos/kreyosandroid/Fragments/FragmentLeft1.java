package com.kreyos.kreyosandroid.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;

import android.net.Uri;
import android.provider.MediaStore;

import android.database.Cursor;
import android.graphics.Bitmap;

import com.kreyos.kreyosandroid.R;

// Kreyos Utilities
import com.kreyos.kreyosandroid.activities.MainActivity;
import com.kreyos.kreyosandroid.managers.PreferencesManager;
import com.kreyos.kreyosandroid.utilities.CircularImageView;
import com.kreyos.kreyosandroid.utilities.CircularProgressbar;
import com.kreyos.kreyosandroid.utilities.Constants;
import com.kreyos.kreyosandroid.utilities.KreyosUtility;

/**
 * HOME
 */

public class FragmentLeft1 extends BaseFragmentMain
    implements
        View.OnClickListener {

    //---------------------------------------------------------------------------------- Variables
    private     CircularImageView           mAvatarImage    = null;
    private     CircularProgressbar         mProgressbar    = null;
    private     Button                      mBtnDailyTarget = null;

    private     boolean                     mBProceedToDailyTargetFragment  = true;


    //---------------------------------------------------------------------------------- onCreate
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:Home )");

        return inflater.inflate(R.layout.activity_fragment_left_1, container, false);
    }

    @Override
    public void onStart() {
        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:Home ) - on Start");

        super.onStart();

        // setup fonts
        KreyosUtility.overrideFonts( this.getActivity().getBaseContext(),
                                     getView(),
                                     Constants.FONT_NAME.LEAGUE_GOTHIC_REGULAR);

        // setup views
        setupViewsAndCallbacks();
    }

    private void setupViewsAndCallbacks() {
        mAvatarImage    = (CircularImageView) getView().findViewById(R.id.img_avatar);
        mProgressbar    = (CircularProgressbar) getView().findViewById(R.id.progbar_circle_1);
        mBtnDailyTarget = (Button) getView().findViewById(R.id.btn_set_daily_target);

        mAvatarImage.setFragment(this);
        mBtnDailyTarget.setOnClickListener(this);

        ((MainActivity)getActivity()).loadProfileImage( mAvatarImage );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        super.onActivityResult(requestCode, resultCode, intent);

        if (intent == null)
            return;

        // Avatar change: from gallery
        if (   requestCode == Constants.RC_IMG_FROM_GALLERY
            && resultCode == getActivity().RESULT_OK) {
            Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:Home ) - on Activity Result: gallery");

            try
            {
                Uri selectedImage = intent.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                Log.d(Constants.TAG_DEBUG, "image path = " + picturePath);
                cursor.close();



                Bitmap bitmap = KreyosUtility.getScaledBitmap(picturePath, 800, 800);
                if (bitmap == null) {
                    return;
                }

                mAvatarImage.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 800, 800, false));

                PreferencesManager.getInstance().saveDataString( Constants.PREFKEY_USER_IMAGE, picturePath );

                Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:Home ) - on Activity Result: avatar change SUCCESSFUL!");
            }
            catch( Exception ex )
            {
                ex.printStackTrace();

                Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:Home ) - on Activity Result: avatar change FAILED! " + ex );
            }
        }

        if (requestCode == Constants.RC_IMG_FROM_CAMERA) {
            Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:Home ) - on Activity Result: camera");

            Uri selectedImage = intent.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            Bitmap bitmap = KreyosUtility.getScaledBitmap(picturePath, 800, 800);
            if (bitmap == null) {
                return;
            }
            mAvatarImage.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 800, 800, false));

            PreferencesManager.getInstance().saveDataString( Constants.PREFKEY_USER_IMAGE, picturePath );
        }
    }

    //---------------------------------------------------------------------------------- Button functions
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_set_daily_target:
                btnShowDailyTarget();
        }
    }

    private void btnShowDailyTarget() {
        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:Home ) - show daily target" );

        mListener.onHomeTargetButtonClick( mBProceedToDailyTargetFragment );
    }

    //---------------------------------------------------------------------------------- Handle avatar
    protected AlertDialog mPhotoDialog = null;

    public void showPhotoDialog() {
        Log.d(Constants.TAG_DEBUG, "( MAIN_FRAGMENT:Home ) - Show Photo Dialog");

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        builder.setTitle(R.string.alert_avatar_title);
        builder.setMessage(R.string.alert_avatar_msg);

        // Capture Image
        builder.setNeutralButton(R.string.alert_avatar_btn_pos, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, Constants.RC_IMG_FROM_CAMERA);
                mPhotoDialog.dismiss();
            }
        });

        // Choose from gallery
        builder.setNegativeButton(R.string.alert_avatar_btn_neg, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, Constants.RC_IMG_FROM_GALLERY);
                mPhotoDialog.dismiss();
            }
        });

        builder.setPositiveButton(R.string.search_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mPhotoDialog.dismiss();
            }
        });

        // Create and show dialog
        mPhotoDialog = builder.create();
        mPhotoDialog.show();
    }


    //---------------------------------------------------------------------------------- Handle progress bar

    public void updateProgressBar(float pMaxSteps, float pSteps, float pDistance, float pCalories) {

        if ( mProgressbar == null )
            return;

        mProgressbar.animateProgress(pMaxSteps, pSteps, pDistance, pCalories);

        if ( pSteps > 0 ) {
            mBProceedToDailyTargetFragment = false;
            mBtnDailyTarget.setText( R.string.fragmentL1_btn_overall_activities );

        } else {
            mBProceedToDailyTargetFragment = true;
            mBtnDailyTarget.setText( R.string.fragmentL1_btn_pick_target );
        }
    }
}
