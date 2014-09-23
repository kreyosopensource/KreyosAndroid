package com.kreyos.watch.adapter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kreyos.watch.KreyosUtility;
import com.kreyos.watch.R;
import com.kreyos.watch.objectdata.ActivityDataRow;


public class ActivityStatsAdapter extends ArrayAdapter<ActivityDataRow> {
	 
    Context context;
 
    public ActivityStatsAdapter(Context context, int resourceId,
    		ArrayList<ActivityDataRow> items) {
        super(context, resourceId, items);
        this.context = context;
    }
 
    /*private view holder class*/
    private class ViewHolder {
    	
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
 
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        ActivityDataRow rowItem = getItem(position);
 
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) 
        {
            convertView = mInflater.inflate(R.layout.activity_stats_item_dup, null);
            holder = new ViewHolder();
            holder.textView_steps 			= (TextView) convertView.findViewById(R.id.textView_steps);
            holder.textView_distance 		= (TextView) convertView.findViewById(R.id.textView_distance);
            holder.textView_calories 		= (TextView) convertView.findViewById(R.id.textView_calories);
            holder.textView_speed 			= (TextView) convertView.findViewById(R.id.textView_speed);
            holder.textView_averagespeed 	= (TextView) convertView.findViewById(R.id.textView_averagespeed);
            holder.textView_topSpeed 		= (TextView) convertView.findViewById(R.id.textView_topspeed);
            
            holder.textView_title			= (TextView) convertView.findViewById(R.id.textView_title);
            
            
            //holder.textView_heartrate = (TextView) convertView.findViewById(R.id.textView_heartrate);
            //holder.textView_averagepace = (TextView) convertView.findViewById(R.id.textView_averagepace);
            
            //holder.textView_altitude = (TextView) convertView.findViewById(R.id.textView_altitude);
            
          //  activity.hour = periodStartHour;
			//activity.minute = periodStartMin;
			//activity.mode = periodMode;
            holder.textView_times = (TextView) convertView.findViewById(R.id.textView_times);
            holder.imageView_activitystats = (ImageView) convertView.findViewById(R.id.imageView_activitystats);
            convertView.setTag(holder);
        } 
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }
        
       
        
       // holder.txtDesc.setText(rowItem.getDesc());
        //holder.txtTitle.setText(rowItem.getTitle());
        
        
        DecimalFormat f = new DecimalFormat("##.##");
        //display only 300 and above steps
        //if(rowItem.data.get(ActivityDataRow.DataType.DATA_COL_STEP) > 300){
        
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
        
        holder.textView_steps.setText(""+f.format(steps));
        holder.textView_distance.setText(""+f.format(distance));
        holder.textView_calories.setText(""+f.format(calories));
        holder.textView_speed.setText(""+f.format(speed));
        holder.textView_averagespeed.setText(""+f.format(avespeed));
        holder.textView_topSpeed.setText(""+f.format(topspeed));
        
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
        holder.textView_title.setText(titleText);
              
              //holder.textView_heartrate.setText(""+f.format(rowItem.data.get(ActivityDataRow.DataType.DATA_COL_HR,0.00)));
              
              String hourValue 		= rowItem.hour < 10 ? ("0" + rowItem.hour) 		: ("" + rowItem.hour);
              String minuteValue 	= rowItem.minute < 10 ? ("0" + rowItem.minute) 	: ("" + rowItem.minute);
              
              holder.textView_times.setText(hourValue+":"+minuteValue);
              switch (rowItem.mode) 
              {
              case 0:
            	  holder.imageView_activitystats.setImageResource(R.drawable.stats_activity);
				break;
              case 1:
            	  holder.imageView_activitystats.setImageResource(R.drawable.stats_running);
  				break;
              case 2:
            	  holder.imageView_activitystats.setImageResource(R.drawable.stats_cycling);
  				break;
              default:
				break;
              }
              
        //}
      
 
        return convertView;
    }
}

/*public class ActivityStatsAdapter extends BaseAdapter {

    Context context;
    ArrayList<String> data;
    private static LayoutInflater inflater = null;

    public ActivityStatsAdapter(Context context, ArrayList<String> data) {
        // TODO Auto-generated constructor stub
        this.context = context;
        this.data = data;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View vi = convertView;
        if (vi == null)
            vi = inflater.inflate(R.layout.activity_stats_item, null);
        
        ImageView imageView_activitystats = (ImageView) vi.findViewById(R.id.imageView_activitystats);
        imageView_activitystats.setImageResource(R.drawable.activityicon);
        
        TextView text = (TextView) vi.findViewById(R.id.textView_steps);
        text.setText(data.get(position));
        return vi;
    }
} */