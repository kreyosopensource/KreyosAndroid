package com.kreyos.kreyosandroid.fragments;

import android.support.v4.app.Fragment;

import com.kreyos.kreyosandroid.listeners.IFragmentMainListener;


public class BaseFragmentMain extends Fragment {

    protected IFragmentMainListener mListener  = null;

    public void setListener(IFragmentMainListener pListener) {
        mListener = pListener;
    }

    public void enableViews (boolean pEnabled) {}
}
