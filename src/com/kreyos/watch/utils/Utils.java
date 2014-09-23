package com.kreyos.watch.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import com.kreyos.watch.KreyosUtility;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.ImageView;

public class Utils
{
	//use for consuming Web Services via post 
	
		public static String doPost(String url,ArrayList<NameValuePair> parameters)
		{
			String responseIto = "";
			try{ 
			HttpClient httpclient;
			    HttpPost httppost;
			    httpclient = new DefaultHttpClient();
			    httppost = new HttpPost(url);
			    
			      httppost.setEntity(new UrlEncodedFormEntity(parameters));

			    HttpResponse response = httpclient.execute(httppost);
			    
			    responseIto = EntityUtils.toString(response.getEntity());
			}catch( Exception e ){
				e.printStackTrace();
			}
			return responseIto;
		
		}
		
		//use for consuming Web Services via  get no paramter
		public static String doGet(String url){
			String responseIto = "";
			try{ 
			HttpClient httpclient;
			    HttpPost httppost;
			    httpclient = new DefaultHttpClient();
			    httppost = new HttpPost(url);
			    
			    HttpResponse response = httpclient.execute(httppost);
			    
			    responseIto = EntityUtils.toString(response.getEntity());
			}catch( Exception e ){
				e.printStackTrace();
			}
			return responseIto;
		
		}
		
		private static String getResponse( HttpEntity entity )
		{
		  String response = "";

		  try
		  {
		    int length = ( int ) entity.getContentLength();
		    StringBuffer sb = new StringBuffer( length );
		    InputStreamReader isr = new InputStreamReader( entity.getContent(), "UTF-8" );
		    char buff[] = new char[length];
		    int cnt;
		    while ( ( cnt = isr.read( buff, 0, length - 1 ) ) > 0 )
		    {
		      sb.append( buff, 0, cnt );
		    }

		      response = sb.toString();
		      
		      isr.close();
		  } catch ( IOException ioe ) {
		    ioe.printStackTrace();
		  }
		  return response;
		}
		
		
	    public static boolean isNetworkAvailable( Context aContext )
	    {
	        ConnectivityManager connectivityManager =
	            ( ConnectivityManager ) aContext.getSystemService( Context.CONNECTIVITY_SERVICE );
	        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	        return activeNetworkInfo != null;
	    }
		
		//not used anymore
		public static String xmlParser( String xml )
		{
			String parseXML = "";
			try{
				 XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		         factory.setNamespaceAware(true);
		         XmlPullParser xpp = factory.newPullParser();
		
		         xpp.setInput( new StringReader ( xml ) );
		         int eventType = xpp.getEventType();
		         while (eventType != XmlPullParser.END_DOCUMENT) {
		          if(eventType == XmlPullParser.START_DOCUMENT) {
		              System.out.println("Start document");
		          } else if(eventType == XmlPullParser.START_TAG) {
		              System.out.println("Start tag "+xpp.getName());
		              
		          } else if(eventType == XmlPullParser.END_TAG) {
		              System.out.println("End tag "+xpp.getName());
		          } else if(eventType == XmlPullParser.TEXT) {
		              System.out.println("Text "+xpp.getText());
		              parseXML = xpp.getText();
		          }
		          eventType = xpp.next();
		         }
			}catch( Exception e ){
				Log.e("xmlParserError", e.getMessage());
			}
			return parseXML;
		}
		
	    public static Bitmap createBitmapByteArray( byte[] imageByteArray )
	    {
	    	
	      Bitmap decodedByte = BitmapFactory.decodeByteArray( imageByteArray, 0, imageByteArray.length );
	      if(decodedByte!=null){  
	        	 System.gc();

	            // decodedByte.recycle();
	       	 }
	        return decodedByte;
	    }
		
		public static byte[] createByteArrayFromImageURL( String imageURL )
		{
		        URL myFileURL;
		        byte[] byteChunk = new byte[ 4096 ];
		        ByteArrayOutputStream bais = new ByteArrayOutputStream();
		        int n;
		        try
		        {
		            myFileURL = new URL( imageURL );
		            HttpURLConnection conn = ( HttpURLConnection ) myFileURL.openConnection();
		            conn.setDoInput( true );
		            conn.connect();
		            InputStream is = conn.getInputStream();

		            while( ( n = is.read( byteChunk ) ) > 0 )
		            {
		                bais.write( byteChunk, 0, n );
		            }
		        }
		        catch( MalformedURLException e )
		        {
		            e.printStackTrace();
		        }
		        catch( IOException e )
		        {
		            e.printStackTrace();
		        }
		        return bais.toByteArray();
		 }
		
	    public static void setImg(final ImageView im, final String url){
	        AsyncTask<Void, Void, Bitmap> t = new AsyncTask<Void, Void, Bitmap>(){
	            protected Bitmap doInBackground(Void... p) {
	                Bitmap bm = null;
	                try {
	                    URL aURL = new URL(url);
	                    URLConnection conn = aURL.openConnection();
	                    conn.setUseCaches(true);
	                    conn.connect(); 
	                    InputStream is = conn.getInputStream(); 
	                    BufferedInputStream bis = new BufferedInputStream(is); 
	                    bm = BitmapFactory.decodeStream(bis);
	                    bis.close(); 
	                    is.close();
	                } catch (IOException e) { 
	                    e.printStackTrace(); 
	                }
	                return bm;
	            }

	            protected void onPostExecute(Bitmap bm){
	              
	                im.setImageBitmap(bm);
	            }
	        };
	        t.execute();
	    }
	    
		

	    
		public static void drawRectBitmap(final ImageView im, Bitmap bm) {
			Bitmap output = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Config.ARGB_8888);
			Canvas canvas = new Canvas(output);

			final Paint paint = new Paint();
			final Rect bmRect = new Rect(0, 0, bm.getWidth(), bm.getHeight());

			paint.setAntiAlias(true);
			canvas.drawBitmap(bm, bmRect, bmRect, paint);
					
			im.setImageBitmap(output);
		}
		
		public static Bitmap loadRemoteBitmap(final String url, final String tag) {
			Bitmap bm = null;
			try {
				
			    URL aURL = new URL(url);
			    URLConnection conn = aURL.openConnection();
			    conn.setUseCaches(true);
			    conn.connect(); 
			    InputStream is = conn.getInputStream();
			    
			    BufferedInputStream bis = new BufferedInputStream(is); 
			    bm = BitmapFactory.decodeStream(bis);

			    bis.close();
			    is.close();
			} catch (IOException e) { 
			    e.printStackTrace(); 
			}
			return bm;
		}
	
	/************************************************************
	 * String Utilities 
	 **/
	public static String streamToString( InputStream p_steam ) {
	    /*
	     * To convert the InputStream to String we use the BufferedReader.readLine()
	     * method. We iterate until the BufferedReader return null which means
	     * there's no more data to read. Each line will appended to a StringBuilder
	     * and returned as String.
	     */
	    BufferedReader reader 	= new BufferedReader( new InputStreamReader( p_steam ) );
	    StringBuilder buffer 	= new StringBuilder();
	    String line 			= null;

	    try {
	        while( (line = reader.readLine() ) != null ) {
	        	buffer.append( line + "\n" );
	        }
	    } 
	    catch( IOException p_error ) {
	    	p_error.printStackTrace();
	    } 
	    finally {
	        try {
	            p_steam.close();
	        } 
	        catch( IOException p_error ) {
	        	p_error.printStackTrace();
	        }
	    }
	    
	    return buffer.toString();
	}
	
	public static int toHash( String p_string ) {
		return p_string.hashCode();
	}
	
	
	
	/************************************************************
	 * Date Utilities
	 * 
	 * Params: must be 1-7 only
	 **/
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
		return Utils.calendar().get( Calendar.YEAR );
	}
	
	public static int month() {
		return Utils.calendar().get( Calendar.MONTH );
	}
	
	public static int day() {
		return Utils.calendar().get( Calendar.DAY_OF_MONTH );
	}
	
	public static int dayInWeek() {
		return Utils.calendar().get( Calendar.DAY_OF_WEEK );
	}
	
	public static String dateString( Calendar p_cal ) {
		int year 			= p_cal.get( Calendar.YEAR );
		String month		= Utils.month( p_cal );
		int dayOfMonth		= p_cal.get( Calendar.DAY_OF_MONTH );
		
		return ( "" + month + " " + dayOfMonth + " " + year + "" );
	}
	
	public static Calendar calendar() {
		return Calendar.getInstance( TimeZone.getDefault() );
	}
	
	public static Calendar calendar( long p_epoch ) {
		Calendar cal = Utils.calendar();
		cal.setTimeInMillis( p_epoch * 1000L );
		return cal;
	}
	
	public static String month( Calendar p_cal ) {
		return MONTHS[ p_cal.get( Calendar.MONTH ) ];
	}
	
	public static String dayOfWeek( Calendar p_cal ) {
		return DAY_OF_WEEK[ p_cal.get( Calendar.DAY_OF_WEEK ) ];
	}
	
	public static long epoch() {
		return Utils.epoch( Utils.calendar() );
	}
	
	public static long epoch( Calendar p_cal ) {
		long epochTime = ( p_cal.getTimeInMillis()/1000L );
		return epochTime;
	}
	
	public static long epochMinusDay( int p_days ) {
		return Utils.epochMinusDay( Utils.calendar(), p_days );
	}
	
	public static long epochMinusDay( Calendar p_cal, int p_days ) {
		//*
		long daysToSub = ( EPOCH_UNIT_DAY * (long)p_days );
		long epochHead = Utils.epoch( p_cal );
		
		Calendar cal = Utils.calendar();
		cal.set( Calendar.HOUR_OF_DAY, 0 );
		cal.set( Calendar.MINUTE, 0 );
		
		long difference = epochHead - Utils.epoch( cal );
		difference += daysToSub;
		
		return ( epochHead - difference );
		//*/
		//return Utils.epochMinusUnit( p_cal, p_days, EPOCH_UNIT_DAY );
	}
	
	public static long epochMinusMonth( int p_months ) {
		return Utils.epochMinusMonth( Utils.calendar(), p_months );
	}
	
	public static long epochMinusMonth( Calendar p_cal, int p_months ) {
		//*
		long monthsToSub = ( EPOCH_UNIT_MONTH * (long)p_months );
		long epochHead = Utils.epoch( p_cal );
		
		Calendar cal = Utils.calendar();
		cal.set( Calendar.HOUR_OF_DAY, 0 );
		cal.set( Calendar.MINUTE, 0 );
		
		long difference = epochHead - Utils.epoch( cal );
		difference += monthsToSub;
		
		return ( epochHead - difference );
		//*/
		//return Utils.epochMinusUnit( p_cal, p_months, EPOCH_UNIT_MONTH );
	}
	
	public static long epochMinusYear( int p_years ) {
		return Utils.epochMinusYear( Utils.calendar(), p_years );
	}
	
	public static long epochMinusYear( Calendar p_cal, int p_years ) {
		//*
		long yearsToSub = ( EPOCH_UNIT_YEAR * (long)p_years );
		long epochHead = Utils.epoch( p_cal );
		
		Calendar cal = Utils.calendar();
		cal.set( Calendar.HOUR_OF_DAY, 0 );
		cal.set( Calendar.MINUTE, 0 );
		
		long difference = epochHead - Utils.epoch( cal );
		difference += yearsToSub;
		
		return ( epochHead - difference );
		//*/
		//return Utils.epochMinusUnit( p_cal, p_years, EPOCH_UNIT_YEAR );
	}
	
	public static long epochMinusUnit( Calendar p_cal, int p_times, long unit ) {
		long unitToSub = ( p_times * (long)p_times );
		long epochHead = Utils.epoch( p_cal );
		
		Calendar cal = Utils.calendar();
		cal.set( Calendar.HOUR_OF_DAY, 0 );
		cal.set( Calendar.MINUTE, 0 );
		
		long difference = epochHead - Utils.epoch( cal );
		difference += unitToSub;
		
		return ( epochHead - difference );
	}
	
	/**
	 * Convert Inches to Centimeters
	 */
	public static int convertIntoCm(int p_in) {
		float cmDivider = 0.39370f;
		int returnVal = (int)(p_in/cmDivider);
		return returnVal;
	}
	
	/**
	 * Convert Pounds to Kilograms
	 */
	public static int convertLbstoKg(int p_lbs) {
		float kgDivider = 2.2046f;
		int returnVal = (int)(p_lbs/kgDivider);
		return returnVal;
	}
	
	/************************************************************
	 * Network Utilities
	 **/
    public static boolean hasConnection( Context p_context )
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)p_context.getSystemService( Context.CONNECTIVITY_SERVICE );
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
    
    /************************************************************
     * Show error message when no network connection
     */
    public static boolean checkInternetConnection(Context p_context) {
    	if (!hasConnection(p_context)) {
    		KreyosUtility.showErrorMessage((Activity)p_context, "Connection Problem:", 
					"Whoops! It seems you aren't connected to the Internet yet.");
    		return false;
    	}
    	return true;
    }
}
