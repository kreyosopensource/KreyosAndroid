package com.kreyos.watch;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;

public class KreyosUtility {

	public enum FONT_NAME {
		LEAGUE_GOTHIC_CONDENSED_ITALIC,
		LEAGUE_GOTHIC_CONDENSED_REGULAR,
		LEAGUE_GOTHIC_ITALIC,
		LEAGUE_GOTHIC_REGULAR
	}
	
	
	/*
	 * Change all font by looping on all child in the activity
	 */
	public static void overrideFonts(final Context context, final View v, FONT_NAME p_font ) {
	    try {
	    	
	        if (v instanceof ViewGroup) {
	        	
	            ViewGroup vg = (ViewGroup) v;
	            
	            for (int i = 0; i < vg.getChildCount(); i++) {
	                View child = vg.getChildAt(i);
	                overrideFonts(context, child, p_font);
	            }
	            
	        } else if (v instanceof TextView ) {
	        	
	        	// + ET 040714 : Get font based on name given in parameter
	            Typeface typeFace = null;
	            
	            switch( p_font ) {
	           
	            case LEAGUE_GOTHIC_CONDENSED_ITALIC:
	            	typeFace = Typeface.createFromAsset(context.getAssets(), "fonts/leaguegothic-condensed-italic-webfont.ttf");
	            	break;
	            	
	            case LEAGUE_GOTHIC_CONDENSED_REGULAR:
	            	typeFace = Typeface.createFromAsset(context.getAssets(), "fonts/leaguegothic-condensed-regular-webfont.ttf");
	            	break;
	            
	            case LEAGUE_GOTHIC_ITALIC:
	            	typeFace = Typeface.createFromAsset(context.getAssets(), "fonts/leaguegothic-italic-webfont.ttf");
	            	break;
	            
	            case LEAGUE_GOTHIC_REGULAR:
	            	typeFace = Typeface.createFromAsset(context.getAssets(), "fonts/leaguegothic-regular-webfont.ttf");
	            	break;
	            }
	            
	            ((TextView) v).setTypeface(typeFace);

	            
	            // + ET 04192014 : Adjust textview based on screen height
	            // Base screen height
	            int baseHeight = 1920;
	            
	            // Get screen height
	            DisplayMetrics dm = new DisplayMetrics();
	            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
	            
	            // Default scale size
	            float scaleX = 0;
	            float scaleY = 0;
	            
	            // Check if greater than base height
	            if(dm.heightPixels > baseHeight) {
	            	// Convert the excess to percent
	            	float excessSize = ((float)(dm.heightPixels) - baseHeight) / baseHeight;
	            	scaleY += ( 1 + excessSize);
	            
	            // Check if less than
	            } else if(dm.heightPixels < baseHeight) {
	            	scaleY = (float)(dm.heightPixels) / baseHeight;
	            
	            // Same screen height
	            } else {
	            	scaleY = 1;
	            }
	            
	            // Log.d("Scale", "Dm Height: " + dm.heightPixels);
	            // Log.d("Scale", "New Scale: " + scaleY);
	            // Set the computed scale
	            
	            // ((TextView) v).setScaleY(scaleY);
	            
	        }
	        
	    } catch (Exception e) {
	    	
	    }
	 }
		
	public static void showErrorMessage( Activity p_activity, String p_title, String p_message ) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(p_activity);
    	builder.setTitle(p_title);
    	builder.setMessage(p_message);
    	builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
        	 
            }
        });		
		// + ET 040714 : Create and show dialog
    	AlertDialog dialog = builder.create();
    	dialog.show();
	}
	
	public static void showPopUpDialog( Activity p_activity, String p_title, String p_message ) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(p_activity);
    	builder.setTitle(p_title);
    	builder.setMessage(p_message);
    	builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
        	 
            }
        });		
		// + ET 040714 : Create and show dialog
    	AlertDialog dialog = builder.create();
    	dialog.show();
	}
	
	public static byte[] convertBitmapToByteArray( Bitmap p_bitmap )
	{
		ByteArrayOutputStream blob = new ByteArrayOutputStream();
		p_bitmap.compress(CompressFormat.PNG, 0 /*ignored for PNG*/, blob);
		byte[] byteArray = blob.toByteArray();
		return byteArray;
	}
	
	public static Bitmap convertByteArrayToBitmap( byte[] p_byteArray)
	{
		Bitmap bitmap = BitmapFactory.decodeByteArray(p_byteArray , 0, p_byteArray .length);
		return bitmap;
	}
	
	public static String convertByteArrayToString(byte[] p_byteArray)
	{
//		String returnVal = null;
//		try {
//			returnVal = new String(p_byteArray, "ISO-8859-1");
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return returnVal;
		
		String encodedImage = Base64.encodeToString(p_byteArray, Base64.DEFAULT);
		return encodedImage;
	}
	
	public static byte[] convertStringToByteArray(String p_stringValue)
	{
		byte[] returnVal = null;
//		try {
//			returnVal = p_stringValue.getBytes("ISO-8859-1");
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		returnVal = Base64.decode(p_stringValue, Base64.DEFAULT);
		return returnVal;
	}

	public static String setDataForDisplay(int p_value)
	{
		DecimalFormat f = new DecimalFormat("##.##");
		String returnVal = "";
		int divisor 	= 1000;
		
		if(p_value < divisor)
		{
			return returnVal = f.format(p_value);
		}
		
		int value 		= p_value / divisor;
		int remainder 	= p_value % divisor;
		returnVal 		= "" +  f.format((value + remainder)) + "K";
		return returnVal;
	}
	
	public static String setDataForDisplay(double p_value)
	{
		DecimalFormat f = new DecimalFormat("##.##");
		String returnVal = "";
//		int divisor 	= 1000;
//		
//		if(p_value < divisor)
//		{
//			return returnVal = f.format(p_value);
//		}
//		
//		double value 		= p_value / divisor;
//		double remainder 	= p_value % divisor;
//		returnVal 		= "" +  f.format((value + remainder)) + "K";
		returnVal 		= "" +  f.format(p_value);
		return returnVal;
	}
	
}
