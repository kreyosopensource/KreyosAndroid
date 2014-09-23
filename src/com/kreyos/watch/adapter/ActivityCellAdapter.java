package com.kreyos.watch.adapter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import com.facebook.android.Util;
import com.kreyos.watch.KreyosUtility;
import com.kreyos.watch.R;
import com.kreyos.watch.objectdata.ActivityDataRow;
import com.kreyos.watch.utils.Utils;

import android.app.Activity;
import android.content.Context;
import android.inputmethodservice.Keyboard.Row;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ActivityCellAdapter extends ArrayAdapter<ActivityDataRow> {
	 
    private Context m_context;
    private LayoutInflater m_inflater;
    //private ArrayList<ActivityDataRow> m_items;
 
    public ActivityCellAdapter(
    	Context context, 
    	int resourceId, 
    	ArrayList<ActivityDataRow> items
    ) {
        super(context, resourceId, items);
        m_context = context;
        m_inflater = (LayoutInflater)context.getSystemService( Activity.LAYOUT_INFLATER_SERVICE );
    }
    
    @Override
    public View getView(
    	int position, 
    	View convertView, 
    	ViewGroup parent
    ) {
    	ActivityDataRow rowItem 	= this.getItem( position );
        TitleView title 			= null;
        ActivityView cell 			= null;
        long epoch					= rowItem.epoch;
        Calendar calendar			= Utils.calendar( epoch );
        String dateString			= Utils.dateString( calendar );
        Log.i( "ActivityCellAdapter::getView", "BLE_Fetch.. displayType:"+rowItem.displayType + " index:" + position + " dateString:" + dateString + " epoch:" + epoch + " dayofWeek:" + calendar.get( Calendar.DAY_OF_WEEK ) );
        
        if( convertView == null ) 
        {
            // display title
            if( rowItem.displayType == ActivityDataRow.TYPE_TITLE )
            {
            	Log.i( "TestAdapter::getView", "Creating title..." );
            	
            	convertView = m_inflater.inflate(R.layout.activity_stats_title, null);
            	
            	title 					= new TitleView();
            	title.txtTotalSteps 	= (TextView)convertView.findViewById( R.id.textView_totalStepsActivityStats );
            	title.txtTotalDistance	= (TextView)convertView.findViewById( R.id.textView_totalDistanceActivityStats );
            	title.txtTotalCalories	= (TextView)convertView.findViewById( R.id.textView_totalCaloriesActivityStats );
            	title.txtStringDate		= (TextView)convertView.findViewById( R.id.txt_date );
            	title.txtDayOfWeek		= (TextView)convertView.findViewById( R.id.txt_day_of_week );
            	
            	title.txtTotalSteps.setText(KreyosUtility.setDataForDisplay((double)rowItem.totalSteps)); 	
            	title.txtTotalDistance.setText(KreyosUtility.setDataForDisplay((double)rowItem.totalDistance)); //Double.toString( rowItem.totalDistance ) ); 		KreyosUtility.setDataForDisplay((double)rowItem.totalDistance)
            	title.txtTotalCalories.setText(KreyosUtility.setDataForDisplay((double)rowItem.totalCalories)); //Double.toString( rowItem.totalCalories ) ); 		
            	title.txtStringDate.setText( dateString );
            	title.txtDayOfWeek.setText( Utils.intToDay( calendar.get( Calendar.DAY_OF_WEEK ) ) ); 	
            	
            	convertView.setTag( title );
            	title = null;																			
            }
            // display unit values
            else if( rowItem.displayType == ActivityDataRow.TYPE_ACTIVITY )
            {
            	Log.i( "TestAdapter::getView", "Creating item..." );
				
            	convertView = m_inflater.inflate(R.layout.activity_stats_item_dup, null);
            	
            	// fix the displays of activity
            	cell 							= new ActivityView();
            	cell.textView_title				= (TextView)convertView.findViewById(R.id.textView_title);
            	cell.textView_steps 			= (TextView)convertView.findViewById(R.id.textView_steps);
            	cell.textView_distance 			= (TextView)convertView.findViewById(R.id.textView_distance);
            	cell.textView_calories 			= (TextView)convertView.findViewById(R.id.textView_calories);
            	cell.textView_speed 			= (TextView)convertView.findViewById(R.id.textView_speed);
            	cell.textView_averagespeed 		= (TextView)convertView.findViewById(R.id.textView_averagespeed);
            	cell.textView_topSpeed 			= (TextView)convertView.findViewById(R.id.textView_topspeed);
            	
            	// cell.textView_heartrate 		= (TextView)convertView.findViewById(R.id.textView_heartrate);
            	// cell.textView_averagepace 		= (TextView)convertView.findViewById(R.id.textView_averagepace);
            	// cell.textView_speed 			= (TextView)convertView.findViewById(R.id.textView_speed);
                // cell.textView_averagespeed 		= (TextView)convertView.findViewById(R.id.textView_averspeed);
                // cell.textView_altitude 			= (TextView)convertView.findViewById(R.id.textView_altitude);
                
                cell.textView_times 			= (TextView)convertView.findViewById(R.id.textView_times);
                cell.imageView_activitystats 	= (ImageView)convertView.findViewById(R.id.imageView_activitystats);
                
                DecimalFormat f = new DecimalFormat("##.##");
                double steps = rowItem.data.get(ActivityDataRow.DataType.DATA_COL_STEP, 0.0);
                double distance = rowItem.data.get(ActivityDataRow.DataType.DATA_COL_DIST, 0.0);
                double calories = rowItem.data.get(ActivityDataRow.DataType.DATA_COL_CALS, 0.0);
                double speed 	= 0;
                double avespeed = 0; 
                double topspeed = 0; 
                if (steps > 0) {
                	speed = (distance / (steps * 0.5));
                	avespeed = speed * 0.7f;
                	topspeed = speed * 1.1f;
                }

                cell.textView_steps.setText(""+f.format(steps));
                cell.textView_distance.setText(""+f.format(distance));
                cell.textView_calories.setText(""+f.format(calories));
                cell.textView_speed.setText(""+f.format(speed));
                cell.textView_averagespeed.setText(""+f.format(avespeed));
                cell.textView_topSpeed.setText(""+f.format(topspeed));
                    
                String hourValue 		= rowItem.hour < 10 ? ("0" + rowItem.hour) 		: ("" + rowItem.hour);
                String minuteValue 		= rowItem.minute < 10 ? ("0" + rowItem.minute) 	: ("" + rowItem.minute);
                
                cell.textView_times.setText(hourValue+":"+minuteValue);
                
                
                // Random text title
                String titleText = "That's Fantastic!"; 
                /* TODO: Check ios for implementation
                Random rand = new Random();
                int max = 3;
                int min = 1;
                int randomNum = rand.nextInt((max - min) + 1) + min;
                switch (randomNum) {
        	        case 1: {
        	        	titleText = "Random 1";
        	        }
        	        case 2: {
        	        	titleText = "Random 2";
        	        }
        	        case 3: {
        	        	titleText = "Random 3";
        	        }
                }
                */
                cell.textView_title.setText(titleText);
                
                switch (rowItem.mode) 
                {
	                case 0:
	                	cell.imageView_activitystats.setImageResource(R.drawable.stats_activity);
	                break;
	                case 1:
	                	cell.imageView_activitystats.setImageResource(R.drawable.stats_running);
	                break;
	                case 2:
	                	cell.imageView_activitystats.setImageResource(R.drawable.stats_cycling);
	                break;
                }
                
                convertView.setTag(cell);
                cell = null;
            }
            else 
            {
            	Log.i( "ActivityCellAdapter::getView", "Creating... oh my cat O_O" );
            }
        }
        else 
        {
        	if( rowItem.displayType == ActivityDataRow.TYPE_TITLE )
            {
        		Log.i( "ActivityCellAdapter::getView", "Getting... title view tag.. position:" + position + "" );
        		title = (TitleView)convertView.getTag();
            }
        	else if( rowItem.displayType == ActivityDataRow.TYPE_ACTIVITY )
            {
        		Log.i( "ActivityCellAdapter::getView", "Getting... cell view tag position:" + position + "" );
        		cell = (ActivityView)convertView.getTag();
            }
        	else
        	{
        		Log.i( "ActivityCellAdapter::getView", "Getting... oh my cat O_O position:" + position + "" );
        	}
        }
        
        // display values here
        if( title != null )
        {
        	title.txtTotalSteps.setText(KreyosUtility.setDataForDisplay((double)rowItem.totalSteps)); 	
        	title.txtTotalDistance.setText(KreyosUtility.setDataForDisplay((double)rowItem.totalDistance)); 
        	title.txtTotalCalories.setText(KreyosUtility.setDataForDisplay((double)rowItem.totalCalories)); 		
        	title.txtStringDate.setText( dateString );
        	title.txtDayOfWeek.setText( Utils.intToDay( calendar.get( Calendar.DAY_OF_WEEK ) ) ); 	
        }
        else if( cell != null )
        {
            DecimalFormat f = new DecimalFormat("##.##");
            
            double steps 	= rowItem.data.get(ActivityDataRow.DataType.DATA_COL_STEP, 0.0);
            double distance = rowItem.data.get(ActivityDataRow.DataType.DATA_COL_DIST, 0.0);
            double calories = rowItem.data.get(ActivityDataRow.DataType.DATA_COL_CALS, 0.0);
            double speed 	= 0;
            double avespeed = 0; 
            double topspeed = 0; 
            if (steps > 0) {
            	speed = (distance / (steps * 0.5));
            	avespeed = speed * 0.7f;
            	topspeed = speed * 1.1f;
            }
            
            cell.textView_steps.setText(""+f.format(steps));
            cell.textView_distance.setText(""+f.format(distance));
            cell.textView_calories.setText(""+f.format(calories));
            cell.textView_speed.setText(""+f.format(speed));
            cell.textView_averagespeed.setText(""+f.format(avespeed));
            cell.textView_topSpeed.setText(""+f.format(topspeed));

            String hourValue 		= rowItem.hour < 10 ? ("0" + rowItem.hour) 		: ("" + rowItem.hour);
            String minuteValue 		= rowItem.minute < 10 ? ("0" + rowItem.minute) 	: ("" + rowItem.minute);

            cell.textView_times.setText(hourValue+":"+minuteValue);
            
         // Random text title
            String titleText = "That's Fantastic!"; 
            /* TODO: Check ios for implementation
            Random rand = new Random();
            int max = 3;
            int min = 1;
            int randomNum = rand.nextInt((max - min) + 1) + min;
            switch (randomNum) {
    	        case 1: {
    	        	titleText = "Random 1";
    	        }
    	        case 2: {
    	        	titleText = "Random 2";
    	        }
    	        case 3: {
    	        	titleText = "Random 3";
    	        }
            }
            */
            cell.textView_title.setText(titleText);
            
          //*
            switch (rowItem.mode) 
            {
                case 0:
                	cell.imageView_activitystats.setImageResource(R.drawable.stats_activity);
                break;
                case 1:
                	cell.imageView_activitystats.setImageResource(R.drawable.stats_running);
                break;
                case 2:
                	cell.imageView_activitystats.setImageResource(R.drawable.stats_cycling);
                break;
            }
            //*/
        }
         
        return convertView;
    }
    
    @Override
    public void clear() {
    	while( this.getCount() > 0 ) {
    		this.remove( this.getItem( 0 ) );
    	}
    }
    
    @Override
    public int getItemViewType(int position) {
    	ActivityDataRow rowItem = this.getItem( position );
    	
    	if( rowItem.displayType == ActivityDataRow.TYPE_TITLE ) {
    		return ActivityDataRow.TYPE_TITLE;
    	}
    	
    	return ActivityDataRow.TYPE_ACTIVITY;
    }
    
    @Override
    public int getViewTypeCount() {
        return 2;
    }
    
    @Override
    public long getItemId(int position) {
        return position;
    }
}

class ActivityView {
	TextView textView_title;
	TextView textView_times;
    ImageView imageView_activitystats;
    TextView textView_steps;
    TextView textView_distance;
    TextView textView_calories;
    TextView textView_speed;
    TextView textView_averagespeed;
    TextView textView_topSpeed;
    
    TextView textView_heartrate;
    TextView textView_averagepace;
    TextView textView_altitude;
}

class TitleView {
	TextView txtTotalSteps;
	TextView txtTotalDistance;
	TextView txtTotalCalories;
	TextView txtStringDate;
	TextView txtDayOfWeek; // In week
}