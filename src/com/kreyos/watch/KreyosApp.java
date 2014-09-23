package com.kreyos.watch;

import android.app.Application;

public class KreyosApp extends Application 
{
	
	private HomeActivity mainActivity = null;	
	private WatchAlarmKreyosActivityGroup groupActivity = null;
	
	public HomeActivity getMainActivity()
	{
		return mainActivity;
	}
	
	public void setMainActivity(HomeActivity s)
	{
		mainActivity = s;
	}
	
	public WatchAlarmKreyosActivityGroup getKreyosActivityGroup()
	{
		return groupActivity;
	}
	
	public void setKreyosActivityGroup(WatchAlarmKreyosActivityGroup s)
	{
		groupActivity = s;
	}

}
