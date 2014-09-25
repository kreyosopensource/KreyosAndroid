package com.kreyos.kreyosandroid.dataobjects;

import com.kreyos.kreyosandroid.adapter.ActivityStatsAdapter;

public class ActivityData
{
	public ActivityStatsAdapter     mAdapter    = null;
	public String                   mSteps      = "";
	public String                   mDistance   = "";
	public String                   mCalories   = "";
	
	public ActivityData( ActivityStatsAdapter pAdapter, String pSteps, String pDistance, String pCalories)
	{
		mAdapter    = pAdapter;
		mSteps      = pSteps;
		mDistance   = pDistance;
		mCalories   = pCalories;
	}
}
