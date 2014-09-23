package com.kreyos.watch;

import java.lang.reflect.Field;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.facebook.Session;
import com.kreyos.watch.bluetooth.BluetoothAgent;
import com.kreyos.watch.bluetooth.KreyosService;
import com.kreyos.watch.bluetooth.Protocol;

import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

public class KreyosNotifService extends NotificationListenerService
{

	public final String TAG = "Notif";
	public final String NOTIFICATION_FILTER_FB 			= "com.facebook.katana";
	public final String NOTIFICATION_FILTER_GMAIL 		= "com.google.android.gm";
	public final String NOTIFICATION_FILTER_EMAIL 		= "com.android.email";
	public final String NOTIFICATION_FILTER_TW 			= "com.twitter.android";
	public final String NOTIFICATION_FILTER_REMINDER 	= "calendar";
	
	@Override
	public void onNotificationPosted(StatusBarNotification sbn)
	{

		// TODO Auto-generated method stub
		Log.i(TAG,"**********  onNotificationPosted:" + sbn.getPackageName().toString() );
		Log.i(TAG,"ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());

		SharedPreferences prefs = KreyosActivity.getPrefs();
		if ( prefs == null )
		{
			return;
		}
		
		if(prefs.getBoolean("notification.email", false) == true)
		{
			if(sbn.getPackageName().toString().contains(NOTIFICATION_FILTER_GMAIL) 
			|| sbn.getPackageName().toString().contains(NOTIFICATION_FILTER_EMAIL))
	        {         
				
				RemoteViews        views = sbn.getNotification().bigContentView;
			    if (views == null) views = sbn.getNotification().contentView;
			    if (views == null) return;

			    // Use reflection to examine the m_actions member of the given RemoteViews object.
			    // It's not pretty, but it works.
			    List<String> text = new ArrayList<String>();
			    
			    try
			    {
			        Field field = views.getClass().getDeclaredField("mActions");
			        field.setAccessible(true);

			        @SuppressWarnings("unchecked")
			        ArrayList<Parcelable> actions = (ArrayList<Parcelable>) field.get(views);

			        // Find the setText() and setTime() reflection actions
			        for (Parcelable p : actions)
			        {
			            Parcel parcel = Parcel.obtain();
			            p.writeToParcel(parcel, 0);
			            parcel.setDataPosition(0);

			            // The tag tells which type of action it is (2 is ReflectionAction, from the source)
			            int tag = parcel.readInt();
			            if (tag != 2) continue;

			            // View ID
			            parcel.readInt();

			            String methodName = parcel.readString();
			            if (methodName == null) continue;

			            // Save strings
			            else if (methodName.equals("setText"))
			            {
			                // Parameter type (10 = Character Sequence)
			                parcel.readInt();
			                // Store the actual string
			                String t = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel).toString().trim();
			                text.add(t);
//			                Log.d("Notfi", "" + t);
			            }

			            // Save times. Comment this section out if the notification time isn't important
			            else if (methodName.equals("setTime"))
			            {
			                // Parameter type (5 = Long)
			                parcel.readInt();
			                String t = new SimpleDateFormat("h:mm a").format(new Date(parcel.readLong()));
			                text.add(t);
//			                Log.d("Notfi", "" + t);
			            }
			            parcel.recycle();
			        }
			    }
			    // It's not usually good style to do this, but then again, neither is the use of reflection...
			    catch (Exception e)
			    {
			        Log.e("NotificationClassifier", e.toString());
			    }
			
			    Log.d("Notif", ""+text.get(text.size()-1));
	        	
			    Protocol pa = new Protocol(BluetoothAgent.getInstance(null), KreyosNotifService.this);
	    		pa.notifyMessage("MS", "Email:", ""+ sbn.getNotification().tickerText + ": " + text.get(text.size()-1));
	        }
		}
		
//		if(Session.getActiveSession() != null 
//		&& Session.getActiveSession().isOpened()
//		&& prefs.getBoolean("notification.facebook", false) == true)
		
		if (prefs.getBoolean("notification.reminder", false)) {
			if(sbn.getPackageName().toString().contains(NOTIFICATION_FILTER_REMINDER)) {
				
				if (sbn.getNotification().tickerText == null) {
					return;
				}
				
				if (sbn.getNotification().tickerText.equals("")) {
					return;
				}
				
				if (sbn.getNotification().tickerText.equals("null")) {
					return;
				}
				
				Protocol pa = new Protocol(BluetoothAgent.getInstance(null), KreyosNotifService.this);
				pa.notifyMessage("MR", "Calendar Reminder:", ""+ sbn.getNotification().tickerText);
			}
		}
		
		if(prefs.getBoolean("notification.facebook", false) == true)
		{
			if(sbn.getPackageName().toString().contains(NOTIFICATION_FILTER_FB))
	        {    
	        	Protocol pa = new Protocol(BluetoothAgent.getInstance(null), KreyosNotifService.this);
	    		pa.notifyMessage("MF", "Facebook:", ""+ sbn.getNotification().tickerText);
	        }
		}
		
		if(prefs.getBoolean("notification.twitter", false) == true)
		{
			if(sbn.getPackageName().toString().contains(NOTIFICATION_FILTER_TW))
	        {    
	        	Protocol pa = new Protocol(BluetoothAgent.getInstance(null), KreyosNotifService.this);
	    		pa.notifyMessage("MT", "Twitter:", ""+ sbn.getNotification().tickerText);
	        }
		}
	}

	@Override
	public void onNotificationRemoved(StatusBarNotification sbn) 
	{
		// TODO Auto-generated method stub
		Log.i(TAG,"********** onNOtificationRemoved");
        Log.i(TAG,"ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText +"\t" + sbn.getPackageName());
	}

}
