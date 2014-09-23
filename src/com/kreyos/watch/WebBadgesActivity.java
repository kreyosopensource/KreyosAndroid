package com.kreyos.watch;

import com.kreyos.watch.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.Menu;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


public class WebBadgesActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature( Window.FEATURE_NO_TITLE );
		setContentView(R.layout.web_badge);
		
		try 
		{
			Panel panel = (Panel)findViewById(R.id.SurfaceView01);
			panel.startDisplay();
		}
		catch ( Exception ex )
		{
			
		}
	}
}
