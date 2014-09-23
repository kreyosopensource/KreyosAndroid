package com.kreyos.watch;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.kreyos.watch.R;

public class SetNewGoalActivity extends Activity{
	
	private ImageView imageView_setngoalwalking;
	private ImageView imageView_setngoalrunning;
	private ImageView imageView_setngoalbiking;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature( Window.FEATURE_NO_TITLE );
		setContentView(R.layout.set_new_goal);
		
		setUI();
	}
	
	private void setUI(){
		imageView_setngoalwalking = (ImageView) findViewById(R.id.imageView_setworkout);
		imageView_setngoalrunning = (ImageView) findViewById(R.id.imageView_setngoalrunning);
		imageView_setngoalbiking  = (ImageView) findViewById(R.id.imageView_setngoalbiking);
		
		//setting up the image from SVG File
		//setSVG(getResources(), R.raw.walking, imageView_setngoalwalking);
		
		 
		// Parse the SVG file from the resource
		//SVG svg = SVGParser.getSVGFromResource(getResources(), R.raw.active_time1_5k);
		// Get a drawable from the parsed SVG and set it as the drawable for the ImageView
		//imageView_setngoalwalking.setImageDrawable(svg.createPictureDrawable());
		
		//setSVG(getResources(), R.raw.running, imageView_setngoalrunning);
		//setSVG(getResources(), R.raw.cycling, imageView_setngoalbiking);
		
		
		imageView_setngoalwalking.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setCallback( v );
			}
		});
		
		imageView_setngoalrunning.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setCallback( v );
			}
		});
		
		imageView_setngoalbiking.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setCallback( v );
			}
		});
		
	}
	
	
	private void setCallback( View v )
	{

		Intent intent = null;
			
		switch ( v.getId() )
		{
		case R.id.imageView_setworkout:
		case R.id.imageView_setngoalrunning:
		case R.id.imageView_setngoalbiking:
			intent = new Intent(getApplicationContext(), WebBadgesActivity.class );
			break;
		}
		
		if( intent != null )
		{
			startActivity(intent);
		}
		
	
	}
	
	/*
	private void setSVG(Resources res, int raw, ImageView image){
		// Parse the SVG file from the resource
		SVG svg = SVGParser.getSVGFromResource(res, raw);
		// Get a drawable from the parsed SVG and set it as the drawable for the ImageView
		image.setImageDrawable(svg.createPictureDrawable());
	}
	*/
	
	
}
