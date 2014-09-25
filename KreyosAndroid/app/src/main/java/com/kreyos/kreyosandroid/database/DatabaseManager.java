package com.kreyos.kreyosandroid.database;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kreyos.kreyosandroid.utilities.Profile;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

/*****************************************************************************
 * TODO: +AS:05262014 Please delete this class
 **/
public class DatabaseManager extends SQLiteOpenHelper
{
	private static DBEnforcer m_enforcer				= null;
	public static DatabaseManager m_instance			= null;
	
	public static DatabaseManager instance()
	{
		return DatabaseManager.instance( null );
	}
	
	public static DatabaseManager instance( Context p_context )
	{
		if( m_instance == null )
		{
			m_enforcer = new DBEnforcer();
			m_instance = new DatabaseManager( p_context );
		}
		
		if( p_context != null ) 
		{ 
			m_instance.setContext( p_context );
		}
		
		return m_instance;
	}
	
	private static final String DATABASE_NAME			= "KreyosDefaultDB";
	private Context m_context							= null;
	private SQLiteDatabase m_db							= null;
	
	// +AS:05192014 Web activity data
	private ArrayList<ContentValues> m_webActivities	= null;
	private String m_localDBName						= null;	// aries@kreyos.comKreyosDefaultDB.sqlite
	private String m_localDBPath						= null; // data/extrapath/extrafolder/aries@kreyos.comKreyosDefaultDB.sqlite
	
	public DatabaseManager( Context p_context )
	{
		// 1? its Database Version
		super( p_context, DATABASE_NAME, null, 1 );
		this.setContext( p_context );
		m_webActivities = new ArrayList<ContentValues>();
	}
	
	public void initLocalDB() {
		AsyncTask<Void, Void, Void> t = new AsyncTask<Void, Void, Void>(){
            protected Void doInBackground(Void... p) {
            	
				Profile profile 			= new Profile();
//				profile.loadfromPrefs();
				
				AssetManager assetManager 	= m_context.getAssets();
				InputStream in 				= null;
		        OutputStream out 			= null;
		        final String filename		= DATABASE_NAME + ".sqlite";
		        m_localDBName				= profile.EMAIL + filename;
		        m_localDBPath 				= m_context.getExternalFilesDir(null) + "/" + m_localDBName;
		        
		        if( DatabaseManager.instance().hasLocalDB() ) {
		        	Log.i( "DatabaseManager::initLocalDB", "db already created... path:" + m_localDBName + "" );
		        	return null;
		        }
		        else {
		        	Log.i( "DatabaseManager::initLocalDB", "creating db... path:" + m_localDBName + "" );
		        }
		        
		        Log.i( "DatabaseManager::initLocalDB", "DB Local db path:" + m_localDBName + "" );
		        
				// create a copy of local db
				try
				{
					//this.copyDataBase();
					in = assetManager.open( "dbtemplate/" + filename );
					File outFile = new File( m_context.getExternalFilesDir(null), m_localDBName );
					out = new FileOutputStream( outFile );
					copyFile( in, out );
					in.close();
					in = null;
					out.flush();
					out.close();
					out = null; 
				}
				catch ( IOException p_exception )
				{
					Log.w( "DB Warning", "exception:" + p_exception.getMessage() );
				}
				return null;
            }
        };
        t.execute();
    }
	
	public void setContext( Context p_context ) {
		m_context = p_context; 
	}
	
	public Cursor queryData( String p_sqlStatement )
	{
		Log.i( "DatabaseManager::queryData", "Querying data to database.. statement:" + p_sqlStatement );
		
		boolean hasLocalDB = this.hasLocalDB();
		
		// sanity checking
		if( !hasLocalDB )
		{
			Log.i( "DatabaseManager::queryData", "Error! you must initialize local db somewhere first!" );
			// Temp error catch
			this.initLocalDB();
		}
		
		Cursor p_queryResult = null;
		
		// always open your database before reading/writing data
		if( this.openDB() )
		{
			Log.i( "DatabaseManager::queryData", "Querying data to database.. statement:" + p_sqlStatement );
			p_queryResult = m_db.rawQuery( p_sqlStatement, null );
		}
		
		return p_queryResult;
	}
	
	public boolean insert( String p_table, ContentValues p_row )
	{
		// debug log
		Log.i( "DatabaseManager::insert", "Insert at table:" + p_table );
		for( String key : p_row.keySet() ) {
			Log.i( "DatabaseManager::insert", "inserting.. key:"+key+" value:"+p_row.getAsString(key) );
		}
		
		try {
			m_db.insertOrThrow( p_table, null, p_row );
			return true;
		}
		catch( Exception ex ) {
			
			ex.printStackTrace(); 
			return false;
		}
	}
	
	/** Sample Usage
	 * 
	 * @param p_table
	 * @param p_row
	 * @param p_condition
	 * 
	 * p_condition = "name=chuful"
	 * ContentValues["IsChuful"] = true;
	 * 
	 * db.update( p_table, p_row, condition );
	 */
	public void update( String p_table, ContentValues p_row, String p_condition )
	{
		m_db.update( p_table, p_row, p_condition, null );
	}
	
	public void saveDataToLocal( JSONArray p_activities ) 
	{
		GenerateWebValuesTask task = new GenerateWebValuesTask();
		task.delegate = this;
		task.jsonData = p_activities;
		task.contentOut = m_webActivities;
		
		// run task
		task.execute();
	}
	
	public void pushWebActivitiesToLocal() {
		Log.i( "DatabaseManager::pushWebActivitiesToLocal", "Inserting data to db.. len:" + m_webActivities.size() );
		if( m_webActivities != null && m_webActivities.size() > 0 ) {
			ContentValues value = m_webActivities.get( 0 );
			m_webActivities.remove( 0 );
			try {
				if( this.openDB() ) {
					DatabaseManager.instance( (Context)m_context ).insert( "Kreyos_User_Activities", value );
				}
			} catch( Exception ex ) { ex.printStackTrace(); }
		}
		
		if( m_webActivities != null && m_webActivities.size() > 0 ) {
			int seconds = 3;
			Handler handler = new Handler();
			handler.postDelayed( new InsertActivities(), seconds);
		}
	}
	
	public boolean hasLocalDB() {
		Profile profile = new Profile();
//		profile.loadfromPrefs();
		final String localDb = profile.EMAIL + DATABASE_NAME + ".sqlite";
		
		Log.i( "DatabaseManager::hasLocalDB", "DB Local db path:" + localDb + "" );
		
		File dbFile = new File( m_context.getExternalFilesDir(null), localDb );
    	return dbFile.exists();
	}
	
    private boolean openDB()
    {
    	Profile profile 			= new Profile();
//		profile.loadfromPrefs();
		
    	//String path = m_context.getExternalFilesDir(null) + "/" + profile.EMAIL + DATABASE_NAME + ".sqlite";
		String path = m_localDBPath;
    	
    	Log.i( "DatabaseManager::openDB", "path:" + path );
    	
    	try 
    	{
    		m_db = SQLiteDatabase.openDatabase( m_localDBPath, null, SQLiteDatabase.CREATE_IF_NECESSARY );
    		//m_db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
    	}
    	catch( SQLException p_exception )
    	{
    		Log.w( "DB_WARNING", "" + p_exception.getMessage() );
    		m_db = null;
    		path = null;
    	}
    	
        return path != null;
    }
    
    @Override
    public synchronized void close() 
    {
        if( m_db != null )
        {
        	m_db.close();
        }
        super.close();
    }
	
	private void copyFile( InputStream in, OutputStream out )
	{
		try 
    	{
		    byte[] buffer = new byte[1024];
		    int read;
		    
		    //while ( (read = in.read(buffer) ) != -1 )
		    while ( (read = in.read(buffer) ) > 0 )
		    {
		    	out.write(buffer, 0, read);
		    }
    	}
    	catch( IOException p_exception )
    	{
    		Log.w( "WRITE_TO_FILE_WARNING", "" + p_exception.getMessage() );
    	}
	}
	
	/***************************************************************************
	 * Autogenerated
	 **/
	@Override
	public void onCreate( SQLiteDatabase arg0 ) 
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void onUpgrade( SQLiteDatabase arg0, int arg1, int arg2 ) 
	{
		// TODO Auto-generated method stub
		
	}
}

class DBEnforcer {}

// Schemas Kreyos_User_Activities
/*
ActivityBestLap
ActivityAvgLap
ActivityCurrentLap
ActivityAvgPace
ActivityPace
ActivityTopSpeed
ActivityAvgSpeed
ActivitySpeed
ActivityElevation
ActivityAltitude
ActivityMaxHeart
AvgActivityHeart
ActivityHeart
Activity_ID
Sport_ID
KreyosuserID
ActivityCalories
ActivitySteps
Coordinates
ActivityDistance
CreatedTime
//*/

class UserActivities
{
	/*
	ActivityBestLap
	ActivityAvgLap
	ActivityCurrentLap
	ActivityAvgPace
	ActivityPace
	ActivityTopSpeed
	ActivityAvgSpeed
	ActivitySpeed
	ActivityElevation
	ActivityAltitude
	ActivityMaxHeart
	AvgActivityHeart
	ActivityHeart
	Activity_ID
	Sport_ID
	KreyosuserID
	ActivityCalories
	ActivitySteps
	Coordinates
	ActivityDistance
	CreatedTime
	//*/
	public static final float INVALID	= 0.0f;
	
	public float speed					= INVALID;
	public float distance				= INVALID;
	public float calories				= INVALID;
	public float steps					= INVALID;
}

class InsertActivities implements Runnable {
	@Override
	public void run() {
		DatabaseManager.instance().pushWebActivitiesToLocal();
	}
}

class GenerateWebValuesTask extends AsyncTask<Void, Void, Void> {

	public DatabaseManager delegate				= null;
	public JSONArray jsonData					= null;
	public ArrayList<ContentValues> contentOut	= null;
	
	@Override
	protected Void doInBackground(Void... params) {
		
		/*
		[{
	      "1400481434": <-- Epoch
	      {
	        "sports_type_id": 0,
	        "num_steps": 7,
	        "calories": 145.77,
	        "elevation": 0,
	        "altitude": 0,
	        "pace": 0,
	        "avg_pace": 0,
	        "avg_lap": 0,
	        "best_lap": 0,
	        "time": "1400481434",
	        "distance": 2.38,
	        "max_heart": 0,
	        "speed": 0,
	        "top_speed": 0
	      }
		}];
		//*/
		final String tableName		 	= "Kreyos_User_Activities";
		ContentValues values 			= null;
		int len							= jsonData.length();
		int index						= 0;
		JSONObject act					= null;
		
		// Content Keys
		final String EPOCH				= "time";
		final String DISTANCE			= "distance";
		final String CALORIES			= "calories";
		final String STEPS				= "num_steps";
		final String MODE				= "sports_type_id";
				
		// DB Keys
		final String DB_MODE			= "Sport_ID";
		final String DB_EPOCH			= "CreatedTime";
		final String DB_DISTANCE		= "ActivityDistance";
		final String DB_CALORIES		= "ActivityCalories";
		final String DB_STEPS			= "ActivitySteps";
		
		try {
			for( index = 0; index < len; index++ ) {
				
				act = (JSONObject)jsonData.get( index );
				Iterator<?> epochs = act.keys();
				
				while( epochs.hasNext() ) {
					
					String epoch = (String)epochs.next();
					
					Log.i( "DatabaseManager::saveDataToLocal", "Data: Epoch:" + epoch + "" );
					
					JSONObject actdata = (JSONObject)act.getJSONObject( epoch );
					
					values = new ContentValues();
					values.put( DB_EPOCH, epoch );
					values.put( DB_MODE, Integer.parseInt( actdata.getString( MODE ) ) );
					values.put( DB_DISTANCE, Long.toString( actdata.getLong( DISTANCE ) ) );
					values.put( DB_CALORIES, Long.toString( actdata.getLong( CALORIES ) ) );
					values.put( DB_STEPS, Integer.toString( actdata.getInt( STEPS ) ) );
					
					// push to array
					contentOut.add( values );
					
				} 
			}
		} 
		catch (Exception e) { e.printStackTrace(); }
		
		return null;
	}
	
	@Override
	protected void onPostExecute(Void p_void) {
		delegate.pushWebActivitiesToLocal();
    }
}