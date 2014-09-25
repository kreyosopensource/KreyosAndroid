package com.kreyos.kreyosandroid.listeners;

import android.os.Message;

/**
 * Created by emman on 7/30/14.
 */
public interface IKreyosListener {
    public void onWatchConnected();
    public void onGetFirmwareVersion(Message pMessage);
    public void onActivityDataReceived(Message pMessage);
    public void onTodaysDataReceived(Message pMessage);
}
