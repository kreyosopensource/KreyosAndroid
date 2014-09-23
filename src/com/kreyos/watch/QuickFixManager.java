package com.kreyos.watch;

import android.util.Log;

public class QuickFixManager 
{
	
	private static QuickFixManager instance = null;
	   
	protected QuickFixManager() {
      // Exists only to defeat instantiation.
	}
   
	public static QuickFixManager getInstance() 
	{
	   
      if(instance == null) {
         instance = new QuickFixManager();
      }
      
      return instance;
	}
	
	public byte SEND_ID = -1;
	public byte SEND_ID_FIRMWARE_TUTORIAL = 1;
	
	TutorialActivity m_tutorialActvity;
	
	public void onTriggerCallback()
	{
		if( SEND_ID == SEND_ID_FIRMWARE_TUTORIAL )
		{
			m_tutorialActvity.onCompleteUpdate();
		}
	}
}
