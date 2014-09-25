package com.kreyos.kreyosandroid.managers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.util.Log;

import com.kreyos.kreyosandroid.R;
import com.kreyos.kreyosandroid.utilities.Constants;

import java.util.ArrayList;

/**
 * Created by emman on 7/30/14.
 */
public class DialogManager {

    private Activity        mActivity;
    private ProgressDialog  mProgressDialog;

    public DialogManager(Activity pActivity) {
        mActivity = pActivity;
    }

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

    public void showPairingDialog(ArrayList<BluetoothDevice> pDevices) {

        final ArrayList<BluetoothDevice> devices = pDevices;

        ArrayList<String> lisfOfWatch = new ArrayList<String>();

        for ( BluetoothDevice btDevice : devices) {
            if (btDevice.getName().startsWith("MeteorLE")) {
                Log.d(Constants.TAG_DEBUG, "( MANAGER:dialog ) - Remove on Pairing Device: " + btDevice.getName());
                devices.remove(btDevice);
            }
        }

        for ( BluetoothDevice btDevice : devices) {
            Log.d(Constants.TAG_DEBUG, "( MANAGER:dialog ) - Pairing Device: " + btDevice.getName());
            lisfOfWatch.add(btDevice.getName());
        }

        final CharSequence[] watchArray = lisfOfWatch.toArray(new CharSequence[lisfOfWatch.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.connect_meteor);
        builder.setCancelable(false);
        builder.setItems(watchArray, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                BluetoothDevice[] itemArray = new BluetoothDevice[devices.size()];
                BluetoothDevice[] deviceArray = devices.toArray(itemArray);
                BluetoothManager.getInstance().pairDevice( deviceArray[item] );
            }
        });

        builder.setPositiveButton(R.string.search_again, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                BluetoothManager.getInstance().getAvailableDevices();
            }
        });

        builder.setNegativeButton(R.string.search_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                Log.d(Constants.TAG_DEBUG, "( MANAGER:dialog ) - Pairing cancelled!");
            }
        });

        builder.show();
    }
}
