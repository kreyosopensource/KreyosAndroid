package com.kreyos.kreyosandroid.managers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kreyos.kreyosandroid.utilities.KreyosUtility;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.StrictMode;
import android.util.Log;

/**
 * @author Aries Sanchez Sulit
 *
 *	This handles the HTTP Requests.
 *	POST, GET, Connect, etc.
 *
 */
public class RequestManager 
{
	/************************************************************
	 * Singleton 
	 **/
	private static RequestManager m_sharedInstance = null;
	
	public static RequestManager instance()
	{
		if( m_sharedInstance == null )
		{
			m_sharedInstance = new RequestManager( new Enforcer() );
		}
		
		return m_sharedInstance;
	}
	
	/************************************************************
	 * Properties 
	 **/
	private HttpClient m_httpClient		= null;
	
	/************************************************************
	 * Constructor 
	 **/
	public RequestManager( Enforcer p_enforcer )
	{
		if( p_enforcer == null )
		{
			Log.e("Error", "RequestManager::RequestManager null enforcer! use the singleton functionality!");
		}
		
		// initialize default avlues
		m_httpClient		= new DefaultHttpClient();	
	}
	
	/************************************************************
	 * Methods 
	 **/
    /*
	public void connect( String p_url )
	{
	    HttpGet httpget 		= new HttpGet( p_url ); 
	    HttpResponse response	= null;
	    
	    try 
	    {
	        response 			= m_httpClient.execute( httpget );
	        HttpEntity entity 	= response.getEntity();
	        
	        // check the entity of the request
	        if( entity != null ) 
	        {
	        	// read the request's responce
	            InputStream instream = entity.getContent();
	            String requestString = Utils.streamToString( instream );
	            
	            Log.i( "RequestManager::connect", "Connect:" + requestString );
	            
	            // close the stream
	            instream.close();
	        }


	    } catch (Exception e) {}
	}
	*/
	public String put(
			String p_url,
			JSONObject p_json
		) {
			String responseIto = "";
			try { 
				
				Log.d("RequestManager", "URL:" + p_url);
				Log.d("RequestManager", "JSON:" + p_json);
		
				HttpClient client = new DefaultHttpClient();
				HttpPut put = new HttpPut(p_url);
				put.addHeader("Content-Type", "application/json");
				put.addHeader("Accept", "application/json");
				put.setEntity(new StringEntity(p_json.toString()));
				HttpResponse response = client.execute(put);
				responseIto = EntityUtils.toString(response.getEntity());
			} catch( Exception p_e ) {
				p_e.printStackTrace();
				responseIto = "ERROR";
			}
			
			return responseIto;
	}

	public String post(String p_url, JSONObject p_json) {

			String strResponse;

			try {
				StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			    StrictMode.setThreadPolicy(policy);

				Log.d("RequestManager", "jsonFile: " + p_json.toString());
		
				HttpClient httpclient		= m_httpClient;

			    HttpPost httppost			= new HttpPost( p_url );
			    httppost.setEntity( new ByteArrayEntity(p_json.toString().getBytes("UTF8")) );

                HttpResponse response 		= httpclient.execute( httppost );
                strResponse 				= EntityUtils.toString(response.getEntity());

			} catch( Exception p_e ) {
				p_e.printStackTrace();
                strResponse = "ERROR";
			}
			
			return strResponse;
		
	}
	/*
	public String post(
			String p_url,
			String p_json
		) {
			String responseIto = "";
			try
			{ 
				Log.d("RequestManager", "jsonFile: " + p_json);
				
				StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			    StrictMode.setThreadPolicy(policy);
				
				HttpClient httpclient		= null;
			    HttpPost httppost			= null;
			    httpclient 					= m_httpClient;
			    httppost 					= new HttpPost( p_url );
			    
			    httppost.setEntity( (HttpEntity) new ByteArrayEntity(p_json.getBytes("UTF8")) );
			    HttpResponse response 		= httpclient.execute( httppost );
			    responseIto 				= EntityUtils.toString(response.getEntity());
			    
			    Log.d("Log", "Test: " + responseIto);
			    JSONObject jsonResponse = new JSONObject(responseIto);
			    if (jsonResponse.has("message")) {
			    	String message = jsonResponse.getString("message");
			    	if(message.equalsIgnoreCase("Your session has expired. Please login again.")) {
			    		Log.d("Log", "End Session");
			    		AppHelper.instance().showSessionErrorPrompt("Session Error", message);
			    	}
			    }
			}
			catch( Exception p_e )
			{
				p_e.printStackTrace();
				responseIto = "ERROR";
			}
			
			return responseIto;
		
		}
	
	public String post(
		String p_url,
		ArrayList<NameValuePair> p_params
	) {
		String responseIto = "";
		try
		{ 
			JSONObject jsonFile = new JSONObject();
			for(int i = 0; i < p_params.size(); i++)
			{
				jsonFile.put(p_params.get(i).getName(), p_params.get(i).getValue());
			}
			
			Log.d("RequestManager", "jsonFile: " + jsonFile.toString());
			
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		    StrictMode.setThreadPolicy(policy);
			
			HttpClient httpclient		= null;
		    HttpPost httppost			= null;
		    httpclient 					= m_httpClient;
		    httppost 					= new HttpPost( p_url );
		    
		    httppost.setEntity( (HttpEntity) new ByteArrayEntity(jsonFile.toString().getBytes("UTF8")) );
		    HttpResponse response 		= httpclient.execute( httppost );
		    responseIto 				= EntityUtils.toString(response.getEntity());
		    
		    Log.d("Log", "Test: " + responseIto);
		    JSONObject jsonResponse = new JSONObject(responseIto);
		    if (jsonResponse.has("message")) {
		    	String message = jsonResponse.getString("message");
		    	if(message.equalsIgnoreCase("Your session has expired. Please login again.")) {
		    		Log.d("Log", "End Session");
		    		AppHelper.instance().showSessionErrorPrompt("Session Error", message);
		    	}
		    }
		}
		catch( Exception p_e )
		{
			p_e.printStackTrace();
			responseIto = "ERROR";
		}
		
		return responseIto;
	
	}
	
	public String get( String p_url )
	{
		String responseIto = "";
		try
		{ 
			HttpClient httpclient		= null;
		    HttpGet httppost			= null;
		    httpclient 					= m_httpClient;
		    httppost 					= new HttpGet( p_url );

		    HttpResponse response		= httpclient.execute( httppost );
		    responseIto 				= EntityUtils.toString( response.getEntity() );
		    
		    Log.d("Log", "Test: " + responseIto);
		    JSONObject jsonResponse = new JSONObject(responseIto);
		    if (jsonResponse.has("message")) {
		    	String message = jsonResponse.getString("message");
		    	if(message.equalsIgnoreCase("Your session has expired. Please login again.")) {
		    		Log.d("Log", "End Session");
		    		AppHelper.instance().showSessionErrorPrompt("Session Error", message);
		    	}
		    }
		}
		catch( Exception p_e )
		{
			p_e.printStackTrace();
		}
		
		return responseIto;
	
	}
	
	public String get( String p_url, ArrayList<NameValuePair> p_params )
	{
		String responseIto = "";
		try
		{ 
			// Add "?" to define a get method
			p_url += "?";
			
			// Differentiate format between one and many parameters
			if(p_params.size() > 1) {
				// More than one value
				for(byte i = 0; i < p_params.size(); i++) {
					// Hold the value of pair
					NameValuePair pair = p_params.get(i);
					// Check if it's 
					if(i == (p_params.size() - 1)) {
						// Format : name=value
						p_url += pair.getName() + "=" + pair.getValue(); 
					} else {
						// Format : name=value,
						p_url += pair.getName() + "=" + pair.getValue() + "&";
					}
				}
				
			} else {
				// Only one value
				NameValuePair pair = p_params.get(0);
				// Format : name=value,
				p_url += pair.getName() + "=" + pair.getValue();
			}
			
			// Logging Get URL
			Log.d("Response", "URL: " + p_url);
			
			HttpClient httpclient		= null;
			HttpGet httpget				= null;
		    httpclient 					= m_httpClient;
		    httpget 					= new HttpGet( p_url );

		    HttpResponse response		= httpclient.execute( httpget );
		    responseIto 				= EntityUtils.toString( response.getEntity() );
		    
		    Log.d("Log", "Test: " + responseIto);
		    JSONObject jsonResponse = new JSONObject(responseIto);
		    if (jsonResponse.has("message")) {
		    	String message = jsonResponse.getString("message");
		    	if(message.equalsIgnoreCase("Your session has expired. Please login again.")) {
		    		Log.d("Log", "End Session");
		    		AppHelper.instance().showSessionErrorPrompt("Session Error", message);
		    	}
		    }

		}
		catch( Exception p_e )
		{
			p_e.printStackTrace();
		}
		
		return responseIto;
	
	}
	
	private String getResponse( HttpEntity p_entity )
	{
		String response = "";

		try
		{
		    int length 				= (int)p_entity.getContentLength();
		    StringBuffer sb 		= new StringBuffer( length );
		    InputStreamReader isr 	= new InputStreamReader( p_entity.getContent(), "UTF-8" );
		    char buff[] 			= new char[length];
		    int cnt					= 0;
		    
		    while ( ( cnt = isr.read( buff, 0, length - 1 ) ) > 0 )
		    {
		    	sb.append( buff, 0, cnt );
		    }

		    response 				= sb.toString();
		    isr.close();
		} 
		catch ( IOException p_error ) 
		{
			p_error.printStackTrace();
		}
		
		return response;
	}
	
	/************************************************************
	 * Getter | Setter 
	 **/
}

// Singleton
class Enforcer {}
