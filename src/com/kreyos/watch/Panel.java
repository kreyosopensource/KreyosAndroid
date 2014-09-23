package com.kreyos.watch;

import java.util.ArrayList;
import java.util.List;

import com.kreyos.watch.R;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.AvoidXfermode.Mode;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.LinearGradient;
import android.graphics.MaskFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

public class Panel extends SurfaceView implements SurfaceHolder.Callback {
	
	CanvasThread canvasthread;
	
	// Zoom & pan touch variables
	int y_old = 0, y_new = 0;
	int zoomMode = 0;
	float pinch_dist_old = 0, pinch_dist_new = 0;
	
	// New and old pinch distance to determine Zoom scale
	int zoomControllerScale1;
	
	// These matrices will be used to move and zoom image
	
	List<TestStructure> testTest = new ArrayList<TestStructure>();
	
	// Remember some things for zooming
	PointF start = new PointF();
	PointF mid = new PointF();
	float oldDist = 1f;
	
	// We can be in one of these states
	static final int NONE = 0;
	static final int PAN = 1;
	static final int ZOOM = 2;
	int mode = NONE;
	
	private static final String TAG = "DEBUG";

	private boolean IsDisplaying = false;
	
	private int[] topBadgeIcons = new int[]
		        {
		    		R.drawable.active_time10,
		    		R.drawable.active_time24,
		    		R.drawable.active_time50,
		    		R.drawable.active_time100,
		    		R.drawable.active_time150,
		    		R.drawable.active_time200,
		    		R.drawable.active_time250,
		    		R.drawable.active_time300,
		    		R.drawable.active_time400,
		    		R.drawable.active_time500,
		    		R.drawable.active_time750,
		    		R.drawable.active_time1k,
		    		R.drawable.active_time1_25k,
		    		R.drawable.active_time1_5k,
		    		R.drawable.active_time2k,
		    		R.drawable.active_time2_5k,
		    		R.drawable.active_time5k,
		    		R.drawable.active_time7_5k,
		    		R.drawable.active_time10k
		        };
		        
	private int[] bottomLeftBadge = new int[] 
		        {
		        	R.drawable.distance_10,
		        	R.drawable.distance_25,
		        	R.drawable.distance_50,
		        	R.drawable.distance_100,
		        	R.drawable.distance_200,
		        	R.drawable.distance_300,
		        	R.drawable.distance_400,
		        	R.drawable.distance_500,
		        	R.drawable.distance_750,
		        	R.drawable.distance_1k,
		        	R.drawable.distance_1_5k,
		        	R.drawable.distance_2k,
		        	R.drawable.distance_2_5k,
		        	R.drawable.distance_5k,
		        	R.drawable.distance_7_5k,
		        	R.drawable.distance_10k
		        };
	
	public Panel( Context context, AttributeSet attrs )
	{
		
		super( context, attrs );
		getHolder().addCallback( this );
        canvasthread = new CanvasThread( getHolder(), this );
        setFocusable( true );
        
	}
	
	public void startDisplay() 
	{
        
        WindowManager wm = ( WindowManager ) this.getContext().getSystemService( Context.WINDOW_SERVICE );
        Display display = wm.getDefaultDisplay();

        int posX = Math.round( (display.getWidth() * 0.5f) );
        int posY = Math.round( (display.getHeight() * 0.5f) );
       
        // center badge
        TestStructure center = new TestStructure();
        
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.runningicon);
        center.Image = bitmap;
        Paint paint = new Paint();
        center.ImagePaint = paint;
        Matrix matrix = new Matrix();
        matrix.setTranslate( posX - Math.round( bitmap.getWidth() * 0.5f ) , //- Math.round( bitmap.getWidth() * 0.5f )
        					 posY - Math.round( bitmap.getHeight() ) * 0.5f ); // - - Math.round( bitmap.getHeight() ) * 0.5f 
       
        center.CurrentMatrix = matrix;
        matrix = new Matrix();
        center.SavedMatrix = matrix;
       
        testTest.add( center );
        
       
        
        
        // top badge
        posX = Math.round ( center.getRect().centerX() * 0.5f );
        posY = center.getRect().centerY() - Math.round( center.getRect().height() * 1.5f );
        
        for( int i = 0; i < topBadgeIcons.length; i++ ) 
        {
	    	  TestStructure badges = new TestStructure();
	          
	          Bitmap topBitmap = BitmapFactory.decodeResource(getResources(), topBadgeIcons[i] );
	          badges.Image = topBitmap;
	          Paint topPaint = new Paint();
	          badges.ImagePaint = topPaint;
	          Matrix topMatrix = new Matrix();
	          topMatrix.setTranslate( posX, //- Math.round( bitmap.getWidth() * 0.5f )
	        		  				  posY ); // - - Math.round( bitmap.getHeight() ) * 0.5f 
	          badges.CurrentMatrix = topMatrix;
	          topMatrix = new Matrix();
	          badges.SavedMatrix = topMatrix;
	          
	          posY -= Math.round( topBitmap.getHeight() * 1.5f );
	          
	          
	          testTest.add( badges );
        }
        
        
        // top left badge
        posX = Math.round ( center.getRect().centerX()  * -1 );
        posY = center.getRect().centerY() + Math.round( center.getRect().height() * 0.7f  );
        
        for( int i = 0; i < bottomLeftBadge.length; i++ ) 
        {
        	  TestStructure badges = new TestStructure();
	          
	          Bitmap topBitmap = BitmapFactory.decodeResource(getResources(), bottomLeftBadge[i] );
	          badges.Image = topBitmap;
	          Paint topPaint = new Paint();
	          badges.ImagePaint = topPaint;
	          Matrix topMatrix = new Matrix();
	          topMatrix.setTranslate( posX, //- Math.round( bitmap.getWidth() * 0.5f )
	        		  				  posY ); // - - Math.round( bitmap.getHeight() ) * 0.5f 
	          badges.CurrentMatrix = topMatrix;
	          topMatrix = new Matrix();
	          badges.SavedMatrix = topMatrix;
	          
	          posX -= topBitmap.getWidth();
	          posY += topBitmap.getHeight();
	          
	          testTest.add( badges );
        }
       
        
        // top right badge
        posX = Math.round ( center.getRect().centerX() * 2f );
        posY = center.getRect().centerY() + Math.round( center.getRect().height() * 0.7f  );
        
        for( int i = 0; i < bottomLeftBadge.length; i++ ) 
        {
        	  TestStructure badges = new TestStructure();
	          
	          Bitmap topBitmap = BitmapFactory.decodeResource(getResources(), bottomLeftBadge[i] );
	          badges.Image = topBitmap;
	          Paint topPaint = new Paint();
	          badges.ImagePaint = topPaint;
	          Matrix topMatrix = new Matrix();
	          topMatrix.setTranslate( posX, //- Math.round( bitmap.getWidth() * 0.5f )
	        		  				  posY ); // - - Math.round( bitmap.getHeight() ) * 0.5f 
	          badges.CurrentMatrix = topMatrix;
	          topMatrix = new Matrix();
	          badges.SavedMatrix = topMatrix;
	          
	          posX += topBitmap.getWidth();
	          posY += topBitmap.getHeight();
	          
	          
	          testTest.add( badges );
        }
        
        IsDisplaying = true;
	}
	
	@Override
	public void surfaceChanged( SurfaceHolder holder, int format, int width, int height ) 
	{
		// TODO Auto-generated method stub
	}
	
	@Override
	public void surfaceCreated( SurfaceHolder holder ) 
	{
		// TODO Auto-generated method stub
		
		// TODO: OPTIMIZED IT IT'S A VERY BAD SOLUTION
		
		canvasthread = new CanvasThread( getHolder(), this );
		canvasthread.setRunning(true);
		canvasthread.start();
		
		/*
		try
		{
			canvasthread.setRunning(true);
			canvasthread.start();
		}
			catch ( Exception ex )
		{
			Log.d(TAG, "" + ex );
			
			canvasthread = new CanvasThread( getHolder(), this );
			canvasthread.setRunning(true);
			canvasthread.start();
		}
		*/
				
			
		
	}
	
	@Override
	public void surfaceDestroyed( SurfaceHolder holder ) 
	{
		// TODO Auto-generated method stub
		boolean retry = true;
		canvasthread.setRunning(false);
		while(retry) 
		{
			try
			{
				canvasthread.join();
				retry = false;
			} 
			catch ( InterruptedException e) 
			{
				// We will try it again again...
			}
		}
	}
	
	@Override
	public void onDraw( Canvas canvas ) {
		
		if( !IsDisplaying )
		{
			return;
		}
		
		try 
		{
			canvas.drawColor( Color.WHITE );
			
			// draw lines logo to bages
			canvas.drawLine(
					testTest.get(0).getRect().exactCenterX(),
					testTest.get(0).getRect().exactCenterY(), 
					testTest.get(1).getRect().exactCenterX(), 
					testTest.get(1).getRect().exactCenterY(), new Paint());
			
			// line to top
			canvas.drawLine(
					testTest.get(0).getRect().exactCenterX(),
					testTest.get(0).getRect().exactCenterY(), 
					testTest.get(topBadgeIcons.length).getRect().exactCenterX(), 
					testTest.get(topBadgeIcons.length).getRect().exactCenterY(), new Paint());
			
			
			// line to bottom left 
			canvas.drawLine(
					testTest.get(0).getRect().exactCenterX(),
					testTest.get(0).getRect().exactCenterY(), 
					testTest.get(topBadgeIcons.length + bottomLeftBadge.length).getRect().exactCenterX(), 
					testTest.get(topBadgeIcons.length + bottomLeftBadge.length ).getRect().exactCenterY(), new Paint());
			
			
			// line to bottom right 
			canvas.drawLine(
					testTest.get(0).getRect().exactCenterX(),
					testTest.get(0).getRect().exactCenterY(), 
					testTest.get(testTest.size() - 1).getRect().exactCenterX(), 
					testTest.get(testTest.size() - 1).getRect().exactCenterY(), new Paint());
			
			/*
			for( int i = 1; i < topBadgeIcons.length; i++ ) {
				
				TestStructure ts = testTest.get(i);
				try
				{
					TestStructure ts2 = testTest.get(i+1);
					canvas.drawLine(ts.getRect().exactCenterX(), 
									ts.getRect().exactCenterY(), 
									ts2.getRect().exactCenterX(), 
									ts2.getRect().exactCenterY(), 
									new Paint());
				} 
				catch ( Exception ex )
				{
					
				}
			}
			*/
			
			
			for( int i = 0; i< testTest.size(); i++ )
			{
				TestStructure ts = testTest.get(i);
				canvas.drawBitmap( ts.Image, ts.CurrentMatrix, ts.ImagePaint );
			}
			
			
		} 
		catch( Exception ex ) 
		{
		
		}
		
	}
	
	private Bitmap maskingImage(Bitmap s, int drawable) {
    	
        Bitmap original = s;
        Bitmap mask = BitmapFactory.decodeResource(getResources(),drawable);
        
        Bitmap result = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas mCanvas = new Canvas(result);
        
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        
        mCanvas.drawBitmap(original, 0, 0, null);
        mCanvas.drawBitmap(mask, 0, 0, paint);
        
        paint.setXfermode(null);
        return result;
    }
	
	private void PanZoomWithTouch( MotionEvent event ) {
		
		if( !IsDisplaying ) 
		{
			return;
		}
		
		switch( event.getAction() & MotionEvent.ACTION_MASK )
		{
		
		// When first finger down, get first point
		case MotionEvent.ACTION_DOWN: 
			
			for( int i=0; i<testTest.size(); i++ ) 
			{
				TestStructure ts = testTest.get(i);
				ts.SavedMatrix.set(ts.CurrentMatrix);
			}
			
			start.set(event.getX(), event.getY());
			mode = PAN;
			Log.d(TAG, "MODE=PAN");
			break;
			
		// When 2nd finger down, get second point
		case MotionEvent.ACTION_POINTER_DOWN:
			
			oldDist = spacing( event );
			if( oldDist > 10f )
			{
				for( int i = 0; i < testTest.size(); i++ ) 
				{
					TestStructure ts = testTest.get( i );
					ts.SavedMatrix.set( ts.CurrentMatrix );
				}
				midPoint( mid, event );
				mode = ZOOM;
				Log.d(TAG, "mode=ZOOM");
			}
			break;
		
		// When bitmap touched
		case MotionEvent.ACTION_UP:
			
			for( int i = 0; i < testTest.size(); i++ ) 
			{
				TestStructure ts = testTest.get( i );
				Rect rect = ts.getRect();
				
				// Check if touched
				if( rect.contains( Math.round(event.getX()), Math.round(event.getY()) )) 
				{
					  Log.d( TAG, "TOUCHED BITMAP" );
					  Intent intent = new Intent( this.getContext().getApplicationContext(), AddGoalActivity.class);
					  this.getContext().startActivity(intent);
				}
			}
			mode = NONE;
			Log.d(TAG, "mode=NONE");
			break;
			
		// When both fingers are released, do nothing
		case MotionEvent.ACTION_POINTER_UP:
			mode = NONE;
			Log.d(TAG, "mode=NONE");
			break;
			
		// When fingers are dragged, transform matrix panning
		case MotionEvent.ACTION_MOVE:
			if( mode == PAN ) {
				for( int i = 0; i < testTest.size(); i++ ) 
				{
					TestStructure ts = testTest.get(i);
					ts.CurrentMatrix.set( ts.SavedMatrix );
					ts.CurrentMatrix.postTranslate( event.getX() - start.x, event.getY() - start.y );
				}
			} 
			else if ( mode == ZOOM )  
			{
				float newDist = spacing(event);
				if( newDist > 10 )
				{
					for( int i = 0; i < testTest.size(); i++ )
					{
						TestStructure ts = testTest.get( i );
						ts.CurrentMatrix.set(ts.SavedMatrix);
		        		float scale = newDist / oldDist;
		        		Log.d(TAG, "SCALE =" + scale );
		        		ts.CurrentMatrix.postScale(scale, scale, mid.x, mid.y);
					}
				}
			}
			break;
		}
	}
	
	
	// Determine the space between the first two finger
	private float spacing( MotionEvent event ) {
		float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
	}
	
	// Calculate the mid point of the first two finger
	private void midPoint( PointF point, MotionEvent event ) {
		float x = event.getX(0) + event.getX(1);
	    float y = event.getY(0) + event.getY(1);
	    point.set(x / 2, y / 2);
	}
	
	@Override
	public boolean onTouchEvent( MotionEvent event ) {
		if( !IsDisplaying ) {
			return true;
		}
		
		PanZoomWithTouch( event );
		
		// Necessary to repaint the canvas
		invalidate(); 
		return true;
	}
	
}