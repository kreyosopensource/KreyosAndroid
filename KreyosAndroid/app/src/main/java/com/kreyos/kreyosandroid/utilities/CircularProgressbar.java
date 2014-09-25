package com.kreyos.kreyosandroid.utilities;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.kreyos.kreyosandroid.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Paint.Align;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.View;

public class CircularProgressbar extends View {
	
	private Bitmap  mBitmap;
    private Paint   mPaint;
    private RectF   mOval;
    private float   mAngle;
    private String  mSteps       = "0";
    private String  mKM          = "0.0";
    private String  mCalories    = "0.0";
    private Paint   mTextPaint;
    private Bitmap  mBackGround;
    private Paint   mBackGroundPaint;
  

    public CircularProgressbar(Context context, AttributeSet attrs) {
    	
        super(context, attrs);
        // use your bitmap instead of R.drawable.ic_launcher
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.util_progress_circle_front);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOval = new RectF();
        mBackGround = BitmapFactory.decodeResource(getResources(), R.drawable.util_progress_circle_back);
        mBackGroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
   
       
        mTextPaint = new Paint();
        mTextPaint.setTextSize(100);
        mTextPaint.setTextAlign(Align.CENTER);
        mTextPaint.setColor(Color.WHITE);
        Typeface typeFace = Typeface.createFromAsset(context.getAssets(), "fonts/leaguegothic-regular-webfont.ttf");
        mTextPaint.setTypeface(typeFace);
        //mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
    }
   
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    	
        Matrix m = new Matrix();
        RectF src = new RectF(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
        RectF dst = new RectF(0, 0, w, h);
        m.setRectToRect(src, dst, ScaleToFit.CENTER);
        Shader shader = new BitmapShader(mBitmap, TileMode.CLAMP, TileMode.CLAMP);
        shader.setLocalMatrix(m);
        mPaint.setShader(shader);
        Shader backgroundShader = new BitmapShader(mBackGround, TileMode.CLAMP, TileMode.CLAMP);
        backgroundShader.setLocalMatrix(m);
        mBackGroundPaint.setShader(backgroundShader);
        m.mapRect(mOval, src);
    }

    @Override
    protected void onDraw(Canvas canvas) {
    	canvas.drawArc(mOval, 90, 360, true, mBackGroundPaint);
        canvas.drawArc(mOval, 90, mAngle, true, mPaint);
    	
    	// All size of text and position are based on screen size
    	
    	mTextPaint.setTextSize(getWidth() * 0.2f);
        canvas.drawText(mSteps, 
        		getWidth() / 2,
        		(getHeight() / 2) - (getHeight() * 0.1f),
        		mTextPaint);
        
        mTextPaint.setTextSize(getWidth() * 0.1f);
        canvas.drawText("Steps", 
        		getWidth() / 2,
        		(getHeight() / 2) + (getHeight() * 0.05f),
        		mTextPaint);
        
        mTextPaint.setTextSize(getWidth() * 0.1f);
        canvas.drawText(mKM, 
        		getWidth() / 2  - (getHeight() * 0.15f),
        		(getHeight() / 2) + (getHeight() * 0.25f),
        		mTextPaint);
        
        mTextPaint.setTextSize(getWidth() * 0.05f);
        canvas.drawText("kilometers", 
        		getWidth() / 2  - (getHeight() * 0.15f),
        		(getHeight() / 2) + (getHeight() * 0.35f),
        		mTextPaint);
        
        mTextPaint.setTextSize(getWidth() * 0.1f);
        canvas.drawText(mCalories, 
        		getWidth() / 2  + (getHeight() * 0.15f),
        		(getHeight() / 2) + (getHeight() * 0.25f),
        		mTextPaint);
        
        mTextPaint.setTextSize(getWidth() * 0.05f);
        canvas.drawText("calories", 
        		getWidth() / 2  + (getHeight() * 0.15f),
        		(getHeight() / 2) + (getHeight() * 0.35f),
        		mTextPaint);
    
    }
    
    public void setAngle(float p_angle) {
    	mAngle = p_angle;
    	postInvalidate();
    }
    
    public void setValue(String p_steps, 
    		String p_kilometers, 
    		String p_calories) {
    	mSteps = p_steps;
    	mKM = p_kilometers;
    	mCalories = p_calories;
    	invalidate();
    }
    
    ScheduledExecutorService executor;
    float maxAngle = 360;
	float maxTime = 3;
	float currentTime = 0;
	float percent = 0;
	float targetTime = 0;
	float targetAngle = 0;
	
	
    public void animateProgress(float pStepMax, float pStepCurrent, float pDistance, float pCalories) {
    	if (executor != null) {
    		executor.shutdown();
    	}

    	mSteps = "" + (int) pStepCurrent;
    	mKM = "" + pDistance;
    	mCalories = "" + pCalories;
    	
    	targetAngle = maxAngle * (pStepCurrent / pStepMax); //90
    	targetTime = maxTime * (targetAngle / maxAngle);
    	Runnable timerAnimation = new Runnable() {
    		public void run() {
    			currentTime += 0.001f;
    			if (currentTime >= targetTime) {
    				// Stop 
    				executor.shutdown();
    			}
    			percent = currentTime / targetTime;
    			float angle = targetAngle * percent;
    			setAngle(angle);
    		}
    	};
 
    	executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(timerAnimation, 0, 1, TimeUnit.MILLISECONDS);
    }
}
