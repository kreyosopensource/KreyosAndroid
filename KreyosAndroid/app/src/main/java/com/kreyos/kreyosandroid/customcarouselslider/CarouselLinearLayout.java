package com.kreyos.kreyosandroid.customcarouselslider;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

import com.kreyos.kreyosandroid.utilities.Constants;

public class CarouselLinearLayout extends LinearLayout {
	private float scale = CarouselManager.BIG_SCALE;

	public CarouselLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
        Log.d(Constants.TAG_DEBUG, "( CarouselLinearLayout ) - constructor 1");
	}

	public CarouselLinearLayout(Context context) {
		super(context);
        Log.d(Constants.TAG_DEBUG, "( CarouselLinearLayout ) - constructor 2");
	}

	public void setScaleBoth(float scale)
	{
		this.scale = scale;
		this.invalidate(); 	// If you want to see the scale every time you set
							// scale you need to have this line here, 
							// invalidate() function will call onDraw(Canvas)
							// to redraw the view for you
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// The main mechanism to display scale animation, you can customize it
		// as your needs
		int w = this.getWidth();
		int h = this.getHeight();
		canvas.scale(scale, scale, w/2, h/2);
		
		super.onDraw(canvas);
	}
}