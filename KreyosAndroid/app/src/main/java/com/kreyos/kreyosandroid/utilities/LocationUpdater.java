package com.kreyos.kreyosandroid.utilities;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class LocationUpdater {
	
	private static final int UPDATE_INTERVAL = 10 * 1000;
	private static final int UPDATE_DISTANCE = 3;

	private Location mLastLocation = null;
    private LocationManager mLocationManager;
    private LocationResult  mLocationResult;
    private boolean gps_enabled = false;
    //private boolean network_enabled = false;
    
    
    //This needs to be called onPause() if getLocation is called from onResume, to prevent crash, as stated on the same stackoverflow thread.
    public void stopLocationTask() 
    { 
    	mLocationManager.removeUpdates(locationListenerGps); 
    	mLocationManager.removeUpdates(locationListenerNetwork);
    }

    public boolean startLocationTask(Context context, LocationResult result)
    {
        //I use LocationResult callback class to pass location value from MyLocation to user code.
        mLocationResult = result;
        
        if(mLocationManager == null)
            mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        //exceptions will be thrown if provider is not permitted.
        try{ gps_enabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER); }catch(Exception ex){}
        //try{ network_enabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER); }catch(Exception ex){}

        //don't start listeners if no provider is enabled
        if(!gps_enabled)
            return false;

        if(gps_enabled)
        {
        	Log.d("GPS", "StartLocationTask");
        	mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_INTERVAL, UPDATE_DISTANCE, locationListenerGps);
        }
            
        //if(network_enabled)
        //    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, UPDATE_INTERVAL, UPDATE_DISTANCE, locationListenerNetwork);
		return true;
    }

    LocationListener locationListenerGps = new LocationListener() {
    	
    	
    	@Override
        public void onLocationChanged(Location location) 
    	{
    		
    		Log.d("GPS", "onLocation Callback");
    		mLastLocation = location;
            mLocationResult.gotLocation(location);
        }
    	
    	@Override
        public void onProviderDisabled(String provider) {}
    	
    	@Override
        public void onProviderEnabled(String provider) {}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			
		}
    };

    LocationListener locationListenerNetwork = new LocationListener() {
    	
    	@Override
        public void onLocationChanged(Location location) {
    		mLastLocation = location;
            mLocationResult.gotLocation(location);
        }
    	
    	@Override
        public void onProviderDisabled(String provider) {}
    	
    	@Override
        public void onProviderEnabled(String provider) {}
    	
    	@Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };
    
    public Location getLastLocation() {
    	return mLastLocation;
    }

    public static abstract class LocationResult{
        public abstract void gotLocation(Location location);
    }

}