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
import android.widget.TextView;
import com.coboltforge.slidemenu.SlideMenu;
import com.kreyos.watch.R;

public class AchievedGoalActivity extends Activity {
	
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate(savedInstanceState);
		requestWindowFeature( Window.FEATURE_NO_TITLE );
		setContentView( R.layout.add_goal );
	
		
		Button btn_startGoal = ( Button )findViewById( R.id.btn_setNewGoal );
		btn_startGoal.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = null;
				intent = new Intent(getApplicationContext(), SetNewGoalActivity.class );
				if( intent != null )
				{
					startActivity(intent);
				}
			}
		});

	}
	
	private void setGoalDescription() 
	{
		ImageView badge = (ImageView)findViewById( R.id.image_achievedGoalBagde );
		TextView title = (TextView)findViewById( R.id.txt_achieveGoalDesc ); 
		
		
		// change or get to sqlite to replace data
	}
	
	
}
