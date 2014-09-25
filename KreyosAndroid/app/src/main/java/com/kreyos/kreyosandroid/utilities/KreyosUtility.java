package com.kreyos.kreyosandroid.utilities;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.TimeZone;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import android.graphics.Bitmap.CompressFormat;


public class KreyosUtility {
	/*
	 * Change all font by looping on all child in the activity
	 */
	public static void overrideFonts(final Context pContext, final View pView, Constants.FONT_NAME pFont ) {
	    try {
	    	
	        if (pView instanceof ViewGroup) {
	        	
	            ViewGroup vg = (ViewGroup) pView;
	            
	            for (int i = 0; i < vg.getChildCount(); i++) {
	                View child = vg.getChildAt(i);
	                overrideFonts(pContext, child, pFont);
	            }
	            
	        } else if (pView instanceof TextView ) {
	        	
	        	// + ET 040714 : Get font based on name given in parameter
	            Typeface typeFace = null;
	            
	            switch( pFont ) {
	           
	            case LEAGUE_GOTHIC_CONDENSED_ITALIC:
	            	typeFace = Typeface.createFromAsset(pContext.getAssets(), "fonts/leaguegothic-condensed-italic-webfont.ttf");
	            	break;
	            	
	            case LEAGUE_GOTHIC_CONDENSED_REGULAR:
	            	typeFace = Typeface.createFromAsset(pContext.getAssets(), "fonts/leaguegothic-condensed-regular-webfont.ttf");
	            	break;
	            
	            case LEAGUE_GOTHIC_ITALIC:
	            	typeFace = Typeface.createFromAsset(pContext.getAssets(), "fonts/leaguegothic-italic-webfont.ttf");
	            	break;
	            
	            case LEAGUE_GOTHIC_REGULAR:
	            	typeFace = Typeface.createFromAsset(pContext.getAssets(), "fonts/leaguegothic-regular-webfont.ttf");
	            	break;/**/
	            }
	            
	            ((TextView) pView).setTypeface(typeFace);

	            
	            // + ET 04192014 : Adjust textview based on screen height
	            // Base screen height
	            int baseHeight = 1920;
	            
	            // Get screen height
	            DisplayMetrics dm = new DisplayMetrics();
	            ((Activity) pContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
	            
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

    public static void showInfoMessage( Activity p_activity, String p_title, String p_message ) {

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



    // IMAGES
    /***************************************************************************************************************************/

    public static Bitmap getScaledBitmap(String picturePath, int width, int height) {
        BitmapFactory.Options sizeOptions = new BitmapFactory.Options();
        sizeOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(picturePath, sizeOptions);

        Log.d("KreyosActivity", "Image Size: Height:" + sizeOptions.outHeight);
        Log.d("KreyosActivity", "Image Size: Width:" + sizeOptions.outWidth);

		/*
		 * if (sizeOptions.outHeight < 800 || sizeOptions.outWidth < 800) {
		 * Log.d("Photo", "Return Null"); return null; }
		 */

        int inSampleSize = calculateInSampleSize(sizeOptions, width, height);

        sizeOptions.inJustDecodeBounds = false;
        sizeOptions.inSampleSize = inSampleSize;

        return BitmapFactory.decodeFile(picturePath, sizeOptions);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    public static byte[] convertBitmapToByteArray( Bitmap p_bitmap ) {
        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        p_bitmap.compress(CompressFormat.PNG, 0 /*ignored for PNG*/, blob);
        byte[] byteArray = blob.toByteArray();
        return byteArray;
    }

    public static String convertByteArrayToString(byte[] p_byteArray) {
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

    public static Bitmap convertByteArrayToBitmap( byte[] p_byteArray)
    {
        Bitmap bitmap = BitmapFactory.decodeByteArray(p_byteArray , 0, p_byteArray .length);
        return bitmap;
    }



    // NETWORK SERVICES
    /***************************************************************************************************************************/

    public static boolean hasConnection( Context p_context ) {
        ConnectivityManager connectivityManager = (ConnectivityManager)p_context.getSystemService( Context.CONNECTIVITY_SERVICE );
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }



    // PROGRESS DIALOG
    /***************************************************************************************************************************/
    private static ProgressDialog mProgressDialog = null;

    public static void showLoadingProgress(boolean pShow, String pTitle, String pMessage, Context pContext) {

        if( pShow ) {
            mProgressDialog = ProgressDialog.show( pContext, pTitle, pMessage, true);
            mProgressDialog.setCancelable(false);

        } else if ( mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }


    // DATE (Params: must be 1-7 only)
    /***************************************************************************************************************************/
    private static final String[] DAY_OF_WEEK 	= new String[]{ "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
    private static final String[] MONTHS 		= new String[]{ "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
    private static final long EPOCH_UNIT_DAY	= 86400L;
    private static final long EPOCH_UNIT_MONTH	= EPOCH_UNIT_DAY * 30L;
    private static final long EPOCH_UNIT_YEAR	= EPOCH_UNIT_MONTH * 12L;

    /**
     * DAY_OF_WEEK
     * 0 - Sunday
     *
     * Calendar cal = Utils.calendar();
     * int year = cal.get( Calendar.YEAR );
     * int month = cal.get( Calendar.MONTH );
     * int dayOfMonth = cal.get( Calendar.DAY_OF_MONTH );
     * int dayOfWeek = cal.get( Calendar.DAY_OF_WEEK );
     * Log.i( "CalendarInfo", "CalendarInfo year:"+year+" month:"+month+" dayOfMonth:"+dayOfMonth+" dayOfWeek:"+dayOfWeek );
     *
     **/
    public static String intToDay( int p_day ) { //throws Exception {
        // Sanity Checking
        //if( p_day <= 0 || p_day > 7 ) {
        //
        //	Log.e( "Error!", "Utils::intToDay p_day must be 8<0! p_day:"+p_day+"" );
        //	p_day = 0;
        //
        //}

        return DAY_OF_WEEK[(p_day - 1)];
    }

    public static int year() {
        return KreyosUtility.calendar().get( Calendar.YEAR );
    }

    public static int month() {
        return KreyosUtility.calendar().get( Calendar.MONTH );
    }

    public static int day() {
        return KreyosUtility.calendar().get( Calendar.DAY_OF_MONTH );
    }

    public static int dayInWeek() {
        return KreyosUtility.calendar().get( Calendar.DAY_OF_WEEK );
    }

    public static String dateString( Calendar p_cal ) {
        int year 			= p_cal.get( Calendar.YEAR );
        String month		= KreyosUtility.month( p_cal );
        int dayOfMonth		= p_cal.get( Calendar.DAY_OF_MONTH );

        return ( "" + month + " " + dayOfMonth + " " + year + "" );
    }

    public static Calendar calendar() {
        return Calendar.getInstance( TimeZone.getDefault() );
    }

    public static Calendar calendar( long p_epoch ) {
        Calendar cal = KreyosUtility.calendar();
        cal.setTimeInMillis( p_epoch * 1000L );
        return cal;
    }

    public static String month( Calendar p_cal ) {
        return MONTHS[ p_cal.get( Calendar.MONTH ) ];
    }

    public static String dayOfWeek( Calendar p_cal ) {

        return "";
    }

    public static long epoch() {
        return KreyosUtility.epoch( KreyosUtility.calendar() );
    }

    public static long epoch( Calendar p_cal ) {
        long epochTime = ( p_cal.getTimeInMillis()/1000L );
        return epochTime;
    }

    public static long epochMinusDay( int p_days ) {
        return KreyosUtility.epochMinusDay( KreyosUtility.calendar(), p_days );
    }

    public static long epochMinusDay( Calendar p_cal, int p_days ) {
        //*
        long daysToSub = ( EPOCH_UNIT_DAY * (long)p_days );
        long epochHead = KreyosUtility.epoch( p_cal );

        Calendar cal = KreyosUtility.calendar();
        cal.set( Calendar.HOUR_OF_DAY, 0 );
        cal.set( Calendar.MINUTE, 0 );

        long difference = epochHead - KreyosUtility.epoch( cal );
        difference += daysToSub;

        return ( epochHead - difference );
        //*/
        //return Utils.epochMinusUnit( p_cal, p_days, EPOCH_UNIT_DAY );
    }



    // OVERALL ACTIVITIES
    /***************************************************************************************************************************/
    public static String setDataForDisplay(double p_value)
    {
        DecimalFormat f = new DecimalFormat("##.##");
        String returnVal;
        returnVal 		= "" +  f.format(p_value);
        return returnVal;
    }

    /**
	 * Convert Pounds to Kilograms
	 */
	public static int convertPoundsToKilograms(int p_lbs) {
		float kgDivider = 2.2046f;
		int returnVal = (int)(p_lbs/kgDivider);
		return returnVal;
	}

    /**
	 * Convert Inches to Centimeters
	 */
	public static int convertInchestoCentimeters(int p_in) {
		float cmDivider = 0.39370f;
		int returnVal = (int)(p_in/cmDivider);
		return returnVal;
	}


}
