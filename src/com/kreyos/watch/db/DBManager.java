package com.kreyos.watch.db;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kreyos.watch.dataobjects.Profile;
import com.kreyos.watch.listeners.IQueryEvent;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

public class DBManager extends SQLiteOpenHelper {
	
	/*****************************************************************************
	 * Constants
	 **/
	private static final String DATABASE_NAME			= "KreyosDefaultDB";
	private static final String QUERY_KEY				= "QUERY_KEY";
	private static final String QUERY_STATEMENT			= "QUERY_STATEMENT";
	
	/*****************************************************************************
	 * Utilities on Stopping all tasks
	 **/
	public static ArrayList<QueryTask> m_tasks			= new ArrayList<QueryTask>();
	
	/*****************************************************************************
	 * Properties
	 **/
	private Context m_context							= null;
	private IQueryEvent m_delegate						= null;
	private SQLiteDatabase m_database					= null;
	private String m_localDBName						= null;	// kreyos@kreyos.comKreyosDefaultDB.sqlite
	private String m_localDBPath						= null; // data/extrapath/extrafolder/kreyos@kreyos.comKreyosDefaultDB.sqlite
	private File m_externalFileDirectory				= null;
	private ArrayList<Map<String, String>> m_queries	= null; // Map < Key, QUERY_KEY( String hashCode ) > < Statement, SQL_QUERY >
	private boolean m_isQuerying						= false;
	
	/*****************************************************************************
	 * Constructors
	 **/
	public DBManager( Context p_context, IQueryEvent p_delegate ) {
		// 1? its the Database Version
		super( p_context, DATABASE_NAME, null, 1 );
		m_context = p_context;
		m_delegate = p_delegate;
		m_queries = new ArrayList<Map<String, String>>();
		m_isQuerying = false;
	}
	
	/*****************************************************************************
	 * Getters | Setters
	 **/
	public SQLiteDatabase database() {
		return m_database;
	}
	
	/*****************************************************************************
	 * Public Static Utils Methods
	 **/
	public static void stopAllTasks() {
		for( QueryTask task : m_tasks ) {
			task.cancel( true );
			task.forceCancelled();
		}
		m_tasks.clear();
	}
	
	/*****************************************************************************
	 * Public Methods
	 **/
	public void init() {
		
		Profile profile 			= new Profile();
		profile.loadfromPrefs();
		m_externalFileDirectory		= m_context.getExternalFilesDir( null );
		
        final String filename		= DATABASE_NAME + ".sqlite";
        m_localDBName				= profile.EMAIL + filename;
        m_localDBPath 				= m_externalFileDirectory + "/" + m_localDBName;
		
		AsyncTask<Void, Void, Void> t = new AsyncTask<Void, Void, Void>(){
            protected Void doInBackground(Void... p) {
            	
            	AssetManager assetManager 	= m_context.getAssets();
        		InputStream in 				= null;
                OutputStream out 			= null;
            	
                if( DBManager.this.hasLocalDB() ) {
                	Log( "DBManager::initLocalDB", "db already created... path:" + m_localDBName + "" );
                	return null;
                }
                else {
                	Log( "DBManager::initLocalDB", "creating db... path:" + m_localDBName + "" );
                }
                
                Log( "DBManager::initLocalDB", "DB Local db path:" + m_localDBName + "" );

        		try {
        			in = assetManager.open( "dbtemplate/" + filename );
        			//File outFile = new File( m_context.getExternalFilesDir( null ), m_localDBName );
        			File outFile = new File( m_externalFileDirectory, m_localDBName );
        			out = new FileOutputStream( outFile );
        			copyFile( in, out );
        			in.close();
        			in = null;
        			out.flush();
        			out.close();
        			out = null; 
        		}
        		catch( IOException p_exception ) {
        			Log.w( "DB Warning", "exception:" + p_exception.getMessage() );
        		}
                return null;
            }
        };
        t.execute();
    }
	
	private void Log(String string, String string2) {
		// TODO Auto-generated method stub
		Log.i(string, string2);
	}

	public void query( String p_key, String p_statement ) {
		Map<String, String> queryTable = new HashMap<String, String>();
		queryTable.put( QUERY_KEY, p_key );
		queryTable.put( QUERY_STATEMENT, p_statement );
		
		// Cue all the queries
		m_queries.add( queryTable );
		
		// If still querying something.. return the process
		if( m_isQuerying ) {
			return;
		}
		
		this.startQuery();
	}
	
	/*****************************************************************************
	 * Private Methods
	 **/
	private void startQuery() {
		
		if( m_queries.size() <= 0 ) {
			Log( "DBManager::startQuery", "NEW_DB_QUERY No Item to query" );
			return;
		}
		
		if( this.openDB() ) {
			Log( "DBManager::startQuery", "NEW_DB_QUERY Querying.. statement:" + m_queries.get( 0 ) + "" );
			Map<String, String> queryTable = m_queries.get( 0 );
			String queryKey = queryTable.get( QUERY_KEY );
			String statement = queryTable.get( QUERY_STATEMENT );
			m_delegate.onQueryStart( QUERY_KEY ); 
			m_isQuerying = true;
			QueryTask query = new QueryTask();
			query.delegate = this;
			query.statement = statement;
			query.execute();
		}
	}
	
	private void onQueryComplete( Cursor p_query ) {
		m_isQuerying = false;
		Map<String, String> queryTable = m_queries.get( 0 );
		String queryKey = queryTable.get( QUERY_KEY );
		String queryStatement = queryTable.get( QUERY_STATEMENT );
		m_queries.remove( 0 );
		Log( "DBManager::onQueryComplete", "NEW_DB_QUERY queryKey:" + queryKey + " queryStatement:" + queryStatement + " queryObject:" + p_query + "'" );
		m_delegate.onQueryComplete( queryKey, p_query );
		this.startQuery();
	}
	
	private void onQueryError( String p_error ) {
		
		// block query when queries <= 0
		if( m_queries.size() <= 0 ) { return; }
			
		m_isQuerying = false;
		Map<String, String> queryTable = m_queries.get( 0 );
		String queryKey = queryTable.get( QUERY_KEY );
		String queryStatement = queryTable.get( QUERY_STATEMENT );
		m_queries.remove( 0 );
		Log( "DBManager::onQueryError", "NEW_DB_QUERY queryKey:" + queryKey + " queryStatement:" + queryStatement + " error:" + p_error + "'" );
		m_delegate.onQueryError( queryKey, p_error );
		this.startQuery();
	}
	
	public Cursor queryData( String p_sqlStatement ) {
		Log( "DBManager::queryData", "NEW_DB_QUERY Querying data to database.. statement:" + p_sqlStatement );
		
		boolean hasLocalDB = this.hasLocalDB();
		
		Cursor p_queryResult = null;
		
		// always open your database before reading/writing data
		if( this.openDB() ) {
			Log( "DBManager::queryData", "Querying data to database.. statement:" + p_sqlStatement );
			p_queryResult = m_database.rawQuery( p_sqlStatement, null );
		}
		
		return p_queryResult;
	}
	
	public boolean insert( String p_table, ContentValues p_row ) {
		try {
			m_database.insertOrThrow( p_table, null, p_row );
			return true;
		}
		catch( Exception ex ) {
			
			ex.printStackTrace(); 
			return false;
		}
	}
	
	public void update( String p_table, ContentValues p_row, String p_condition ) {
		m_database.update( p_table, p_row, p_condition, null );
	}
	
	public boolean hasLocalDB() {
		Profile profile = new Profile();
		profile.loadfromPrefs();
		final String localDb = profile.EMAIL + DATABASE_NAME + ".sqlite";
		File dbFile = new File( m_context.getExternalFilesDir(null), localDb );
    	return dbFile.exists();
	}
	
    private boolean openDB() {
    	Profile profile = new Profile();
		profile.loadfromPrefs();
		String path = m_localDBPath;
		
		Log("DBManager::openDB","path:"+path);
		
    	try {
    		m_database = SQLiteDatabase.openDatabase( m_localDBPath, null, SQLiteDatabase.CREATE_IF_NECESSARY );
    	}
    	catch( SQLException p_exception ) {
    		Log.w( "DB_WARNING", "" + p_exception.getMessage() );
    		m_database = null;
    		path = null;
    	}
    	
        return path != null;
    }
    
	private void copyFile( InputStream in, OutputStream out ) {
		try  {
		    byte[] buffer = new byte[1024];
		    int read;
		    
		    //while ( (read = in.read(buffer) ) != -1 )
		    while ( (read = in.read(buffer) ) > 0 )
		    {
		    	out.write(buffer, 0, read);
		    }
    	}
    	catch( IOException p_exception ) {
    		Log.w( "WRITE_TO_FILE_WARNING", "" + p_exception.getMessage() );
    	}
	}
	
	/*****************************************************************************
	 * SQLiteOpenHelper Functions
	 **/
	@Override
	public void onCreate( SQLiteDatabase arg0 ) {
	}

	@Override
	public void onUpgrade( SQLiteDatabase arg0, int arg1, int arg2 ) {
	}
	
	@Override
    public synchronized void close() {
        if( m_database != null ) {
        	m_database.close();
        }
        super.close();
    }
}

/*****************************************************************************
 * Helper classes
 **/
class QueryTask extends AsyncTask<Void, Void, Void> {
	
	public DBManager delegate		= null;
	public String statement			= null;
	
	public QueryTask() {
		DBManager.m_tasks.add( this );
	}
	
	public void forceCancelled() {
		this.onQueryError( "CANCELLED" );
	}
	
	@Override
	protected Void doInBackground( Void... params ) {
		try {
			Cursor queryData = delegate.database().rawQuery( statement, null );
			Method onQueryComplete = DBManager.class.getDeclaredMethod( "onQueryComplete", Cursor.class );
			onQueryComplete.setAccessible( true );
			onQueryComplete.invoke( delegate, queryData );
			DBManager.m_tasks.remove( this );
			//delegate.onQueryComplete( statement, queryData );
		} 
		catch( IllegalArgumentException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.onQueryError( e.getMessage() );
		} 
		catch( NoSuchMethodException e ) {
			e.printStackTrace();
			this.onQueryError( e.getMessage() );
		} 
		catch( IllegalAccessException e ) {
			e.printStackTrace();
			this.onQueryError( e.getMessage() );
		} 
		catch( InvocationTargetException e ) {
			e.printStackTrace();
			this.onQueryError( e.getMessage() );
		}
		return null;
	}
	
	private void onQueryError( String p_error ) {
		try {
			Method onQueryError = DBManager.class.getDeclaredMethod( "onQueryError", String.class );
			onQueryError.setAccessible( true );
			onQueryError.invoke( delegate, statement );
		}
		catch( NoSuchMethodException e ) {
			e.printStackTrace();
		} 
		catch( IllegalAccessException e ) {
			e.printStackTrace();
		} 
		catch( InvocationTargetException e ) {
			e.printStackTrace();
		} 
	}
	
	private static void Log( String p_tag, String p_log ) {
		//Log.i( p_tag, p_log );
	}
}

/*****************************************************************************
 * Sameple Usage
 **/
class SampleUsage extends Activity
implements
	IQueryEvent
{
	public void howTouse() {
		DBManager db = new DBManager( (Context)this, this );
	    db.init();
	    db.query( "USER_ACTIVITIES", "SELECT * FROM Kreyos_User_Activities" );
	}
	
	@Override
	public void onQueryStart( String p_queryKey ) {
		// Handle the initial start of query here
	}
	
	@Override
	public void onQueryComplete( String p_queryKey, Cursor p_query ) {
		// Handle the query data here!
	}

	@Override
	public void onQueryError( String p_queryKey, String p_error ) {
		// Query error occured!
	}
	
}