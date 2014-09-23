package com.kreyos.watch;

import java.text.AttributedCharacterIterator.Attribute;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.kreyos.watch.R;

public class DailyTargetCustomSlider extends View
{

	Typeface m_typeFace = Typeface.createFromAsset(getContext().getAssets(), "fonts/leaguegothic-regular-webfont.ttf");
	Bitmap m_bitmap 	= null;
	float m_posX		= 0;
	float m_posY		= 0;
	
	boolean m_isSetOnBeginnning = false;
	boolean m_isTouchingBitmap 	= false;
	
	public DailyTargetCustomSlider(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		setMinimumHeight(100);
		setMinimumWidth(100);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void onDraw(Canvas cv) 
	{
		// TODO Auto-generated method stub
//		super.onDraw(canvas);
		
		
		cv.drawColor(Color.WHITE);
		
        Paint p = new Paint();
        p.setColor(Color.GRAY);
        p.setStrokeWidth(cv.getHeight() * 0.02f); // width of line
        
        float width = (cv.getWidth() * 0.9f) - (cv.getWidth() * 0.1f);
        
        cv.drawLine(cv.getWidth()  * 0.1f,
        			cv.getHeight() * 0.4f, 
        			cv.getWidth()  * 0.9f, 
        			cv.getHeight() * 0.4f, 
        			p);
        
        float divider = cv.getWidth()  * 0.1f;
        String[] textKilometers = new String[]
        {
        	"4K",
        	"6K",
        	"8K",
        	"10K",
        	"12K"
        	
        };
        
        Paint textPaint = new Paint();
        textPaint.setTextSize(80);
        textPaint.setTextAlign(Align.CENTER);
        textPaint.setColor(Color.GRAY);
        textPaint.setTypeface(m_typeFace);

        for(int i = 0; i < 5; i++)
        {
        	// draw circles
        	cv.drawCircle(divider, 
      			  		  cv.getHeight() * 0.4f, 
      			          cv.getHeight() * 0.05f, // size of circle
      			          p);
        	
        	// draw text
        	cv.drawText(textKilometers[i], 
        				divider, 
        				cv.getHeight() * 0.25f,  // y position of text
        				textPaint);
        	
        	// adjust position
        	divider += (width * 0.25f);
        }
        
    	m_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.button_slider);

    	if(!m_isSetOnBeginnning)
    	{
    		m_posX 		= cv.getWidth()  * 0.5f - (m_bitmap.getHeight() / 2);
            m_posY 		= cv.getHeight() * 0.4f - (m_bitmap.getHeight() / 2);
            m_isSetOnBeginnning = true;
            
    	}
    	
    	if(m_posX > cv.getWidth() * 0.9f)
    	{
    		m_posX = cv.getWidth() * 0.9f - (m_bitmap.getWidth() * 0.5f);
    	}
    	else if(m_posX < cv.getWidth() * 0.1f)
    	{
    		m_posX = (cv.getWidth() * 0.1f) - (m_bitmap.getWidth() * 0.5f);
    	}
    	
    	Paint bitmapPaint = new Paint();
        cv.drawBitmap(m_bitmap, 
        			  m_posX, 
        			  m_posY, 
        			  new Paint());
        
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) 
	{
		// TODO Auto-generated method stub
//		return super.onTouchEvent(event);
		
		
		switch( event.getAction() & MotionEvent.ACTION_MASK )
		{
			case MotionEvent.ACTION_DOWN: 
			{
				Rect rect = new Rect(Math.round(m_posX), 
									 Math.round(m_posY), 
								     Math.round(m_posX + m_bitmap.getWidth()), 
								     Math.round(m_posY + m_bitmap.getHeight()));
				// Check if touched
				if( rect.contains(Math.round(event.getX()), Math.round(event.getY()))) 
				{
					  Log.d( "LOG", "TOUCHED BITMAP" );
					  m_isTouchingBitmap = true;
				}
			}
			break;
			
			case MotionEvent.ACTION_MOVE:
			{
				if(m_isTouchingBitmap)
				{
					Log.d( "LOG", "MOVING BITMAP");
					m_posX = event.getX() - (m_bitmap.getWidth() * 0.5f);
				}
			}
			break;
			
			case MotionEvent.ACTION_UP:
			{
				m_isTouchingBitmap = false;
			}
			break;
		}
		
		invalidate();
		return true;
	}
	
	
	
}
