package com.kreyos.kreyosandroid.listeners;


// implemented by MainActivity
// called by FragmentLogin1 (onLoginClick)
// called by FragmentLogin2 (onNextClick)
// called by FragmentLogin3 (onDoneClick)

public interface IFragmentMainListener {
    public void onHomeAvatarClick();
    public void onHomeTargetButtonClick( boolean pBProceedToDailyTargetFragment );
    public void onActivitiesStartQuery( String pKey, long pHead, long pTail );

    // Date & Time
    public void onDateClick();
    public void onTimeClick();
    public void onUpdateWatchTimeFromInput();

    // Silent Alarm
    public void onAlarmTimeClick();

    // Personal Information
    public void loadProfile();
    public void onBirthdayClick();
    public void onWeightClick( float pWeightOrigValue );
    public void onHeightClick();
}
