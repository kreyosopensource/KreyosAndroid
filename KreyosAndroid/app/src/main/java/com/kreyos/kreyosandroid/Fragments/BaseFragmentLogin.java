package com.kreyos.kreyosandroid.fragments;

import android.support.v4.app.Fragment;

import com.kreyos.kreyosandroid.listeners.IFragmentLoginListener;


public class BaseFragmentLogin extends Fragment {

    protected IFragmentLoginListener mListener  = null;

    public void setListener(IFragmentLoginListener pListener) {
        mListener = pListener;
    }

    public void enableViews (boolean pEnabled) {}
}
