package com.kreyos.watch;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.coboltforge.slidemenu.SlideMenu;
import com.kreyos.watch.R;

public class AddGoalActivity extends Activity {
	
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate(savedInstanceState);
		requestWindowFeature( Window.FEATURE_NO_TITLE );
		setContentView( R.layout.add_goal );
	
		
		Button btn_startGoal = ( Button )findViewById( R.id.btn_startGoal );
		btn_startGoal.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setCallback( v );	
			}
		});
		
		Button btn_cancelPickAnother = ( Button )findViewById( R.id.btn_cancelPickAnother );
		btn_cancelPickAnother.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setCallback( v );	
			}
		});
	}
	
	
	private void setCallback( View v )
	{

		Intent intent = null;
			
		switch ( v.getId() )
		{
		case R.id.btn_startGoal:
			//intent = new Intent(getApplicationContext(), SportsActivity1.class );
			break;
			
		case R.id.btn_cancelPickAnother:
			intent = new Intent(getApplicationContext(), WebBadgesActivity.class );
			break;
		}
		
		if( intent != null )
		{
			startActivity(intent);
		}
		
	
	}
	
	
}
