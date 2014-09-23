package com.kreyos.watch.utils;

import com.kreyos.watch.HomeActivity;
import com.kreyos.watch.KreyosActivity;
import com.kreyos.watch.TestStructure;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

public class CircularImageView extends ImageView {
   
	private Rect mTouchRect;
	private HomeActivity mActivity;
	
	public CircularImageView(Context context) {
        super(context);
    }

    public CircularImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CircularImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    public void setActivityCallback(HomeActivity p_activity) {
    	mActivity = p_activity;
    }

    @Override
    protected void onDraw(Canvas canvas) {
    	
        Drawable drawable = getDrawable(); 
        
        if (drawable == null) {
            return;
        }

        if (getWidth() == 0) {
            return; 
        }
        
        if (getHeight() == 0) {
        	return;
        }
        
        
        
        float halfPercent = 0.5f;
        int borderX = (int) ((int)getWidth() * halfPercent);
        int borderY = (int) ((int)getHeight() * halfPercent);
        int borderRadius = borderY;
        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.WHITE);
        canvas.drawCircle(borderX, borderY, borderRadius, borderPaint);
        
        Bitmap b =  ((BitmapDrawable)drawable).getBitmap();
        Bitmap bitmap = b.copy(Bitmap.Config.ARGB_8888, true);      
        Bitmap roundBitmap =  getCroppedBitmap(bitmap, (int)(getHeight() * 0.95f));
        int photoX = (int)((getWidth() * halfPercent) - (roundBitmap.getWidth() * halfPercent));
        int photoY = (int)((getHeight() * halfPercent) - (roundBitmap.getHeight() * halfPercent));
        canvas.drawBitmap(roundBitmap, photoX, photoY, null);
       
        mTouchRect = new Rect(photoX, 
        		photoY, 
        		photoX + roundBitmap.getWidth(), 
        		photoY + roundBitmap.getHeight());
    }
    

    public static Bitmap getCroppedBitmap(Bitmap bmp, int radius) {
        Bitmap sbmp;
        if(bmp.getWidth() != radius || bmp.getHeight() != radius)
            sbmp = Bitmap.createScaledBitmap(bmp, radius, radius, false);
        else
            sbmp = bmp;

        Bitmap output = Bitmap.createBitmap(sbmp.getWidth(), sbmp.getHeight(), Bitmap.Config.ARGB_8888);
        final Rect rect = new Rect(0, 0, sbmp.getWidth(), sbmp.getHeight());

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);      
        paint.setColor(Color.parseColor("#BAB399"));

        Canvas c = new Canvas(output);        
        c.drawARGB(0, 0, 0, 0);
        c.drawCircle(sbmp.getWidth() / 2+0.7f, sbmp.getHeight() / 2+0.7f, sbmp.getWidth() / 2+0.1f, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        c.drawBitmap(sbmp, rect, rect, paint);

        return output;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	// TODO Auto-generated method stub
    	onTouchBitmap(event);
    	return super.onTouchEvent(event);
    	
    }
    
    private void onTouchBitmap(MotionEvent event) {
    	switch( event.getAction() & MotionEvent.ACTION_MASK ) {
		// When first finger down, get first point
		case MotionEvent.ACTION_DOWN: 
			if (mTouchRect == null) {
				return;
			}
			if (!mTouchRect.contains(Math.round(event.getX()), Math.round(event.getY()))) {
				return;
			}
			mActivity.showPhotoDialog();
			break;
		}
    }
}