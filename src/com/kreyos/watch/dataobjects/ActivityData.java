package com.kreyos.watch.dataobjects;

import com.kreyos.watch.adapter.ActivityStatsAdapter;

public class ActivityData
{
	public ActivityStatsAdapter m_adapter 	= null;
	public String m_steps 					= "";
	public String m_distance 				= "";
	public String m_calories 				= "";
	
	public ActivityData( ActivityStatsAdapter p_adapter, String p_steps, String p_distance, String p_calories)
	{
		m_adapter 	= p_adapter;
		m_steps 	= p_steps;
		m_distance	= p_distance;
		m_calories	= p_calories;
	}
}
