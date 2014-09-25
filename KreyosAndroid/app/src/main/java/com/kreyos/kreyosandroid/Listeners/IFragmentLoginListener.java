package com.kreyos.kreyosandroid.listeners;

import android.widget.EditText;

import com.kreyos.kreyosandroid.utilities.Constants;


// implemented by LoginActivity
// called by FragmentLogin1 (onLoginClick)
// called by FragmentLogin2 (onNextClick)
// called by FragmentLogin3 (onDoneClick)

public interface IFragmentLoginListener {
    public void onLoginClick(EditText pEmail, EditText pPassword);
    public void onNextClick(EditText pEmail, EditText pPassword, EditText pPasswordConf);
    public void onBirthdayClick(int pId);
    public void onDoneClick(String pFirstNameText, String pLastNameText, String pBirthdayText, byte pGenderValue, String pHeightValue, String pWeight);
}
