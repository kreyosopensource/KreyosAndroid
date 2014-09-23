package com.kreyos.watch;

import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

public class SportsGridItem {
	
	private Button[] m_triggers;
	private TextView[] m_displays;

	public enum DISPLAYS 
	{
		STATS,
		VALUE,
		LABEL,
		MAX,
	}
	
	public enum TRIGGERS
	{
		DELETE,
		REFRESH,
		MAX
	}
	
	public SportsGridItem() 
	{
		m_displays = new TextView[ DISPLAYS.MAX.ordinal() ];
		m_triggers = new Button[ TRIGGERS.MAX.ordinal() ];
	}
	
	public void setDisplay( DISPLAYS p_type, TextView p_display  ) 
	{
		
		if( p_type.ordinal() >= DISPLAYS.MAX.ordinal() || p_type.ordinal() < 0 )
		{
			return;
		}
		m_displays[ p_type.ordinal() ] = p_display; 
		
		
	}
	
	public void setTriggers( TRIGGERS p_type, Button p_triggers ) 
	{
		
		if( p_type.ordinal() >= TRIGGERS.MAX.ordinal() || p_type.ordinal() < 0 )
		{
			return;
		}
		m_triggers[ p_type.ordinal() ] = p_triggers;
		
		
	}
	

	// follow up methods
	// --------------------
	// Update Displays
	// Callbacks on Buttons
	
	public void updateDisplay()
	{
		
	}
	
	public void updateDisplay( String p_stat, String p_value, String p_label ) 
	{
		String[] updatedDisplay = new String[]
		{
			p_stat,
			p_value,
			p_label
		};
		
		for( int i = 0; i < DISPLAYS.MAX.ordinal(); i++ )
		{
			m_displays[ i ].setText( updatedDisplay[i] );
		}
	}
	
	
}
