package com.kreyos.kreyosandroid.listeners;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Message;

import java.util.ArrayList;

/**
 * Created by emman on 7/28/14.
 */
public interface IBluetoothListener {

    // update progress bar
    public void updateProgressBar(float pMaxSteps, float pSteps, float pDistance, float pCalories);
    // service connected
    public void onBluetoothHeadsetConnected();
    public void onUnpairConnectedDevice();
}
