package com.kreyos.kreyosandroid.managers;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.kreyos.kreyosandroid.activities.MainActivity;
import com.kreyos.kreyosandroid.bluetooth.BluetoothAgent;
import com.kreyos.kreyosandroid.bluetooth.Protocol;
import com.kreyos.kreyosandroid.utilities.Constants;

import java.lang.ref.WeakReference;


public class KreyosManager {

    private Activity mActivity;
    private BluetoothAgent mAgent;
    private Protocol mProtocol;

    private static class BTDataHandler extends Handler {

        private final KreyosManager mManager;

        public BTDataHandler(KreyosManager pManager) {
            mManager = pManager;
        }

        @Override
        public void handleMessage(Message msg) {
            if (mManager != null) {
                mManager.handleBTData(msg);
            }
        }
    }

    public KreyosManager(Activity pActivity) {
        mActivity = pActivity;
    }

    public void init() {
        Log.d(Constants.TAG_DEBUG, "(Manager:KreyosManager) - " + BluetoothManager.getInstance().getSelectedDeviceName());

        BluetoothAgent.initBluetoothAgent(mActivity);
        BluetoothAgent.getInstance(null).initialize();

        BTDataHandler msgHanlder = new BTDataHandler(this);
        mAgent = BluetoothAgent.getInstance(msgHanlder);
        mAgent.bindDevice(BluetoothManager.getInstance().getSelectedDeviceName());
        mAgent.startActiveSession();

        mProtocol = new Protocol(mAgent, mActivity);
    }

    public void unlockWatch() {
        mProtocol.unlockWatch();
    }

    public void notify(String pType, String pTitle, String pMessage) {
        mProtocol.notifyMessage(pType, pTitle, pMessage);
    }

    public void getActivityData() {
        mProtocol.getActivityData();
    }

    public void getTodayData() {
        mProtocol.sendDailyActivityRequest();
    }


    public void handleBTData(Message pMessage) {
        Log.d(Constants.TAG_DEBUG, "(Manager:KreyosManager) - Message ID:" + pMessage.what);
        switch (pMessage.what) {
            case Protocol.MessageID.MSG_BLUETOOTH_STATUS:
                Log.d(Constants.TAG_DEBUG, "(Manager:KreyosManager) - Bluetooth Status:" + pMessage.toString());
                String status = pMessage.toString();
                if (status.equalsIgnoreCase("Running")) {
                    ((MainActivity)mActivity).onWatchConnected();
                }
            break;

            case Protocol.MessageID.MSG_DEVICE_ID_GOT:
            break;

            case Protocol.MessageID.MSG_FIRMWARE_VERSION:
                ((MainActivity)mActivity).onGetFirmwareVersion(pMessage);
            break;

            case Protocol.MessageID.MSG_FILE_RECEIVED:
                ((MainActivity)mActivity).onActivityDataReceived(pMessage);
            break;

            case Protocol.MessageID.MSG_TODAY_ACTIVITY:
                ((MainActivity)mActivity).onTodaysDataReceived(pMessage);
            break;
        }
    }
}
